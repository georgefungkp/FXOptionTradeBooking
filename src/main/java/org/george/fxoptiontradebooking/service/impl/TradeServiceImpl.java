package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.george.fxoptiontradebooking.service.TradeService;
import org.george.fxoptiontradebooking.service.ValidationService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final ValidationService validationService;
    private final ModelMapper modelMapper;

    @Override
    public TradeResponse bookTrade(TradeBookingRequest request) {
        if (request == null) {
            throw new BusinessValidationException("Trade booking request cannot be null");
        }
        
        log.info("Booking trade with reference: {}", request.getTradeReference());
        
        try {
            performPreTradeValidation(request);
            Counterparty counterparty = validateAndGetCounterparty(request.getCounterpartyId());
            validateCounterpartyEligibility(counterparty);
            validateUniqueTradeReference(request.getTradeReference());
            
            Trade trade = createTradeEntity(request, counterparty);
            applyBusinessLogic(trade, request);
            
            Trade savedTrade = saveTradeWithAudit(trade);
            performPostTradeProcessing(savedTrade);
            
            log.info("Successfully booked trade: {}", savedTrade.getTradeReference());
            return modelMapper.map(savedTrade, TradeResponse.class);
        } catch (BusinessValidationException | TradeNotFoundException e) {
            log.error("Trade booking failed for reference {}: {}", request.getTradeReference(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during trade booking for reference {}: {}", request.getTradeReference(), e.getMessage(), e);
            throw new BusinessValidationException("Trade booking failed due to unexpected error");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TradeResponse getTradeById(Long tradeId) {
        if (tradeId == null || tradeId <= 0) {
            throw new BusinessValidationException("Trade ID must be a positive number");
        }
        
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with ID: " + tradeId));
        
        return modelMapper.map(trade, TradeResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TradeResponse getTradeByReference(String tradeReference) {
        if (tradeReference == null || tradeReference.trim().isEmpty()) {
            throw new BusinessValidationException("Trade reference cannot be null or empty");
        }
        
        Trade trade = tradeRepository.findByTradeReference(tradeReference)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with reference: " + tradeReference));
        
        return modelMapper.map(trade, TradeResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByCounterparty(Long counterpartyId) {
        if (counterpartyId == null || counterpartyId <= 0) {
            throw new BusinessValidationException("Counterparty ID must be a positive number");
        }
        
        if (!counterpartyRepository.existsById(counterpartyId)) {
            throw new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId);
        }
        
        List<Trade> trades = tradeRepository.findByCounterpartyCounterpartyId(counterpartyId);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByStatus(TradeStatus status) {
        if (status == null) {
            throw new BusinessValidationException("Trade status cannot be null");
        }
        
        List<Trade> trades = tradeRepository.findByStatus(status);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessValidationException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date cannot be after end date");
        }
        
        if (ChronoUnit.DAYS.between(startDate, endDate) > 365) {
            throw new BusinessValidationException("Date range cannot exceed 1 year");
        }
        
        List<Trade> trades = tradeRepository.findByTradeDateBetween(startDate, endDate);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeResponse> getAllTrades(Pageable pageable) {
        Page<Trade> trades = tradeRepository.findAll(pageable);
        return trades.map(trade -> modelMapper.map(trade, TradeResponse.class));
    }

    @Override
    public TradeResponse updateTradeStatus(Long tradeId, TradeStatus newStatus) {
        if (tradeId == null || tradeId <= 0) {
            throw new BusinessValidationException("Trade ID must be a positive number");
        }
        
        if (newStatus == null) {
            throw new BusinessValidationException("New status cannot be null");
        }
        
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with ID: " + tradeId));
        
        TradeStatus oldStatus = trade.getStatus();
        validateStatusTransition(oldStatus, newStatus);
        
        trade.setStatus(newStatus);
        trade.setUpdatedAt(LocalDateTime.now());
        
        Trade updatedTrade = tradeRepository.save(trade);
        handleStatusChangeEvents(updatedTrade, oldStatus, newStatus);
        
        log.info("Updated trade {} status from {} to {}", tradeId, oldStatus, newStatus);
        return modelMapper.map(updatedTrade, TradeResponse.class);
    }

    @Override
    public void cancelTrade(Long tradeId) {
        if (tradeId == null || tradeId <= 0) {
            throw new BusinessValidationException("Trade ID must be a positive number");
        }
        
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with ID: " + tradeId));
        
        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new BusinessValidationException("Only PENDING trades can be cancelled");
        }
        
        if (!trade.getTradeDate().equals(LocalDate.now())) {
            throw new BusinessValidationException("Trades can only be cancelled on the same business day");
        }
        
        trade.setStatus(TradeStatus.CANCELLED);
        trade.setUpdatedAt(LocalDateTime.now());
        
        tradeRepository.save(trade);
        log.info("Cancelled trade: {}", trade.getTradeReference());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new BusinessValidationException("Currency cannot be null or empty");
        }
        
        if (currency.length() != 3) {
            throw new BusinessValidationException("Currency must be a 3-character ISO code");
        }
        
        List<Trade> trades = tradeRepository.findTradesByCurrency(currency.toUpperCase());
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByProductType(ProductType productType) {
        if (productType == null) {
            throw new BusinessValidationException("Product type cannot be null");
        }
        
        List<Trade> trades = tradeRepository.findByProductType(productType);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeResponse> getTradesByProductTypePaginated(ProductType productType, Pageable pageable) {
        if (productType == null) {
            throw new BusinessValidationException("Product type cannot be null");
        }
        
        Page<Trade> trades = tradeRepository.findByProductType(productType, pageable);
        return trades.map(trade -> modelMapper.map(trade, TradeResponse.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeResponse> getTradesByCounterpartyPaginated(Long counterpartyId, Pageable pageable) {
        if (counterpartyId == null || counterpartyId <= 0) {
            throw new BusinessValidationException("Counterparty ID must be a positive number");
        }
        
        if (!counterpartyRepository.existsById(counterpartyId)) {
            throw new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId);
        }
        
        Page<Trade> trades = tradeRepository.findByCounterpartyCounterpartyId(counterpartyId, pageable);
        return trades.map(trade -> modelMapper.map(trade, TradeResponse.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getVanillaOptionsExpiringBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessValidationException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date cannot be after end date");
        }
        
        List<Trade> trades = tradeRepository.findVanillaOptionsExpiringBetween(startDate, endDate);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getExoticOptionsByType(ExoticOptionType exoticType) {
        if (exoticType == null) {
            throw new BusinessValidationException("Exotic option type cannot be null");
        }
        
        List<Trade> trades = tradeRepository.findExoticOptionsByType(exoticType);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getAllSwaps() {
        List<Trade> trades = tradeRepository.findAllSwaps();
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getSwapsByType(SwapType swapType) {
        if (swapType == null) {
            throw new BusinessValidationException("Swap type cannot be null");
        }
        
        List<Trade> trades = tradeRepository.findBySwapType(swapType);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getInterestRateSwapsByIndex(String floatingRateIndex) {
        if (floatingRateIndex == null || floatingRateIndex.trim().isEmpty()) {
            throw new BusinessValidationException("Floating rate index cannot be null or empty");
        }
        
        List<Trade> trades = tradeRepository.findInterestRateSwapsByIndex(floatingRateIndex);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getAllFXContracts() {
        List<Trade> trades = tradeRepository.findAllFXContracts();
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getFXForwardsMaturing(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessValidationException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date cannot be after end date");
        }
        
        List<Trade> trades = tradeRepository.findFXForwardsMaturing(startDate, endDate);
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    // Private helper methods

    private void performPreTradeValidation(TradeBookingRequest request) {
        validationService.validateTradeRequest(request);
    }

    private Counterparty validateAndGetCounterparty(Long counterpartyId) {
        return counterpartyRepository.findById(counterpartyId)
            .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));
    }

    private void validateCounterpartyEligibility(Counterparty counterparty) {
        if (!counterparty.getIsActive()) {
            throw new BusinessValidationException("Cannot trade with inactive counterparty: " + counterparty.getName());
        }
    }

    private void validateUniqueTradeReference(String tradeReference) {
        Optional<Trade> existingTrade = tradeRepository.findByTradeReference(tradeReference);
        if (existingTrade.isPresent()) {
            throw new BusinessValidationException("Trade reference already exists: " + tradeReference);
        }
    }

    private Trade createTradeEntity(TradeBookingRequest request, Counterparty counterparty) {
        Trade trade = new Trade();
        trade.setTradeReference(request.getTradeReference());
        trade.setCounterparty(counterparty);
        trade.setProductType(request.getProductType());
        trade.setBaseCurrency(request.getBaseCurrency());
        trade.setQuoteCurrency(request.getQuoteCurrency());
        trade.setNotionalAmount(request.getNotionalAmount());
        trade.setTradeDate(request.getTradeDate());
        trade.setValueDate(request.getValueDate());
        trade.setMaturityDate(request.getMaturityDate());
        trade.setCreatedBy(request.getCreatedBy());
        trade.setStatus(TradeStatus.PENDING);
        trade.setCreatedAt(LocalDateTime.now());
        trade.setUpdatedAt(LocalDateTime.now());
        
        // Set product-specific fields
        if (request.getProductType() == ProductType.VANILLA_OPTION || request.getProductType() == ProductType.EXOTIC_OPTION) {
            trade.setOptionType(request.getOptionType());
            trade.setStrikePrice(request.getStrikePrice());
            trade.setPremiumAmount(request.getPremiumAmount());
            trade.setPremiumCurrency(request.getPremiumCurrency());
            
            if (request.getProductType() == ProductType.EXOTIC_OPTION) {
                trade.setExoticOptionType(request.getExoticOptionType());
                trade.setBarrierLevel(request.getBarrierLevel());
                trade.setKnockInOut(request.getKnockInOut());
                trade.setObservationFrequency(request.getObservationFrequency());
            }
        }
        
        if (request.getProductType() == ProductType.FX_FORWARD || request.getProductType() == ProductType.FX_SPOT) {
            trade.setForwardRate(request.getForwardRate());
            trade.setSpotRate(request.getSpotRate());
        }
        
        if (isSwapProduct(request.getProductType())) {
            trade.setSwapType(request.getSwapType());
            trade.setNearLegAmount(request.getNearLegAmount());
            trade.setFarLegAmount(request.getFarLegAmount());
            trade.setNearLegRate(request.getNearLegRate());
            trade.setFarLegRate(request.getFarLegRate());
            trade.setNearLegDate(request.getNearLegDate());
            trade.setFarLegDate(request.getFarLegDate());
            
            if (request.getSwapType() == SwapType.INTEREST_RATE_SWAP) {
                trade.setFixedRate(request.getFixedRate());
                trade.setFloatingRateIndex(request.getFloatingRateIndex());
                trade.setPaymentFrequency(request.getPaymentFrequency());
            }
        }
        
        return trade;
    }

    private boolean isSwapProduct(ProductType productType) {
        return productType == ProductType.FX_SWAP || 
               productType == ProductType.CURRENCY_SWAP || 
               productType == ProductType.INTEREST_RATE_SWAP;
    }

    private boolean isOptionProduct(Trade trade) {
        return trade.getProductType() == ProductType.VANILLA_OPTION || 
               trade.getProductType() == ProductType.EXOTIC_OPTION;
    }

    private void applyBusinessLogic(Trade trade, TradeBookingRequest request) {
        if (isOptionProduct(trade) && trade.getPremiumAmount() == null) {
            trade.setPremiumAmount(calculateDefaultPremium(trade));
            trade.setPremiumCurrency(trade.getBaseCurrency());
        }
    }

    private BigDecimal calculateDefaultPremium(Trade trade) {
        BigDecimal baseRate = new BigDecimal("0.02"); // 2% base premium
        BigDecimal notional = trade.getNotionalAmount();
        
        if (trade.getMaturityDate() != null) {
            long daysToMaturity = ChronoUnit.DAYS.between(LocalDate.now(), trade.getMaturityDate());
            BigDecimal timeAdjustment = new BigDecimal(daysToMaturity).divide(new BigDecimal("365"), 4, RoundingMode.HALF_UP);
            baseRate = baseRate.multiply(timeAdjustment);
        }
        
        return notional.multiply(baseRate);
    }

    private Trade saveTradeWithAudit(Trade trade) {
        return tradeRepository.save(trade);
    }

    private void performPostTradeProcessing(Trade trade) {
        if (trade == null) {
            log.warn("performPostTradeProcessing called with null trade");
            return;
        }

        if (trade.getNotionalAmount().compareTo(new BigDecimal("10000000")) > 0) {
            log.info("Large trade detected: {} with notional {}", trade.getTradeReference(), trade.getNotionalAmount());
        }
    }

    private void validateStatusTransition(TradeStatus currentStatus, TradeStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                // PENDING can transition to any status
                break;
            case CONFIRMED:
                if (newStatus == TradeStatus.PENDING) {
                    throw new BusinessValidationException("Cannot revert CONFIRMED trade to PENDING");
                }
                break;
            case SETTLED:
            case CANCELLED:
            case EXPIRED:
                throw new BusinessValidationException("Cannot change status of " + currentStatus + " trade");
            default:
                throw new BusinessValidationException("Unknown trade status: " + currentStatus);
        }
    }

    private void handleStatusChangeEvents(Trade trade, TradeStatus oldStatus, TradeStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED:
                log.info("Trade {} confirmed, initiating settlement process", trade.getTradeReference());
                break;
            case SETTLED:
                log.info("Trade {} settled, updating positions", trade.getTradeReference());
                break;
            case CANCELLED:
                log.info("Trade {} cancelled, releasing credit limits", trade.getTradeReference());
                break;
            case EXPIRED:
                log.info("Trade {} expired, processing option expiry", trade.getTradeReference());
                break;
        }
    }
}
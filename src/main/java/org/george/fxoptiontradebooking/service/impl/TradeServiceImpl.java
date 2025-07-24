package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.george.fxoptiontradebooking.service.TradeService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;
    private final org.george.fxoptiontradebooking.service.impl.TradeValidationService tradeValidationService;
    private final TradeFactoryService tradeFactoryService;
    private final TradeBusinessLogicService tradeBusinessLogicService;
    private final TradeQueryService tradeQueryService;
    private final ModelMapper modelMapper;

    @Override
    public TradeResponse bookTrade(TradeBookingRequest request) {
        if (request == null) {
            throw new BusinessValidationException("Trade booking request cannot be null");
        }
        
        log.info("Booking trade with reference: {}", request.getTradeReference());
        
        try {
            // Pre-trade validation
            tradeValidationService.performPreTradeValidation(request);
            Counterparty counterparty = tradeValidationService.validateAndGetCounterparty(request.getCounterpartyId());
            tradeValidationService.validateCounterpartyEligibility(counterparty);
            tradeValidationService.validateUniqueTradeReference(request.getTradeReference());
            
            // Create and process trade
            Trade trade = tradeFactoryService.createTradeEntity(request, counterparty);
            tradeBusinessLogicService.applyBusinessLogic(trade, request);
            
            // Save and post-process
            Trade savedTrade = tradeBusinessLogicService.saveTradeWithAudit(trade);
            tradeBusinessLogicService.performPostTradeProcessing(savedTrade);
            
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
        tradeValidationService.validateStatusTransition(oldStatus, newStatus);
        
        trade.setStatus(newStatus);
        trade.setUpdatedAt(LocalDateTime.now());
        
        Trade updatedTrade = tradeRepository.save(trade);
        tradeBusinessLogicService.handleStatusChangeEvents(updatedTrade, oldStatus, newStatus);
        
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
        
        tradeValidationService.validateTradeForCancellation(trade);
        
        trade.setStatus(TradeStatus.CANCELLED);
        trade.setUpdatedAt(LocalDateTime.now());
        
        tradeRepository.save(trade);
        log.info("Cancelled trade: {}", trade.getTradeReference());
    }

    // Delegate all query methods to TradeQueryService
    @Override
    @Transactional(readOnly = true)
    public TradeResponse getTradeById(Long tradeId) {
        return tradeQueryService.getTradeById(tradeId);
    }

    @Override
    @Transactional(readOnly = true)
    public TradeResponse getTradeByReference(String tradeReference) {
        return tradeQueryService.getTradeByReference(tradeReference);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByCounterparty(Long counterpartyId) {
        return tradeQueryService.getTradesByCounterparty(counterpartyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByStatus(TradeStatus status) {
        return tradeQueryService.getTradesByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByDateRange(LocalDate startDate, LocalDate endDate) {
        return tradeQueryService.getTradesByDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeResponse> getAllTrades(Pageable pageable) {
        return tradeQueryService.getAllTrades(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByCurrency(String currency) {
        return tradeQueryService.getTradesByCurrency(currency);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByProductType(ProductType productType) {
        return tradeQueryService.getTradesByProductType(productType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeResponse> getTradesByProductTypePaginated(ProductType productType, Pageable pageable) {
        return tradeQueryService.getTradesByProductTypePaginated(productType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeResponse> getTradesByCounterpartyPaginated(Long counterpartyId, Pageable pageable) {
        return tradeQueryService.getTradesByCounterpartyPaginated(counterpartyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getVanillaOptionsExpiringBetween(LocalDate startDate, LocalDate endDate) {
        return tradeQueryService.getVanillaOptionsExpiringBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getExoticOptionsByType(ExoticOptionType exoticType) {
        return tradeQueryService.getExoticOptionsByType(exoticType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getAllSwaps() {
        return tradeQueryService.getAllSwaps();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getSwapsByType(SwapType swapType) {
        return tradeQueryService.getSwapsByType(swapType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getInterestRateSwapsByIndex(String floatingRateIndex) {
        return tradeQueryService.getInterestRateSwapsByIndex(floatingRateIndex);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getAllFXContracts() {
        return tradeQueryService.getAllFXContracts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getFXForwardsMaturing(LocalDate startDate, LocalDate endDate) {
        return tradeQueryService.getFXForwardsMaturing(startDate, endDate);
    }
}
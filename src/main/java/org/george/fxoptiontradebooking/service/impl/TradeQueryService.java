package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TradeQueryService {
    
    private final TradeRepository tradeRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final ModelMapper modelMapper;

    public TradeResponse getTradeById(Long tradeId) {
        validateTradeId(tradeId);
        
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with ID: " + tradeId));
        
        return modelMapper.map(trade, TradeResponse.class);
    }

    public TradeResponse getTradeByReference(String tradeReference) {
        validateTradeReference(tradeReference);
        
        Trade trade = tradeRepository.findByTradeReference(tradeReference)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with reference: " + tradeReference));
        
        return modelMapper.map(trade, TradeResponse.class);
    }

    public List<TradeResponse> getTradesByCounterparty(Long counterpartyId) {
        validateCounterpartyId(counterpartyId);
        validateCounterpartyExists(counterpartyId);
        
        List<Trade> trades = tradeRepository.findByCounterparty_CounterpartyId(counterpartyId);
        return mapTradesToResponses(trades);
    }

    public List<TradeResponse> getTradesByStatus(TradeStatus status) {
        if (status == null) {
            throw new BusinessValidationException("Trade status cannot be null");
        }
        
        List<Trade> trades = tradeRepository.findByStatus(status);
        return mapTradesToResponses(trades);
    }

    public List<TradeResponse> getTradesByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        List<Trade> trades = tradeRepository.findByTradeDateBetween(startDate, endDate);
        return mapTradesToResponses(trades);
    }

    public Page<TradeResponse> getAllTrades(Pageable pageable) {
        Page<Trade> trades = tradeRepository.findAll(pageable);
        return trades.map(trade -> modelMapper.map(trade, TradeResponse.class));
    }

    public List<TradeResponse> getTradesByCurrency(String currency) {
        validateCurrency(currency);
        
        List<Trade> trades = tradeRepository.findByCurrency(currency.toUpperCase());
        return mapTradesToResponses(trades);
    }

    public List<TradeResponse> getTradesByProductType(ProductType productType) {
        if (productType == null) {
            throw new BusinessValidationException("Product type cannot be null");
        }
        
        List<Trade> trades = switch (productType) {
            case VANILLA_OPTION -> tradeRepository.findAllVanillaOptions();
            case EXOTIC_OPTION -> tradeRepository.findAllExoticOptions();
            case FX_FORWARD, FX_SPOT -> tradeRepository.findAllFXTrades();
            case FX_SWAP -> tradeRepository.findAllSwaps();
            default -> List.of();
        };

        return mapTradesToResponses(trades);
    }

    public Page<TradeResponse> getTradesByProductTypePaginated(ProductType productType, Pageable pageable) {
        if (productType == null) {
            throw new BusinessValidationException("Product type cannot be null");
        }
        
        // For paginated queries, we need to create custom repository methods
        // For now, we'll get all and paginate manually (not optimal for large datasets)
        List<TradeResponse> allTrades = getTradesByProductType(productType);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTrades.size());
        
        List<TradeResponse> pageContent = allTrades.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(
                pageContent, 
                pageable, 
                allTrades.size()
        );
    }

    public Page<TradeResponse> getTradesByCounterpartyPaginated(Long counterpartyId, Pageable pageable) {
        validateCounterpartyId(counterpartyId);
        validateCounterpartyExists(counterpartyId);
        
        Page<Trade> trades = tradeRepository.findByCounterparty_CounterpartyId(counterpartyId, pageable);
        return trades.map(trade -> modelMapper.map(trade, TradeResponse.class));
    }

    public List<TradeResponse> getVanillaOptionsExpiringBetween(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        List<VanillaOptionTrade> vanillaOptions = tradeRepository.findVanillaOptionsExpiringBetween(startDate, endDate);
        return vanillaOptions.stream()
                .map(trade -> modelMapper.map(trade, TradeResponse.class))
                .collect(Collectors.toList());
    }

    public List<TradeResponse> getExoticOptionsByType(ExoticOptionType exoticType) {
        if (exoticType == null) {
            throw new BusinessValidationException("Exotic option type cannot be null");
        }
        
        List<ExoticOptionTrade> exoticOptions = tradeRepository.findExoticOptionsByType(exoticType);
        return exoticOptions.stream()
                .map(trade -> modelMapper.map(trade, TradeResponse.class))
                .collect(Collectors.toList());
    }

    public List<TradeResponse> getAllSwaps() {
        List<Trade> trades = tradeRepository.findAllSwaps();
        return mapTradesToResponses(trades);
    }

    public List<TradeResponse> getSwapsByType(SwapType swapType) {
        if (swapType == null) {
            throw new BusinessValidationException("Swap type cannot be null");
        }
        
        List<SwapTrade> swaps = tradeRepository.findSwapsByType(swapType);
        return swaps.stream()
                .map(trade -> modelMapper.map(trade, TradeResponse.class))
                .collect(Collectors.toList());
    }

    public List<TradeResponse> getInterestRateSwapsByIndex(String floatingRateIndex) {
        if (floatingRateIndex == null || floatingRateIndex.trim().isEmpty()) {
            throw new BusinessValidationException("Floating rate index cannot be null or empty");
        }
        
        List<SwapTrade> swaps = tradeRepository.findInterestRateSwapsByIndex(floatingRateIndex);
        return swaps.stream()
                .map(trade -> modelMapper.map(trade, TradeResponse.class))
                .collect(Collectors.toList());
    }

    public List<TradeResponse> getAllFXContracts() {
        List<Trade> trades = tradeRepository.findAllFXTrades();
        return mapTradesToResponses(trades);
    }

    public List<TradeResponse> getFXForwardsMaturing(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        List<FXTrade> fxForwards = tradeRepository.findFXForwardsMaturing(startDate, endDate);
        return fxForwards.stream()
                .map(trade -> modelMapper.map(trade, TradeResponse.class))
                .collect(Collectors.toList());
    }

    private List<TradeResponse> mapTradesToResponses(List<Trade> trades) {
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    private void validateTradeId(Long tradeId) {
        if (tradeId == null || tradeId <= 0) {
            throw new BusinessValidationException("Trade ID must be a positive number");
        }
    }

    private void validateTradeReference(String tradeReference) {
        if (tradeReference == null || tradeReference.trim().isEmpty()) {
            throw new BusinessValidationException("Trade reference cannot be null or empty");
        }
    }

    private void validateCounterpartyId(Long counterpartyId) {
        if (counterpartyId == null || counterpartyId <= 0) {
            throw new BusinessValidationException("Counterparty ID must be a positive number");
        }
    }

    private void validateCounterpartyExists(Long counterpartyId) {
        if (!counterpartyRepository.existsById(counterpartyId)) {
            throw new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessValidationException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date cannot be after end date");
        }
        
        if (ChronoUnit.DAYS.between(startDate, endDate) > 365) {
            throw new BusinessValidationException("Date range cannot exceed 1 year");
        }
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new BusinessValidationException("Currency cannot be null or empty");
        }
        
        if (currency.length() != 3) {
            throw new BusinessValidationException("Currency must be a 3-character ISO code");
        }
    }
}
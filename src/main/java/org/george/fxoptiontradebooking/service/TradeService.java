package org.george.fxoptiontradebooking.service;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.ExoticOptionType;
import org.george.fxoptiontradebooking.entity.ProductType;
import org.george.fxoptiontradebooking.entity.SwapType;
import org.george.fxoptiontradebooking.entity.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TradeService {
    
    TradeResponse bookTrade(TradeBookingRequest request);
    TradeResponse getTradeById(Long tradeId);
    TradeResponse getTradeByReference(String tradeReference);
    List<TradeResponse> getTradesByCounterparty(Long counterpartyId);
    List<TradeResponse> getTradesByStatus(TradeStatus status);
    List<TradeResponse> getTradesByDateRange(LocalDate startDate, LocalDate endDate);
    Page<TradeResponse> getAllTrades(Pageable pageable);
    TradeResponse updateTradeStatus(Long tradeId, TradeStatus newStatus);
    void cancelTrade(Long tradeId);
    List<TradeResponse> getTradesByCurrency(String currency);
    
    List<TradeResponse> getTradesByProductType(ProductType productType);
    Page<TradeResponse> getTradesByProductTypePaginated(ProductType productType, Pageable pageable);
    Page<TradeResponse> getTradesByCounterpartyPaginated(Long counterpartyId, Pageable pageable);
    
    List<TradeResponse> getVanillaOptionsExpiringBetween(LocalDate startDate, LocalDate endDate);
    List<TradeResponse> getExoticOptionsByType(ExoticOptionType exoticType);
    
    List<TradeResponse> getAllSwaps();
    List<TradeResponse> getSwapsByType(SwapType swapType);
    List<TradeResponse> getInterestRateSwapsByIndex(String floatingRateIndex);
    
    List<TradeResponse> getAllFXContracts();
    List<TradeResponse> getFXForwardsMaturing(LocalDate startDate, LocalDate endDate);
}
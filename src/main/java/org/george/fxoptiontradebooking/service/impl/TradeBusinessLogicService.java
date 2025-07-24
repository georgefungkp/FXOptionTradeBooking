package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.ProductType;
import org.george.fxoptiontradebooking.entity.Trade;
import org.george.fxoptiontradebooking.entity.TradeStatus;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeBusinessLogicService {
    
    private final TradeRepository tradeRepository;

    public void applyBusinessLogic(Trade trade, TradeBookingRequest request) {
        if (isOptionProduct(trade) && trade.getPremiumAmount() == null) {
            trade.setPremiumAmount(calculateDefaultPremium(trade));
            trade.setPremiumCurrency(trade.getBaseCurrency());
        }
    }

    public Trade saveTradeWithAudit(Trade trade) {
        return tradeRepository.save(trade);
    }

    public void performPostTradeProcessing(Trade trade) {
        if (trade == null) {
            log.warn("performPostTradeProcessing called with null trade");
            return;
        }

        if (trade.getNotionalAmount().compareTo(new BigDecimal("10000000")) > 0) {
            log.info("Large trade detected: {} with notional {}", trade.getTradeReference(), trade.getNotionalAmount());
        }
    }

    public void handleStatusChangeEvents(Trade trade, TradeStatus oldStatus, TradeStatus newStatus) {
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

    private boolean isOptionProduct(Trade trade) {
        return trade.getProductType() == ProductType.VANILLA_OPTION || 
               trade.getProductType() == ProductType.EXOTIC_OPTION;
    }
}

package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.Counterparty;
import org.george.fxoptiontradebooking.entity.Trade;
import org.george.fxoptiontradebooking.entity.TradeStatus;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.george.fxoptiontradebooking.service.ValidationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeValidationService {
    
    private final TradeRepository tradeRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final ValidationService validationService;

    public void performPreTradeValidation(TradeBookingRequest request) {
        validationService.validateTradeRequest(request);
    }

    public Counterparty validateAndGetCounterparty(Long counterpartyId) {
        return counterpartyRepository.findById(counterpartyId)
            .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));
    }

    public void validateCounterpartyEligibility(Counterparty counterparty) {
        if (!counterparty.getIsActive()) {
            throw new BusinessValidationException("Cannot trade with inactive counterparty: " + counterparty.getName());
        }
    }

    public void validateUniqueTradeReference(String tradeReference) {
        Optional<Trade> existingTrade = tradeRepository.findByTradeReference(tradeReference);
        if (existingTrade.isPresent()) {
            throw new BusinessValidationException("Trade reference already exists: " + tradeReference);
        }
    }

    public void validateStatusTransition(TradeStatus currentStatus, TradeStatus newStatus) {
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

    public void validateTradeForCancellation(Trade trade) {
        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new BusinessValidationException("Only PENDING trades can be cancelled");
        }
        
        if (!trade.getTradeDate().equals(LocalDate.now())) {
            throw new BusinessValidationException("Trades can only be cancelled on the same business day");
        }
    }
}
package org.george.fxoptiontradebooking.util;

import java.time.DayOfWeek;
import java.time.LocalDate;



/**
 * Utility class for FX option trade date-related operations and validations.
 * This class provides methods for working with business days, validating trade dates,
 * and handling date-related calculations specific to FX option trading requirements.
 */
public class DateUtils {
    /**
     * Determines if a given date falls on a weekday (Monday through Friday).
     * <p>
     * In financial markets, most settlements and option exercises can only
     * occur on business days, not weekends. This method provides the basic
     * weekend check that is fundamental to all business day validations.
     * 
     * @param date The date to check
     * @return true if the date is a weekday, false if it's a weekend
     */
    public static boolean isWeekday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * Finds the next business day after a given date, skipping weekends.
     * 
     * This method is used for calculating settlement dates, adjusted maturity dates,
     * and other business-day-dependent dates in the FX option trading workflow.
     * 
     * Note: This implementation only skips weekends. A full production system would
     * also incorporate holiday calendars for relevant currency pairs.
     * 
     * @param date The starting date
     * @return The next business day after the given date
     */
    public static LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate nextDay = date.plusDays(1);
        while (!isWeekday(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    /**
     * Validates if a value date is valid for a given trade date.
     * 
     * For FX options, the value date (when premium is settled) must:
     * 1. Be after the trade date (no same-day settlement)
     * 2. Fall on a business day (not a weekend)
     * 
     * This method enforces both rules to ensure valid settlement dates.
     * 
     * @param tradeDate The date when the trade is executed
     * @param valueDate The proposed settlement date to validate
     * @return true if the value date is valid, false otherwise
     */
    public static boolean isValidValueDate(LocalDate tradeDate, LocalDate valueDate) {
        return valueDate.isAfter(tradeDate) && isWeekday(valueDate);
    }
}

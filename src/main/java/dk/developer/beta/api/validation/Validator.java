package dk.developer.beta.api.validation;

import dk.developer.beta.api.Sale;
import dk.developer.validation.GenericValidator;

import static dk.developer.validation.GenericValidator.Status.*;
import static java.util.Arrays.asList;

public class Validator extends GenericValidator {
    private static Validator INSTANCE = new Validator();

    public static Validator get() {
        return INSTANCE;
    }

    private Validator() {
    }

    public Status hasConsistentDates(Sale sale) {
        if ( sale == null )
            return DEFAULT;

        long fromDate = sale.getFromDate();
        long toDate = sale.getToDate();
        return fromDate <= toDate ? VALID : INVALID;
    }

    public Status areOpeningHours(String string) {
        if (string == null || string.isEmpty())
            return DEFAULT;

        String[] days = string.split("#");
        boolean containEmptyDay = asList(days).stream()
                .anyMatch(day -> day == null || day.isEmpty());

        return days.length == 7 && !containEmptyDay ? VALID : INVALID;
    }
}

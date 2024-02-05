package info.kgeorgiy.ja.kadyrov.i18n;

import java.text.ParsePosition;
import java.util.*;

public class CurrencyStatistics extends AbstractNumberStatistics {
    private final Set<Number> currencies = new TreeSet<>(Comparator.comparingDouble(Number::doubleValue));

    private final ResultStatistics<Number> result = new ResultStatistics<>();

    public CurrencyStatistics(final String text, final Locale locale) {
        super(text, locale);
        result.averageValue = 0;
    }

    @Override
    void parseNumber(ParsePosition pos) {
        Number currency = currencyFormat.parse(text, pos);
        if (currency != null) {
            currencies.add(currency);
            result.numberOfEntries++;
        }
    }

    @Override
    void updateStatistics() {
        result.numberOfDistinctEntries = currencies.size();
        result.minimumValue = currencies.stream().map(Number::doubleValue).min(Comparator.naturalOrder()).orElse(0.0);
        result.maximumValue = currencies.stream().map(Number::doubleValue).max(Comparator.naturalOrder()).orElse(0.0);
        result.averageValue = currencies.stream().map(Number::doubleValue).reduce(Double::sum).orElse(0.0) / result.numberOfEntries;
    }

    public ResultStatistics<Number> getResult() {
        return result;
    }

}

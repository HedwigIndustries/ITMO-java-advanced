package info.kgeorgiy.ja.kadyrov.i18n;

import java.text.ParsePosition;
import java.util.*;

public class NumberStatistics extends AbstractNumberStatistics {
    private final Set<Double> numbers = new TreeSet<>();
    private final ResultStatistics<Double> result = new ResultStatistics<>();


    public NumberStatistics(final String text, final Locale locale) {
        super(text, locale);
        result.averageValue = 0.0;
    }

    @Override
    void parseNumber(ParsePosition pos) {
        Number number = numberFormat.parse(text, pos);
        if (number != null) {
            numbers.add(number.doubleValue());
            result.numberOfEntries++;
            result.averageValue += number.doubleValue();
        }
    }

    @Override
    void updateStatistics() {
        result.numberOfDistinctEntries = numbers.size();
        result.minimumValue = numbers.stream().min(Comparator.naturalOrder()).orElse(null);
        result.maximumValue = numbers.stream().max(Comparator.naturalOrder()).orElse(null);
        result.averageValue = result.averageValue / result.numberOfEntries;
    }

    public ResultStatistics<Double> getResult() {
        return result;
    }


}

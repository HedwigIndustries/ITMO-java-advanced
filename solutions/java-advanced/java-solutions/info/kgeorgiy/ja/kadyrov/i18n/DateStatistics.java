package info.kgeorgiy.ja.kadyrov.i18n;

import java.text.ParsePosition;
import java.util.*;

public class DateStatistics extends AbstractNumberStatistics {
    private final Set<Date> dates = new TreeSet<>();
    private Double sum = 0.0;

    private final ResultStatistics<Date> result = new ResultStatistics<>();

    public DateStatistics(final String text, final Locale locale) {
        super(text, locale);
        result.averageValue = new Date(0);
    }

    @Override
    void parseNumber(ParsePosition pos) {
        Date date = dateFormat.parse(text, pos);
        if (date != null) {
            dates.add(date);
            result.numberOfEntries++;
            sum += date.getTime();
        }
    }

    @Override
    void updateStatistics() {
        result.numberOfDistinctEntries = dates.size();
        result.minimumValue = dates.stream().min(Date::compareTo).orElse(null);
        result.maximumValue = dates.stream().max(Date::compareTo).orElse(null);
        result.averageValue = new Date((long) (sum / result.numberOfEntries));
    }

    public ResultStatistics<Date> getResult() {
        return result;
    }
}

package info.kgeorgiy.ja.kadyrov.i18n;

import java.text.BreakIterator;
import java.text.Collator;
import java.util.*;

public class SentenceStatistics extends AbstractStringStatistics {
    private final Set<String> sentences = new TreeSet<>();

    public SentenceStatistics(final String text, final Locale locale) {
        super(text, locale);
        breakIterator = BreakIterator.getSentenceInstance(locale);
    }

    @Override
    void parseString(String str) {
        if (str != null) {
            sentences.add(str);
            result.numberOfEntries++;
            result.averageLength += str.length();
        }
    }

    @Override
    public void updateStatistics() {
        result.numberOfDistinctEntries = sentences.size();
        result.minimumValue = sentences.stream().min(collator::compare).orElse(null);
        result.maximumValue = sentences.stream().max(collator::compare).orElse(null);
        result.maximumLength = sentences.stream().map(String::length).max(Comparator.naturalOrder()).orElse(0);
        result.minimumLength = sentences.stream().map(String::length).min(Comparator.naturalOrder()).orElse(0);
        result.averageLength /= result.numberOfEntries;
    }
    public ResultStatistics<String> getResult() {
        return result;
    }


}

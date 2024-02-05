package info.kgeorgiy.ja.kadyrov.i18n;

import java.text.BreakIterator;
import java.util.*;

public class WordStatistics extends AbstractStringStatistics {
    private final Set<String> words = new HashSet<>();

    public WordStatistics(final String text, final Locale locale) {
        super(text, locale);
        breakIterator = BreakIterator.getWordInstance(locale);
    }

    public void parseString(String word) {
        boolean isWord = false;
        for (char ch : word.toCharArray()) {
            if (Character.isLetter(ch)) {
                isWord = true;
                break;
            }
        }
        if (isWord) {
            words.add(word);
            result.numberOfEntries++;
            result.averageLength += word.length();
        }

    }

    @Override
    public void updateStatistics() {
        result.numberOfDistinctEntries = words.size();
        result.minimumValue = words.stream().min(collator::compare).orElse(null);
        result.maximumValue = words.stream().max(collator::compare).orElse(null);
        result.maximumLength = words.stream().map(String::length).max(Comparator.naturalOrder()).orElse(0);
        result.minimumLength = words.stream().map(String::length).min(Comparator.naturalOrder()).orElse(0);
        result.averageLength /= result.numberOfEntries;
    }

    public ResultStatistics<String> getResult() {
        return result;
    }
}

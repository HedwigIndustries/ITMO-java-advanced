package info.kgeorgiy.ja.kadyrov.i18n;

import java.text.BreakIterator;
import java.util.*;

abstract public class AbstractStringStatistics extends AbstractStatistics {

    protected BreakIterator breakIterator;

    protected final ResultStatistics<String> result = new ResultStatistics<>();

    AbstractStringStatistics(final String text, final Locale locale) {
        super(text, locale);
    }

    public void getStringStatistics() {
        breakIterator.setText(text);
        for (int i = breakIterator.first(), j = breakIterator.next();
             j != BreakIterator.DONE;
             i = j, j = breakIterator.next()
        ) {
            String str = text.substring(i, j);
            parseString(str);
        }
        updateStatistics();
    }

    abstract void parseString(String str);

    abstract void updateStatistics();

}

package info.kgeorgiy.ja.kadyrov.i18n;

import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

abstract public class AbstractNumberStatistics extends AbstractStatistics {

    protected final NumberFormat numberFormat;
    protected final NumberFormat currencyFormat;
    protected final DateFormat dateFormat;
    protected final BreakIterator breakIterator;


    public AbstractNumberStatistics(final String text, final Locale locale) {
        super(text, locale);
        this.numberFormat = NumberFormat.getNumberInstance(locale);
        this.currencyFormat = NumberFormat.getCurrencyInstance(locale);
        this.dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        this.breakIterator = BreakIterator.getWordInstance(locale);
    }

    public void getNumberStatistics() {
        breakIterator.setText(text);
        for (int i = breakIterator.first(), j = breakIterator.next();
             j != BreakIterator.DONE;
             i = j, j = breakIterator.next()
        ) {
            ParsePosition pos = new ParsePosition(i);
            parseNumber(pos);
        }
        updateStatistics();

    }

    abstract void parseNumber(ParsePosition pos);
    abstract void updateStatistics();



}

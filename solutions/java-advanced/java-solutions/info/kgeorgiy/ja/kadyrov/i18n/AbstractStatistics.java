package info.kgeorgiy.ja.kadyrov.i18n;

import java.text.Collator;
import java.util.Locale;

abstract public class AbstractStatistics {
    protected final String text;
    protected final Locale locale;
    protected final Collator collator;


    AbstractStatistics(final String text, final Locale locale) {
        this.text = text;
        this.locale = locale;
        this.collator = Collator.getInstance(this.locale);
    }

}

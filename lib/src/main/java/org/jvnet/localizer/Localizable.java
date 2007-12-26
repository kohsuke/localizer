package org.jvnet.localizer;

import java.util.Locale;
import java.text.MessageFormat;

/**
 * Captures the localizable string. Can be converted to a string just by
 * supplying a {@link Locale}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Localizable {
    private final ResourceBundleHolder holder;
    private final String key;
    private final Object[] args;

    public Localizable(ResourceBundleHolder holder, String key, Object... args) {
        this.holder = holder;
        this.key = key;
        this.args = args;
    }

    public String toString(Locale locale) {
        return MessageFormat.format(holder.get(locale).getString(key),args);
    }

    public String toString() {
        return toString(Locale.getDefault());
    }
}

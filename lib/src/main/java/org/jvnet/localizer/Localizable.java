package org.jvnet.localizer;

import java.util.Locale;
import java.text.MessageFormat;
import java.io.Serializable;

/**
 * Captures the localizable string. Can be converted to a string just by
 * supplying a {@link Locale}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Localizable implements Serializable {
    private final ResourceBundleHolder holder;
    private final String key;
    private final Serializable[] args;

    public Localizable(ResourceBundleHolder holder, String key, Object... args) {
        this.holder = holder;
        this.key = key;
        this.args = new Serializable[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Serializable) {
                this.args[i] = (Serializable)args[i];
            } else {
                // MessageFormat only supports formats of "number", "date", "time" and "choice"
                // All of which will be formatting objects that must be Serializable
                // Anything else will just have it's toString() method invoked
                // by MessageFormat, so we'll just call toString up front.
                this.args[i] = args[i].toString();
            }
        }
    }

    public String toString(Locale locale) {
        return MessageFormat.format(holder.get(locale).getString(key),args);
    }

    public String toString() {
        return toString(LocaleProvider.getLocale());
    }
}

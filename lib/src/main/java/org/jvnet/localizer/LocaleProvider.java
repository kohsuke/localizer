package org.jvnet.localizer;

import java.util.Locale;

/**
 * Determines the locale, normally from the context.
 *
 * <p>
 * For example, in webapps, you might use the current request's <tt>Accept-Language</tt>
 * header, or maybe it's just an invocation to {@link Locale#getDefault()}.
 *
 * <p>
 * A single instance of {@link LocaleProvider} is maintained in this class
 * for the use by {@link ResourceBundleHolder}. 
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class LocaleProvider {
    /**
     * Determines the locale to be used.
     *
     * @return
     *      must not be null.
     */
    public abstract Locale get();

    public static void setProvider(LocaleProvider p) {
        if(p==null) throw new IllegalArgumentException();
        theInstance = p;
    }

    /**
     * Gets the currently installed system-wide {@link LocaleProvider}.
     * @return
     *      always non-null.
     */
    public static LocaleProvider getProvider() {
        return theInstance;
    }

    /**
     * Short for {@code getProvider().get()}
     */
    public static Locale getLocale() {
        return theInstance.get();
    }

    /**
     * {@link LocaleProvider} that uses {@link Locale#getDefault()}.
     */
    public static final LocaleProvider DEFAULT = new LocaleProvider() {
        public Locale get() {
            return Locale.getDefault();
        }
    };

    private static volatile LocaleProvider theInstance = DEFAULT;
}

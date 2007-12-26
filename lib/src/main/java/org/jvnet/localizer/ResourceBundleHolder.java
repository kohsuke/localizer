package org.jvnet.localizer;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.text.MessageFormat;

/**
 * Maintains {@link ResourceBundle}s per locale.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ResourceBundleHolder {
    private final Map<Locale,ResourceBundle> bundles = new ConcurrentHashMap<Locale,ResourceBundle>();
    private final Class owner;

    /**
     * @param owner
     *      The name of the generated resource bundle class.
     */
    public ResourceBundleHolder(Class owner) {
        this.owner = owner;
    }

    /**
     * Loads {@link ResourceBundle} for the locale.
     */
    public ResourceBundle get(Locale locale) {
        ResourceBundle rb = bundles.get(locale);
        if(rb!=null)    return rb;

        // try to update the map
        synchronized(this) {
            rb = bundles.get(locale);
            if(rb!=null)    return rb;
            bundles.put(locale, rb=ResourceBundle.getBundle(owner.getName(),locale));
        }
        return rb;
    }

    /**
     * Formats a resource specified by the given key by using the default locale
     */
    public String format(String key, Object... args) {
        return MessageFormat.format(get(Locale.getDefault()).getString(key),args);
    }
}

package org.jvnet.localizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.WeakReference;

/**
 * Maintains {@link ResourceBundle}s per locale.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ResourceBundleHolder implements Serializable {
    /** Need to cache, but not tie up a classloader refernce in cases of unloading */
    private static final Map<Class, WeakReference<Map<Locale, ResourceBundle>>> cache =
            new WeakHashMap<Class, WeakReference<Map<Locale, ResourceBundle>>> ();
    private static final Object cacheLock = new Object();

    private static Map<Locale,ResourceBundle> lookupCache(Class clazz) {
        synchronized (cacheLock) {
            WeakReference<Map<Locale, ResourceBundle>> entry = cache.get(clazz);
            if (entry == null || entry.get() == null) {
                final ConcurrentHashMap<Locale, ResourceBundle> bundles = new ConcurrentHashMap<Locale, ResourceBundle>();
                entry = new WeakReference<Map<Locale, ResourceBundle>>(bundles);
                cache.put(clazz, entry);
                return bundles;
            }
            return entry.get();
        }
    }

    private transient Map<Locale,ResourceBundle> bundles;
    private final Class owner;

    /**
     * {@link Locale} object that corresponds to the base bundle.
     */
    private static final Locale ROOT = new Locale("");

    /**
     * @param owner
     *      The name of the generated resource bundle class.
     */
    public ResourceBundleHolder(Class owner) {
        this.owner = owner;
        this.bundles = lookupCache(owner);
    }

    /**
     * Work around deserialization issues.
     */
    private Object readResolve() throws ObjectStreamException {
        this.bundles = lookupCache(owner);
        return this;
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

            // turns out this is totally unsable because the getBundle method
            // always checks Locale.getDefault() and that wins over the bundle for the root locale. 
            // bundles.put(locale, rb=ResourceBundle.getBundle(owner.getName(),locale,owner.getClassLoader()));

            Locale next = getBaseLocale(locale);

            String s = locale.toString();
            URL res = owner.getResource(owner.getSimpleName()+(s.length()>0?'_'+s:"")+".properties");
            if(res!=null) {
                // found property file for this locale.
                try {
                    InputStream is = res.openStream();
                    ResourceBundleImpl bundle = new ResourceBundleImpl(is);
                    is.close();
                    rb = bundle;
                    if(next!=null)
                    bundle.setParent(get(next));
                    bundles.put(locale,bundle);
                } catch (IOException e) {
                    MissingResourceException x = new MissingResourceException("Unable to load resource " + res, owner.getName(), null);
                    x.initCause(e);
                    throw x;
                }
            } else {
                if(next!=null)
                    // no matching resource, so just use the locale for the base
                    bundles.put(locale,rb=get(next));
                else
                    throw new MissingResourceException(
                            "No resource was found for "+owner.getName(),owner.getName(),null);
            }

        }
        return rb;
    }

    static class ResourceBundleImpl extends PropertyResourceBundle {
        ResourceBundleImpl(InputStream stream) throws IOException {
            super(stream);
        }

        protected void setParent(ResourceBundle parent) {
            super.setParent(parent);
        }
    }

    /**
     * Returns the locale to fall back to.
     */
    private Locale getBaseLocale(Locale l) {
        if (l.getVariant().length() > 0)
            return new Locale(l.getLanguage(), l.getCountry());
        if (l.getCountry().length() > 0)
            return new Locale(l.getLanguage());
        if (l.getLanguage().length()>0)
            return ROOT;
        return null;
    }

    /**
     * Formats a resource specified by the given key by using the default locale
     */
    public String format(String key, Object... args) {
        return MessageFormat.format(get(LocaleProvider.getLocale()).getString(key),args);
    }
}

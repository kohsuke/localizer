package org.jvnet.localizer;

import java.net.URL;

/**
 * Allows customizing what class loader is used to find a given resource.
 *
 * By default, it's the class loader for the class of the generated resource bundle, but certain
 * localization patterns might require a different source.
 */
public abstract class ResourceProvider {
    /**
     * Return the resource URL specified by the given String
     */
    public abstract URL getResource(String name, Class<?> resourceBundle);

    public static void setProvider(ResourceProvider p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        theInstance = p;
    }

    /**
     * Gets the currently installed system-wide {@link ResourceProvider}.
     * @return
     *      always non-null.
     */
    public static ResourceProvider getProvider() {
        return theInstance;
    }

    /**
     * Short for {@code getProvider().getResource()}
     */
    public static URL findResource(String name, Class<?> resourceBundleClass) {
        return theInstance.getResource(name, resourceBundleClass);
    }

    public static final ResourceProvider DEFAULT = new ResourceProvider() {
        public URL getResource(String name, Class<?> resourceBundleClass) {
            return resourceBundleClass.getResource(name);
        }
    };

    private static volatile ResourceProvider theInstance = DEFAULT;
}

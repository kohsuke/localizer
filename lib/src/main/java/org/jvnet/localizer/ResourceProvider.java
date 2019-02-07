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
     * Return the resource URL specified by the given String if it is to be overwritten.
     * @param name the name of the resource
     * @param resourceBundle the class for which a resource should be loaded
     * @return the resource, or {@code null} if no overwritten resource is found
     */
    public abstract URL getResource(String name, Class<?> resourceBundle);

    /**
     * Set a new system-wide {@link ResourceProvider}.
     * @param p singleton instance
     * @throws IllegalArgumentException if the parameter is {@code null}.
     */
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

    /**
     * The default resource provider which just calls {@link Class#getResource(String)}.
     */
    public static final ResourceProvider DEFAULT = new ResourceProvider() {
        public URL getResource(String name, Class<?> resourceBundleClass) {
            return resourceBundleClass.getResource(name);
        }
    };

    private static volatile ResourceProvider theInstance = DEFAULT;
}

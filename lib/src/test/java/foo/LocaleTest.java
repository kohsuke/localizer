package foo;

import junit.framework.TestCase;

import java.util.Locale;

import org.jvnet.localizer.ResourceBundleHolder;

/**
 * @author Kohsuke Kawaguchi
 */
public class LocaleTest extends TestCase {
    public void test1() {
        Locale.setDefault(Locale.GERMANY);
        routineCheck();
    }

    public void test2() {
        Locale.setDefault(Locale.ENGLISH);
        routineCheck();
    }

    private void routineCheck() {
        ResourceBundleHolder h = new ResourceBundleHolder(LocaleTest.class);
        assertEquals("base",h.get(Locale.ENGLISH).getString("abc"));
        assertEquals("german",h.get(Locale.GERMANY).getString("abc"));
        assertEquals("german",h.get(Locale.GERMAN).getString("abc"));
    }

    public void testXml() throws Exception {
        Locale.setDefault(Locale.JAPANESE);
        @SuppressWarnings("deprecation")        // not to use a cache.
        ResourceBundleHolder h = new ResourceBundleHolder(LocaleTest.class);
        assertEquals("日本語", h.format("abc"));
    }

    public void testBoth() throws Exception {
        Locale.setDefault(Locale.FRENCH);
        @SuppressWarnings("deprecation")        // not to use a cache.
        ResourceBundleHolder h = new ResourceBundleHolder(LocaleTest.class);
        assertEquals("Français(properties)", h.format("abc"));
    }
}

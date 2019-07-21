package foo;

import junit.framework.TestCase;

import java.util.Locale;

import org.jvnet.localizer.ResourceBundleHolder;

/**
 * @author IKEDA Yasuyuki
 */
public class Utf8LocaleTest extends TestCase {
    public void testUtf8() throws Exception {
        Locale.setDefault(Locale.JAPANESE);
        @SuppressWarnings("deprecation")        // not to use a cache.
        ResourceBundleHolder h = new ResourceBundleHolder(Utf8LocaleTest.class);
        assertEquals("日本語", h.format("abc"));
    }
}

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
}

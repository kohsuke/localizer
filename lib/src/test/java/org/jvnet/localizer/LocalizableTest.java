package org.jvnet.localizer;

import junit.framework.TestCase;

import java.util.Locale;

public class LocalizableTest extends TestCase {

    public void testNullArgument() {
        ResourceBundleHolder holder = ResourceBundleHolder.get(LocalizableTest.class);

        Localizable localizable = new Localizable(holder, "arg", (Object) null);
        assertEquals("arg: null", localizable.toString(Locale.ENGLISH));
    }
}

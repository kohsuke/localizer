package org.jvnet.localizer.test;

import org.junit.Test;

import org.junit.Assert;

public class MessagesTest {

    @Test
    public void smokeTest() throws Exception {
        Assert.assertEquals(Messages.abc("one", "two"), "Test message one and two");
    }
}

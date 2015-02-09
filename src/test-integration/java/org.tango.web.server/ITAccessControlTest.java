package org.tango.web.server;


import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ITAccessControlTest {
    private final String tangoHost = System.getenv("TANGO_HOST");

    @Test
    public void testCheckUserCanRead() throws Exception {
        AccessControl instance = new AccessControl(tangoHost);

        assertTrue(instance.checkUserCanRead("test", "127.0.0.1", "sys/tg_test/1"));
    }

}
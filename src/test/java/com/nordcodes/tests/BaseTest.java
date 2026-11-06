package com.nordcodes.tests;

import com.nordcodes.services.MockService;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {
    protected static MockService mockService_8888;

    private static boolean initialized = false;

    @BeforeAll
    static synchronized void oneTimeBeforeAll() {
        if (!initialized) {
            mockService_8888 = new MockService(8888);
            initialized = true;
        }
    }
}

package com.nordcodes.config;

public class TestConfig {
    private TestConfig() {
    }

    public static final String INTERNAL_BASE_URL =
            System.getProperty("internal.base.url", "http://localhost:8080");

    public static final String API_KEY =
            System.getProperty("app.api.key", "qazWSXedc");
}

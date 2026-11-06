package com.nordcodes.tests.internal.rest;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.nordcodes.services.rest.InternalRestService;
import com.nordcodes.tests.BaseTest;
import com.nordcodes.utils.TokenHelper;
import io.qameta.allure.Epic;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static java.lang.String.format;

@Epic("Internal Service")
public abstract class BaseInternalRestTest extends BaseTest {
    protected String token;
    protected List<StubMapping> stubs;

    static String AUTH = "/auth";
    static String DO_ACTION = "/doAction";

    static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";
    static final String TOKEN_ERROR_MESSAGE = "token: должно соответствовать \"^[0-9A-F]{32}$\"";
    static final String INVALID_API_KEY_ERROR_MESSAGE = "Missing or invalid API Key";

    static Stream<Integer> provideExternalErrors() {
        return Stream.of(400, 401, 403, 404, 429, 500, 503);
    }

    public static String getTokenNotFoundMessage(String token) {
        return format("Token '%s' not found", token);
    }

    @AfterEach
    void afterEach() {
        if (stubs != null && !stubs.isEmpty()) mockService_8888.deleteStubs(stubs);
    }

    Map<String, String> getDefaultFormParams(String token) {
        return Map.of("token", token);
    }

    public void setStub(String endpoint, int statusCode) {
        if (stubs == null) {
            stubs = new ArrayList<>();
        }

        stubs.add(mockService_8888.stubEndpoint(POST, endpoint, statusCode, getDefaultFormParams(token)));
    }

    public Response regToken() {
        token = TokenHelper.generate();
        setStub(AUTH, 200);
        return InternalRestService.doAction(token, InternalRestService.InternalAction.LOGIN);
    }

    public Response regTokenAndDoAction() {
        regToken();
        setStub(DO_ACTION, 200);
        return InternalRestService.doAction(token, InternalRestService.InternalAction.ACTION);
    }
}

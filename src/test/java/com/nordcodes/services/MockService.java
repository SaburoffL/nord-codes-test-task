package com.nordcodes.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.qameta.allure.Step;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockService {
    protected final WireMockServer mockServer;

    public MockService(int port) {
        mockServer = new WireMockServer(port);
        mockServer.start();
    }

    public StubMapping stubEndpoint(RequestMethod method, String url, int status, Map<String, String> formParams) {
        return stubEndpoint(method, url, status, null, null, formParams);
    }

    public StubMapping stubEndpoint(RequestMethod method, String url, int status,
                                    String responseBody, Map<String, String> responseHeaders, Map<String, String> formParams) {
        return stubEndpoint(method, url, status, responseBody, responseHeaders, formParams, 0);
    }

    @Step("Установить заглушку {method}: {url} статус: {status} с задержкой {delayMillis}мс")
    public StubMapping stubEndpoint(RequestMethod method, String url, int status,
                                    String responseBody, Map<String, String> responseHeaders,
                                    Map<String, String> formParams, int delayMillis) {
        var response = aResponse().withStatus(status);

        if (responseBody != null) {
            response.withBody(responseBody);
        }

        if (responseHeaders != null) {
            responseHeaders.forEach(response::withHeader);
        }

        if (delayMillis > 0) {
            response.withFixedDelay(delayMillis);
        }

        var request = request(method.getName(), urlEqualTo(url));

        if (formParams != null) {
            formParams.forEach((key, value) ->
                    request.withFormParam(key, equalTo(value))
            );
        }

        return mockServer.stubFor(
                request.willReturn(response)
        );
    }

    public void deleteStubs(List<StubMapping> stubs) {
        for(StubMapping s: stubs) {
            deleteStub(s);
        }
    }

    @Step("Удалить заглушку {stub}")
    public void deleteStub(StubMapping stub) {
        mockServer.removeStub(stub);
    }
}

package com.nordcodes.assertions;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

public class AllureAssertions {

    @Step("Валидация REST ответа")
    public static void validateRestResponse(Response response, int expectedStatusCode, Object expectedBody) {
        assertEquals("статус ответа", expectedStatusCode, response.statusCode());
        assertEquals("тело ответа", expectedBody, response.body().as(expectedBody.getClass()));
    }

    @Step("Проверить, что значение '{0}' = '{1}'")
    public static void assertEquals(String meaningName, Object expected, Object actual) {
        Assertions.assertEquals(expected, actual);
    }
}

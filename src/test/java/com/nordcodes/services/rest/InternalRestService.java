package com.nordcodes.services.rest;

import com.nordcodes.config.TestConfig;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class InternalRestService {

    public static Response doAction(String token, InternalAction action) {
        return doAction(token, action, TestConfig.API_KEY);
    }

    @Step("REST: internal-service/{1}")
    public static Response doAction(String token, InternalAction action, String apiKey) {
        var spec = given()
                .filter(new AllureRestAssured())
                .baseUri(TestConfig.INTERNAL_BASE_URL)
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .formParam("token", token)
                .formParam("action", action);

        if (apiKey != null) {
            spec.header("X-Api-Key", apiKey);
        }

        return spec.when().post("/endpoint");
    }

    public enum InternalAction {
        LOGIN,
        ACTION,
        LOGOUT,
        INVALID
    }

}

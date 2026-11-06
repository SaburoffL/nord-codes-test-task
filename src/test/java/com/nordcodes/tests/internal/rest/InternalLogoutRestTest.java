package com.nordcodes.tests.internal.rest;

import com.nordcodes.assertions.AllureAssertions;
import com.nordcodes.model.rest.internal.InternalResponseDto;
import com.nordcodes.services.rest.InternalRestService;
import com.nordcodes.utils.TokenHelper;
import io.qameta.allure.Allure;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.nordcodes.model.rest.internal.InternalResponseDto.StatusCode.ERROR;
import static com.nordcodes.model.rest.internal.InternalResponseDto.StatusCode.OK;
import static com.nordcodes.services.rest.InternalRestService.InternalAction.*;

@Feature("LOGOUT")
public class InternalLogoutRestTest extends BaseInternalRestTest {
    @Test
    @DisplayName("LOGOUT после успешного LOGIN")
    public void logoutAfterSuccessLogin() {
        regToken();

        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("LOGOUT после успешного ACTION")
    public void logoutAfterSuccessAction() {
        regTokenAndDoAction();

        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("Повторный LOGOUT")
    public void doubleLogout() {
        regToken();

        InternalRestService.doAction(token, LOGOUT);

        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 403, new InternalResponseDto(ERROR, getTokenNotFoundMessage(token)));
    }

    @Test
    @DisplayName("LOGOUT без предварительного LOGIN")
    public void logoutWithoutLogin() {
        token = TokenHelper.generate();

        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 403, new InternalResponseDto(ERROR, getTokenNotFoundMessage(token)));
    }

    @Test
    @DisplayName("Успешный LOGOUT (200, невалидный JSON в ответе внешнего сервиса)")
    public void logoutWithBrokenBodyFromMock() {
        regTokenAndDoAction();
        stubs.add(
                mockService_8888.stubEndpoint(
                        POST,
                        DO_ACTION,
                        200,
                        "INVALID_JSON_FOR_LOGOUT",
                        null,
                        getDefaultFormParams(token)
                )
        );

        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("LOGOUT во время ACTION с таймаутом (гонка)")
    //в тз действие это абстракция, но в реальной системе, конечно нужно чекнуть подтверждение того что действие реально выполнено
    //фактически система откладывает выполнение LOGOUT до завершения ACTION
    public void logoutDuringLongActionRaceCondition() throws Exception {
        regToken();
        stubs.add(
                mockService_8888.stubEndpoint(
                        POST,
                        DO_ACTION,
                        200,
                        null,
                        null,
                        getDefaultFormParams(token),
                        5000
                )
        );

        String currentTestUuid = Allure.getLifecycle().getCurrentTestCase().orElseThrow();

        CompletableFuture<Response> actionFuture =
                CompletableFuture.supplyAsync(() -> {
                    Allure.getLifecycle().setCurrentTestCase(currentTestUuid);
                    return InternalRestService.doAction(token, InternalRestService.InternalAction.ACTION);
                });


        Thread.sleep(1000);


        var logoutResponse = InternalRestService.doAction(token, InternalRestService.InternalAction.LOGOUT);
        AllureAssertions.validateRestResponse(logoutResponse, 200, new InternalResponseDto(OK, null));

        Response actionResponse = actionFuture.get();

        AllureAssertions.validateRestResponse(actionResponse, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("LOGOUT с невалидным токеном (31 символ)")
    public void logoutWithShortToken() {
        token = TokenHelper.generateShortToken();
        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGOUT с невалидным токеном (33 символа)")
    public void logoutWithLongToken() {
        token = TokenHelper.generateLongToken();
        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGOUT с невалидным токеном (нижний регистр)")
    public void logoutWithLowercaseToken() {
        token = TokenHelper.generateLowercaseToken();
        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGOUT с невалидным токеном (спецсимволы)")
    public void logoutWithSpecialCharactersToken() {
        token = TokenHelper.generateTokenWithSpecialCharacters();
        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGOUT с невалидным токеном (пустая строка)")
    public void logoutWithEmptyToken() {
        token = "";
        var response = InternalRestService.doAction(token, LOGOUT);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGOUT без обязательного заголовка X-Api-Key")
    public void logoutWithoutApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, LOGOUT, null);
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGOUT с пустым значением X-Api-Key")
    public void logoutWithEmptyApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, LOGOUT, "");
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGOUT с невалидным значением X-Api-Key")
    public void logoutWithInvalidApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, LOGOUT, "wrong_api_key_value");
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }
}

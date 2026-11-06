package com.nordcodes.tests.internal.rest;

import com.nordcodes.assertions.AllureAssertions;
import com.nordcodes.model.rest.internal.InternalResponseDto;
import com.nordcodes.services.rest.InternalRestService;
import com.nordcodes.utils.TokenHelper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.nordcodes.model.rest.internal.InternalResponseDto.StatusCode.*;
import static com.nordcodes.services.rest.InternalRestService.InternalAction.*;

@Feature("ACTION")
public class InternalActionTests extends BaseInternalRestTest {

    @Test
    @DisplayName("ACTION после успешного LOGIN")
    public void actionAfterSuccessLogin() {
        var response = regTokenAndDoAction();
        AllureAssertions.validateRestResponse(response, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("Успешный ACTION (200, невалидный JSON в ответе)")
    public void ok_200_withBrokenBodyFromMock() {
        regToken();
        stubs.add(
                mockService_8888.stubEndpoint(
                        POST,
                        DO_ACTION,
                        200,
                        "INVALID_JSON",
                        null,
                        getDefaultFormParams(token)
                )
        );

        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("Успешный ACTION (200 timeout)")
    public void ok_200_timeout() {
        regToken();
        stubs.add(
                mockService_8888.stubEndpoint(
                        POST,
                        DO_ACTION,
                        200,
                        null,
                        null,
                        getDefaultFormParams(token),
                        8000
                )
        );

        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("Повторный ACTION")
    public void doubleAction() {
        regToken();
        setStub(DO_ACTION, 200);

        InternalRestService.doAction(token, ACTION);

        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("ACTION без предварительного LOGIN")
    public void actionWithoutLogin() {
        token = TokenHelper.generate();
        setStub(DO_ACTION, 200);

        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 403, new InternalResponseDto(ERROR, getTokenNotFoundMessage(token)));
    }

    @Test
    @DisplayName("ACTION после неуспешного LOGIN")
    public void actionAfterFailedLogin() {
        token = TokenHelper.generate();
        setStub(AUTH, 400);

        InternalRestService.doAction(token, LOGIN);

        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 403, new InternalResponseDto(ERROR, getTokenNotFoundMessage(token)));
    }

    @Test
    @DisplayName("ACTION после успешного LOGOUT")
    public void actionAfterLogout() {
        regTokenAndDoAction();

        InternalRestService.doAction(token, LOGOUT);

        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 403, new InternalResponseDto(ERROR, getTokenNotFoundMessage(token)));
    }

    @Test
    @DisplayName("ACTION с ошибкой внешнего сервиса (timeout)")
    public void actionWithExternalServiceTimeout() {
        regToken();
        stubs.add(
                mockService_8888.stubEndpoint(
                        POST,
                        DO_ACTION,
                        200,
                        null,
                        null,
                        getDefaultFormParams(token),
                        10000
                )
        );

        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 500, new InternalResponseDto(ERROR, INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @ParameterizedTest(name = "ACTION с ошибкой внешнего сервиса ({0})")
    @MethodSource("provideExternalErrors")
    public void actionExternalErrors(int statusCode) {
        token = TokenHelper.generate();
        setStub(AUTH, 200);
        setStub(DO_ACTION, statusCode);

        InternalRestService.doAction(token, LOGIN);

        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 500, new InternalResponseDto(ERROR, INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("ACTION с невалидным токеном (31 символ)")
    public void actionWithShortToken() {
        token = TokenHelper.generateShortToken();
        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("ACTION с невалидным токеном (33 символа)")
    public void actionWithLongToken() {
        token = TokenHelper.generateLongToken();
        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("ACTION с невалидным токеном (нижний регистр)")
    public void actionWithLowercaseToken() {
        token = TokenHelper.generateLowercaseToken();
        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("ACTION с невалидным токеном (спецсимволы)")
    public void actionWithSpecialCharactersToken() {
        token = TokenHelper.generateTokenWithSpecialCharacters();
        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("ACTION с невалидным токеном (пустая строка)")
    public void actionWithEmptyToken() {
        token = "";
        var response = InternalRestService.doAction(token, ACTION);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("ACTION без обязательного заголовка X-Api-Key")
    public void actionWithoutApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, ACTION, null);
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("ACTION с пустым значением X-Api-Key")
    public void actionWithEmptyApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, ACTION, "");
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("ACTION с невалидным значением X-Api-Key")
    public void actionWithInvalidApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, ACTION, "qazWSXedy");
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }
}

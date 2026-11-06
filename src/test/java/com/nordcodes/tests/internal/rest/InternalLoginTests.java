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

import java.util.List;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.nordcodes.model.rest.internal.InternalResponseDto.StatusCode.*;
import static com.nordcodes.services.rest.InternalRestService.InternalAction.*;

@Feature("LOGIN")
public class InternalLoginTests extends BaseInternalRestTest {

    @Test
    @DisplayName("Успешный LOGIN (200)")
    public void ok_200() {
        var response = regToken();
        setStub(AUTH, 200);
        AllureAssertions.validateRestResponse(response, 200,
                new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("Успешный LOGIN (200, невалидный JSON в ответе)")
    public void ok_200_withBrokenBodyFromMock() {
        token = TokenHelper.generate();
        stubs = List.of(
                mockService_8888.stubEndpoint(
                        POST,
                        AUTH,
                        200,
                        "INVALID_JSON",
                        null,
                        getDefaultFormParams(token)
                )
        );

        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 200, new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("Успешный LOGIN (200 timeout)")
    public void ok_200_timeout() {
        token = TokenHelper.generate();
        stubs = List.of(
                mockService_8888.stubEndpoint(
                        POST,
                        AUTH,
                        200,
                        null,
                        null,
                        getDefaultFormParams(token),
                        8000
                )
        );

        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 200,
                new InternalResponseDto(OK, null));
    }

    @Test
    @DisplayName("Повторный LOGIN")
    public void doubleLogin() {
        token = TokenHelper.generate();
        setStub(AUTH, 200);

        InternalRestService.doAction(token, LOGIN);

        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 409, new InternalResponseDto(ERROR, "Token '" + token + "' already exists"));
    }

    @ParameterizedTest(name = "LOGIN с ошибкой внешнего сервиса ({0})")
    @MethodSource("provideExternalErrors")
    public void loginExternalErrors(int statusCode) {
        token = TokenHelper.generate();
        setStub(AUTH, statusCode);

        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 500, new InternalResponseDto(ERROR, INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN с ошибкой внешнего сервиса (timeout)")
    public void loginWithExternalServiceTimeout() {
        token = TokenHelper.generate();
        stubs = List.of(
                mockService_8888.stubEndpoint(
                        POST,
                        AUTH,
                        200,
                        null,
                        null,
                        getDefaultFormParams(token),
                        10000
                )
        );

        var response = InternalRestService.doAction(token, LOGIN);

        int expectedTimeoutStatusCode = 500; //по хорошему бы 504 и тело ответа другое
        AllureAssertions.validateRestResponse(response, expectedTimeoutStatusCode, new InternalResponseDto(ERROR, INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN с невалидным токеном (31 символ)")
    public void loginWithShortToken() {
        token = TokenHelper.generateShortToken();
        
        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN с невалидным токеном (33 символа)")
    public void loginWithLongToken() {
        token = TokenHelper.generateLongToken();
        
        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN с невалидным токеном (нижний регистр)")
    public void loginWithLowercaseToken() {
        token = TokenHelper.generateLowercaseToken();
        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN с невалидным токеном (спецсимволы)")
    public void loginWithSpecialCharactersToken() {
        token = TokenHelper.generateTokenWithSpecialCharacters();
        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN с невалидным токеном (пустая строка)")
    public void loginWithEmptyToken() {
        token = "";
        var response = InternalRestService.doAction(token, LOGIN);
        AllureAssertions.validateRestResponse(response, 400, new InternalResponseDto(ERROR, TOKEN_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN без обязательного заголовка X-Api-Key")
    public void loginWithoutApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, LOGIN, null);
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN с пустым значением X-Api-Key")
    public void loginWithEmptyApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, LOGIN, "");
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("LOGIN с невалидным API-ключом")
    public void loginWithInvalidApiKey() {
        token = TokenHelper.generate();
        var response = InternalRestService.doAction(token, LOGIN, "qazWSXedy");
        AllureAssertions.validateRestResponse(response, 401, new InternalResponseDto(ERROR, INVALID_API_KEY_ERROR_MESSAGE));
    }
}

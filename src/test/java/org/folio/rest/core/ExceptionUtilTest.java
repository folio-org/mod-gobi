package org.folio.rest.core;

import static org.folio.gobi.exceptions.ErrorCodes.GENERIC_ERROR_CODE;
import static org.folio.rest.core.ExceptionUtil.ERROR_CAUSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Errors;
import org.folio.rest.utils.CopilotGenerated;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;

@CopilotGenerated(model = "Claude Sonnet 4.5")
class ExceptionUtilTest {

  @Test
  void testConvertToErrorsWithHttpException() {
    // Given
    Error error = new Error()
      .withCode("test_error")
      .withMessage("Test error message");
    Errors errors = new Errors()
      .withErrors(List.of(error))
      .withTotalRecords(1);
    HttpException httpException = new HttpException(400, errors);
    Throwable throwable = new RuntimeException(httpException);

    // When
    Errors result = ExceptionUtil.convertToErrors(throwable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalRecords());
    assertEquals("test_error", result.getErrors().getFirst().getCode());
    assertEquals("Test error message", result.getErrors().getFirst().getMessage());
  }

  @Test
  void testConvertToErrorsWithNonHttpException() {
    // Given
    String errorMessage = "Generic exception message";
    Throwable throwable = new RuntimeException(errorMessage);

    // When
    Errors result = ExceptionUtil.convertToErrors(throwable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalRecords());
    assertEquals(GENERIC_ERROR_CODE.getCode(), result.getErrors().getFirst().getCode());
    assertEquals(errorMessage, result.getErrors().getFirst().getAdditionalProperties().get(ERROR_CAUSE));
  }

  @Test
  void testConvertToErrorsWithEmptyErrors() {
    // Given
    Errors emptyErrors = new Errors()
      .withErrors(Collections.emptyList())
      .withTotalRecords(0);
    HttpException httpException = new HttpException(400, emptyErrors);

    // When
    Errors result = ExceptionUtil.convertToErrors(httpException);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getTotalRecords());
    assertTrue(result.getErrors().isEmpty());
  }

  @Test
  void testGetHttpExceptionWithJsonError() {
    // Given
    int statusCode = 400;
    Error error = new Error()
      .withCode("json_error")
      .withMessage("JSON error message");
    Errors errors = new Errors()
      .withErrors(List.of(error))
      .withTotalRecords(1);
    String jsonError = JsonObject.mapFrom(errors).encode();

    // When
    HttpException result = ExceptionUtil.getHttpException(statusCode, jsonError);

    // Then
    assertNotNull(result);
    assertEquals(statusCode, result.getCode());
    assertEquals("json_error", result.getErrors().getErrors().getFirst().getCode());
  }

  @Test
  void testGetHttpExceptionWithPlainTextError() {
    // Given
    int statusCode = 500;
    String plainTextError = "Plain text error message";

    // When
    HttpException result = ExceptionUtil.getHttpException(statusCode, plainTextError);

    // Then
    assertNotNull(result);
    assertEquals(statusCode, result.getCode());
    assertEquals(plainTextError, result.getErrors().getErrors().getFirst().getMessage());
  }

  @Test
  void testConvertToErrorsWithErrorContainingJsonMessage() {
    // Given
    Error innerError = new Error()
      .withCode("inner_code")
      .withMessage("Inner message");
    String jsonMessage = JsonObject.mapFrom(innerError).encode();
    Error outerError = new Error()
      .withCode("outer_code")
      .withMessage(jsonMessage);
    Errors errors = new Errors()
      .withErrors(List.of(outerError))
      .withTotalRecords(1);
    HttpException httpException = new HttpException(400, errors);

    // When
    Errors result = ExceptionUtil.convertToErrors(httpException);

    // Then
    assertNotNull(result);
    assertEquals("inner_code", result.getErrors().getFirst().getCode());
    assertEquals("Inner message", result.getErrors().getFirst().getMessage());
  }

  @Test
  void testConvertToErrorsWithMultipleErrors() {
    // Given
    Error error1 = new Error().withCode("error1").withMessage("First error");
    Error error2 = new Error().withCode("error2").withMessage("Second error");
    Errors errors = new Errors()
      .withErrors(List.of(error1, error2))
      .withTotalRecords(2);
    HttpException httpException = new HttpException(400, errors);

    // When
    Errors result = ExceptionUtil.convertToErrors(httpException);

    // Then
    assertEquals(2, result.getTotalRecords());
    assertEquals("error1", result.getErrors().get(0).getCode());
    assertEquals("error2", result.getErrors().get(1).getCode());
  }
}


package org.folio.rest.core;

import static java.util.stream.Collectors.toList;
import static org.folio.gobi.exceptions.ErrorCodes.GENERIC_ERROR_CODE;

import java.util.Collections;
import java.util.List;

import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Errors;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionUtil {

  private static final String ERROR_CAUSE = "cause";

  public static Errors convertToErrors(Throwable throwable) {
    final Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
    Errors errors;

    if (cause instanceof HttpException) {
      errors = ((HttpException) cause).getErrors();
      List<Error> errorList = errors.getErrors()
        .stream()
        .map(ExceptionUtil::getError)
        .collect(toList());

      errors.setErrors(errorList);
      errors.setTotalRecords(errorList.size());
    } else {
      errors = new Errors().withErrors(Collections.singletonList(GENERIC_ERROR_CODE.toError()
          .withAdditionalProperty(ERROR_CAUSE, cause.getMessage())))
        .withTotalRecords(1);
    }
    return errors;
  }

  public static HttpException getHttpException(int statusCode, String error) {
    if (isErrorMessageJson(error)) {
      return new HttpException(statusCode,  new JsonObject(error).mapTo(Errors.class));
    }
    return new HttpException(statusCode, error);
  }

  private static Error getError(Error error) {
    if (isErrorMessageJson(error.getMessage())) {
      return new JsonObject(error.getMessage()).mapTo(Error.class);
    }
    return error;
  }

  private static boolean isErrorMessageJson(String errorMessage) {
    try {
      new JsonObject(errorMessage);
      return true;
    } catch (DecodeException e) {
      return false;
    }
  }

}

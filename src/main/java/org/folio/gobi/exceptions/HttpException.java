package org.folio.gobi.exceptions;

import static org.folio.gobi.exceptions.ErrorCodes.CONFLICT;
import static org.folio.gobi.exceptions.ErrorCodes.GENERIC_ERROR_CODE;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.folio.gobi.exceptions.ErrorCodes;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Errors;

public class HttpException extends RuntimeException {
  private static final long serialVersionUID = 8109197948434861504L;

  private final int code;
  private Errors errors;

  public HttpException(int code, String message) {
    super(StringUtils.isNotEmpty(message) ? message : GENERIC_ERROR_CODE.getDescription());
    this.code = code;
    ErrorCodes ec = code == 409 ? CONFLICT : GENERIC_ERROR_CODE;
    this.errors = new Errors()
      .withErrors(Collections.singletonList(new Error().withCode(ec.getCode()).withMessage(message)))
      .withTotalRecords(1);
  }

  public HttpException(int code, ErrorCodes errCodes) {
    super(errCodes.getDescription());
    this.errors = new Errors()
      .withErrors(Collections.singletonList(new Error().withCode(errCodes.getCode()).withMessage(errCodes.getDescription())))
      .withTotalRecords(1);
    this.code = code;
  }

  public HttpException(int code, Error error) {
    this.code = code;
    this.errors = new Errors().withErrors(Collections.singletonList(error))
      .withTotalRecords(1);
  }

  public HttpException(int code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public HttpException(int code, Errors errors) {
    this.code = code;
    this.errors = errors;
  }

  public int getCode() {
    return code;
  }

  public Errors getErrors() {
    return errors;
  }
}

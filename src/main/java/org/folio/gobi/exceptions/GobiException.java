package org.folio.gobi.exceptions;

public class GobiException extends RuntimeException {

  private static final long serialVersionUID = 4357685342954598012L;

  private final ErrorCodes errorCode;

  public GobiException(ErrorCodes errorCode, Throwable cause) {
    super(cause);
    this.errorCode = errorCode;
  }

  public ErrorCodes getErrorCode() {
    return errorCode;
  }

}

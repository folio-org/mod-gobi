package org.folio.gobi;

public class HttpException extends Exception {
  private static final long serialVersionUID = 8109197948434861504L;

  private final int code;

  public HttpException(int code) {
    this.code = code;
  }

  public HttpException(int code, String message) {
    super(message);
    this.code = code;
  }

  public HttpException(int code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public HttpException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public HttpException(int code, String message, Throwable cause,
                       boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}

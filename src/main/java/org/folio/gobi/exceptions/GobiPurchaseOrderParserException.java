package org.folio.gobi.exceptions;

public class GobiPurchaseOrderParserException extends Exception {
  private static final long serialVersionUID = 8646677219457903984L;

  public GobiPurchaseOrderParserException() {
    super();
  }

  public GobiPurchaseOrderParserException(String message) {
    super(message);
  }

  public GobiPurchaseOrderParserException(Throwable cause) {
    super(cause);
  }

  public GobiPurchaseOrderParserException(String message, Throwable cause) {
    super(message, cause);
  }

  public GobiPurchaseOrderParserException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

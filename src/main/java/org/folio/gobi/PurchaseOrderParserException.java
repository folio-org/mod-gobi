package org.folio.gobi;

public class PurchaseOrderParserException extends Exception {
  private static final long serialVersionUID = 8646677219457903984L;

  public PurchaseOrderParserException() {
    super();
  }

  public PurchaseOrderParserException(String message) {
    super(message);
  }

  public PurchaseOrderParserException(Throwable cause) {
    super(cause);
  }

  public PurchaseOrderParserException(String message, Throwable cause) {
    super(message, cause);
  }

  public PurchaseOrderParserException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

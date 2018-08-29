package org.folio.gobi.exceptions;

public class InvalidTokenException extends Exception {

  private static final long serialVersionUID = -2479148623444479452L;

  public InvalidTokenException(String message) {
    super(message);
  }
}

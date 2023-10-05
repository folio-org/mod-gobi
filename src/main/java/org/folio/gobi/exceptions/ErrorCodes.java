package org.folio.gobi.exceptions;

import org.folio.rest.jaxrs.model.Error;

public enum ErrorCodes {

  GENERIC_ERROR_CODE("genericError", "Generic error"),
  CONFLICT("conflict", "Conflict when updating a record"),
  ORDER_MAPPINGS_RECORD_NOT_FOUND("order_mappings_record_not_found", "Custom mapping record not found"),
  ERROR_READING_DEFAULT_MAPPING_FILE("error_reading_default_mapping_file", "Error reading default mapping file"),
  INVALID_ORDER_MAPPING_FILE("invalid_order_mapping_file", "Invalid order mapping file");

  private final String code;
  private final String description;

  ErrorCodes(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return code + ": " + description;
  }

  public Error toError() {
    return new Error().withCode(code)
      .withMessage(description);
  }
}

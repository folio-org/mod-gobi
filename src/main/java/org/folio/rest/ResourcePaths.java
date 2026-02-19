package org.folio.rest;

public final class ResourcePaths {

  private ResourcePaths() {

  }
  public static final String PREFIXES_ENDPOINT = "/orders/configuration/prefixes";
  public static final String SUFFIXES_ENDPOINT = "/orders/configuration/suffixes";
  public static final String FUNDS_ENDPOINT = "/finance/funds";
  public static final String EXPENSE_CLASS_ENDPOINT = "/finance/expense-classes";
  public static final String ACQUISITION_METHOD_ENDPOINT = "/orders/acquisition-methods";
  public static final String GET_ORGANIZATION_ENDPOINT = "/organizations-storage/organizations";
  public static final String ACQUISITION_UNIT_ENDPOINT = "/acquisitions-units/units";
  public static final String LOCATIONS_ENDPOINT = "/search/consortium/locations";
  public static final String ORDER_LINES_ENDPOINT = "/orders/order-lines";
  public static final String MATERIAL_TYPES_ENDPOINT = "/material-types";
  public static final String CONTRIBUTOR_NAME_TYPES_ENDPOINT = "/contributor-name-types";
  public static final String IDENTIFIERS_ENDPOINT = "/identifier-types";
  public static final String TENANT_ADDRESSES_ENDPOINT = "/tenant-addresses";
  public static final String ORDERS_ENDPOINT = "/orders/composite-orders";
  public static final String ORDERS_BY_ID_ENDPOINT = "/orders/composite-orders/%s";
}

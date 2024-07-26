package org.folio.gobi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class HelperUtils {

  public static final String CONTRIBUTOR_NAME_TYPES = "contributorNameTypes";
  public static final String FUND_CODE_EXPENSE_CLASS_SEPARATOR = ":";
  public static final String INVALID_ISBN_PRODUCT_ID_TYPE = "Invalid ISBN";
  public static final String BASEACCOUNT_SUBACCOUNT_SEPARATOR = "-";

  private HelperUtils() {
  }

  public static String extractMaterialTypeId(JsonObject obj) {
    return extractIdOfFirst(obj, "mtypes");
  }

  public static String extractOrderId(JsonObject obj) {
    return extractIdOfFirst(obj, "purchaseOrders");
  }

  public static String extractProductTypeId(JsonObject obj) {
    return extractIdOfFirst(obj, "identifierTypes");
  }

  public static String extractContributorNameTypeId(JsonObject obj) {
    return extractIdOfFirst(obj, CONTRIBUTOR_NAME_TYPES);
  }

  public static String extractIdOfFirst(JsonObject obj, String arrField) {
    if (obj == null || arrField == null || arrField.isEmpty()) {
      return null;
    }
    JsonArray jsonArray = obj.getJsonArray(arrField);
    if (jsonArray == null || jsonArray.size() == 0) {
      return null;
    }

    JsonObject item = jsonArray.getJsonObject(0);
    if (item == null) {
      return null;
    }
    return item.getString("id");
  }

  public static String encodeValue(String query) {
    return URLEncoder.encode(query, StandardCharsets.UTF_8);
  }

  public static String extractFundCode(String fundCode) {
    return StringUtils.substringBefore(fundCode, FUND_CODE_EXPENSE_CLASS_SEPARATOR);
  }

  public static String extractExpenseClassFromFundCode(String fundCode) {
    return StringUtils.substringAfterLast(fundCode, FUND_CODE_EXPENSE_CLASS_SEPARATOR);
  }

  public static String normalizeSubAccout(String organisationAccountNo) {
    return organisationAccountNo.replace(BASEACCOUNT_SUBACCOUNT_SEPARATOR, "");
  }

  public static String getVendAccountFromOrgAccountsList(String vendorAccountNo, List<String> orgAccountNoList) {
    return orgAccountNoList.stream().filter(account ->
      normalizeSubAccout(account).equals(vendorAccountNo)).findFirst().map(Object::toString).orElse("");
  }

}

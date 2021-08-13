package org.folio.gobi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;

import org.apache.commons.lang3.StringUtils;
import org.folio.gobi.exceptions.HttpException;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
public class HelperUtils {

  public static final String CONTRIBUTOR_NAME_TYPES = "contributorNameTypes";
  public static final String FUND_CODE_EXPENSE_CLASS_SEPARATOR = ":";

  private HelperUtils() {

  }

  public static String truncate(String message, int limit) {
    return (message != null && limit > 0) ? message.substring(0, Math.min(message.length(), limit)) : message;
  }

  public static JsonObject verifyAndExtractBody(org.folio.rest.tools.client.Response response) {
    if (response == null) {
      throw new CompletionException(new NullPointerException("response is null"));
    }

    if (!org.folio.rest.tools.client.Response.isSuccess(response.getCode())) {
      throw new CompletionException(new HttpException(response.getCode(), response.getError().toString()));
    }

    return response.getBody();
  }

  public static String extractLocationId(JsonObject obj) {
    return extractIdOfFirst(obj, "locations");
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

}

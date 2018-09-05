package org.folio.gobi;

import java.util.concurrent.CompletionException;

import org.folio.gobi.exceptions.HttpException;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class HelperUtils {

  private HelperUtils() {

  }

  public static String truncate(String message, int limit) {
    return message.substring(0, Math.min(message.length(), limit));
  }

  public static JsonObject verifyAndExtractBody(org.folio.rest.tools.client.Response response) {
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

  public static String extractVendorId(JsonObject obj) {
    return extractIdOfFirst(obj, "vendors");
  }

  public static String extractIdOfFirst(JsonObject obj, String arrField) {
    JsonArray jsonArray = obj.getJsonArray(arrField);
    if (jsonArray == null) {
      return null;
    }
    JsonObject item = jsonArray.getJsonObject(0);
    if (item == null) {
      return null;
    }
    return item.getString("id");
  }
}

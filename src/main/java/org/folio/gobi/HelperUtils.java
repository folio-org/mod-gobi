package org.folio.gobi;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.apache.log4j.Logger;
import org.folio.gobi.Mapper.Field;
import org.folio.gobi.Mapper.NodeCombinator;
import org.folio.gobi.Mapper.Translation;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.Mappings;
import org.w3c.dom.NodeList;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.xml.crypto.Data;

public class HelperUtils {
  private static final Logger logger = Logger.getLogger(HelperUtils.class);

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

  public static String extractVendorId(JsonObject obj) {
    return extractIdOfFirst(obj, "vendors");
  }

  public static String extractIdOfFirst(JsonObject obj, String arrField) {
    if (obj == null || arrField == null || arrField.isEmpty()) {
      return null;
    }
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

  public static String encodeValue(String value) throws UnsupportedEncodingException {
    return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
  }

  public static Map<Field, DataSource> extractOrderMappings(JsonObject jo) {
    final String mappingsString = jo.getJsonArray("configs").getJsonObject(0).getString("value");
    final Mappings mappings = Json.decodeValue(mappingsString, Mappings.class);

    final Map<Field, DataSource> map = new EnumMap<>(Field.class);

    List<Mapping> mappingsList = mappings.getMappings();
    if (mappingsList != null) {
      for (Mapping mapping : mappingsList) {
        logger.info("Mapping existis for field: " + mapping.getField());
        Field field = Field.valueOf(mapping.getField().toString());
        org.folio.rest.mappings.model.DataSource ds = mapping.getDataSource();
        DataSource dataSource = extractOrderMapping(ds, map);
        map.put(field, dataSource);
      }
    }

    return map;
  }

  public static DataSource extractOrderMapping(org.folio.rest.mappings.model.DataSource dataSource, Map<Field, DataSource> map){
    org.folio.rest.mappings.model.DataSource.Combinator combinator = dataSource.getCombinator();
    NodeCombinator nc = null;
    if (combinator != null) {
      try {
        Method combinatorMethod = Mapper.class.getMethod(combinator.toString(), NodeList.class);
        nc = data -> {
          try {
            return (String) combinatorMethod.invoke(null, data);
          } catch (Exception e) {
            logger.error("Unable to invoke combinator method: " + combinator, e);
          }
          return null;
        };
      } catch (NoSuchMethodException e) {
        logger.error("Combinator method not found: " + combinator, e);
      }
    }
    org.folio.rest.mappings.model.DataSource.Translation translation = dataSource.getTranslation();
    Translation<?> t = null;
    if (translation != null) {
      try {

        Method translationMethod = Mapper.class.getMethod(translation.toString(), String.class);
        t = data -> {
          try {
            return (CompletableFuture<Object>) translationMethod.invoke(null, data);
          } catch (Exception e) {
            logger.error("Unable to invoke translation method: " + translation, e);
          }
          return null;
        };
      } catch (NoSuchMethodException e) {
        logger.error("Translation method not found: " + translation, e);
      }
    }

    Object defaultValue = new Object();

    if (dataSource.getDefault() != null) {
      defaultValue = dataSource.getDefault();
    }
    else if (dataSource.getFromOtherField() != null){
      String otherField = dataSource.getFromOtherField().value();
      defaultValue = map.get(Field.valueOf(otherField));
    }
    else if(dataSource.getDefaultMapping() != null) {
      defaultValue = extractOrderMapping(dataSource.getDefaultMapping().getDataSource(), map);
    }

    String from = dataSource.getFrom();

    DataSource dataSrc = DataSource.builder()
      .withFrom(from)
      .withTranslation(t)
      .withTranslateDefault(true)
      .withCombinator(nc)
      .withDefault(defaultValue)
      .build();

    return dataSrc;
  }
}

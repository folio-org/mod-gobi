package org.folio.gobi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.mappings.model.OrderMappings;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MappingDetailsService {

  private static final Logger logger = LogManager.getLogger(MappingDetailsService.class);
  private JsonObject mappingsProperties;

  private final String PROPERTIES = "properties";
  private final String DATA_SOURCE = "dataSource";
  private final String TRANSLATION = "translation";
  private final String JAVA_ENUMS = "javaEnums";
  private final String FIELD = "field";

  public MappingDetailsService() {
    URL mappingJson = ClassLoader.getSystemClassLoader().getResource("mapping.json");
    try (InputStream mappingJsonStream = mappingJson.openStream()) {
      String jsonString = new String(mappingJsonStream.readAllBytes(), StandardCharsets.UTF_8);
      mappingsProperties = new JsonObject(jsonString).getJsonObject(PROPERTIES);
    } catch (IOException e) {
      logger.error(String.format("Exception when reading a mappingJson file %s", e.getMessage()));
    }
  }

  public JsonArray retrieveTranslators() {
    return mappingsProperties.getJsonObject(DATA_SOURCE)
      .getJsonObject(PROPERTIES)
      .getJsonObject(TRANSLATION)
      .getJsonArray(JAVA_ENUMS);
  }

  public JsonArray retrieveFields() {
    return mappingsProperties.getJsonObject(FIELD)
      .getJsonArray(JAVA_ENUMS);
  }

  public List<OrderMappings.OrderType> retrieveMappingsTypes() {
    return List.of(OrderMappings.OrderType.values());
  }
}

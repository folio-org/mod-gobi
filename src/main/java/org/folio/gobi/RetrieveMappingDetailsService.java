package org.folio.gobi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.OrderMappings;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RetrieveMappingDetailsService {

  private static final Logger logger = LogManager.getLogger(RetrieveMappingDetailsService.class);
  private JsonObject mappings;
  private static final String MAPPING_JSON_PATH = "ramls/acq-models/mod-gobi/schemas/mapping.json";

  public RetrieveMappingDetailsService() {
    URL mappingJson = RetrieveMappingDetailsService.class.getClassLoader().getResource(MAPPING_JSON_PATH);
    try (InputStream mappingJsonStream = mappingJson.openStream()) {
      String jsonString = new String(mappingJsonStream.readAllBytes(), StandardCharsets.UTF_8);
      mappings = new JsonObject(jsonString);
    } catch (IOException e) {
      logger.error(String.format("Exception when reading a mappingJson file %s", e.getMessage()));
    }
  }

  public JsonArray retrieveTranslators() {
    return mappings.getJsonObject("properties")
      .getJsonObject("dataSource")
      .getJsonObject("properties")
      .getJsonObject("translation")
      .getJsonArray("javaEnums");
  }

  public JsonArray retrieveFields() {
    return mappings.getJsonObject("properties")
      .getJsonObject("field")
      .getJsonArray("javaEnums");
  }

  public List<OrderMappings.OrderType> retrieveMappingsTypes() {
    return List.of(OrderMappings.OrderType.values());
  }
}

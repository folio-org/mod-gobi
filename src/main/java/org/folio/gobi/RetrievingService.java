package org.folio.gobi;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.mappings.model.OrderMappings;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RetrievingService {

  private static final Logger logger = LogManager.getLogger(RetrievingService.class);
  private JsonObject mappings;

  public RetrievingService() {
    try {
      URL mappingJson = ClassLoader.getSystemClassLoader().getResource("mapping.json");
      String jsonString = new String(mappingJson.openStream().readAllBytes(), StandardCharsets.UTF_8);
      mappings = new JsonObject(jsonString);
    } catch (IOException e) {
      logger.error("Exception when reading a mappingJson file " + e);
    }
  }

  public List retrieveTranslators() {
    return mappings.getJsonObject("properties")
      .getJsonObject("dataSource")
      .getJsonObject("properties")
      .getJsonObject("translation")
      .getJsonArray("javaEnums")
      .getList();
  }

  public List retrieveFields() {
    return mappings.getJsonObject("properties")
      .getJsonObject("field")
      .getJsonArray("javaEnums")
      .getList();
  }

  public List<OrderMappings.OrderType> retrieveMappingsTypes() {
    return List.of(OrderMappings.OrderType.values());
  }
}

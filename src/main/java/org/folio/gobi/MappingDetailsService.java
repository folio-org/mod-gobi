package org.folio.gobi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.acq.model.Field;
import org.folio.rest.acq.model.FolioOrderFields;
import org.folio.rest.acq.model.FolioOrderTranslators;
import org.folio.rest.acq.model.Translator;
import org.folio.rest.jaxrs.model.OrderMappings;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MappingDetailsService {

  private static final Logger logger = LogManager.getLogger(MappingDetailsService.class);
  private JsonObject mappingsProperties;

  private static final String PROPERTIES = "properties";
  private static final String DATA_SOURCE = "dataSource";
  private static final String TRANSLATION = "translation";
  private static final String JAVA_ENUMS = "javaEnums";
  private static final String FIELD = "field";

  private static final String NAME = "name";
  private static final String TITLE = "title";
  private static final String DESCRIPTION = "description";

  public MappingDetailsService() {
    URL mappingJson = MappingDetailsService.class.getClassLoader().getResource("ramls/acq-models/mod-gobi/schemas/mapping.json");
    try (InputStream mappingJsonStream = mappingJson.openStream()) {
      String jsonString = new String(mappingJsonStream.readAllBytes(), StandardCharsets.UTF_8);
      mappingsProperties = new JsonObject(jsonString).getJsonObject(PROPERTIES);
    } catch (IOException e) {
      logger.error(String.format("Exception when reading a mappingJson file %s", e.getMessage()));
    }
  }

  public FolioOrderTranslators retrieveTranslators() {
    JsonArray translatorsArray = mappingsProperties.getJsonObject(DATA_SOURCE)
      .getJsonObject(PROPERTIES)
      .getJsonObject(TRANSLATION)
      .getJsonArray(JAVA_ENUMS);

    List<Translator> translators = translatorsArray.stream()
      .map(translatorObject -> {
        JsonObject translatorJsonObject = JsonObject.mapFrom(translatorObject);
        return new Translator()
          .withName(translatorJsonObject.getString(NAME))
          .withTranslator(translatorJsonObject.getString(TITLE))
          .withDescription(translatorJsonObject.getString(DESCRIPTION));
      }).collect(Collectors.toList());

    return new FolioOrderTranslators()
      .withTranslators(translators)
      .withTotalRecords(translators.size());
  }

  public FolioOrderFields retrieveFields() {
    JsonArray fieldsArray = mappingsProperties.getJsonObject(FIELD)
      .getJsonArray(JAVA_ENUMS);

    List<Field> fields = fieldsArray.stream()
      .map(fieldsObject -> {
        JsonObject fieldsJsonObject = JsonObject.mapFrom(fieldsObject);
        return new Field()
          .withName(fieldsJsonObject.getString(NAME))
          .withPath(fieldsJsonObject.getString(TITLE))
          .withDescription(fieldsJsonObject.getString(DESCRIPTION));
      }).collect(Collectors.toList());

    return new FolioOrderFields()
      .withFields(fields)
      .withTotalRecords(fields.size());
  }

  public List<OrderMappings.OrderType> retrieveMappingsTypes() {
    return List.of(OrderMappings.OrderType.values());
  }
}

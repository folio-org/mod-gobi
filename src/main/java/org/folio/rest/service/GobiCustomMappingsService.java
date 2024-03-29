package org.folio.rest.service;

import static java.util.stream.Collectors.toList;
import static org.folio.gobi.exceptions.ErrorCodes.ERROR_READING_DEFAULT_MAPPING_FILE;
import static org.folio.gobi.exceptions.ErrorCodes.ORDER_MAPPINGS_RECORD_NOT_FOUND;
import static org.folio.rest.ResourcePaths.CONFIGURATION_ENDPOINT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.core.RestClient;
import org.folio.rest.jaxrs.model.Config;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappingsView;
import org.folio.rest.jaxrs.model.OrderMappingsViewCollection;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class GobiCustomMappingsService {
  private final RestClient restClient;
  public static final String SEARCH_ENDPOINT = "%s?limit=%s&offset=%s%s";
  public static final String CONFIG_FIELD = "configs";
  public static final String MAPPINGS_BY_ORDER_TYPE_QUERY = "module==GOBI AND configName==orderMappings AND code==gobi.order.%s";
  private static final Logger logger = LogManager.getLogger(GobiCustomMappingsService.class);

  public GobiCustomMappingsService(RestClient client) {
    this.restClient = client;
  }

  public Future<OrderMappingsViewCollection> getCustomMappingListByQuery(int offset, int limit) {
    var query = "module==GOBI AND configName==orderMappings";
    return restClient.handleGetRequest(CONFIGURATION_ENDPOINT, query, offset, limit)
      .map(this::buildOrderMappingsViewCollectionResponse);
  }

  public Future<OrderMappingsView> getCustomMappingByOrderType(String orderType) {
    return getCustomMappingConfigByOrderType(orderType)
      .map(configs -> buildOrderMappingsViewResponse(configs, orderType));

  }

  private Future<JsonObject> getCustomMappingConfigByOrderType(String orderType) {
    String query = String.format(MAPPINGS_BY_ORDER_TYPE_QUERY, orderType);
    return restClient.handleGetRequest(CONFIGURATION_ENDPOINT, query, 0, 1);

  }

  private OrderMappingsView buildOrderMappingsViewResponse(JsonObject entries, String orderType) {
    var omvResponse = new OrderMappingsView();
    if (entries.getJsonArray(CONFIG_FIELD).isEmpty()) {
      return omvResponse
        .withOrderMappings(loadDefaultMappingByType(orderType))
        .withMappingType(OrderMappingsView.MappingType.DEFAULT);
    }
    else {
      var config = entries.getJsonArray(CONFIG_FIELD).getJsonObject(0);
      var customOrderMappings = Json.decodeValue(config.mapTo(Config.class).getValue(), OrderMappings.class);
      return omvResponse
        .withMappingType(OrderMappingsView.MappingType.CUSTOM)
        .withOrderMappings(customOrderMappings);
    }
  }

  /**
   * Receives a JsonObject as a parameter which contains custom mappings and merges these
   * with default mappings, returning a list of all mappings, by first checking if a
   * custom mapping exists for an order type, otherwise uses its default mapping
   *
   * @param configs Object containing custom mappings
   * @return mappings for each order type, custom if present, otherwise default is returned
   */
  private OrderMappingsViewCollection buildOrderMappingsViewCollectionResponse(JsonObject configs) {
    final var customMappings = getCustomMappings(configs.getJsonArray(CONFIG_FIELD));
    var mappings = loadDefaultMappings().stream()
      .map(defMap -> new OrderMappingsView()
        .withMappingType(getMappingType(defMap, customMappings))
        .withOrderMappings(getMapping(defMap, customMappings)))
      .collect(toList());

    return new OrderMappingsViewCollection()
      .withOrderMappingsViews(mappings)
      .withTotalRecords(mappings.size());
  }

  private List<OrderMappings> loadDefaultMappings() {
    return Arrays.stream(OrderMappings.OrderType.values())
      .map(OrderMappings.OrderType::value)
      .map(this::loadDefaultMappingByType)
      .collect(toList());

  }

  private OrderMappings loadDefaultMappingByType(String orderType) {
    URL mappingJson = ClassLoader.getSystemClassLoader().getResource(orderType + ".json");
    String jsonString;
    try (InputStream mappingJsonStream = mappingJson.openStream()) {
       jsonString = new String(mappingJsonStream.readAllBytes(), StandardCharsets.UTF_8);
      return Json.decodeValue(jsonString, OrderMappings.class);

    } catch (IOException e) {
      logger.error(String.format("Exception when reading a mappingJson file %s", e.getMessage()));
      throw new HttpException(500, ERROR_READING_DEFAULT_MAPPING_FILE);
    }
  }


  public Future<OrderMappingsView> postCustomMapping(OrderMappings orderMappings) {
    Config configEntry = buildCustomMappingConfigurationEntry(orderMappings, UUID.randomUUID().toString());
    return restClient.post(CONFIGURATION_ENDPOINT, JsonObject.mapFrom(configEntry))
      .map(createdConfig -> {
        var entry = createdConfig.mapTo(Config.class);
        var createdOrderMappings = Json.decodeValue(entry.getValue(), OrderMappings.class);
        return new OrderMappingsView()
        .withMappingType(OrderMappingsView.MappingType.CUSTOM)
        .withOrderMappings(createdOrderMappings);
      });
  }

  public Future<Void> putCustomMapping(String orderType, OrderMappings orderMappings) {
    return getCustomMappingConfigByOrderType(orderType)
      .compose(configEntries -> {
        if (!configEntries.getJsonArray(CONFIG_FIELD).isEmpty()) {
          var config = configEntries.getJsonArray(CONFIG_FIELD).getJsonObject(0).mapTo(Config.class);
          Config configEntryForUpdate = buildCustomMappingConfigurationEntry(orderMappings, config.getId());

          return restClient.handlePutRequest(CONFIGURATION_ENDPOINT + "/" + config.getId(), JsonObject.mapFrom(configEntryForUpdate));
        }
        throw new HttpException(404, ORDER_MAPPINGS_RECORD_NOT_FOUND);
      });
  }

  public Future<Void> deleteCustomMapping(String orderType) {
    return getCustomMappingConfigByOrderType(orderType)
      .compose(configEntries -> {
      if (!configEntries.getJsonArray(CONFIG_FIELD).isEmpty()) {
        var config = configEntries.getJsonArray(CONFIG_FIELD).getJsonObject(0).mapTo(Config.class);
        return restClient.delete(CONFIGURATION_ENDPOINT + "/" + config.getId());
      }
      throw new HttpException(404, ORDER_MAPPINGS_RECORD_NOT_FOUND);
    });
  }

  private Config buildCustomMappingConfigurationEntry(OrderMappings orderMappings, String id) {
    return new Config()
      .withId(id)
      .withModule("GOBI")
      .withConfigName("orderMappings")
      .withValue(JsonObject.mapFrom(orderMappings).encode())
      .withEnabled(true)
      .withCode("gobi.order." + orderMappings.getOrderType());
  }

  private Map<OrderMappings.OrderType, OrderMappings> getCustomMappings(JsonArray configArray) {
    final var customMappings = new HashMap<OrderMappings.OrderType, OrderMappings>();
    for (Object configObj : configArray) {
      if (configObj instanceof JsonObject config) {
        var orderType = extractMappingOrderType(config.getString("code"));
        var customOrderMappings = Json.decodeValue(config.mapTo(Config.class).getValue(), OrderMappings.class);
        customMappings.put(orderType, customOrderMappings);
      }
    }
    return customMappings;
  }

  private OrderMappings.OrderType extractMappingOrderType(String code) {
    return OrderMappings.OrderType.fromValue(code.substring(code.lastIndexOf(".") + 1));
  }

  private OrderMappingsView.MappingType getMappingType(OrderMappings mapping, Map<OrderMappings.OrderType, OrderMappings> customMappings) {
    return customMappings.containsKey(mapping.getOrderType()) ? OrderMappingsView.MappingType.CUSTOM : OrderMappingsView.MappingType.DEFAULT;
  }

  private OrderMappings getMapping(OrderMappings mapping, Map<OrderMappings.OrderType, OrderMappings> customMappings) {
    return Optional.ofNullable(customMappings.get(mapping.getOrderType())).orElse(mapping);
  }

}

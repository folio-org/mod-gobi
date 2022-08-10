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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.core.RestClient;
import org.folio.rest.jaxrs.model.Config;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappingsView;
import org.folio.rest.jaxrs.model.OrderMappingsViewCollection;

import io.vertx.core.Context;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class GobiCustomMappingsService {
  private final RestClient restClient;
  public static final String SEARCH_ENDPOINT = "%s?limit=%s&offset=%s%s";
  public static final String CONFIG_FIELD = "configs";
  public static final String MAPPINGS_BY_ORDER_TYPE_QUERY = "module==GOBI AND configName==orderMappings AND code==gobi.order.%s";
  private static final Logger logger = LogManager.getLogger(GobiCustomMappingsService.class);

  public GobiCustomMappingsService(Map<String, String> okapiHeaders, Context vertxContext) {
    this.restClient = new RestClient(okapiHeaders, vertxContext);
  }

  public CompletableFuture<OrderMappingsViewCollection> getCustomMappingListByQuery(String query, int offset, int limit) {
    query = "configName==GOBI AND configName==orderMappings";
    return restClient.handleGetRequest(CONFIGURATION_ENDPOINT, query, offset, limit)
      .thenApply(this::buildOrderMappingsViewCollectionResponse);
  }

  public CompletableFuture<OrderMappingsView> getCustomMappingByOrderType(String orderType) {
    return getCustomMappingConfigByOrderType(orderType)
      .thenApply(configs -> buildOrderMappingsViewResponse(configs, orderType));

  }

  private CompletableFuture<JsonObject> getCustomMappingConfigByOrderType(String orderType) {
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

  private OrderMappingsViewCollection buildOrderMappingsViewCollectionResponse(JsonObject configs) {
    List<OrderMappings> defaultMappings = loadDefaultMappings();
    var omvc = new OrderMappingsViewCollection();
    List<Config> configList = configs.getJsonArray(CONFIG_FIELD)
      .stream()
      .map(JsonObject.class::cast)
      .map(json -> json.mapTo(Config.class))
      .collect(toList());


  //  final List<Mapping> orderMappingList = orderMapping.getMappings();
    List<OrderMappingsView> omv = configList.stream()
      .map(conf -> new OrderMappingsView()
        .withMappingType(OrderMappingsView.MappingType.CUSTOM)
        .withOrderMappings(null))
      .collect(toList());

    omvc.withOrderMappingsViews(omv);
    return omvc;
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
      // TODO
      throw new HttpException(500, ERROR_READING_DEFAULT_MAPPING_FILE);
    }
  }


  public CompletableFuture<OrderMappingsView> postCustomMapping(OrderMappings orderMappings) {
    return restClient.post(CONFIGURATION_ENDPOINT, JsonObject.mapFrom(orderMappings))
      .thenApply(json -> json.mapTo(OrderMappingsView.class));
  }

  public CompletableFuture<Void> putCustomMapping(String orderType, OrderMappings orderMappings) {
    return getCustomMappingConfigByOrderType(orderType)
      .thenCompose(configEntries -> {
        if (!configEntries.isEmpty()) {
          var config = configEntries.getJsonArray(CONFIG_FIELD).getJsonObject(0).mapTo(Config.class);
          return restClient.handlePutRequest(CONFIGURATION_ENDPOINT + "/" + config.getId(), JsonObject.mapFrom(orderMappings));
        }
        throw new HttpException(404, ORDER_MAPPINGS_RECORD_NOT_FOUND);
      });
  }

  public CompletableFuture<Void> deleteCustomMapping(String orderType) {
    return getCustomMappingConfigByOrderType(orderType)
      .thenCompose(configEntries -> {
      if (!configEntries.isEmpty()) {
        var config = configEntries.getJsonArray(CONFIG_FIELD).getJsonObject(0).mapTo(Config.class);
        return restClient.delete(CONFIGURATION_ENDPOINT + "/", config.getId());
      }
      throw new HttpException(404, ORDER_MAPPINGS_RECORD_NOT_FOUND);
    });
  }
}

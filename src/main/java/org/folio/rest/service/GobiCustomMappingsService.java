package org.folio.rest.service;

import static java.util.stream.Collectors.toList;
import static org.folio.rest.ResourcePaths.CONFIGURATION_ENDPOINT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.folio.rest.client.ConfigurationsClient;
import org.folio.rest.core.RestClient;
import org.folio.rest.jaxrs.model.Config;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappingsView;

import io.vertx.core.Context;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.OrderMappingsViewCollection;

/*import lombok.extern.log4j.Log4j;

@Log4j*/
public class GobiCustomMappingsService {
  private final RestClient restClient;
  public static final String SEARCH_ENDPOINT = "%s?limit=%s&offset=%s%s";

  ConfigurationsClient configurationsClient;
  public GobiCustomMappingsService(Map<String, String> okapiHeaders, Context vertxContext) {
    this.restClient = new RestClient(okapiHeaders, vertxContext);
  }

  public CompletableFuture<OrderMappingsViewCollection> getCustomMappingListByQuery(String query, int offset, int limit) {
    query = "configName==GOBI AND configName==orderMappings";
    return restClient.handleGetRequest(CONFIGURATION_ENDPOINT, query, offset, limit)
      .thenApply(this::buildOrderMappingsViewCollectionResponse);
  }

  public CompletableFuture<OrderMappingsView> getCustomMappingByOrderType(String orderType) {
    var query = String.format("configName==GOBI AND configName==orderMappings + code==gobi.order.%s", orderType);
    return restClient.handleGetRequest(CONFIGURATION_ENDPOINT, query, 0, 1)
      .thenApply(configs -> buildOrderMappingsViewResponse(configs, orderType));

  }

  private OrderMappingsView buildOrderMappingsViewResponse(JsonObject entries, String orderType) {
    var omvResponse = new OrderMappingsView();
    if (entries.getJsonArray("configs").isEmpty()) {
      return omvResponse
        .withOrderMappings(loadDefaultMappingByType(orderType))
        .withMappingType(OrderMappingsView.MappingType.DEFAULT);
    }
    else {
      var config = entries.getJsonArray("configs").getJsonObject(0);
      var customOrderMappings = Json.decodeValue(config.mapTo(Config.class).getValue(), OrderMappings.class);
      return omvResponse
        .withMappingType(OrderMappingsView.MappingType.CUSTOM)
        .withOrderMappings(customOrderMappings);
    }
  }

  private OrderMappingsViewCollection buildOrderMappingsViewCollectionResponse(JsonObject configs) {
    List<OrderMappings> defaultMappings = loadDefaultMappings();
    var omvc = new OrderMappingsViewCollection();
    List<Config> configList = configs.getJsonArray("configs")
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
    StringBuilder sb = new StringBuilder();
    try (Stream<String> lines = Files.lines(Paths.get(orderType + ".json"))) {
      lines.forEach(sb::append);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Json.decodeValue(sb.toString(), OrderMappings.class);
  }


  public CompletableFuture<OrderMappingsView> postCustomMapping(OrderMappings orderMappingsView) {
    return restClient.post("", JsonObject.mapFrom(orderMappingsView))
      .thenApply(json -> json.mapTo(OrderMappingsView.class));
  }

  public CompletableFuture<Void> putCustomMapping(String orderType, OrderMappings orderMappingsView) {
    return restClient.handlePutRequest("", JsonObject.mapFrom(orderMappingsView));
  }

  public CompletableFuture<Void> deleteCustomMapping(String id) {
    return restClient.delete(CONFIGURATION_ENDPOINT + "/", id);
  }
}

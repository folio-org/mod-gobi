package org.folio.rest.service;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.folio.rest.ResourcePaths.CONFIGURATION_ENDPOINT;
import static org.folio.rest.utils.TestUtils.getMockData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.folio.rest.core.RestClient;
import org.folio.rest.jaxrs.model.Configs;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappingsView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

@ExtendWith(MockitoExtension.class)
class GobiCustomMappingsServiceTest {

  @Mock
  RestClient restClient;

  @BeforeEach
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getCustomMappingListByQuery() throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");

    doReturn(completedFuture(new JsonObject(jsonConfigs)))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    var orderMappingsViewCollection = new GobiCustomMappingsService(restClient)
      .getCustomMappingListByQuery(0,1)
      .join();

    assertFalse(orderMappingsViewCollection.getOrderMappingsViews().isEmpty());
  }

  @Test
  void getCustomMappingByOrderType() throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");

    doReturn(completedFuture(new JsonObject(jsonConfigs)))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    var orderMappingsView = new GobiCustomMappingsService(restClient)
      .getCustomMappingByOrderType(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH.value())
      .join();

    assertEquals(OrderMappingsView.MappingType.CUSTOM, orderMappingsView.getMappingType());
    assertFalse(orderMappingsView.getOrderMappings().getMappings().isEmpty());
  }

  @Test
  void getDefaultMappingByOrderType() {

    doReturn(completedFuture(JsonObject.mapFrom(new Configs())))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    var orderMappingsView = new GobiCustomMappingsService(restClient)
      .getCustomMappingByOrderType(OrderMappings.OrderType.LISTED_PRINT_SERIAL.value())
      .join();

    assertEquals(OrderMappingsView.MappingType.DEFAULT, orderMappingsView.getMappingType());
    assertFalse(orderMappingsView.getOrderMappings().getMappings().isEmpty());
  }

  @Test
  void postCustomMapping() throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");
    var config =  new JsonObject(jsonConfigs).mapTo(Configs.class).getConfigs().get(0);
    var orderMapping = Json.decodeValue(config.getValue(), OrderMappings.class);

    doReturn(completedFuture(JsonObject.mapFrom(config)))
      .when(restClient).post(anyString(), any());

    var orderMappingsView = new GobiCustomMappingsService(restClient)
      .postCustomMapping(orderMapping)
      .join();

    assertEquals(OrderMappingsView.MappingType.CUSTOM, orderMappingsView.getMappingType());
    assertFalse(orderMappingsView.getOrderMappings().getMappings().isEmpty());
  }

  @Test
  void putCustomMapping() throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");
    var config =  new JsonObject(jsonConfigs).mapTo(Configs.class).getConfigs().get(0);
    var orderMapping = Json.decodeValue(config.getValue(), OrderMappings.class);

    doReturn(completedFuture(new JsonObject(jsonConfigs)))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    doReturn(completedFuture(null))
      .when(restClient).handlePutRequest(eq(CONFIGURATION_ENDPOINT + "/" + config.getId()), any());

    var nullResult = new GobiCustomMappingsService(restClient)
      .putCustomMapping(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH.value(), orderMapping)
      .join();
    assertNull(nullResult);

  }

  @Test
  void deleteCustomMapping() throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");
    var config =  new JsonObject(jsonConfigs).mapTo(Configs.class).getConfigs().get(0);

    doReturn(completedFuture(new JsonObject(jsonConfigs)))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    doReturn(completedFuture(null))
      .when(restClient).delete(CONFIGURATION_ENDPOINT + "/" + config.getId());

    var nullResult = new GobiCustomMappingsService(restClient)
      .deleteCustomMapping(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH.value())
      .join();
    assertNull(nullResult);
  }
}

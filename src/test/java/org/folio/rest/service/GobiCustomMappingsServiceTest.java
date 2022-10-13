package org.folio.rest.service;

import static io.vertx.core.Future.succeededFuture;
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
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(MockitoExtension.class)
@ExtendWith(VertxExtension.class)
class GobiCustomMappingsServiceTest {

  @Mock
  RestClient restClient;

  @BeforeEach
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getCustomMappingListByQuery(VertxTestContext vtc) throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");

    doReturn(succeededFuture(new JsonObject(jsonConfigs)))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    new GobiCustomMappingsService(restClient)
    .getCustomMappingListByQuery(0,1)
    .onComplete(vtc.succeeding(orderMappingsViewCollection -> {
      assertFalse(orderMappingsViewCollection.getOrderMappingsViews().isEmpty());
      vtc.completeNow();
    }));
  }

  @Test
  void getCustomMappingByOrderType(VertxTestContext vtc) throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");

    doReturn(succeededFuture(new JsonObject(jsonConfigs)))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    new GobiCustomMappingsService(restClient)
    .getCustomMappingByOrderType(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH.value())
    .onComplete(vtc.succeeding(orderMappingsView -> {
      assertEquals(OrderMappingsView.MappingType.CUSTOM, orderMappingsView.getMappingType());
      assertFalse(orderMappingsView.getOrderMappings().getMappings().isEmpty());
      vtc.completeNow();
    }));
  }

  @Test
  void getDefaultMappingByOrderType(VertxTestContext vtc) {

    doReturn(succeededFuture(JsonObject.mapFrom(new Configs())))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    new GobiCustomMappingsService(restClient)
    .getCustomMappingByOrderType(OrderMappings.OrderType.LISTED_PRINT_SERIAL.value())
    .onComplete(vtc.succeeding(orderMappingsView -> {
      assertEquals(OrderMappingsView.MappingType.DEFAULT, orderMappingsView.getMappingType());
      assertFalse(orderMappingsView.getOrderMappings().getMappings().isEmpty());
      vtc.completeNow();
    }));
  }

  @Test
  void postCustomMapping(VertxTestContext vtc) throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");
    var config =  new JsonObject(jsonConfigs).mapTo(Configs.class).getConfigs().get(0);
    var orderMapping = Json.decodeValue(config.getValue(), OrderMappings.class);

    doReturn(succeededFuture(JsonObject.mapFrom(config)))
      .when(restClient).post(anyString(), any());

    new GobiCustomMappingsService(restClient)
    .postCustomMapping(orderMapping)
    .onComplete(vtc.succeeding(orderMappingsView -> {
      assertEquals(OrderMappingsView.MappingType.CUSTOM, orderMappingsView.getMappingType());
      assertFalse(orderMappingsView.getOrderMappings().getMappings().isEmpty());
      vtc.completeNow();
    }));
  }

  @Test
  void putCustomMapping(VertxTestContext vtc) throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");
    var config =  new JsonObject(jsonConfigs).mapTo(Configs.class).getConfigs().get(0);
    var orderMapping = Json.decodeValue(config.getValue(), OrderMappings.class);

    doReturn(succeededFuture(new JsonObject(jsonConfigs)))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    doReturn(succeededFuture(null))
      .when(restClient).handlePutRequest(eq(CONFIGURATION_ENDPOINT + "/" + config.getId()), any());

    new GobiCustomMappingsService(restClient)
    .putCustomMapping(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH.value(), orderMapping)
    .onComplete(vtc.succeeding(result -> {
      assertNull(result);
      vtc.completeNow();
    }));
  }

  @Test
  void deleteCustomMapping(VertxTestContext vtc) throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");
    var config =  new JsonObject(jsonConfigs).mapTo(Configs.class).getConfigs().get(0);

    doReturn(succeededFuture(new JsonObject(jsonConfigs)))
      .when(restClient).handleGetRequest(anyString(), anyString(), anyInt(), anyInt());

    doReturn(succeededFuture(null))
      .when(restClient).delete(CONFIGURATION_ENDPOINT + "/" + config.getId());

    new GobiCustomMappingsService(restClient)
    .deleteCustomMapping(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH.value())
    .onComplete(vtc.succeeding(result -> {
      assertNull(result);
      vtc.completeNow();
    }));
  }
}

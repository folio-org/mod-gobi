package org.folio.rest.impl;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.jaxrs.model.OrderMappings.OrderType.LISTED_PRINT_MONOGRAPH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.folio.rest.jaxrs.model.DataSource;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappings.OrderType;
import org.folio.rest.jaxrs.model.OrderMappingsView;
import org.folio.rest.jaxrs.model.OrderMappingsView.MappingType;
import org.folio.rest.jaxrs.model.OrderMappingsViewCollection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Log4j2
class GobiOrdersCustomMappingsImplTest extends BaseIntegrationTest {

  private static final String MAPPINGS_PATH = "/gobi/orders/custom-mappings";
  protected RequestSpecification spec;

  @BeforeEach
  void setUpForEach() {
    spec = new RequestSpecBuilder().setContentType(ContentType.JSON)
      .addHeader(OKAPI_HEADER_TENANT, TENANT_ID)
      .addHeader("X-Okapi-User-Id", UUID.randomUUID().toString())
      .addHeader("Accept", "text/plain, application/json")
      .setBaseUri("http://localhost")
      .setPort(okapiPort)
      .build();
  }

  @AfterEach
  void cleanup() {
    OrderMappingsViewCollection collection = RestAssured.with()
      .spec(spec)
      .get(MAPPINGS_PATH)
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .extract().as(OrderMappingsViewCollection.class);

    collection.getOrderMappingsViews().stream()
      .filter(view -> MappingType.CUSTOM == view.getMappingType())
      .forEach(view -> {
        OrderType orderType = view.getOrderMappings().getOrderType();
        log.info("Cleaning up mapping for order type: {}", orderType);
        RestAssured.with()
          .spec(spec)
          .delete(MAPPINGS_PATH + "/" + orderType.value())
          .then()
          .statusCode(200);
    });
  }

  @Test
  void testGetGobiOrdersCustomMappings() {
    // Action
    OrderMappingsViewCollection collection = RestAssured.with()
      .spec(spec)
      .get(MAPPINGS_PATH)
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .extract().as(OrderMappingsViewCollection.class);

    // Assert
    assertNotNull(collection);
    assertNotNull(collection.getOrderMappingsViews());
    assertFalse(collection.getOrderMappingsViews().isEmpty());
    // All mappings should be of type DEFAULT initially
    collection.getOrderMappingsViews().forEach(view -> assertEquals(OrderMappingsView.MappingType.DEFAULT, view.getMappingType()));
  }

  @Test
  void testPostAndGetCustomMapping() {
    // Arrange
    OrderMappings newMapping = createSampleOrderMapping(OrderType.LISTED_ELECTRONIC_MONOGRAPH);

    // Action: POST the custom mapping
    OrderMappingsView createdView = RestAssured.with()
      .spec(spec)
      .body(JsonObject.mapFrom(newMapping).encode())
      .post(MAPPINGS_PATH)
      .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .extract().as(OrderMappingsView.class);

    // Assert: Check the created mapping
    assertNotNull(createdView);
    assertNotNull(createdView.getOrderMappings().getId());
    assertEquals(OrderMappingsView.MappingType.CUSTOM, createdView.getMappingType());

    // Action: GET the mapping by type and verify it's custom
    OrderMappingsView retrievedView = RestAssured.with()
      .spec(spec)
      .get(MAPPINGS_PATH + "/" + newMapping.getOrderType().value())
      .then()
      .statusCode(200)
      .extract().as(OrderMappingsView.class);

    // Assert: Check the retrieved mapping
    assertEquals(createdView.getOrderMappings().getId(), retrievedView.getOrderMappings().getId());
    assertEquals(OrderMappingsView.MappingType.CUSTOM, retrievedView.getMappingType());
  }

  @Test
  void testDeleteGobiOrdersCustomMappingsByOrderType() {
    // Arrange
    OrderMappings newMapping = createSampleOrderMapping(LISTED_PRINT_MONOGRAPH);

    RestAssured.with()
      .spec(spec)
      .body(JsonObject.mapFrom(newMapping).encode())
      .post(MAPPINGS_PATH)
      .then()
      .statusCode(201);

    // Action: Delete the mapping
    RestAssured.with()
      .spec(spec)
      .delete(MAPPINGS_PATH + "/" + LISTED_PRINT_MONOGRAPH.value())
      .then()
      .statusCode(200);

    // Assert: Verify it's gone (should return default mapping)
    OrderMappingsView view = RestAssured.with()
      .spec(spec)
      .get(MAPPINGS_PATH + "/" + LISTED_PRINT_MONOGRAPH.value())
      .then()
      .statusCode(200)
      .extract().as(OrderMappingsView.class);

    assertEquals(OrderMappingsView.MappingType.DEFAULT, view.getMappingType());
  }

  @Test
  void testPutGobiOrdersCustomMappingsByOrderType() {
    // Arrange
    OrderMappings newMapping = createSampleOrderMapping(LISTED_PRINT_MONOGRAPH);

    OrderMappingsView createdView = RestAssured.with()
      .spec(spec)
      .body(JsonObject.mapFrom(newMapping).encode())
      .post(MAPPINGS_PATH)
      .then()
      .statusCode(201)
      .extract().as(OrderMappingsView.class);

    // Action: Update the mapping
    OrderMappings updatedMapping = createdView.getOrderMappings();
    updatedMapping.getMappings().getFirst().setField(Mapping.Field.CONTRIBUTOR);

    RestAssured.with()
      .spec(spec)
      .body(JsonObject.mapFrom(updatedMapping).encode())
      .put(MAPPINGS_PATH + "/" + LISTED_PRINT_MONOGRAPH.value())
      .then()
      .statusCode(204);

    // Assert: Verify the update
    OrderMappingsView retrievedView = RestAssured.with()
      .spec(spec)
      .get(MAPPINGS_PATH + "/" + LISTED_PRINT_MONOGRAPH.value())
      .then()
      .statusCode(200)
      .extract().as(OrderMappingsView.class);

    assertEquals(Mapping.Field.CONTRIBUTOR, retrievedView.getOrderMappings().getMappings().getFirst().getField());
  }

  @Test
  void testGetGobiOrdersCustomMappingsByOrderType_Default() {
    // Action & Assert: Get a mapping that has no custom version
    OrderMappingsView view = RestAssured.with()
      .spec(spec)
      .get(MAPPINGS_PATH + "/" + LISTED_PRINT_MONOGRAPH.value())
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .extract().as(OrderMappingsView.class);

    assertEquals(OrderMappingsView.MappingType.DEFAULT, view.getMappingType());
    assertNotNull(view.getOrderMappings());
  }

  private OrderMappings createSampleOrderMapping(OrderType orderType) {
    OrderMappings orderMappings = new OrderMappings();
    orderMappings.setOrderType(orderType);

    List<Mapping> mappings = new ArrayList<>();
    Mapping mapping = new Mapping();
    mapping.setField(Mapping.Field.VENDOR);
    DataSource dataSource = new DataSource();
    dataSource.setFrom("//vendor");
    mapping.setDataSource(dataSource);
    mappings.add(mapping);

    orderMappings.setMappings(mappings);
    return orderMappings;
  }
}

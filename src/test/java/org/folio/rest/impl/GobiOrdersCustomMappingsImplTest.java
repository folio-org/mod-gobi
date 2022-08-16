package org.folio.rest.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.jaxrs.model.OrderMappings.OrderType.LISTED_PRINT_MONOGRAPH;
import static org.folio.rest.tools.utils.NetworkUtils.nextFreePort;
import static org.folio.rest.utils.TestUtils.getMockData;

import java.io.IOException;
import java.util.UUID;

import org.folio.rest.ResourcePaths;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Configs;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RunWith(VertxUnitRunner.class)
public class GobiOrdersCustomMappingsImplTest {
  private static final int OKAPI_PORT = nextFreePort();
  private static Vertx vertx;
  protected static RequestSpecification spec;

  String jsonConfigs = getMockData("ConfigData/success.json");
  String orderMappingString = new JsonObject(jsonConfigs).mapTo(Configs.class)
    .getConfigs()
    .get(0)
    .getValue();
  OrderMappings orderMappingJson = Json.decodeValue(orderMappingString, OrderMappings.class);
  @Rule
  public WireMockRule wireMockServer = new WireMockRule(WireMockConfiguration.wireMockConfig()
    .dynamicPort()
    .notifier(new ConsoleNotifier(true)));

  public GobiOrdersCustomMappingsImplTest() throws IOException {
  }

  @BeforeClass
  public static void setUpOnce(TestContext context) {
    vertx = Vertx.vertx();

    final JsonObject conf = new JsonObject();
    conf.put("http.port", OKAPI_PORT);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    Async async = context.async();
    vertx.deployVerticle(RestVerticle.class.getName(), opt, h -> async.complete());
    async.await();

    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    log.info("GOBI Integration Service Test Setup Done using port {}", OKAPI_PORT);
  }

  @Before
  public void setUpForEach() {
    spec = new RequestSpecBuilder().setContentType(ContentType.JSON)
      .addHeader("x-okapi-url", "http://localhost:" + wireMockServer.port())
      .addHeader(OKAPI_HEADER_TENANT, "GobiOrdersCustomMappingsImplTest")
      .addHeader(RestVerticle.OKAPI_USERID_HEADER, UUID.randomUUID().toString())
      .addHeader("Accept", "text/plain, application/json")
      .setBaseUri("http://localhost:" + OKAPI_PORT)
      .build();
  }

  @AfterClass
  public static void tearDownOnce(TestContext context) {
    log.info("GOBI Integration Service Testing Complete");
    Async async = context.async();

    vertx.close(v -> async.complete());
    async.await();
  }

  @Test
  public void testGetGobiOrdersCustomMappings() {
    WireMock.stubFor(WireMock.get(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.ok()
        .withBody(jsonConfigs)));

    RestAssured.with()
      .spec(spec)
      .get("/gobi/orders/custom-mappings")
      .then()
      .statusCode(200)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());
  }

  @Test
  public void testPostGobiOrdersCustomMappings() {
    var config = new JsonObject(jsonConfigs).mapTo(Configs.class)
      .getConfigs()
      .get(0);
    var configAsString = JsonObject.mapFrom(config)
      .encodePrettily();
    var ordMappingsAsString = JsonObject.mapFrom(orderMappingJson)
      .encodePrettily();

    WireMock.stubFor(WireMock.post(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT))
      .willReturn(WireMock.ok().withBody(configAsString)));

    RestAssured.with()
      .spec(spec)
      .body(ordMappingsAsString)
      .post("/gobi/orders/custom-mappings")
      .then()
      .statusCode(201)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());
  }

  @Test
  public void testDeleteGobiOrdersCustomMappingsByOrderType() {
    WireMock.stubFor(WireMock.get(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.okJson(jsonConfigs)
        .withBody(jsonConfigs)));

    WireMock.stubFor(WireMock.delete(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.noContent()));

    RestAssured.with()
      .spec(spec)
      .delete("/gobi/orders/custom-mappings/" + LISTED_PRINT_MONOGRAPH)
      .then()
      .statusCode(200)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());
  }

  @Test
  public void testDeleteGobiOrdersCustomMappingsByOrderTypeBadRequest() {
    WireMock.stubFor(WireMock.get(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.okJson(jsonConfigs)
        .withBody(jsonConfigs)));

    WireMock.stubFor(WireMock.delete(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.badRequest()));

    RestAssured.with()
      .spec(spec)
      .delete("/gobi/orders/custom-mappings/" + LISTED_PRINT_MONOGRAPH)
      .then()
      .statusCode(400)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());
  }

  @Test
  public void testPutGobiOrdersCustomMappingsByOrderType() {
    WireMock.stubFor(WireMock.get(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.ok()
        .withBody(jsonConfigs)));

    WireMock.stubFor(WireMock.put(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.noContent()));

    RestAssured.with()
      .spec(spec)
      .body(JsonObject.mapFrom(orderMappingJson)
        .encodePrettily())
      .put("/gobi/orders/custom-mappings/" + LISTED_PRINT_MONOGRAPH)
      .then()
      .statusCode(204);
  }

  @Test
  public void testGetGobiOrdersCustomMappingsByOrderType() {
    WireMock.stubFor(WireMock.get(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.ok()
        .withBody(jsonConfigs)));

    RestAssured.with()
      .spec(spec)
      .get("/gobi/orders/custom-mappings/" + LISTED_PRINT_MONOGRAPH)
      .then()
      .statusCode(200)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());

  }

  @Test
  public void testGetGobiOrdersCustomMappingsByOrderTypeEmptyConfig() {
    WireMock.stubFor(WireMock.get(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + ".*"))
      .willReturn(WireMock.ok()
        .withBody("{\"configs\": []}")));

    RestAssured.with()
      .spec(spec)
      .get("/gobi/orders/custom-mappings/" + LISTED_PRINT_MONOGRAPH)
      .then()
      .statusCode(200)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());

  }

  @Test
  public void testGetGobiOrdersCustomMappingsByOrderTypeServerError() {
    wireMockServer.stop();

    RestAssured.with()
      .spec(spec)
      .get("/gobi/orders/custom-mappings/" + LISTED_PRINT_MONOGRAPH)
      .then()
      .statusCode(500)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());
  }

}

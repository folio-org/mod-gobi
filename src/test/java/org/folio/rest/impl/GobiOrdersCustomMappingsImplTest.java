package org.folio.rest.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.jaxrs.model.OrderMappings.OrderType.LISTED_PRINT_MONOGRAPH;
import static org.folio.rest.tools.utils.NetworkUtils.nextFreePort;
import static org.folio.rest.utils.TestUtils.getMockData;

import java.io.IOException;

import org.folio.rest.ResourcePaths;
import org.folio.rest.RestVerticle;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
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
  public static final Header TENANT_HEADER = new Header(OKAPI_HEADER_TENANT, "GobiOrdersCustomMappingsImplTest");
  public static final Header URL_HEADER = new Header("X-Okapi-Url", "http://localhost:" + OKAPI_PORT);

  public static final Header TOKEN_HEADER = new Header("X-Okapi-Token",
      "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg");
  private static Vertx vertx;

  @BeforeClass
  public static void setUpOnce(TestContext context) throws Throwable {
    vertx = Vertx.vertx();

    final JsonObject conf = new JsonObject();
    conf.put("http.port", OKAPI_PORT);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    Async async = context.async();
    vertx.deployVerticle(RestVerticle.class.getName(), opt, h -> async.complete());
    async.await();

    RestAssured.port = OKAPI_PORT;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    log.info("GOBI Integration Service Test Setup Done using port {}", OKAPI_PORT);
  }

  @AfterClass
  public static void tearDownOnce(TestContext context) throws Throwable {
    log.info("GOBI Integration Service Testing Complete");
    Async async = context.async();

    vertx.close(v -> {
      async.complete();
    });
    async.await();
  }

  @Rule
  public WireMockRule wireMockServer = new WireMockRule(WireMockConfiguration.wireMockConfig()
    .dynamicPort()
    .notifier(new Slf4jNotifier(true)));

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testGetGobiOrdersCustomMappings() {

  }

  public void testPostGobiOrdersCustomMappings() {
  }

  public void testDeleteGobiOrdersCustomMappingsByOrderType() {
  }

  public void testPutGobiOrdersCustomMappingsByOrderType() {
  }

  @Test
  public void testGetGobiOrdersCustomMappingsByOrderType(TestContext testContext) throws IOException {
    var jsonConfigs = getMockData("ConfigData/success.json");
    WireMock.get(urlMatching(ResourcePaths.CONFIGURATION_ENDPOINT + "/.*"))
      .withPort(OKAPI_PORT)
      .willReturn(WireMock.ok()
        .withBody(Json.encode(jsonConfigs)));
    /*
     * new GobiOrdersCustomMappingsImpl().getGobiOrdersCustomMappingsByOrderType("", "", Collections.unmodifiableMap(okapiHeaders),
     * h -> { async.complete(); System.out.println("asd"); }, Vertx.vertx() .getOrCreateContext()); async.await();
     */
    new Header("X-Okapi-Url", "http://localhost:" + OKAPI_PORT);
    Headers headers = prepareHeaders(TENANT_HEADER, URL_HEADER);

    RestAssured.with()
      .headers(headers)
      .get("/gobi/orders/custom-mappings/" + LISTED_PRINT_MONOGRAPH)
      .then()
      .statusCode(404)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());

    /*
     * RestAssured.given() .header(TENANT_HEADER) .header(TOKEN_HEADER) .header(URL_HEADER) .when()
     * .get("/gobi/orders/custom-mappings") .then() .statusCode(200) .contentType(APPLICATION_JSON) .body(Matchers.notNullValue());
     */
  }

  @Test
  public void testGetGobiOrdersCustomMappingsByOrderTypeResourceNotFound() {
    // no mocks configured
    RestAssured.with()
      .header(TENANT_HEADER)
      .header(TOKEN_HEADER)
      .header(URL_HEADER)
      .when()
      .get("/gobi/orders/custom-mappings/" + LISTED_PRINT_MONOGRAPH)
      .then()
      .statusCode(404)
      .contentType(APPLICATION_JSON)
      .body(Matchers.notNullValue());

  }

  public static Headers prepareHeaders(Header... headers) {
    return new Headers(headers);
  }
}

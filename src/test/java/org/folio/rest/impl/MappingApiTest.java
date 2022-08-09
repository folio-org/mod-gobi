package org.folio.rest.impl;

import static org.folio.rest.impl.GOBIIntegrationServiceResourceImplTest.TENANT_HEADER;
import static org.folio.rest.impl.GOBIIntegrationServiceResourceImplTest.TOKEN_HEADER;
import static org.folio.rest.impl.GOBIIntegrationServiceResourceImplTest.URL_HEADER;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.utils.NetworkUtils;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MappingApiTest {

  private static final Logger logger = LogManager.getLogger(MappingApiTest.class);
  private final String ROOT_PATH = "/gobi/orders/mappings";
  private static final int OKAPI_PORT = NetworkUtils.nextFreePort();
  private static final int MOCK_PORT = NetworkUtils.nextFreePort();
  private static Vertx vertx;
  private static GOBIIntegrationServiceResourceImplTest.MockServer mockServer;

  @BeforeClass
  public static void setUpOnce(TestContext context) {
    vertx = Vertx.vertx();

    mockServer = new GOBIIntegrationServiceResourceImplTest.MockServer(MOCK_PORT);
    mockServer.start(context);

    final JsonObject conf = new JsonObject();
    conf.put("http.port", OKAPI_PORT);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, context.asyncAssertSuccess());
    RestAssured.port = OKAPI_PORT;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    logger.info("GOBI Integration Service Test Setup Done using port {}", OKAPI_PORT);
  }

  @AfterClass
  public static void tearDownOnce(TestContext context) {
    logger.info("GOBI Integration Service Testing Complete");
    Async async = context.async();
    vertx.close(v -> {
      mockServer.close();
      async.complete();
    });
    async.awaitSuccess();
  }

  @Before
  public void setUp() {
    GOBIIntegrationServiceResourceImplTest.MockServer.serverRqRs.clear();
  }

  @Test
  public final void testGetGobiOrdersMappingsFields(TestContext context) {
    logger.info("Begin: Testing for Get Gobi Orders Mappings Fields 200 - valid call");

    final Async asyncLocal = context.async();
    RestAssured
      .given()
      .header(TENANT_HEADER)
      .header(TOKEN_HEADER)
      .header(URL_HEADER)
      .when()
      .get(ROOT_PATH + "/fields")
      .then()
      .statusCode(200).body(Matchers.notNullValue());

    asyncLocal.complete();

    logger.info("End: Testing for Get Gobi Orders Mappings Fields 200 - valid call");
  }

  @Test
  public final void testGetGobiOrdersMappingsTranslators(TestContext context) {
    logger.info("Begin: Testing for Get Gobi Orders Mappings Translators 200 - valid call");

    final Async asyncLocal = context.async();
    RestAssured
      .given()
      .header(TENANT_HEADER)
      .header(TOKEN_HEADER)
      .header(URL_HEADER)
      .when()
      .get(ROOT_PATH + "/translators")
      .then()
      .statusCode(200).body(Matchers.notNullValue());

    asyncLocal.complete();

    logger.info("End: Testing for Get Gobi Orders Mappings Translators 200 - valid call");
  }

  @Test
  public final void testGetGobiOrdersMappingsTypes(TestContext context) {
    logger.info("Begin: Testing for Get Gobi Orders Mappings Types 200 - valid call");

    final Async asyncLocal = context.async();
    RestAssured
      .given()
      .header(TENANT_HEADER)
      .header(TOKEN_HEADER)
      .header(URL_HEADER)
      .when()
      .get(ROOT_PATH + "/types")
      .then()
      .statusCode(200).body(Matchers.notNullValue());

    asyncLocal.complete();

    logger.info("End: Testing for Get Gobi Orders Mappings Types 200 - valid call");
  }
}

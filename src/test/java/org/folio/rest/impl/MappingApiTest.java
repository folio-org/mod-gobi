package org.folio.rest.impl;

import static org.folio.rest.impl.GOBIIntegrationServiceResourceImplTest.TENANT_HEADER;
import static org.folio.rest.impl.GOBIIntegrationServiceResourceImplTest.TOKEN_HEADER;
import static org.folio.rest.impl.GOBIIntegrationServiceResourceImplTest.URL_HEADER;
import static org.folio.rest.utils.TestUtils.checkVertxContextCompletion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.utils.NetworkUtils;
import org.folio.rest.utils.TestUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class MappingApiTest {

  private static final Logger logger = LogManager.getLogger(MappingApiTest.class);
  private final String ROOT_PATH = "/gobi/orders/mappings";
  private static final int OKAPI_PORT = NetworkUtils.nextFreePort();
  private static Vertx vertx;

  @BeforeAll
  public static void setUpOnce(VertxTestContext context) throws Throwable {
    vertx = Vertx.vertx();

    final JsonObject conf = new JsonObject();
    conf.put("http.port", OKAPI_PORT);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, (f)->{
      context.completeNow();
    });
    checkVertxContextCompletion(context);
    RestAssured.port = OKAPI_PORT;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    logger.info("GOBI Integration Service Test Setup Done using port {}", OKAPI_PORT);
  }

  @AfterAll
  public static void tearDownOnce(VertxTestContext context) throws Throwable {
    logger.info("GOBI Integration Service Testing Complete");
    vertx.close(v -> {
      context.completeNow();
    });
    TestUtils.checkVertxContextCompletion(context);
  }

  @BeforeEach
  public void setUp() {
    GOBIIntegrationServiceResourceImplTest.MockServer.serverRqRs.clear();
  }

  @Test
  void testGetGobiOrdersMappingsFields() {
    logger.info("Begin: Testing for Get Gobi Orders Mappings Fields 200 - valid call");

    RestAssured
      .given()
      .header(TENANT_HEADER)
      .header(TOKEN_HEADER)
      .header(URL_HEADER)
      .when()
      .get(ROOT_PATH + "/fields")
      .then()
      .statusCode(200).body(Matchers.notNullValue());


    logger.info("End: Testing for Get Gobi Orders Mappings Fields 200 - valid call");
  }

  @Test
  final void testGetGobiOrdersMappingsTranslators() {
    logger.info("Begin: Testing for Get Gobi Orders Mappings Translators 200 - valid call");

    RestAssured
      .given()
      .header(TENANT_HEADER)
      .header(TOKEN_HEADER)
      .header(URL_HEADER)
      .when()
      .get(ROOT_PATH + "/translators")
      .then()
      .statusCode(200).body(Matchers.notNullValue());


    logger.info("End: Testing for Get Gobi Orders Mappings Translators 200 - valid call");
  }

  @Test
  public final void testGetGobiOrdersMappingsTypes() {
    logger.info("Begin: Testing for Get Gobi Orders Mappings Types 200 - valid call");

    RestAssured
      .given()
      .header(TENANT_HEADER)
      .header(TOKEN_HEADER)
      .header(URL_HEADER)
      .when()
      .get(ROOT_PATH + "/types")
      .then()
      .statusCode(200).body(Matchers.notNullValue());

    logger.info("End: Testing for Get Gobi Orders Mappings Types 200 - valid call");
  }
}

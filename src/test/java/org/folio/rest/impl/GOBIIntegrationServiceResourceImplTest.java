package org.folio.rest.impl;

import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Order;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.utils.NetworkUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class GOBIIntegrationServiceResourceImplTest {
  private final Logger logger = LoggerFactory.getLogger(GOBIIntegrationServiceResourceImplTest.class);
  private final int okapiPort = NetworkUtils.nextFreePort();
//  private final int serverPort = NetworkUtils.nextFreePort();
  private final Header tenantHeader = new Header("X-Okapi-Tenant", "gobiintegrationserviceresourceimpltest");
//  private final Header urlHeader = new Header("X-Okapi-Url", "http://localhost:" + serverPort);
  private final Header contentTypeHeaderJSON = new Header("Content-Type", "application/json");
  private final Header contentTypeHeaderXML = new Header("Content-Type", "application/xml");

  // API paths
  private final String rootPath = "/gobi";
  private final String validatePath = rootPath + "/validate";
  private final String ordersPath = rootPath + "/orders";

  // Mock data paths
  private final String mockDataRootPath = "GOBIIntegrationServiceResourceImpl";
  private final String poListedElectronicMonographPath = mockDataRootPath + "/po_listed_electronic_monograph.xml";
  private final String poListedElectronicSerialPath = mockDataRootPath + "/po_listed_electronic_serial.xml";
  private final String poListedPrintMonographPath = mockDataRootPath + "/po_listed_print_monograph.xml";
  private final String poListedPrintSerialPath = mockDataRootPath + "/po_listed_print_serial.xml";
  private final String poUnlistedPrintMonographPath = mockDataRootPath + "/po_unlisted_print_monograph.xml";
  private final String poUnlistedPrintSerialPath = mockDataRootPath + "/po_unlisted_print_serial.xml";
  private final String poListedElectronicMonographBadDataPath = mockDataRootPath + "/po_listed_electronic_monograph_bad_data.xml";

  private Vertx vertx;
  private String moduleName;
  private String moduleVersion;
  private String moduleId;

  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = Vertx.vertx();

    moduleName = PomReader.INSTANCE.getModuleName().replaceAll("_", "-");
    moduleVersion = PomReader.INSTANCE.getVersion();
    moduleId = moduleName + "-" + moduleVersion;
    logger.info("Test setup starting for " + moduleId);

    final JsonObject conf = new JsonObject();
    conf.put("http.port", okapiPort);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, context.asyncAssertSuccess());
    RestAssured.port = okapiPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    logger.info("GOBI Integration Service Test Setup Done using port " + okapiPort);
  }

  @After
  public void tearDown(TestContext context) {
    logger.info("GOBI Integration Service Testing Complete");
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public final void testGetGobiValidate(TestContext context) {
    logger.info("Begin: Testing for 204 - valid call");

    final Async asyncLocal = context.async();

    RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderJSON)
      .when()
        .get(validatePath)
      .then()
        .statusCode(204)
        .content(Matchers.equalTo(""));

    asyncLocal.complete();

    logger.info("End: Testing for 204 - valid call");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic monograph");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedElectronicMonographPath));

    final Order order = RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .extract()
          .body()
            .as(Order.class);

    boolean success;
    try {
      UUID.fromString(order.getId());
      success = true;
    } catch (IllegalArgumentException e) {
      success = false;
    }

    context.assertTrue(success);

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed electronic monograph");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic serial");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedElectronicSerialPath));

    final Order order = RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .extract()
          .body()
            .as(Order.class);

    boolean success;
    try {
      UUID.fromString(order.getId());
      success = true;
    } catch (IllegalArgumentException e) {
      success = false;
    }

    context.assertTrue(success);

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed electronic serial");
  }

  @Test
  public final void testPostGobiOrdersPOListedPrintMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonographPath));

    final Order order = RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .extract()
          .body()
            .as(Order.class);

    boolean success;
    try {
      UUID.fromString(order.getId());
      success = true;
    } catch (IllegalArgumentException e) {
      success = false;
    }

    context.assertTrue(success);

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  public final void testPostGobiOrdersPOListedPrintSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print serial");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintSerialPath));

    final Order order = RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .extract()
          .body()
            .as(Order.class);

    boolean success;
    try {
      UUID.fromString(order.getId());
      success = true;
    } catch (IllegalArgumentException e) {
      success = false;
    }

    context.assertTrue(success);

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print serial");
  }

  @Test
  public final void testPostGobiOrdersPOUnlistedPrintMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print monograph");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poUnlistedPrintMonographPath));

    final Order order = RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .extract()
          .body()
            .as(Order.class);

    boolean success;
    try {
      UUID.fromString(order.getId());
      success = true;
    } catch (IllegalArgumentException e) {
      success = false;
    }

    context.assertTrue(success);

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order unlisted print monograph");
  }

  @Test
  public final void testPostGobiOrdersPOUnlistedPrintSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print serial");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poUnlistedPrintSerialPath));

    final Order order = RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .extract()
          .body()
            .as(Order.class);

    boolean success;
    try {
      UUID.fromString(order.getId());
      success = true;
    } catch (IllegalArgumentException e) {
      success = false;
    }

    context.assertTrue(success);

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order unlisted print serial");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicMonographBadData(TestContext context) throws Exception {
    logger.info("Begin: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedElectronicMonographBadDataPath));

    final String error = RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(400)
        .contentType(ContentType.TEXT)
        .extract()
          .body()
            .asString();

    context.assertNotNull(error);

    asyncLocal.complete();

    logger.info("End: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");
  }
}

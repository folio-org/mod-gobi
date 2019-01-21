package org.folio.rest.impl;

import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_TOKEN;
import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_XML;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.folio.rest.RestVerticle;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.utils.NetworkUtils;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.mapper.ObjectMapperType;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

@RunWith(VertxUnitRunner.class)
public class GOBIIntegrationServiceResourceImplTest {

  private static final Logger logger = LoggerFactory.getLogger(GOBIIntegrationServiceResourceImplTest.class);

  private static final String APPLICATION_JSON = "application/json";

  private static final int okapiPort = NetworkUtils.nextFreePort();
  private static final int mockPort = NetworkUtils.nextFreePort();

  // private final int serverPort = NetworkUtils.nextFreePort();
  private final Header tenantHeader = new Header("X-Okapi-Tenant", "gobiintegrationserviceresourceimpltest");
  private final Header tokenHeader = new Header("X-Okapi-Token",
      "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg");
  private final Header urlHeader = new Header("X-Okapi-Url", "http://localhost:" + mockPort);
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

  private static Vertx vertx;
  private static MockServer mockServer;

  @BeforeClass
  public static void setUpOnce(TestContext context) throws Exception {
    vertx = Vertx.vertx();

    mockServer = new MockServer(mockPort);
    mockServer.start(context);

    String moduleName = PomReader.INSTANCE.getModuleName().replaceAll("_", "-");
    String moduleVersion = PomReader.INSTANCE.getVersion();
    String moduleId = moduleName + "-" + moduleVersion;
    logger.info("Test setup starting for " + moduleId);

    final JsonObject conf = new JsonObject();
    conf.put("http.port", okapiPort);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, context.asyncAssertSuccess());
    RestAssured.port = okapiPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    logger.info("GOBI Integration Service Test Setup Done using port " + okapiPort);
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

  @Test
  public final void testGetGobiValidate(TestContext context) {
    logger.info("Begin: Testing for Get Gobi Validate 200 - valid call");

    final Async asyncLocal = context.async();

    RestAssured
      .given()
        .header(tenantHeader)
        .header(tokenHeader)
        .header(urlHeader)
      .when()
        .get(validatePath)
      .then()
        .statusCode(200).content(Matchers.equalTo("<test>GET - OK</test>"));

    asyncLocal.complete();

    logger.info("End: Testing for Get Gobi Validate 200 - valid call");
  }

  @Test
  public final void testPostGobiValidate(TestContext context) {
    logger.info("Begin: Testing for Post Gobi Validate 200 - valid call");

    final Async asyncLocal = context.async();

    RestAssured
      .given()
        .header(tenantHeader)
        .header(tokenHeader)
        .header(urlHeader)
      .when()
        .post(validatePath)
      .then()
        .statusCode(200).content(Matchers.equalTo("<test>POST - OK</test>"));

    asyncLocal.complete();

    logger.info("End: Testing for Post Gobi Validate 200 - valid call");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic monograph");

    final Async asyncLocal = context.async();

    final String body = getMockData(poListedElectronicMonographPath);

    final GobiResponse order = RestAssured
      .given()
        .header(tokenHeader)
        .header(urlHeader)
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed electronic monograph");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic serial");

    final Async asyncLocal = context.async();

    final String body = getMockData(poListedElectronicSerialPath);

    final GobiResponse order = RestAssured
      .given()
        .header(tokenHeader)
        .header(urlHeader)
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed electronic serial");
  }

  @Test
  public final void testPostGobiOrdersPOListedPrintMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph");

    final Async asyncLocal = context.async();

    final String body = getMockData(poListedPrintMonographPath);

    final GobiResponse order = RestAssured
      .given()
        .header(tokenHeader)
        .header(urlHeader)
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  public final void testPostGobiOrdersPOListedPrintSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print serial");

    final Async asyncLocal = context.async();

    final String body = getMockData(poListedPrintSerialPath);

    final GobiResponse order = RestAssured
      .given()
        .header(tokenHeader)
        .header(urlHeader)
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print serial");
  }

  @Test
  public final void testPostGobiOrdersPOUnlistedPrintMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print monograph");

    final Async asyncLocal = context.async();

    final String body = getMockData(poUnlistedPrintMonographPath);

    final GobiResponse order = RestAssured
      .given()
        .header(tokenHeader)
        .header(urlHeader)
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order unlisted print monograph");
  }

  @Test
  public final void testPostGobiOrdersPOUnlistedPrintSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print serial");

    final Async asyncLocal = context.async();

    final String body = getMockData(poUnlistedPrintSerialPath);

    final GobiResponse order = RestAssured
      .given()
        .header(tokenHeader)
        .header(tenantHeader)
        .header(urlHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order unlisted print serial");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicMonographBadData(TestContext context) throws Exception {
    logger.info("Begin: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");

    final Async asyncLocal = context.async();

    final String body = getMockData(poListedElectronicMonographBadDataPath);

    final GobiResponse error = RestAssured
      .given()
        .header(tenantHeader)
        .header(tokenHeader)
        .header(urlHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(400)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(error);
    context.assertNotNull(error.getError());
    context.assertEquals(CODE_INVALID_XML, error.getError().getCode());
    context.assertNotNull(error.getError().getMessage());

    asyncLocal.complete();

    logger.info("End: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");
  }

  @Test
  public final void testPostContentWithValidOkapiToken(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph with valid Okapi token");

    final Async asyncLocal = context.async();

    final String body = getMockData(poListedPrintMonographPath);

    final GobiResponse order = RestAssured
      .given()
        .header(tokenHeader)
        .header(urlHeader)
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  public final void testPostContentWithInvalidOkapiToken(TestContext context) throws Exception {
    logger.info("Begin: Testing for 400 - posted order listed print monograph with invalid okapi token");

    final Async asyncLocal = context.async();

    final String body = getMockData(poListedPrintMonographPath);

    final GobiResponse error = RestAssured
      .given()
        .header(tenantHeader)
        .header(urlHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(400)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(error);
    context.assertNotNull(error.getError());
    context.assertEquals(CODE_INVALID_TOKEN, error.getError().getCode());
    context.assertNotNull(error.getError().getMessage());

    asyncLocal.complete();

    logger.info("End: Testing for 400 - posted order with invalid token");
  }

  public static class MockServer {

    private static final String ORDERS_ENDPOINT = "/orders/composite-orders";
    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);
    private static final Random rand = new Random(System.nanoTime());

    public final int port;
    public final Vertx vertx;

    public MockServer(int port) {
      this.port = port;
      this.vertx = Vertx.vertx();
    }

    public void close() {
      vertx.close(res -> {
        if (res.failed()) {
          logger.error("Failed to shut down mock server", res.cause());
        } else {
          logger.info("Successfully shut down mock server");
        }
      });
    }

    protected Router defineRoutes() {
      Router router = Router.router(vertx);

      router.route().handler(BodyHandler.create());
      router.route(HttpMethod.POST, ORDERS_ENDPOINT).handler(this::handlePostPurchaseOrder);
      router.route(HttpMethod.GET, "/vendor").handler(this::handleGetVendor);
      router.route(HttpMethod.GET, "/material-types").handler(this::handleGetMaterialType);
      router.route(HttpMethod.GET, "/locations").handler(this::handleGetLocation);
      router.route(HttpMethod.GET, "/configurations/entries").handler(this::handleGetConfigurationsEntries);

      return router;
    }

    public void start(TestContext context) {
      logger.info("Starting mock server on port: " + port);

      // Setup Mock Server...
      HttpServer server = vertx.createHttpServer();

      final Async async = context.async();
      server.requestHandler(defineRoutes()::accept).listen(port, result -> {
        if (result.failed()) {
          logger.warn("Failure", result.cause());
        }
        context.assertTrue(result.succeeded());
        async.complete();
      });
    }

    private void handlePostPurchaseOrder(RoutingContext ctx) {
      logger.info("got: " + ctx.getBodyAsString());

      JsonObject compPO = ctx.getBodyAsJson();

      compPO.put("id", UUID.randomUUID().toString());
      String poNumber = "PO_" + randomDigits(10);
      compPO.put("po_number", poNumber);
      compPO.getJsonArray("po_lines").forEach(line -> {
        JsonObject poLine = (JsonObject) line;
        poLine.put("id", UUID.randomUUID().toString());
        poLine.put("po_line_number", poNumber + "-1");
        poLine.put("purchase_order_id", compPO.getString("id"));
        poLine.put("barcode", randomDigits(10));
      });

      ctx.response()
        .setStatusCode(201)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(compPO.encodePrettily());
    }

    private void handleGetVendor(RoutingContext ctx) {

      logger.info("got vendor request: " + ctx.request().query());

      JsonObject vendors = new JsonObject()
        .put("vendors", new JsonArray()
          .add(new JsonObject()
            .put("id", UUID.randomUUID().toString())
            .put("name", "GOBI LIbrary Systems")
            .put("code", "GOBI")))
        .put("total_records", 1);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(vendors.encodePrettily());
    }

    private void handleGetMaterialType(RoutingContext ctx) {

      logger.info("got material-type request: " + ctx.request().query());

      JsonObject mtypes = new JsonObject()
        .put("mtypes", new JsonArray()
          .add(new JsonObject()
            .put("id", UUID.randomUUID().toString())
            .put("name", ctx.queryParam("query").get(0).split("=")[1])))
        .put("total_records", 1);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(mtypes.encodePrettily());
    }

    private void handleGetLocation(RoutingContext ctx) {

      logger.info("got location request: " + ctx.request().query());

      JsonObject locations = new JsonObject()
        .put("locations", new JsonArray()
          .add(new JsonObject()
            .put("id", UUID.randomUUID().toString())
            .put("code", ctx.queryParam("query").get(0).split("=")[1])))
        .put("total_records", 1);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(locations.encodePrettily());
    }

    private void handleGetConfigurationsEntries(RoutingContext ctx) {

      logger.info("got configurations entries request: " + ctx.request().query());

      JsonObject configurationsEntries = new JsonObject()
        .put("configs", new JsonArray())
        .put("total_records", 0);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(configurationsEntries.encodePrettily());
    }

    private String randomDigits(int len) {
      return rand.ints(len, 0, 9).mapToObj(Integer::toString).collect(Collectors.joining());
    }
  }

  private String getMockData(String path) throws IOException {
    return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(path));
  }
}

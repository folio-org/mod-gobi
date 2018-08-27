package org.folio.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.folio.gobi.exceptions.InvalidTokenException;
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
  private static final String TEXT_PLAIN = "text/plain";

  private static final int okapiPort = NetworkUtils.nextFreePort();
  private static final int mockPort = NetworkUtils.nextFreePort();

  // private final int serverPort = NetworkUtils.nextFreePort();
  private final Header tenantHeader = new Header("X-Okapi-Tenant", "gobiintegrationserviceresourceimpltest");
  private final Header tokenHeader = new Header("X-Okapi-Token",
      "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg");
  private final Header urlHeader = new Header("X-Okapi-Url", "http://localhost:" + mockPort);
  
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
  private final String poListedElectronicMonographBadDataPath = mockDataRootPath
      + "/po_listed_electronic_monograph_bad_data.xml";

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
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public final void testGetGobiValidate(TestContext context) {
    logger.info("Begin: Testing for 204 - valid call");

    final Async asyncLocal = context.async();

    RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
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

    final String body = IOUtils
      .toString(this.getClass().getClassLoader().getResourceAsStream(poListedElectronicMonographPath));

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

    final String body = IOUtils
      .toString(this.getClass().getClassLoader().getResourceAsStream(poListedElectronicSerialPath));

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

    final String body = IOUtils
      .toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonographPath));

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

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintSerialPath));

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

    final String body = IOUtils
      .toString(this.getClass().getClassLoader().getResourceAsStream(poUnlistedPrintMonographPath));

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

    final String body = IOUtils
      .toString(this.getClass().getClassLoader().getResourceAsStream(poUnlistedPrintSerialPath));

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

    final String body = IOUtils
      .toString(this.getClass().getClassLoader().getResourceAsStream(poListedElectronicMonographBadDataPath));

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
    context.assertEquals("INVALID_XML", error.getError().getCode());
    context.assertNotNull(error.getError().getMessage());

    asyncLocal.complete();

    logger.info("End: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");
  }

  @Test
  public final void testPostContentWithValidOkapiToken(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph with valid Okapi token");

    final Async asyncLocal = context.async();

    final String body = IOUtils
      .toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonographPath));

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

    final String body = IOUtils
      .toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonographPath));

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
    context.assertEquals("INVALID_TOKEN", error.getError().getCode());
    context.assertNotNull(error.getError().getMessage());

    asyncLocal.complete();

    logger.info("End: Testing for 400 - posted order with invalid token");
  }

  @Test
  public final void testGetUuidWithInvalidOkapiToken(TestContext context) throws InvalidTokenException {
    logger.info(
        "Begin: Testing for InvalidTokenException to be thrown when calling getUuid with NULL or empty okapi token");

    String okapiToken = null;
    String expectedMessage = "x-okapi-tenant is NULL or empty";

    try {
      PostGobiOrdersHelper.getUuid(okapiToken);
      fail("Expected InvalidTokenException to be thrown");
    } catch (InvalidTokenException e) {
      assertEquals(expectedMessage, e.getMessage());
    }

    okapiToken = "";
    try {
      PostGobiOrdersHelper.getUuid(okapiToken);
      fail("Expected InvalidTokenException to be thrown");
    } catch (InvalidTokenException e) {
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public final void testGetUuidWithValidOkapiTokenMissingContentPart(TestContext context) throws InvalidTokenException {
    logger.info("Begin: Testing for InvalidTokenException to be thrown when calling getUuid with invalid okapi token");

    String okapiToken = "eyJhbGciOiJIUzUxMiJ9.";
    String expectedMessage = "user_id is not found in x-okapi-token";

    try {
      PostGobiOrdersHelper.getUuid(okapiToken);
      fail("Expected InvalidTokenException to be thrown");
    } catch (InvalidTokenException e) {
      assertEquals(expectedMessage, e.getMessage());
    }

    okapiToken = "eyJhbGciOiJIUzUxMiJ9";
    try {
      PostGobiOrdersHelper.getUuid(okapiToken);
      fail("Expected InvalidTokenException to be thrown");
    } catch (InvalidTokenException e) {
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public final void testGetUuidWithValidOkapiTokenMissingUuid(TestContext context) throws InvalidTokenException {
    logger
      .info("Begin: Testing for InvalidTokenException to be thrown when calling getUuid with okapi token missing UUID");

    String expectedMessage = "user_id is not found in x-okapi-token";

    // Missing UUID
    String okapiToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRlbmFudCI6ImZzMDAwMDAwMDAifQ.dpljk7LAzgM_a1fD0jAqVUE4HhxKKeXmE2lrTmyf-HOxUyPf2Byj0OIN2fn3eUdQnt1_ABZTTxafceyt7Rj3mg";

    try {
      PostGobiOrdersHelper.getUuid(okapiToken);
      fail("Expected InvalidTokenException to be thrown");
    } catch (InvalidTokenException e) {
      assertEquals(expectedMessage, e.getMessage());
    }

    // empty UUID
    okapiToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.PabbXTw5TqrrOxeKOEac5WkmmAOL4f8UoWKPCqCINvmuZCLLC0197CfVq0CBv2MjSwxU-3nf_TkwhM4mVmHnyA";

    try {
      PostGobiOrdersHelper.getUuid(okapiToken);
      fail("Expected InvalidTokenException to be thrown");
    } catch (InvalidTokenException e) {
      assertEquals(expectedMessage, e.getMessage());
    }

    // empty Payload
    okapiToken = "eyJhbGciOiJIUzUxMiJ9.e30.ToOwht_WTL7ib-z-u0Bg4UmSIZ8qOsTCnX7IhPMbQghCGBzCJMzfu_w9VZPzA9JOk1g2GnH0_ujnhMorxK2LJw";

    try {
      PostGobiOrdersHelper.getUuid(okapiToken);
      fail("Expected InvalidTokenException to be thrown");
    } catch (InvalidTokenException e) {
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public final void testGetUuidWithValidOkapiToken(TestContext context) throws InvalidTokenException {
    logger.info("Begin: Testing for valid UUID from valid OkapiToken");

    String okapiToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg";

    try {
      String uuid = PostGobiOrdersHelper.getUuid(okapiToken);
      assertEquals("ef676db9-d231-479a-aa91-f65eb4b17872", uuid);
    } catch (InvalidTokenException e) {
      fail("InvalidTokenException was not expected to be thrown");
    }
  }

  public static class MockServer {

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
          fail(res.cause().getMessage());
        } else {
          logger.info("Successfully shut down mock server");
        }
      });
    }

    protected Router defineRoutes() {
      Router router = Router.router(vertx);

      router.route().handler(BodyHandler.create());
      router.route(HttpMethod.POST, "/orders").handler(this::handlePostPurchaseOrder);

      return router;
    }

    public void start(TestContext context) {

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
      JsonObject po = compPO.getJsonObject("purchase_order");
      po.put("id", UUID.randomUUID().toString());
      String poNumber = "PO_" + rand.ints(10, 0, 9).mapToObj(Integer::toString).collect(Collectors.joining());
      po.put("po_number", poNumber);
      compPO.getJsonArray("po_lines").forEach(line -> {
        JsonObject poLine = (JsonObject) line;
        poLine.put("id", UUID.randomUUID().toString());
        poLine.put("purchase_order_id", po.getString("id"));
        poLine.put("barcode", rand.ints(10, 0, 9).mapToObj(Integer::toString).collect(Collectors.joining()));
      });

      ctx.response()
        .setStatusCode(201)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(compPO.encodePrettily());
    }
  }

  public static String getMockData(String path) throws IOException {
    StringBuilder sb = new StringBuilder();
    Files.lines(Paths.get(path)).forEach(sb::append);
    return sb.toString();
  }
}

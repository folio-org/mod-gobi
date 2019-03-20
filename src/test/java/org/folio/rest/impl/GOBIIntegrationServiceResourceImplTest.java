package org.folio.rest.impl;

import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_XML;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import org.apache.commons.io.IOUtils;
import org.folio.rest.RestVerticle;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.utils.NetworkUtils;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

@RunWith(VertxUnitRunner.class)
public class GOBIIntegrationServiceResourceImplTest {
  private static final String CONFIGS = "configs";

  private static final String PURCHASEORDER = "PURCHASEORDER";

  private static final String CONFIGURATION = "CONFIGURATION";

  static {
    System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, "io.vertx.core.logging.Log4j2LogDelegateFactory");
  }
  private static final Logger logger = LoggerFactory.getLogger(GOBIIntegrationServiceResourceImplTest.class);

  private static final String APPLICATION_JSON = "application/json";

  private static final int OKAPIPORT = NetworkUtils.nextFreePort();
  private static final int MOCKPORT = NetworkUtils.nextFreePort();

  // private final int serverPort = NetworkUtils.nextFreePort();
  private final Header TENANTHEADER = new Header("X-Okapi-Tenant", "gobiintegrationserviceresourceimpltest");
  private final Header TOKENHEADER = new Header("X-Okapi-Token",
      "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg");
  private final Header URLHEADER = new Header("X-Okapi-Url", "http://localhost:" + MOCKPORT);
  private final Header CONTENTTYPEHEADERXML = new Header("Content-Type", "application/xml");

  // API paths
  private final String ROOTPATH = "/gobi";
  private final String VALIDATEPATH = ROOTPATH + "/validate";
  private final String ORDERSPATH = ROOTPATH + "/orders";

  // Mock data paths
  private final String MOCKDATAROOTPATH = "GOBIIntegrationServiceResourceImpl";
  private final String POLISTEDELECTRONICMONOGRAPHPATH = MOCKDATAROOTPATH + "/po_listed_electronic_monograph.xml";
  private final String POLISTEDELECTRONICSERIALPATH = MOCKDATAROOTPATH + "/po_listed_electronic_serial.xml";
  private final String POLISTEDPRINTMONOGRAPHPATH = MOCKDATAROOTPATH + "/po_listed_print_monograph.xml";
  private final String POLISTEDPRINTSERIALPATH = MOCKDATAROOTPATH + "/po_listed_print_serial.xml";
  private final String POUNLISTEDPRINTMONOGRAPHPATH = MOCKDATAROOTPATH + "/po_unlisted_print_monograph.xml";
  private final String POUNLISTEDPRINTSERIALPATH = MOCKDATAROOTPATH + "/po_unlisted_print_serial.xml";
  private final String POLISTEDELECTRONICMONOGRAPHBADDATAPATH = MOCKDATAROOTPATH + "/po_listed_electronic_monograph_bad_data.xml";
  private static final String CUSTOM_LISTED_ELECTRONIC_SERIAL_MAPPING = "MappingHelper/Custom_ListedElectronicSerial.json";
  private static final  String VENDOR_MOCK_DATA =  "GOBIIntegrationServiceResourceImpl/vendor.json";
  private static final String LOCATION = "LOCATION";
  private static final String MATERIAL_TYPES = "MATERIAL-TYPES";
  private static final String VENDOR = "VENDOR";
  private static final String UNSPECIFIED_MATERIAL_TYPE_ID = "be44c321-ab73-43c4-a114-70f82fa13f17";

  private static Vertx vertx;
  private static MockServer mockServer;

  @BeforeClass
  public static void setUpOnce(TestContext context) throws Exception {
    vertx = Vertx.vertx();

    mockServer = new MockServer(MOCKPORT);
    mockServer.start(context);

    String moduleName = PomReader.INSTANCE.getModuleName().replaceAll("_", "-");
    String moduleVersion = PomReader.INSTANCE.getVersion();
    String moduleId = moduleName + "-" + moduleVersion;
    logger.info("Test setup starting for {}", moduleId);

    final JsonObject conf = new JsonObject();
    conf.put("http.port", OKAPIPORT);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, context.asyncAssertSuccess());
    RestAssured.port = OKAPIPORT;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    logger.info("GOBI Integration Service Test Setup Done using port {}", OKAPIPORT);
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
    MockServer.serverRqRs.clear();
  }

  @Test
  public final void testGetGobiValidate(TestContext context) {
    logger.info("Begin: Testing for Get Gobi Validate 200 - valid call");

    final Async asyncLocal = context.async();

    RestAssured
      .given()
        .header(TENANTHEADER)
        .header(TOKENHEADER)
        .header(URLHEADER)
      .when()
        .get(VALIDATEPATH)
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
        .header(TENANTHEADER)
        .header(TOKENHEADER)
        .header(URLHEADER)
      .when()
        .post(VALIDATEPATH)
      .then()
        .contentType("application/xml")
        .statusCode(200).content(Matchers.equalTo("<test>POST - OK</test>"));

    asyncLocal.complete();

    logger.info("End: Testing for Post Gobi Validate 200 - valid call");
  }

  @Test
  public final void testPostGobiXMLContent(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - XML content");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDELECTRONICMONOGRAPHPATH);

    RestAssured
      .given()
        .header(TOKENHEADER)
        .header(URLHEADER)
        .header(TENANTHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
      .then()
        .statusCode(201)
        .contentType("application/xml");

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertEquals(4, column.size());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - XML content");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic monograph");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDELECTRONICMONOGRAPHPATH);

    final GobiResponse order = RestAssured
      .given()
        .header(TOKENHEADER)
        .header(URLHEADER)
        .header(TENANTHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);
    context.assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertEquals(4, column.size());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed electronic monograph");
  }

  @Test
  public final void testPostGobiOrdersCustomPOListedElectronicSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic serial with custom mappings");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDELECTRONICSERIALPATH);

    final GobiResponse order = RestAssured
      .given()
        .header(TOKENHEADER)
        .header(URLHEADER)
        .header(TENANTHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    List<JsonObject> configEntries = MockServer.serverRqRs.get(CONFIGURATION, HttpMethod.GET);
    assertNotNull(configEntries);

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    // 4 calls one each to configurations,vendors,material types and location
    assertEquals(4, column.size());

    // Make sure the mappings from custom configuration were used
    assertEquals(1, column.get(CONFIGURATION).get(0).getJsonArray(CONFIGS).size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASEORDER, HttpMethod.POST);
    CompositePurchaseOrder ppo = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    assertEquals("Description from Custom Mapping", ppo.getCompositePoLines().get(0).getPoLineDescription());
    assertThat(ppo.getCompositePoLines().get(0).getCost().getPoLineEstimatedPrice(), equalTo(0.0));

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed electronic serial");
  }

  @Test
  public final void testPostGobiOrdersPOListedPrintMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDPRINTMONOGRAPHPATH);

    final GobiResponse order = RestAssured
      .given()
        .header(TOKENHEADER)
        .header(URLHEADER)
        .header(TENANTHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertEquals(4, column.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASEORDER, HttpMethod.POST);
    CompositePurchaseOrder ppo = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    assertThat(ppo.getCompositePoLines().get(0).getCost().getListUnitPriceElectronic(), nullValue());
    assertThat(ppo.getCompositePoLines().get(0).getCost().getListUnitPrice(), equalTo(14.95));

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  public final void testPostGobiOrdersPOListedPrintSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print serial");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDPRINTSERIALPATH);

    final GobiResponse order = RestAssured
      .given()
        .header(TOKENHEADER)
        .header(URLHEADER)
        .header(TENANTHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertEquals(4, column.size());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print serial");
  }

  @Test
  public final void testPostGobiOrdersPOUnlistedPrintMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print monograph");

    final Async asyncLocal = context.async();

    final String body = getMockData(POUNLISTEDPRINTMONOGRAPHPATH);

    final GobiResponse order = RestAssured
      .given()
        .header(TOKENHEADER)
        .header(URLHEADER)
        .header(TENANTHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertEquals(4, column.size());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order unlisted print monograph");
  }

  @Test
  public final void testPostGobiOrdersPOUnlistedPrintSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print serial");

    final Async asyncLocal = context.async();

    final String body = getMockData(POUNLISTEDPRINTSERIALPATH);

    final GobiResponse order = RestAssured
      .given()
        .header(TOKENHEADER)
        .header(TENANTHEADER)
        .header(URLHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertEquals(4, column.size());
    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order unlisted print serial");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicMonographBadData(TestContext context) throws Exception {
    logger.info("Begin: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDELECTRONICMONOGRAPHBADDATAPATH);

    final GobiResponse error = RestAssured
      .given()
        .header(TENANTHEADER)
        .header(TOKENHEADER)
        .header(URLHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
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

    assertTrue(MockServer.serverRqRs.isEmpty());

    asyncLocal.complete();

    logger.info("End: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");
  }

  @Test
  public final void testPostContentWithValidOkapiToken(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph with valid Okapi token");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDPRINTMONOGRAPHPATH);

    final GobiResponse order = RestAssured
      .given()
        .header(TOKENHEADER)
        .header(URLHEADER)
        .header(TENANTHEADER)
        .header(CONTENTTYPEHEADERXML)
        .body(body)
      .when()
        .post(ORDERSPATH)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertEquals(4, column.size());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  public final void testPostGobiOrdersFallBackMaterialTypes(TestContext context) throws Exception {
    logger.info("Begin: Testing for falling back to the unspecified material id, if a non existent code is sent");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDELECTRONICSERIALPATH);

    final GobiResponse order = RestAssured
      .given()
       .header(TOKENHEADER)
       .header(URLHEADER)
       .header(TENANTHEADER)
       .header(CONTENTTYPEHEADERXML)
      .body(body)
      .when()
      .post(ORDERSPATH)
       .then()
         .statusCode(201)
         .contentType(ContentType.XML)
         .extract()
         .body()
         .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    List<JsonObject> configEntries = MockServer.serverRqRs.get(MATERIAL_TYPES, HttpMethod.GET);
    //2 calls must be made to material types end point, once for non existent and other for unspecified
    assertEquals(2, configEntries.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASEORDER, HttpMethod.POST);
    CompositePurchaseOrder po = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    assertEquals(UNSPECIFIED_MATERIAL_TYPE_ID, po.getCompositePoLines().get(0).getDetails().getMaterialTypes().get(0));

    asyncLocal.complete();

    logger.info("End: Testing for falling back to the unspecified material id, if a non existent code is sent");
  }

  @Test
  public final void testPostGobiOrdersFallBackLocation(TestContext context) throws Exception {
    logger.info("Begin: Testing for falling back to the first location id, if a non existent code is sent");

    final Async asyncLocal = context.async();

    final String body = getMockData(POLISTEDELECTRONICSERIALPATH);

    final GobiResponse order = RestAssured
      .given()
       .header(TOKENHEADER)
       .header(URLHEADER)
       .header(TENANTHEADER)
       .header(CONTENTTYPEHEADERXML)
      .body(body)
      .when()
      .post(ORDERSPATH)
       .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
        .body()
        .as(GobiResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    List<JsonObject> configEntries = MockServer.serverRqRs.get(LOCATION, HttpMethod.GET);
    // 2 calls must be made to location end point
    assertEquals(2, configEntries.size());

    asyncLocal.complete();

    logger.info("End: Testing for falling back to tthe first location id, if a non existent code is sent");
  }

  public static class MockServer {

    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String TOTAL_RECORDS = "totalRecords";
    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);
    private static final Random rand = new Random(System.nanoTime());
    static Table<String, HttpMethod, List<JsonObject>> serverRqRs = HashBasedTable.create();

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
      router.route(HttpMethod.POST, PostGobiOrdersHelper.ORDERS_ENDPOINT).handler(this::handlePostPurchaseOrder);
      router.get(PostGobiOrdersHelper.GET_VENDORS_ENDPOINT).handler(this::handleGetVendor);
      router.get(PostGobiOrdersHelper.MATERIAL_TYPES_ENDPOINT).handler(this::handleGetMaterialType);
      router.get(PostGobiOrdersHelper.LOCATIONS_ENDPOINT).handler(this::handleGetLocation);
      router.get(PostGobiOrdersHelper.CONFIGURATION_ENDPOINT).handler(this::handleGetConfigurationsEntries);

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
      logger.info("Handle Post Purchase Order got: {}", ctx.getBodyAsString());

      JsonObject compPO = ctx.getBodyAsJson();

      compPO.put(ID, UUID.randomUUID().toString());
      String poNumber = "PO_" + randomDigits(10);
      compPO.put("poNumber", poNumber);
      compPO.getJsonArray("compositePoLines").forEach(line -> {
        JsonObject poLine = (JsonObject) line;
        poLine.put(ID, UUID.randomUUID().toString());
        poLine.put("poLineNumber", poNumber + "-1");
        poLine.put("purchaseOrderId", compPO.getString(ID));
      });

      addServerRqRsData(HttpMethod.POST, PURCHASEORDER, compPO);

      ctx.response()
        .setStatusCode(201)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(compPO.encodePrettily());
    }

    private void handleGetVendor(RoutingContext ctx) {
      logger.info("got vendor request: {}", ctx.request().query());

      try {
        JsonObject vendors = new JsonObject(getMockData(VENDOR_MOCK_DATA));

        addServerRqRsData(HttpMethod.GET, VENDOR, vendors);

        ctx.response()
          .setStatusCode(200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end(vendors.encodePrettily());
      }catch (IOException e) {
        ctx.response()
        .setStatusCode(404)
        .end();
       }
    }

    private void handleGetMaterialType(RoutingContext ctx) {

      logger.info("got material-type request: {}", ctx.request().query());
      JsonObject mtypes = new JsonObject();

      if (ctx.request().query().contains("HUM")) {
        mtypes.put("mtypes", new JsonArray())
          .put(TOTAL_RECORDS, 0);
      } else if (ctx.request().query().contains("unspecified")) {
        mtypes.put("mtypes", new JsonArray()
          .add(new JsonObject()
            .put(ID, UNSPECIFIED_MATERIAL_TYPE_ID)
            .put(NAME, "unspecified")))
          .put(TOTAL_RECORDS, 1);
      } else {
        mtypes.put("mtypes", new JsonArray()
          .add(new JsonObject()
            .put(ID, UUID.randomUUID().toString())
            .put(NAME, ctx.queryParam("query").get(0).split("=")[1])))
          .put(TOTAL_RECORDS, 1);
      }

      addServerRqRsData(HttpMethod.GET, MATERIAL_TYPES, mtypes);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(mtypes.encodePrettily());
    }

    private void handleGetLocation(RoutingContext ctx) {

      logger.info("got location request: {}", ctx.request().query());

      JsonObject locations = new JsonObject();
      if (ctx.request().query().contains("HUM")) {
        locations.put("locations", new JsonArray())
          .put(TOTAL_RECORDS, 0);
      }
      else {
        locations.put("locations", new JsonArray()
          .add(new JsonObject()
            .put(ID, UUID.randomUUID().toString())
            .put("code", ctx.queryParam("query").get(0).split("=")[1])))
        .put(TOTAL_RECORDS, 1);
      }

      addServerRqRsData(HttpMethod.GET, LOCATION, locations);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(locations.encodePrettily());
    }

    private void handleGetConfigurationsEntries(RoutingContext ctx) {

      logger.info("got configurations entries request: {}", ctx.request()
          .query());

      JsonObject configurationsEntries = new JsonObject();
      try {
        if (ctx.request().query().contains("ListedElectronicSerial")) {
          configurationsEntries
            .put(CONFIGS, new JsonArray().add(
                    new JsonObject().put("value", getMockData(CUSTOM_LISTED_ELECTRONIC_SERIAL_MAPPING))))
            .put(TOTAL_RECORDS, 1);

        } else {
          configurationsEntries.put(CONFIGS, new JsonArray())
            .put(TOTAL_RECORDS, 0);
        }
        addServerRqRsData(HttpMethod.GET, CONFIGURATION, configurationsEntries);
        ctx.response()
          .setStatusCode(200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end(configurationsEntries.encodePrettily());
      } catch (IOException e) {
        ctx.response()
        .setStatusCode(500)
        .end();
       }
    }

    private String randomDigits(int len) {
      return rand.ints(len, 0, 9).mapToObj(Integer::toString).collect(Collectors.joining());
    }

    private void addServerRqRsData(HttpMethod method, String objName, JsonObject data) {
      List<JsonObject> entries = serverRqRs.get(objName, method);
      if (entries == null) {
        entries = new ArrayList<>();
      }
      entries.add(data);
      serverRqRs.put(objName, method, entries);
    }
  }

  private static String getMockData(String path) throws IOException {
    logger.info("Using mock datafile: {}", path);
    try (InputStream resourceAsStream = GOBIIntegrationServiceResourceImplTest.class.getClassLoader().getResourceAsStream(path)) {
      if (resourceAsStream != null) {
        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
      } else {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
          lines.forEach(sb::append);
        }
        return sb.toString();
      }
    }
  }

}

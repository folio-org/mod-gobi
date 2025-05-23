package org.folio.rest.impl;

import static java.util.UUID.randomUUID;
import static org.folio.gobi.HelperUtils.CONTRIBUTOR_NAME_TYPES;
import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_XML;
import static org.folio.rest.utils.TestUtils.checkVertxContextCompletion;
import static org.folio.rest.utils.TestUtils.getMockData;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.LookupService;
import org.folio.rest.ResourcePaths;
import org.folio.rest.RestVerticle;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.acq.model.PoLine;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.tools.utils.NetworkUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class GOBIIntegrationServiceResourceImplTest {

  private static final Logger logger = LogManager.getLogger(GOBIIntegrationServiceResourceImplTest.class);

  private static final String APPLICATION_JSON = "application/json";
  private static final String CONFIGS = "configs";
  private static final String PURCHASE_ORDER = "PURCHASE_ORDER";
  private static final String CONFIGURATION = "CONFIGURATION";

  private static final int OKAPI_PORT = NetworkUtils.nextFreePort();
  private static final int MOCK_PORT = NetworkUtils.nextFreePort();


  // private final int serverPort = NetworkUtils.nextFreePort();
  public static final Header TENANT_HEADER = new Header("X-Okapi-Tenant", "gobiintegrationserviceresourceimpltest");
  public static final Header TOKEN_HEADER = new Header("X-Okapi-Token",
      "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg");
  public static final Header URL_HEADER = new Header("X-Okapi-Url", "http://localhost:" + MOCK_PORT);
  private final Header CONTENT_TYPE_HEADER_XML = new Header("Content-Type", "application/xml");

  // API paths
  private final String ROOT_PATH = "/gobi";
  private final String VALIDATE_PATH = ROOT_PATH + "/validate";
  private final String ORDERS_PATH = ROOT_PATH + "/orders";

  // Mock data paths
  private final String MOCK_DATA_ROOT_PATH = "GOBIIntegrationServiceResourceImpl";
  private final String PO_LISTED_ELECTRONIC_MONOGRAPH_PATH = MOCK_DATA_ROOT_PATH + "/po_listed_electronic_monograph.xml";
  private final String PO_LISTED_ELECTRONIC_SERIAL_PATH = MOCK_DATA_ROOT_PATH + "/po_listed_electronic_serial.xml";
  private final String PO_LISTED_PRINT_MONOGRAPH_PATH = MOCK_DATA_ROOT_PATH + "/po_listed_print_monograph.xml";
  private final String PO_LISTED_PRINT_SERIAL_PATH = MOCK_DATA_ROOT_PATH + "/po_listed_print_serial.xml";
  private final String PO_UNLISTED_PRINT_MONOGRAPHPATH = MOCK_DATA_ROOT_PATH + "/po_unlisted_print_monograph.xml";
  private final String PO_UNLISTED_PRINT_SERIAL_PATH = MOCK_DATA_ROOT_PATH + "/po_unlisted_print_serial.xml";
  private final String PO_LISTED_ELECTRONIC_MONOGRAPH_BAD_DATA_PATH = MOCK_DATA_ROOT_PATH + "/po_listed_electronic_monograph_bad_data.xml";

  private static final String CUSTOM_LISTED_ELECTRONIC_SERIAL_MAPPING = "MappingHelper/Custom_ListedElectronicSerial.json";
  private static final  String VENDOR_MOCK_DATA =  "MockData/GOBI_organization.json";
  private static final  String ORDER_MOCK_DATA =  "MockData/purchaseOrders.json";
  private static final  String COMPOSITE_ORDER_MOCK_DATA =  "MockData/compositePurchaseOrder.json";
  private static final  String VALID_EXPENSE_CLASS =  "PostGobiOrdersHelper/valid_expenseClasses.json";
  private static final  String VALID_ACQUISITION_METHOD =  "PostGobiOrdersHelper/valid_acquisition_methods.json";
  private static final  String VALID_ACQUISITION_UNITS =  "PostGobiOrdersHelper/acquisitions_units.json";

  private static final String LOCATION = "LOCATION";
  private static final String FUNDS = "FUNDS";
  private static final String MATERIAL_TYPES = "MATERIAL-TYPES";
  private static final String VENDOR = "VENDOR";
  private static final String DONOR = "DONOR";
  private static final String MATERIAL_SUPPLIER = "MATERIAL_SUPPLIER";
  private static final String COMPOSITE_PURCHASE_ORDER = "COMPOSITE_PURCHASE_ORDER";
  private static final String IDENTIFIER_TYPES = "IDENTIFIER_TYPES";
  private static final String UNSPECIFIED_MATERIAL_TYPE_ID = "be44c321-ab73-43c4-a114-70f82fa13f17";
  private static final String EXPENSE_CLASS = "EXPENSE_CLASS";
  private static final String ACQUISITION_METHOD = "ACQUISITION_METHOD";
  private static final String ACQUISITION_UNITS = "ACQUISITION_UNITS";

  private static final String MOCK_OKAPI_GET_ORDER_BY_ID_HEADER = "X-Okapi-MockGetOrderById";
  private static final String MOCK_OKAPI_PUT_ORDER_HEADER = "X-Okapi-MockPutOrder";
  private static final String MOCK_OKAPI_GET_ORDER_HEADER = "X-Okapi-MockGetOrder";
  private static final String MOCK_OKAPI_GET_IDENTIFIER_HEADER = "X-Okapi-MockGetIdentifier";
  private static final String MOCK_OKAPI_GET_FUND_HEADER = "X-Okapi-MockGetFund";
  private static final String MOCK_OKAPI_GET_CONTRIBUTOR_NAME_HEADER = "X-Okapi-MockGetContributorName";
  private static final String MOCK_INSTRUCTION_GET_BYID_FAIL = "GetOrderFail";
  private static final String MOCK_INSTRUCTION_PUT_FAIL = "PutFail";
  private static final String MOCK_INSTRUCTION_GET_PENDING_ORDER = "GetPendingOrder";
  private static final String MOCK_INSTRUCTION_GET_OPEN_ORDER = "GetOpenOrder";
  private static final String MOCK_INSTRUCTION_FAIL_ORDER = "FailOrder";
  private static final String MOCK_INSTRUCTION_FAIL_PRODUCTYPE = "Fail";
  private static final String MOCK_INSTRUCTION_NO_PRODUCTYPE = "NoProductType";
  private static final String MOCK_INSTRUCTION_NOT_EXIST = "NotExist";
  private static final String MOCK_INSTRUCTION_USE_DEFAULT = "UseDefault";

  private static Vertx vertx;
  private static MockServer mockServer;

  @BeforeAll
  public static void setUpOnce(VertxTestContext context) throws Throwable {
    vertx = Vertx.vertx();

    mockServer = new MockServer(MOCK_PORT);
    mockServer.start(context);
    final JsonObject conf = new JsonObject();
    conf.put("http.port", OKAPI_PORT);

    VertxTestContext deploymentContext = new VertxTestContext();
    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, h -> {
      deploymentContext.completeNow();
    });
    checkVertxContextCompletion(deploymentContext);
    RestAssured.port = OKAPI_PORT;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    logger.info("GOBI Integration Service Test Setup Done using port {}", OKAPI_PORT);
  }

  @AfterAll
  public static void tearDownOnce(VertxTestContext context) throws Throwable {
    logger.info("GOBI Integration Service Testing Complete");
    vertx.close(v -> {
      mockServer.close();
      context.completeNow();
    });
    checkVertxContextCompletion(context);
  }

  @BeforeEach
  public void setUp() {
    MockServer.serverRqRs.clear();
  }

  @Test
   void testGetGobiValidate() {
    logger.info("Begin: Testing for Get Gobi Validate 200 - valid call");

    RestAssured
      .given()
        .header(TENANT_HEADER)
        .header(TOKEN_HEADER)
        .header(URL_HEADER)
      .when()
        .get(VALIDATE_PATH)
      .then()
        .statusCode(200).body(Matchers.equalTo("<test>GET - OK</test>"));

    logger.info("End: Testing for Get Gobi Validate 200 - valid call");
  }

  @Test
  void testPostGobiValidate() {
    logger.info("Begin: Testing for Post Gobi Validate 200 - valid call");

    RestAssured
      .given()
        .header(TENANT_HEADER)
        .header(TOKEN_HEADER)
        .header(URL_HEADER)
      .when()
        .post(VALIDATE_PATH)
      .then()
        .contentType("application/xml")
        .statusCode(200).body(Matchers.equalTo("<test>POST - OK</test>"));

    logger.info("End: Testing for Post Gobi Validate 200 - valid call");
  }

  @Test
  void testPostGobiXMLContent() throws Exception {
    logger.info("Begin: Testing for 201 - XML content");

    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_PATH);

    postOrderSuccess(body);

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    // Listed Electronic Monograph has to get the Product type ID so there will be an additional call made
    assertThat(column.keySet(), containsInAnyOrder(CONFIGURATION, CONTRIBUTOR_NAME_TYPES, FUNDS, IDENTIFIER_TYPES, LOCATION,
        MATERIAL_TYPES, PURCHASE_ORDER, VENDOR, ACQUISITION_METHOD, MATERIAL_SUPPLIER, DONOR));

    logger.info("End: Testing for 201 - XML content");
  }

  @Test
  void testPostGobiOrdersPOListedElectronicMonograph() throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic monograph");

    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_PATH);

    final GobiResponse order = postOrderSuccess(body);
    assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    // Listed Electronic Monograph has to get the Product type ID so there will be an additional call made
    assertThat(column.keySet(), containsInAnyOrder(CONFIGURATION, CONTRIBUTOR_NAME_TYPES, FUNDS, IDENTIFIER_TYPES, LOCATION,
        MATERIAL_TYPES, PURCHASE_ORDER, VENDOR, ACQUISITION_METHOD, MATERIAL_SUPPLIER, DONOR));

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    compPO.getPoLines().get(0).withDescription("test");
    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    //make sure that the qualifier field is empty
    assertNull(compPO.getPoLines().get(0).getDetails().getProductIds().get(0).getQualifier());
    assertEquals("9781410352224",compPO.getPoLines().get(0).getDetails().getProductIds().get(0).getProductId());

    assertFalse(compPO.getPoLines().get(0).getDescription().isEmpty());

    logger.info("End: Testing for 201 - posted order listed electronic monograph");
  }

  @Test
  void testPostGobiOrdersCustomPOListedElectronicSerial() throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic serial with custom mappings");

    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> configEntries = MockServer.serverRqRs.get(CONFIGURATION, HttpMethod.GET);
    assertNotNull(configEntries);

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);

    // Make sure the mappings from custom configuration were used
    assertEquals(1, column.get(CONFIGURATION).get(0).getJsonArray(CONFIGS).size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
     CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
     verifyRequiredFieldsAreMapped(compPO);
    assertEquals("Description from Custom Mapping", compPO.getPoLines().get(0).getPoLineDescription());
    // verify if the currency specified in the request is used
    assertEquals("GBP", compPO.getPoLines().get(0).getCost().getCurrency());

    // MODGOBI-61 - check createInventory populated for eresource only
    assertNotNull(compPO.getPoLines().get(0).getEresource().getCreateInventory());
    assertNull(compPO.getPoLines().get(0).getPhysical());

    verifyRequiredFieldsAreMapped(compPO);
    assertFalse(compPO.getAcqUnitIds().isEmpty());
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    logger.info("End: Testing for 201 - posted order listed electronic serial");
  }

  @Test
  void testPostGobiOrdersPOListedPrintMonograph() throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph");

    final String body = getMockData(PO_LISTED_PRINT_MONOGRAPH_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    // Listed Print Monograph has to get the Product type ID so there will be an additional call made
    assertThat(column.keySet(), containsInAnyOrder(
      CONFIGURATION, CONTRIBUTOR_NAME_TYPES, FUNDS, IDENTIFIER_TYPES, LOCATION,
      MATERIAL_TYPES, PURCHASE_ORDER, VENDOR, ACQUISITION_METHOD, MATERIAL_SUPPLIER, DONOR
    ));

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    assertThat(compPO.getPoLines().get(0).getCost().getListUnitPriceElectronic(), nullValue());
    assertThat(compPO.getPoLines().get(0).getCost().getListUnitPrice(), equalTo(14.95));
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());
    assertNotNull(compPO.getPoLines().get(0).getAcquisitionMethod());

    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  void testPostGobiOrdersPOListedPrintSerial() throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print serial");

    final String body = getMockData(PO_LISTED_PRINT_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertThat(column.keySet(), containsInAnyOrder(CONFIGURATION, FUNDS, LOCATION, MATERIAL_TYPES, PURCHASE_ORDER, VENDOR,
      EXPENSE_CLASS, ACQUISITION_METHOD, MATERIAL_SUPPLIER, DONOR));

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    // verify if default currency is used
    assertEquals("USD", compPO.getPoLines().get(0).getCost().getCurrency());

    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());
    assertTrue(compPO.getPoLines().get(0).getTags().getTagList().isEmpty());

    logger.info("End: Testing for 201 - posted order listed print serial");
  }

  @Test
  void testPostGobiOrdersPOUnlistedPrintMonograph() throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print monograph");

    final String body = getMockData(PO_UNLISTED_PRINT_MONOGRAPHPATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertThat(column.keySet(),
        containsInAnyOrder(CONFIGURATION, CONTRIBUTOR_NAME_TYPES, FUNDS, LOCATION, MATERIAL_TYPES, PURCHASE_ORDER,
                  VENDOR, ACQUISITION_METHOD, MATERIAL_SUPPLIER, DONOR));

    verifyResourceCreateInventoryNotMapped();

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    logger.info("End: Testing for 201 - posted order unlisted print monograph");
  }

  @Test
  void testPostGobiOrdersPOUnlistedPrintSerial() throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print serial");

    final String body = getMockData(PO_UNLISTED_PRINT_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    assertThat(column.keySet(), containsInAnyOrder(CONFIGURATION, FUNDS, LOCATION, MATERIAL_TYPES, PURCHASE_ORDER,
            VENDOR, ACQUISITION_METHOD, MATERIAL_SUPPLIER, DONOR));

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    assertEquals("USD", compPO.getPoLines().get(0).getCost().getCurrency());

    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());
    assertTrue(compPO.getPoLines().get(0).getTags().getTagList().isEmpty());

    logger.info("End: Testing for 201 - posted order unlisted print serial");
  }

  @Test
  void testPostGobiOrdersPOListedElectronicMonographBadData() throws Exception {
    logger.info("Begin: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");

    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_BAD_DATA_PATH);

    final GobiResponse error = buildRequest(body)
      .when()
        .post(ORDERS_PATH)
      .then()
        .statusCode(400)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);

    assertNotNull(error);
    assertNotNull(error.getError());
    assertEquals(CODE_INVALID_XML, error.getError().getCode());
    assertNotNull(error.getError().getMessage());
    assertTrue(MockServer.serverRqRs.isEmpty());

    logger.info("End: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");
  }

  @Test
  void testPostContentWithValidOkapiToken() throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph with valid Okapi token");

    final String body = getMockData(PO_LISTED_PRINT_MONOGRAPH_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    Map<String, List<JsonObject>> column = MockServer.serverRqRs.column(HttpMethod.GET);
    //Listed Print Monograph has Product Id type so there will be an additional call made
    assertThat(column.keySet(), containsInAnyOrder(
      CONFIGURATION, CONTRIBUTOR_NAME_TYPES, FUNDS, IDENTIFIER_TYPES, LOCATION,
      MATERIAL_TYPES, PURCHASE_ORDER, VENDOR, ACQUISITION_METHOD, MATERIAL_SUPPLIER, DONOR
    ));

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  void testPostGobiOrdersFallBackMaterialTypes() throws Exception {
    logger.info("Begin: Testing for falling back to the unspecified material id, if a non existent code is sent");


    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> configEntries = MockServer.serverRqRs.get(MATERIAL_TYPES, HttpMethod.GET);
    //2 calls must be made to material types end point, once for non existent and other for unspecified
    assertEquals(2, configEntries.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    assertEquals(UNSPECIFIED_MATERIAL_TYPE_ID, compPO.getPoLines().get(0).getEresource().getMaterialType());

    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    logger.info("End: Testing for falling back to the unspecified material id, if a non existent code is sent");
  }

  @Test
  void testPostGobiOrdersFallBackLocation() throws Exception {
    logger.info("Begin: Testing for falling back to the first location id, if a non existent code is sent");


    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> configEntries = MockServer.serverRqRs.get(LOCATION, HttpMethod.GET);
    // 2 calls must be made to location end point
    assertEquals(2, configEntries.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    verifyRequiredFieldsAreMapped(compPO);

    logger.info("End: Testing for falling back to tthe first location id, if a non existent code is sent");
  }

  @Test
  void testPostGobiOrdersFallBackFundDistribution() throws Exception {
    logger.info("Begin: Testing for falling back to the first fund id, if a non existent code is sent");

    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> configEntries = MockServer.serverRqRs.get(FUNDS, HttpMethod.GET);
    // 2 calls must be made to funds endpoint
    assertEquals(2, configEntries.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    logger.info("End: Testing for falling back to the first location id, if a non existent code is sent");
  }

  @Test
  void testPostGobiOrdersPopulateAcqUnitDefault() throws Exception {
    logger.info("Begin: Testing for falling back to the first fund id, if a non existent code is sent");

    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body);

    assertNotNull(order.getPoLineNumber());


    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());
    assertFalse(compPO.getAcqUnitIds().isEmpty());

    logger.info("End: Testing for falling back to the first location id, if a non existent code is sent");
  }


  @Test
   void testPostGobiOrdersFallBackProductType() throws Exception {
    logger.info("Begin: Testing for falling back to the first product id, if a non existent code is sent");

    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_PATH);

    final GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_IDENTIFIER_HEADER, MOCK_INSTRUCTION_FAIL_PRODUCTYPE));

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> identifierTypes = MockServer.serverRqRs.get(IDENTIFIER_TYPES, HttpMethod.GET);
    // 2 calls must be made to identifiers endpoint
    assertEquals(2, identifierTypes.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0)
      .mapTo(CompositePurchaseOrder.class);
    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    logger.info("End: Testing for falling back to the first product id, if a non existent code is sent");
  }

  @Test
   void testPostGobiOrdersFallBackContributorTypeName() throws Exception {
    logger.info("Begin: Testing for falling back to the first random contributor name type, if a non existent code is sent");

    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_PATH);

    GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_CONTRIBUTOR_NAME_HEADER, MOCK_INSTRUCTION_USE_DEFAULT));

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> types = MockServer.serverRqRs.get(CONTRIBUTOR_NAME_TYPES, HttpMethod.GET);
    // 2 calls must be made
    assertEquals(2, types.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    assertEquals(1, postedOrder.size());

    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getContributors().get(0).getContributorNameTypeId());
    assertNotNull(compPO.getPoLines().get(0).getContributors().get(0).getContributor());

    logger.info("End: Testing for falling back to the first random contributor name type, if a non existent code is sent");
  }

  @Test
  void testPostGobiOrdersContributorNameTypeNotAvailable() throws Exception {
    logger.info("Begin: Testing contributor is ignored if contributor name type cannot be resolved");

    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_PATH);

    GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_CONTRIBUTOR_NAME_HEADER, MOCK_INSTRUCTION_NOT_EXIST));

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> types = MockServer.serverRqRs.get(CONTRIBUTOR_NAME_TYPES, HttpMethod.GET);
    // 2 calls must be made
    assertEquals(2, types.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    assertEquals(1, postedOrder.size());

    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);
    verifyRequiredFieldsAreMapped(compPO);
    assertTrue(compPO.getPoLines().get(0).getContributors().isEmpty());

    logger.info("End: Testing contributor is ignored if contributor name type cannot be resolved");
  }

  @Test
  void testPostGobiOrdersNoProductIdTypes() throws Exception {
    logger.info("Begin: Testing for checking if productId is set if there are no productIdTypes in the environment");

    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_PATH);

    final GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_IDENTIFIER_HEADER, MOCK_INSTRUCTION_NO_PRODUCTYPE));

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> identifierTypes = MockServer.serverRqRs.get(IDENTIFIER_TYPES, HttpMethod.GET);
    // 2 calls must be made to identifiers endpoint
    assertEquals(2, identifierTypes.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0)
      .mapTo(CompositePurchaseOrder.class);
    // assert that the productId is mapped, so that if the order is created in Pending state even though
    // there is no ProductIdType
    assertNull(compPO.getPoLines().get(0).getDetails().getProductIds().get(0).getProductIdType());
    assertNotNull(compPO.getPoLines().get(0).getDetails().getProductIds().get(0).getProductId());
    verifyRequiredFieldsAreMapped(compPO);
    assertNotNull(compPO.getPoLines().get(0).getFundDistribution().get(0).getFundId());

    logger.info("End: Testing for checking if productId is set if there are no productIdTypes in the environment");
  }


  @Test
  void testPostGobiOrdersNoFundsExist() throws Exception {
    logger.info("Begin: Testing for checking if FundId is not set if there are no Funds in the environment");

    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_PATH);

    final GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_FUND_HEADER, MOCK_INSTRUCTION_NOT_EXIST));

    assertNotNull(order.getPoLineNumber());

    List<JsonObject> fundsRequests = MockServer.serverRqRs.get(FUNDS, HttpMethod.GET);
    // 1 call must be made to Funds endpoint, as there is no default mapping
    assertEquals(1, fundsRequests.size());

    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    CompositePurchaseOrder compPO = postedOrder.get(0).mapTo(CompositePurchaseOrder.class);

    verifyRequiredFieldsAreMapped(compPO);

    //Fund Distribution List must be empty, with no proper FundId
    assertEquals(Collections.emptyList(), compPO.getPoLines().get(0).getFundDistribution());

    logger.info("End: Testing for checking if FundId is not set if there are no Funds in the environment");
  }

  @Test
  void testPostGobiOrdersExistingOrder() throws Exception {
    logger.info("Begin: Testing for 201 - posted order returns existing Order if present");


    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_ORDER_HEADER, MOCK_INSTRUCTION_GET_OPEN_ORDER));

    // should try to fetch the order in Open status
    List<JsonObject> getOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.GET);
    assertEquals(1, getOrder.size());

    // Should not try to create an order if present
    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    assertNull(postedOrder);

    // Returned order is Open, should not retry to Open
    List<JsonObject> putOrder = MockServer.serverRqRs.get(COMPOSITE_PURCHASE_ORDER, HttpMethod.PUT);
    assertNull(putOrder);

    // return the existing PO Line Number
    assertNotNull(order.getPoLineNumber());

    logger.info("End: Testing for 201 - posted order returns existing Order if present");
  }

  @Test
  void testPostGobiOrdersFailedToRetrieveExistingOrder() throws Exception {
    logger.info("Begin: Testing for 201 - Create new Order if retrieving existing Order fails");


    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_ORDER_HEADER, MOCK_INSTRUCTION_FAIL_ORDER));

    // should try to fetch the order, and gets 500
    List<JsonObject> getOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.GET);
    assertNull(getOrder);

    // should create an order if the call to fetch existing order fails
    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    assertEquals(1, postedOrder.size());

    // should not call PUT to open order
    List<JsonObject> putOrder = MockServer.serverRqRs.get(COMPOSITE_PURCHASE_ORDER, HttpMethod.PUT);
    assertNull(putOrder);

    // return the created PO Line Number
    assertNotNull(order.getPoLineNumber());

    logger.info("End: Testing for 201 - Create new Order if retrieving existing Order fails");
  }

  @Test
  void testPostGobiOrdersRetryToOpenOrderFails() throws Exception {
    logger.info("Begin: Testing for 201 - Return existing Order even if it is pending, and retry to Open Fails");

    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_ORDER_HEADER, MOCK_INSTRUCTION_GET_PENDING_ORDER),
        new Header(MOCK_OKAPI_PUT_ORDER_HEADER, MOCK_INSTRUCTION_PUT_FAIL));

    // should fetch existing order which is in Pending state
    List<JsonObject> getOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.GET);
    assertEquals(1, getOrder.size());

    // should try to Open, the existing Pending Order, which fails
    List<JsonObject> putOrder = MockServer.serverRqRs.get(COMPOSITE_PURCHASE_ORDER, HttpMethod.PUT);
    assertEquals(1, putOrder.size());

    // should get the existing Pending Order Line Number
    List<JsonObject> getOrderById = MockServer.serverRqRs.get(COMPOSITE_PURCHASE_ORDER, HttpMethod.GET);
    assertEquals(1, getOrderById.size());

    // should not try to create a new Order
    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    assertNull(postedOrder);

    // return the existing Order in Pending State
    assertNotNull(order.getPoLineNumber());

    logger.info("End: Testing for 201 - Return existing Order even if it is pending, and retry to Open Fails");
  }


  @Test
  void testPostGobiOrdersRetryToOpenOrderSuccess() throws Exception {
    logger.info("Begin: Testing for 201 - Return existing Order even if it is pending, and retry to Open succeeds");

    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_ORDER_HEADER, MOCK_INSTRUCTION_GET_PENDING_ORDER));

    // should fetch existing order which is in Pending state
    List<JsonObject> getOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.GET);
    assertEquals(1, getOrder.size());

    // should try to Open, the existing Pending Order, which succeds
    List<JsonObject> putOrder = MockServer.serverRqRs.get(COMPOSITE_PURCHASE_ORDER, HttpMethod.PUT);
    assertEquals(1, putOrder.size());

    // should not try to create a new Order
    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    assertNull(postedOrder);

    // return the existing PO Line Number
    assertNotNull(order.getPoLineNumber());

    logger.info("End: Testing for 201 - Return existing Order even if it is pending, and retry to Open succeeds");
  }

  @Test
   void testPostGobiOrdersGetExistingPOLineNumberFails() throws Exception {
    logger.info("Begin: Testing for 201 - Create new Order, if fetching existing PO Line Number Fails");

    final String body = getMockData(PO_LISTED_ELECTRONIC_SERIAL_PATH);

    final GobiResponse order = postOrderSuccess(body, new Header(MOCK_OKAPI_GET_ORDER_HEADER, MOCK_INSTRUCTION_GET_OPEN_ORDER),
        new Header(MOCK_OKAPI_GET_ORDER_BY_ID_HEADER, MOCK_INSTRUCTION_GET_BYID_FAIL));

    // should fetch existing order which is in Open state
    List<JsonObject> getOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.GET);
    assertEquals(1, getOrder.size());

    // should not retry to Open
    List<JsonObject> putOrder = MockServer.serverRqRs.get(COMPOSITE_PURCHASE_ORDER, HttpMethod.PUT);
    assertNull(putOrder);

    // should try to create a new Order
    List<JsonObject> postedOrder = MockServer.serverRqRs.get(PURCHASE_ORDER, HttpMethod.POST);
    assertEquals(1, postedOrder.size());
    assertNotNull(order.getPoLineNumber());

    logger.info("End: Testing for 201 - Create new Order, if fetching existing PO Line Number Fails");
  }

  @Test
  void testPostElectronicMonographISBNQualifierSameField() throws Exception {
    logger.info("Begin: Testing ISBN and Qualifier on same field");

    Document doc = getDocumentFromXml();

    logger.info("Test: ISBN and qualifier on same field");
    Element productNode = (Element) doc.getElementsByTagName("datafield").item(0);
    Node productID = productNode.getElementsByTagName("subfield").item(0).getFirstChild();
    productID.setNodeValue("9781410352224 (ebook print)");

    postOrderSuccess(toString(doc));
    validateProductIDAndQualifier("9781410352224","(ebook print)");

    logger.info("End: Testing ISBN and Qualifier on same field");

  }

  @Test
   void testPostElectronicMonographISBNQualifierWithSpaces() throws Exception {
    logger.info("Begin: Testing ISBN and qualifier with Trailing and Leading spaces");

    Document doc = getDocumentFromXml();


    //2. Test ISBN and qualifier with Trailing and Leading spaces
    Element productNode  = (Element) doc.getElementsByTagName("datafield").item(0);
    Node productID = productNode.getElementsByTagName("subfield").item(0).getFirstChild();
    productID.setNodeValue(" 9781410352224 (ebook print) ");

    postOrderSuccess(toString(doc));

    validateProductIDAndQualifier("9781410352224","(ebook print)");

    logger.info("End: Testing ISBN and qualifier with Trailing and Leading spaces");
  }

  @Test
   void testPostElectronicMonographISBNQualifierSeparateField() throws Exception {
    logger.info("Begin: Testing ISBN and qualifier with Qualifier in a separate field");

    Document doc = getDocumentFromXml();


  //3. Test ISBN and qualifier with Qualifier in a separate field
    Element productNode  = (Element) doc.getElementsByTagName("datafield").item(0);
    Node productID = productNode.getElementsByTagName("subfield").item(0).getFirstChild();
    productID.setNodeValue("9781410352224 (ebook print)");
    Element qualifier = doc.createElement("subfield");
    qualifier.setAttribute("code", "q");
    qualifier.appendChild(doc.createTextNode("(print)"));
    productNode.appendChild(qualifier);

    postOrderSuccess(toString(doc));

    validateProductIDAndQualifier("9781410352224","(print)");

    logger.info("End: Testing ISBN and qualifier with Qualifier in a separate field");
  }

  @Test
   void testPostElectronicMonographISBNQualifierBothFields() throws Exception {
    logger.info("Begin: Testing ISBN and qualifier with Qualifier present in both Fields: Subfield is given precedence");

    Document doc = getDocumentFromXml();
   //4. Test ISBN and qualifier with Qualifier present in both places(subfield and along with product ID)
    // the subfield qualifier must be picked
    Element productNode  = (Element) doc.getElementsByTagName("datafield").item(0);
    Node productID = productNode.getElementsByTagName("subfield").item(0).getFirstChild();
    productID.setNodeValue("9781410352224 (ebook print)");
    Element qualifier = doc.createElement("subfield");
    qualifier.setAttribute("code", "q");
    qualifier.appendChild(doc.createTextNode("(print)"));
    productNode.appendChild(qualifier);

    postOrderSuccess(toString(doc));

    validateProductIDAndQualifier("9781410352224","(print)");

    logger.info("End: Testing ISBN and qualifier with Qualifier present in both places");


  }

  private void validateProductIDAndQualifier(String productID, String qualifier) {
    Map<String, List<JsonObject>> postOrder = MockServer.serverRqRs.column(HttpMethod.POST);
    CompositePurchaseOrder compPO = postOrder.get(PURCHASE_ORDER).get(0).mapTo(CompositePurchaseOrder.class);
    assertNotNull(compPO.getPoLines().get(0).getDetails().getProductIds().get(0).getQualifier());
    assertEquals(productID,compPO.getPoLines().get(0).getDetails().getProductIds().get(0).getProductId());
    assertEquals(qualifier,compPO.getPoLines().get(0).getDetails().getProductIds().get(0).getQualifier());
  }

  /**
   * Get Document from XML
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  private Document getDocumentFromXml() throws IOException, SAXException, ParserConfigurationException {
    final String body = getMockData(PO_LISTED_ELECTRONIC_MONOGRAPH_PATH);

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    final InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
    return docFactory.newDocumentBuilder().parse(stream);
  }

  /**
   * Convert Document object to Sting
   * @param newDoc
   * @return
   * @throws Exception
   */
  private static String toString(Document newDoc) throws Exception {
    DOMSource domSource = new DOMSource(newDoc);
    Transformer transformer = TransformerFactory.newInstance()
      .newTransformer();
    StringWriter sw = new StringWriter();
    StreamResult sr = new StreamResult(sw);
    transformer.transform(domSource, sr);
    return sw.toString();
  }

  private RequestSpecification buildRequest(String body, Header... additionalHeaders) {
    RequestSpecification request = RestAssured.given()
      .header(TOKEN_HEADER)
      .header(URL_HEADER)
      .header(TENANT_HEADER)
      .header(CONTENT_TYPE_HEADER_XML)
      .body(body);

    for (Header header : additionalHeaders) {
      request.header(header);
    }

    return request;
  }

  private GobiResponse postOrderSuccess(String body, Header... additionalHeaders) {
    return buildRequest(body, additionalHeaders)
      .when()
        .post(ORDERS_PATH)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GobiResponse.class, ObjectMapperType.JAXB);
  }

  // MODGOBI-61 check createInventory field not mapped
  private void verifyResourceCreateInventoryNotMapped() {
    Map<String, List<JsonObject>> postOrder = MockServer.serverRqRs.column(HttpMethod.POST);
    CompositePurchaseOrder compPO = postOrder.get(PURCHASE_ORDER).get(0).mapTo(CompositePurchaseOrder.class);
    compPO.getPoLines().stream()
      .filter(line -> line.getEresource() != null)
      .forEach(line -> assertNull(line.getEresource().getCreateInventory()));
    compPO.getPoLines().stream()
      .filter(line -> line.getPhysical() != null)
      .forEach(line -> assertNull(line.getPhysical().getCreateInventory()));
  }

  /**
   * Make sure all the required fields for creating an order are mapped
   * @param compPO CompositePurchaseOrder object to be verified
   */
  private void verifyRequiredFieldsAreMapped(CompositePurchaseOrder compPO){
    assertNotNull(compPO.getVendor());
    assertNotNull(compPO.getOrderType());

    PoLine poLine = compPO.getPoLines().get(0);
    assertNotNull(poLine.getAcquisitionMethod());
    assertNotNull(poLine.getCost());
    assertNotNull(poLine.getOrderFormat());
    assertNotNull(poLine.getSource());
    assertNotNull(poLine.getTitleOrPackage());
    if (!poLine.getContributors().isEmpty()) {
      poLine.getContributors().forEach(contributor -> {
        assertNotNull(contributor.getContributor());
        assertNotNull(contributor.getContributorNameTypeId());
      });
    }
  }

  public static class MockServer {

    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String TENANT_ID = "tenantId";
    private static final String TOTAL_RECORDS = "totalRecords";
    private static final Logger logger = LogManager.getLogger(MockServer.class);
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
      router.route(HttpMethod.POST, ResourcePaths.ORDERS_ENDPOINT).handler(this::handlePostPurchaseOrder);
      router.get(ResourcePaths.CONFIGURATION_ENDPOINT).handler(this::handleGetConfigurationsEntries);
      router.get(ResourcePaths.ORDERS_ENDPOINT).handler(this::handleGetOrders);
      router.get(ResourcePaths.ORDERS_ENDPOINT+"/:id").handler(this::handleGetOrderById);
      router.get(ResourcePaths.EXPENSE_CLASS_ENDPOINT).handler(this::handleGetExpenseClass);
      router.get(ResourcePaths.ACQUISITION_METHOD_ENDPOINT).handler(this::handleGetAcquisitionMethods);
      router.get(ResourcePaths.ACQUISITION_UNIT_ENDPOINT).handler(this::handleGetAcquisitionUnits);
      router.get(ResourcePaths.IDENTIFIERS_ENDPOINT).handler(this::handleGetProductTypes);
      router.get(ResourcePaths.CONTRIBUTOR_NAME_TYPES_ENDPOINT).handler(this::handleGetContributorNameTypes);
      router.get(ResourcePaths.GET_ORGANIZATION_ENDPOINT).handler(this::handleGetOrganization);
      router.get(ResourcePaths.MATERIAL_TYPES_ENDPOINT).handler(this::handleGetMaterialType);
      router.get(ResourcePaths.LOCATIONS_ENDPOINT).handler(this::handleGetLocation);
      router.get(ResourcePaths.FUNDS_ENDPOINT).handler(this::handleGetFund);

      router.put(ResourcePaths.ORDERS_ENDPOINT+"/:id").handler(this::handlePutOrderById);
      return router;
    }

    private void handleGetAcquisitionUnits(RoutingContext ctx) {
      logger.info("got acquisition-units request: {}", ctx.request().query());
      JsonObject acquisitionMethods = new JsonObject();

      try {
        if (ctx.request().query().contains("name")) {
          acquisitionMethods = new JsonObject(getMockData(VALID_ACQUISITION_UNITS));
        }
      } catch (IOException e) {
        acquisitionMethods = new JsonObject();
      }

      addServerRqRsData(HttpMethod.GET, ACQUISITION_UNITS, acquisitionMethods);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(acquisitionMethods.encodePrettily());
    }

    public void start(VertxTestContext testContext) throws Throwable {
      logger.info("Starting mock server on port: " + port);

      // Setup Mock Server...
      HttpServer server = vertx.createHttpServer();

      server.requestHandler(defineRoutes()).listen(port, result -> {
        if (result.failed()) {
          logger.warn("Failure", result.cause());
        }
        assertTrue(result.succeeded());
        testContext.completeNow();
      });
      checkVertxContextCompletion(testContext);

    }

    private void handlePostPurchaseOrder(RoutingContext ctx) {
      logger.info("Handle Post Purchase Order got: {}", ctx.getBodyAsString());

      JsonObject compPO = ctx.getBodyAsJson();

      compPO.put(ID, randomUUID().toString());
      String poNumber = "PO_" + randomDigits(10);
      compPO.put("poNumber", poNumber);
      compPO.getJsonArray("poLines").forEach(line -> {
        JsonObject poLine = (JsonObject) line;
        poLine.put(ID, randomUUID().toString());
        poLine.put("poLineNumber", poNumber + "-1");
        poLine.put("purchaseOrderId", compPO.getString(ID));
      });

      addServerRqRsData(HttpMethod.POST, PURCHASE_ORDER, compPO);

      ctx.response()
        .setStatusCode(201)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(compPO.encodePrettily());
    }

    private void handleGetOrganization(RoutingContext ctx) {
      logger.info("got request for Organization that is a vendor: {}", ctx.request().query());

      try {
        JsonObject vendors = new JsonObject(getMockData(VENDOR_MOCK_DATA));

        addServerRqRsData(HttpMethod.GET, VENDOR, vendors);
        addServerRqRsData(HttpMethod.GET, MATERIAL_SUPPLIER, vendors);
        addServerRqRsData(HttpMethod.GET, DONOR, vendors);

        ctx.response()
          .setStatusCode(200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end(vendors.encodePrettily());
      } catch (IOException e) {
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
            .put(ID, randomUUID().toString())
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
      locations.put("locations", new JsonArray()
          .add(new JsonObject()
            .put(ID, randomUUID().toString())
            .put(TENANT_ID, randomUUID().toString())
            .put("code", "KU/CC/DI/O")))
        .put(TOTAL_RECORDS, 1);

      addServerRqRsData(HttpMethod.GET, LOCATION, locations);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(locations.encodePrettily());
    }

    private void handleGetFund(RoutingContext ctx) {
      logger.info("got location request: {}", ctx.request().query());

      String getByIdInstruction = ctx.request().getHeader(MOCK_OKAPI_GET_FUND_HEADER);

      JsonObject funds = new JsonObject();
      if (ctx.request().query().contains("HUM")) {
        funds.put("funds", new JsonArray()).put(TOTAL_RECORDS, 0);

      } else if (MOCK_INSTRUCTION_NOT_EXIST.equals(getByIdInstruction)) {
        funds.put("funds", new JsonArray()).put(TOTAL_RECORDS, 0);

      } else if (ctx.queryParam("query").get(0).split("==")[1].contains(LookupService.DEFAULT_LOOKUP_CODE)) {
        funds.put("funds", new JsonArray().add(new JsonObject().put(ID, randomUUID().toString())
          .put("code", "HUM")))
          .put(TOTAL_RECORDS, 1);

      } else {
        funds.put("funds", new JsonArray().add(new JsonObject().put(ID, randomUUID().toString())
          .put("code", ctx.queryParam("query").get(0).split("==")[1])))
          .put(TOTAL_RECORDS, 1);
      }

      addServerRqRsData(HttpMethod.GET, FUNDS, funds);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(funds.encodePrettily());
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

        } else if (ctx.request().query().contains("ListedElectronicSerial")) {

        }
        else {
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

    private void handleGetOrders(RoutingContext ctx) {

      logger.info("got Orders request: {}", ctx.request()
        .query());
      String getInstruction = StringUtils.trimToEmpty(ctx.request().getHeader(MOCK_OKAPI_GET_ORDER_HEADER));

      JsonObject purchaseOrders;
      try {
        if (getInstruction.equals(MOCK_INSTRUCTION_FAIL_ORDER)) {
          ctx.response()
            .setStatusCode(500)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .end(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
        } else {
          if (getInstruction.equals(MOCK_INSTRUCTION_GET_OPEN_ORDER)) {
            purchaseOrders = new JsonObject(getMockData(ORDER_MOCK_DATA));
          } else if (getInstruction.equals(MOCK_INSTRUCTION_GET_PENDING_ORDER)) {
            purchaseOrders = new JsonObject(getMockData(ORDER_MOCK_DATA));
            purchaseOrders.getJsonArray("purchaseOrders")
              .getJsonObject(0)
              .put("workflowStatus", "Pending");
          } else {
            purchaseOrders = new JsonObject().put("purchaseOrders", new JsonArray());
          }

          addServerRqRsData(HttpMethod.GET, PURCHASE_ORDER, purchaseOrders);

          ctx.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .end(purchaseOrders.encodePrettily());
        }
      } catch (IOException e) {
        ctx.response()
          .setStatusCode(404)
          .end();
      }
    }

    private void handleGetOrderById(RoutingContext ctx) {
      logger.info("got {}", ctx.request()
        .path());
      String getByIdInstruction = ctx.request()
        .getHeader(MOCK_OKAPI_GET_ORDER_BY_ID_HEADER);

      try {
        if (MOCK_INSTRUCTION_GET_BYID_FAIL.equals(getByIdInstruction)) {
          ctx.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .setStatusCode(500)
            .end();
        } else {
          JsonObject compPO = new JsonObject(getMockData(COMPOSITE_ORDER_MOCK_DATA));

          addServerRqRsData(HttpMethod.GET, COMPOSITE_PURCHASE_ORDER, compPO);

          ctx.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .end(compPO.encodePrettily());
        }
      } catch (IOException e) {
        ctx.response()
          .setStatusCode(404)
          .end();
      }
    }

    private void handlePutOrderById(RoutingContext ctx) {
      logger.info("got {}", ctx.request()
        .path());
      String putInstruction = ctx.request()
        .getHeader(MOCK_OKAPI_PUT_ORDER_HEADER);
      addServerRqRsData(HttpMethod.PUT, COMPOSITE_PURCHASE_ORDER, ctx.getBodyAsJson());

      if (putInstruction.equals(MOCK_INSTRUCTION_PUT_FAIL)) {
        ctx.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .setStatusCode(500)
          .end();
      } else {
        ctx.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .setStatusCode(204)
          .end();
      }
    }

    private void handleGetProductTypes(RoutingContext ctx) {
      logger.info("got productTypes request: {}", ctx.request()
        .query());
      String getByIdInstruction = ctx.request()
        .getHeader(MOCK_OKAPI_GET_IDENTIFIER_HEADER);

      JsonObject productTypes = new JsonObject();
      addServerRqRsData(HttpMethod.GET, IDENTIFIER_TYPES, productTypes);

      if (MOCK_INSTRUCTION_FAIL_PRODUCTYPE.equals(getByIdInstruction) && ctx.request()
        .query()
        .contains("ISBN")) {
        productTypes.put("identifierTypes", new JsonArray())
          .put(TOTAL_RECORDS, 0);
      } else if (MOCK_INSTRUCTION_NO_PRODUCTYPE.equals(getByIdInstruction)) {
        productTypes.put("identifierTypes", new JsonArray())
          .put(TOTAL_RECORDS, 0);
      } else {
        productTypes.put("identifierTypes", new JsonArray().add(new JsonObject().put(ID, randomUUID()
          .toString())
          .put("code", ctx.queryParam("query")
            .get(0)
            .split("=")[1])))
          .put(TOTAL_RECORDS, 1);
      }
      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(productTypes.encodePrettily());
    }

    private void handleGetContributorNameTypes(RoutingContext ctx) {
      logger.info("got contributorNameTypes request: {}", ctx.request().query());
      String instruction = ctx.request().getHeader(MOCK_OKAPI_GET_CONTRIBUTOR_NAME_HEADER);

      JsonObject types = new JsonObject();
      addServerRqRsData(HttpMethod.GET, CONTRIBUTOR_NAME_TYPES, types);

      String name = ctx.queryParam("query").get(0).split("==")[1];

      if (MOCK_INSTRUCTION_NOT_EXIST.equals(instruction)
          || (MOCK_INSTRUCTION_USE_DEFAULT.equals(instruction) && !LookupService.DEFAULT_LOOKUP_CODE.equals(name))) {
        types.put(CONTRIBUTOR_NAME_TYPES, new JsonArray())
          .put(TOTAL_RECORDS, 0);
      } else {
        types.put(CONTRIBUTOR_NAME_TYPES, new JsonArray().add(new JsonObject().put(ID, randomUUID().toString())
          .put(NAME, name)))
          .put(TOTAL_RECORDS, 1);
      }

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(types.encodePrettily());
    }

    private void handleGetExpenseClass(RoutingContext ctx) {

      logger.info("got material-type request: {}", ctx.request().query());
      JsonObject expenseClasses = new JsonObject();

      try {
        if (ctx.request().query().contains("code")) {
          expenseClasses = new JsonObject(getMockData(VALID_EXPENSE_CLASS));
        }
      } catch (IOException e) {
        expenseClasses = new JsonObject();
      }

      addServerRqRsData(HttpMethod.GET, EXPENSE_CLASS, expenseClasses);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(expenseClasses.encodePrettily());
    }

    private void handleGetAcquisitionMethods(RoutingContext ctx) {
      logger.info("got acquisition-methods request: {}", ctx.request().query());
      JsonObject acquisitionMethods = new JsonObject();

      try {
        if (ctx.request().query().contains("value")) {
          acquisitionMethods = new JsonObject(getMockData(VALID_ACQUISITION_METHOD));
        }
      } catch (IOException e) {
        acquisitionMethods = new JsonObject();
      }

      addServerRqRsData(HttpMethod.GET, ACQUISITION_METHOD, acquisitionMethods);

      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(acquisitionMethods.encodePrettily());
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

}

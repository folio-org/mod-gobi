package org.folio.rest.impl;

import org.apache.commons.io.IOUtils;
import org.drools.core.command.assertion.AssertEquals;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.GOBIResponse;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.utils.NetworkUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.mapper.ObjectMapperType;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(VertxUnitRunner.class)
public class GOBIIntegrationServiceResourceImplTest {
  private final Logger logger = LoggerFactory.getLogger(GOBIIntegrationServiceResourceImplTest.class);
  private final int okapiPort = NetworkUtils.nextFreePort();
//  private final int serverPort = NetworkUtils.nextFreePort();
  private final Header tenantHeader = new Header("X-Okapi-Tenant", "gobiintegrationserviceresourceimpltest");
  private final Header realTenantHeader = new Header ("X-Okapi-Tenant", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg");
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

    final GOBIResponse order = RestAssured
      .given()
        .header(realTenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(201)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GOBIResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed electronic monograph");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed electronic serial");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedElectronicSerialPath));

    final GOBIResponse order = RestAssured
        .given()
          .header(realTenantHeader)
          .header(contentTypeHeaderXML)
          .body(body)
        .when()
          .post(ordersPath)
        .then()
          .statusCode(201)
          .contentType(ContentType.XML)
          .extract()
            .body()
              .as(GOBIResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed electronic serial");
  }

  @Test
  public final void testPostGobiOrdersPOListedPrintMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print monograph");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonographPath));

    final GOBIResponse order = RestAssured
        .given()
          .header(realTenantHeader)
          .header(contentTypeHeaderXML)
          .body(body)
        .when()
          .post(ordersPath)
        .then()
          .statusCode(201)
          .contentType(ContentType.XML)
          .extract()
            .body()
              .as(GOBIResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  public final void testPostGobiOrdersPOListedPrintSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order listed print serial");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintSerialPath));

    final GOBIResponse order = RestAssured
        .given()
          .header(realTenantHeader)
          .header(contentTypeHeaderXML)
          .body(body)
        .when()
          .post(ordersPath)
        .then()
          .statusCode(201)
          .contentType(ContentType.XML)
          .extract()
            .body()
              .as(GOBIResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print serial");
  }

  @Test
  public final void testPostGobiOrdersPOUnlistedPrintMonograph(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print monograph");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poUnlistedPrintMonographPath));

    final GOBIResponse order = RestAssured
        .given()
          .header(realTenantHeader)
          .header(contentTypeHeaderXML)
          .body(body)
        .when()
          .post(ordersPath)
        .then()
          .statusCode(201)
          .contentType(ContentType.XML)
          .extract()
            .body()
              .as(GOBIResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order unlisted print monograph");
  }

  @Test
  public final void testPostGobiOrdersPOUnlistedPrintSerial(TestContext context) throws Exception {
    logger.info("Begin: Testing for 201 - posted order unlisted print serial");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poUnlistedPrintSerialPath));

    final GOBIResponse order = RestAssured
        .given()
          .header(realTenantHeader)
          .header(contentTypeHeaderXML)
          .body(body)
        .when()
          .post(ordersPath)
        .then()
          .statusCode(201)
          .contentType(ContentType.XML)
          .extract()
            .body()
              .as(GOBIResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order unlisted print serial");
  }

  @Test
  public final void testPostGobiOrdersPOListedElectronicMonographBadData(TestContext context) throws Exception {
    logger.info("Begin: Testing for 400 - posted order listed electronic monograph bad data (missing tag)");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedElectronicMonographBadDataPath));

    final GOBIResponse error = RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeaderXML)
        .body(body)
      .when()
        .post(ordersPath)
      .then()
        .statusCode(400)
        .contentType(ContentType.XML)
        .extract()
          .body()
            .as(GOBIResponse.class, ObjectMapperType.JAXB);

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

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonographPath));

    final GOBIResponse order = RestAssured
      .given()
      .header(realTenantHeader)
      .header(contentTypeHeaderXML)
      .body(body)
      .when()
      .post(ordersPath)
      .then()
      .statusCode(201)
      .contentType(ContentType.XML)
      .extract()
      .body()
      .as(GOBIResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(order.getPoLineNumber());

    asyncLocal.complete();

    logger.info("End: Testing for 201 - posted order listed print monograph");
  }

  @Test
  public final void testPostContentWithInvalidOkapiToken(TestContext context) throws Exception {
    logger.info("Begin: Testing for 400 - posted order listed print monograph with invalid okapi token");

    final Async asyncLocal = context.async();

    final String body = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonographPath));

    final GOBIResponse error = RestAssured
      .given()
      .header(tenantHeader)
      .header(contentTypeHeaderXML)
      .body(body)
      .when()
      .post(ordersPath)
      .then()
      .statusCode(400)
      .contentType(ContentType.XML)
      .extract()
      .body()
      .as(GOBIResponse.class, ObjectMapperType.JAXB);

    context.assertNotNull(error);
    context.assertNotNull(error.getError());
    context.assertEquals("INVALID_TOKEN", error.getError().getCode());
    context.assertNotNull(error.getError().getMessage());

    asyncLocal.complete();

    logger.info("End: Testing for 400 - posted order with invalid token");
  }

  @Test
  public final void testGetUuidWithInvalidOkapiToken(TestContext context) throws IllegalArgumentException {
    logger.info("Begin: Testing for IllegalArgumentException to be thrown when calling getUuid with NULL or empty okapi token");

      GOBIIntegrationServiceResourceImpl impl = new  GOBIIntegrationServiceResourceImpl();
      String okapiToken = null;
      String expectedMessage = "x-okapi-tenant is NULL or empty";

      try{
        impl.getUuid(okapiToken);
        fail("Expected IllegalArgumentException to be thrown");
      }
      catch (IllegalArgumentException e){
        assertEquals(expectedMessage, e.getMessage());
      }

      okapiToken =  "";
      try{
        impl.getUuid(okapiToken);
        fail("Expected IllegalArgumentException to be thrown");
      }
      catch (IllegalArgumentException e){
        assertEquals(expectedMessage, e.getMessage());
      }
  }

  @Test
  public final void testGetUuidWithValidOkapiTokenMissingContentPart(TestContext context) throws IllegalArgumentException {
    logger.info("Begin: Testing for IllegalArgumentException to be thrown when calling getUuid with invalid okapi token");

    GOBIIntegrationServiceResourceImpl impl = new  GOBIIntegrationServiceResourceImpl();
    String okapiToken = "eyJhbGciOiJIUzUxMiJ9.";
    String expectedMessage = "user_id is not found in x-okapi-tenant";

    try{
      impl.getUuid(okapiToken);
      fail("Expected IllegalArgumentException to be thrown");
    }
    catch (IllegalArgumentException e){
      assertEquals(expectedMessage, e.getMessage());
    }

    okapiToken = "eyJhbGciOiJIUzUxMiJ9";
    try{
      impl.getUuid(okapiToken);
      fail("Expected IllegalArgumentException to be thrown");
    }
    catch (IllegalArgumentException e){
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public final void testGetUuidWithValidOkapiTokenMissingUuid(TestContext context) throws IllegalArgumentException {
    logger.info("Begin: Testing for IllegalArgumentException to be thrown when calling getUuid with okapi token missing UUID");

    GOBIIntegrationServiceResourceImpl impl = new  GOBIIntegrationServiceResourceImpl();
    String expectedMessage = "user_id is not found in x-okapi-tenant";

    //Missing UUID
    String okapiToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRlbmFudCI6ImZzMDAwMDAwMDAifQ.dpljk7LAzgM_a1fD0jAqVUE4HhxKKeXmE2lrTmyf-HOxUyPf2Byj0OIN2fn3eUdQnt1_ABZTTxafceyt7Rj3mg";

    try{
      impl.getUuid(okapiToken);
      fail("Expected IllegalArgumentException to be thrown");
    }
    catch (IllegalArgumentException e){
      assertEquals(expectedMessage, e.getMessage());
    }

    //empty UUID
    okapiToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.PabbXTw5TqrrOxeKOEac5WkmmAOL4f8UoWKPCqCINvmuZCLLC0197CfVq0CBv2MjSwxU-3nf_TkwhM4mVmHnyA";

    try{
      impl.getUuid(okapiToken);
      fail("Expected IllegalArgumentException to be thrown");
    }
    catch (IllegalArgumentException e){
      assertEquals(expectedMessage, e.getMessage());
    }

    //empty Payload
    okapiToken = "eyJhbGciOiJIUzUxMiJ9.e30.ToOwht_WTL7ib-z-u0Bg4UmSIZ8qOsTCnX7IhPMbQghCGBzCJMzfu_w9VZPzA9JOk1g2GnH0_ujnhMorxK2LJw";

    try{
      impl.getUuid(okapiToken);
      fail("Expected IllegalArgumentException to be thrown");
    }
    catch (IllegalArgumentException e){
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public final void testGetUuidWithValidOkapiToken(TestContext context) throws IllegalArgumentException {
    logger.info("Begin: Testing for valid UUID from valid OkapiToken");

    GOBIIntegrationServiceResourceImpl impl = new  GOBIIntegrationServiceResourceImpl();
    String expectedMessage = "user_id is not found in x-okapi-tenant";

    String okapiToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg";

    try{
      String uuid = impl.getUuid(okapiToken);
      assertEquals("ef676db9-d231-479a-aa91-f65eb4b17872", uuid);
    }
    catch (IllegalArgumentException e){
      fail("IllegalArgumentException was not expected to be thrown");
    }
  }
}

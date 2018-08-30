package org.folio.rest.impl;

import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_BAD_REQUEST;
import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_TOKEN;
import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_XML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.concurrent.CompletionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.gobi.exceptions.HttpException;
import org.folio.gobi.exceptions.InvalidTokenException;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.tools.utils.BinaryOutStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class PostGobiOrdersHelperTest {

  private static final Logger logger = LoggerFactory.getLogger(PostGobiOrdersHelperTest.class);

  private Unmarshaller jaxbUnmarshaller;

  @Before
  public void setUp(TestContext context) throws Exception {
    jaxbUnmarshaller = JAXBContext.newInstance(GobiResponse.class).createUnmarshaller();
  }

  @After
  public void tearDown(TestContext context) throws Exception {

  }

  @Test
  public void testHandleErrorHttpClientBadRequest(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException bad request");
    Async async = context.async();

    Throwable t = new Throwable("invalid foo");

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = new Handler<AsyncResult<javax.ws.rs.core.Response>>() {
      public void handle(AsyncResult<javax.ws.rs.core.Response> response) {
        context.assertEquals(400, response.result().getStatus());

        try {
          String body = new String(((BinaryOutStream) response.result().getEntity()).getData());
          GobiResponse gobiResp = (GobiResponse) jaxbUnmarshaller.unmarshal(new StringReader(body));

          context.assertEquals(CODE_BAD_REQUEST, gobiResp.getError().getCode());
          context.assertEquals(t.toString(), gobiResp.getError().getMessage());
        } catch (JAXBException e) {
          context.fail(e.getMessage());
        }

        async.complete();
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new HttpException(400, t)));
  }

  public void testHandleErrorGobiPurchaseOrderParserException(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException bad request");
    Async async = context.async();

    String msg = "invalid gobi request xml";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = new Handler<AsyncResult<javax.ws.rs.core.Response>>() {
      public void handle(AsyncResult<javax.ws.rs.core.Response> response) {
        context.assertEquals(400, response.result().getStatus());

        try {
          String body = new String(((BinaryOutStream) response.result().getEntity()).getData());
          GobiResponse gobiResp = (GobiResponse) jaxbUnmarshaller.unmarshal(new StringReader(body));

          context.assertEquals(CODE_INVALID_XML, gobiResp.getError().getCode());
          context.assertEquals(msg, gobiResp.getError().getMessage());
        } catch (JAXBException e) {
          context.fail(e.getMessage());
        }

        async.complete();
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new GobiPurchaseOrderParserException(msg)));
  }

  public void testHandleErrorInvalidTokenException(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException bad request");
    Async async = context.async();

    String msg = "invalid token!";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = new Handler<AsyncResult<javax.ws.rs.core.Response>>() {
      public void handle(AsyncResult<javax.ws.rs.core.Response> response) {
        context.assertEquals(400, response.result().getStatus());

        try {
          String body = new String(((BinaryOutStream) response.result().getEntity()).getData());
          GobiResponse gobiResp = (GobiResponse) jaxbUnmarshaller.unmarshal(new StringReader(body));

          context.assertEquals(CODE_INVALID_TOKEN, gobiResp.getError().getCode());
          context.assertEquals(msg, gobiResp.getError().getMessage());
        } catch (JAXBException e) {
          context.fail(e.getMessage());
        }

        async.complete();
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new InvalidTokenException(msg)));
  }

  @Test
  public void testHandleErrorHttpClientUnauthorized(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException 401");
    Async async = context.async();

    String msg = "requires permission foo.bar.get";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = new Handler<AsyncResult<javax.ws.rs.core.Response>>() {
      public void handle(AsyncResult<javax.ws.rs.core.Response> response) {
        context.assertEquals(401, response.result().getStatus());
        context.assertEquals(msg, (String) response.result().getEntity());
        async.complete();
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new HttpException(401, msg)));
  }

  @Test
  public void testHandleErrorHttpClientInternalServerError(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException 500");
    Async async = context.async();

    String msg = "you zigged when you should have zagged";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = new Handler<AsyncResult<javax.ws.rs.core.Response>>() {
      public void handle(AsyncResult<javax.ws.rs.core.Response> response) {
        context.assertEquals(500, response.result().getStatus());
        context.assertEquals(msg, (String) response.result().getEntity());
        async.complete();
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new HttpException(500, msg)));
  }

  @Test
  public void testHandleErrorHttpClientNotImplemented(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException 501");
    Async async = context.async();

    String msg = "not implemented";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = new Handler<AsyncResult<javax.ws.rs.core.Response>>() {
      public void handle(AsyncResult<javax.ws.rs.core.Response> response) {
        context.assertEquals(500, response.result().getStatus());
        context.assertEquals(msg, (String) response.result().getEntity());
        async.complete();
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new HttpException(501, msg)));
  }

  @Test
  public void testHandleErrorGenericThrowable(TestContext context) {
    logger.info("Begin: Testing handleError on generic Throwable");
    Async async = context.async();

    Throwable expected = new Throwable("whoops!");

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = new Handler<AsyncResult<javax.ws.rs.core.Response>>() {
      public void handle(AsyncResult<javax.ws.rs.core.Response> response) {
        context.assertEquals(500, response.result().getStatus());
        context.assertEquals(expected.getMessage(), (String) response.result().getEntity());
        async.complete();
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(expected);
  }

  @Test
  public final void testGetUuidWithInvalidOkapiToken(TestContext context) throws InvalidTokenException {
    logger.info("Begin: Testing for InvalidTokenException to be thrown on NULL or empty okapi token");

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
    logger.info("Begin: Testing for InvalidTokenException to be thrown on invalid okapi token");

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
      .info("Begin: Testing for InvalidTokenException to be thrown on okapi token missing UUID");

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
    logger.info("Begin: Testing for valid UUID from valid okapi token");

    String okapiToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJfaWQiOiJlZjY3NmRiOS1kMjMxLTQ3OWEtYWE5MS1mNjVlYjRiMTc4NzIiLCJ0ZW5hbnQiOiJmczAwMDAwMDAwIn0.KC0RbgafcMmR5Mc3-I7a6SQPKeDSr0SkJlLMcqQz3nwI0lwPTlxw0wJgidxDq-qjCR0wurFRn5ugd9_SVadSxg";

    try {
      String uuid = PostGobiOrdersHelper.getUuid(okapiToken);
      assertEquals("ef676db9-d231-479a-aa91-f65eb4b17872", uuid);
    } catch (InvalidTokenException e) {
      fail("InvalidTokenException was not expected to be thrown");
    }
  }

  @Test
  public void testExtractVendorUid(){

    logger.info("Begin: Testing for extracting valid Uid when only 1 vendor is in response");

    String vendorId = "9db7d395-a664-4c56-8e2f-f015413bc783";
    JsonArray vendorArray = new JsonArray();
    JsonObject vendor = new JsonObject();
    vendor.put("id", vendorId);
    vendor.put("name", "GOBI");
    vendor.put("code", "EBR");
    vendor.put("description", "This is Yankee Book Peddler");
    vendor.put("vendor_status", "Active");

    vendorArray.add(vendor);
    JsonObject vendorResponse = new JsonObject();

    vendorResponse.put("vendors", vendorArray );

     PostGobiOrdersHelper gobiOrdersHelper = new PostGobiOrdersHelper(null, null, null);
     String extracteVendorId = gobiOrdersHelper.extractVendorId(vendorResponse);
     assertEquals(vendorId, extracteVendorId );

  }

  @Test
  public void testExtractVendorUidWithMultipleVendors(){

    logger.info("Begin: Testing for extracting valid Uid when multiple vendors are returned, expecting it to grab the first one");

    String vendorId = "9db7d395-a664-4c56-8e2f-f015413bc783";
    JsonArray vendorArray = new JsonArray();

    for (int i = 0; i< 2; i++){
      JsonObject vendor = new JsonObject();
      vendor.put("id", vendorId + i);
      vendor.put("name", "GOBI" + i);
      vendor.put("code", "EBR" + i);
      vendor.put("description", "This is Yankee Book Peddler" + i);
      vendor.put("vendor_status", i % 2 == 0? "Active" : "Inactive");

      vendorArray.add(vendor);
    }

    JsonObject vendorResponse = new JsonObject();

    vendorResponse.put("vendors", vendorArray );

    PostGobiOrdersHelper gobiOrdersHelper = new PostGobiOrdersHelper(null, null, null);
    String extracteVendorId = gobiOrdersHelper.extractVendorId(vendorResponse);
    assertEquals(vendorId + "0", extracteVendorId );

  }
}

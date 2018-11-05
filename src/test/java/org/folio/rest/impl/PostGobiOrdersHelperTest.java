package org.folio.rest.impl;

import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_BAD_REQUEST;
import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_TOKEN;
import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_XML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.folio.gobi.DataSource;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.gobi.exceptions.HttpException;
import org.folio.gobi.exceptions.InvalidTokenException;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMappings;
import org.folio.rest.tools.utils.BinaryOutStream;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
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
  public final void testGetOrderType(TestContext context) throws Exception {
    logger.info("Begin: Testing for valid order type in the GOBI order XML");

    String[] orderFiles = {
        "GOBIIntegrationServiceResourceImpl/po_listed_electronic_monograph.xml",
        "GOBIIntegrationServiceResourceImpl/po_listed_electronic_serial.xml",
        "GOBIIntegrationServiceResourceImpl/po_listed_print_monograph.xml",
        "GOBIIntegrationServiceResourceImpl/po_listed_print_serial.xml",
        "GOBIIntegrationServiceResourceImpl/po_unlisted_print_monograph.xml",
        "GOBIIntegrationServiceResourceImpl/po_unlisted_print_serial.xml"
    };
    OrderMappings.OrderType[] expected = {
        OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH,
        OrderMappings.OrderType.LISTED_ELECTRONIC_SERIAL,
        OrderMappings.OrderType.LISTED_PRINT_MONOGRAPH,
        OrderMappings.OrderType.LISTED_PRINT_SERIAL,
        OrderMappings.OrderType.UNLISTED_PRINT_MONOGRAPH,
        OrderMappings.OrderType.UNLISTED_PRINT_SERIAL
    };

    for (int i = 0; i < orderFiles.length; i++) {
      InputStream data = this.getClass().getClassLoader().getResourceAsStream(orderFiles[i]);
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
      OrderMappings.OrderType orderType = PostGobiOrdersHelper.getOrderType(doc);
      assertEquals(expected[i], orderType);
    }

    String orderFile = "GOBIIntegrationServiceResourceImpl/po_unknown_order_type.xml";
    try {
      InputStream data = this.getClass().getClassLoader().getResourceAsStream(orderFile);
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
      PostGobiOrdersHelper.getOrderType(doc);
      fail("Expected IllegalArgumentException to be thrown for unknown order type");
    } catch (IllegalArgumentException e) {
      logger.info("Got expected IllegalArgumentException for unknown order type");
    }
  }

  @Test
  public final void testLookupOrderMappings(TestContext context) throws Exception {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals("/configurations/entries")) {
        req.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json")
          .sendFile("ConfigData/success.json");
      } else {
        req.response().setStatusCode(500).end("Unexpected call: " + req.path());
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");
      PostGobiOrdersHelper pgoh = new PostGobiOrdersHelper(
          GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), null, okapiHeaders,
          vertx.getOrCreateContext());
      pgoh.lookupOrderMappings(OrderMappings.OrderType.fromValue("ListedElectronicMonograph"))
        .thenAccept(map -> {
          context.assertNotNull(map);
          context.assertNotNull(map.get(Mapping.Field.CURRENCY));
          DataSource ds = map.get(Mapping.Field.CURRENCY);
          context.assertEquals("//ListPrice/Currency", ds.from);
          context.assertEquals("USD", ds.defValue);

          context.assertNotNull(map.get(Mapping.Field.LIST_PRICE));
          ds = map.get(Mapping.Field.LIST_PRICE);
          context.assertEquals("//ListPrice/Amount", ds.from);
          context.assertEquals("0", ds.defValue);
          try {
            Double result = (Double) ds.translation.apply(ds.defValue.toString()).get();
            context.assertEquals(0.0, result);
          } catch (Exception e) {
            logger.error("Failed to execute translation LIST_PRICE", e);
          }

          context.assertNotNull((map.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE)));
          ds = map.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE);
          context.assertEquals("//NetPrice/Amount", ds.from);
          context.assertNotNull(ds.defValue);
          DataSource defVal = (DataSource) ds.defValue;
          context.assertEquals("//ListPrice/Amount//EstPrice", defVal.from);
          context.assertEquals("15.0", defVal.defValue);
          try {
            Double result = (Double) defVal.translation.apply(defVal.defValue.toString()).get();
            context.assertEquals(15.0, result);
          } catch (Exception e) {
            logger.error("Failed to execute translation for ESTIMATED_ PRICE with recursive default mapping", e);
          }

          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  
 
  @Test
  public final void testLookupPaymentStatusId(TestContext context) throws Exception {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals("/payment_status")) {
        req.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json")
          .sendFile("PostGobiOrdersHelper/payment_statuses.json");
      } else {
        req.response().setStatusCode(500).end("Unexpected call: " + req.path());
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");
      PostGobiOrdersHelper pgoh = new PostGobiOrdersHelper(
          GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), null, okapiHeaders,
          vertx.getOrCreateContext());
      pgoh.lookupPaymentStatusId("AP")
        .thenAccept(id -> {
          context.assertNotNull(id);
          context.assertEquals("37ea6927-bc92-485a-b748-288b50660e02", id);

          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

}

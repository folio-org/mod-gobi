package org.folio.rest.impl;

import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_BAD_REQUEST;
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

import org.folio.gobi.DataSourceResolver;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMappings;
import org.folio.rest.tools.utils.BinaryOutStream;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

  private static final Logger logger = LogManager.getLogger(PostGobiOrdersHelperTest.class);

  private Unmarshaller jaxbUnmarshaller;

  @Before
  public void setUp() throws Exception {
    jaxbUnmarshaller = JAXBContext.newInstance(GobiResponse.class).createUnmarshaller();
  }

  @Test
  public void testHandleErrorHttpClientBadRequest(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException bad request");
    Async async = context.async();

    Throwable t = new Throwable("invalid foo");

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
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
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new HttpException(400, t)));
  }

  public void testHandleErrorGobiPurchaseOrderParserException(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException bad request");
    Async async = context.async();

    String msg = "invalid gobi request xml";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
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
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new GobiPurchaseOrderParserException(msg)));
  }


  @Test
  public void testHandleErrorHttpClientUnauthorized(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException 401");
    Async async = context.async();

    String msg = "requires permission foo.bar.get";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      context.assertEquals(401, response.result().getStatus());
      context.assertEquals(msg, response.result().getEntity());
      async.complete();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new HttpException(401, msg)));
  }

  @Test
  public void testHandleErrorHttpClientInternalServerError(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException 500");
    Async async = context.async();

    String msg = "you zigged when you should have zagged";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      context.assertEquals(500, response.result().getStatus());
      context.assertEquals(msg, response.result().getEntity());
      async.complete();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new HttpException(500, msg)));
  }

  @Test
  public void testHandleErrorHttpClientNotImplemented(TestContext context) {
    logger.info("Begin: Testing handleError on HttpException 501");
    Async async = context.async();

    String msg = "not implemented";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      context.assertEquals(500, response.result().getStatus());
      context.assertEquals(msg, response.result().getEntity());
      async.complete();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(new CompletionException(new HttpException(501, msg)));
  }

  @Test
  public void testHandleErrorGenericThrowable(TestContext context) {
    logger.info("Begin: Testing handleError on generic Throwable");
    Async async = context.async();

    Throwable expected = new Throwable("whoops!");

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      context.assertEquals(500, response.result().getStatus());
      context.assertEquals(expected.getMessage(), response.result().getEntity());
      async.complete();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(null, asyncResultHandler, null, null);
    helper.handleError(expected);
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
  public final void testLookupOrderMappings(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(PostGobiOrdersHelper.CONFIGURATION_ENDPOINT)) {
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
          DataSourceResolver ds = map.get(Mapping.Field.CURRENCY);
          context.assertEquals("//ListPrice/Currency", ds.from);
          context.assertEquals("USD", ds.defValue);

          context.assertNotNull(map.get(Mapping.Field.LIST_UNIT_PRICE_ELECTRONIC));
          ds = map.get(Mapping.Field.LIST_UNIT_PRICE_ELECTRONIC);
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
          DataSourceResolver defVal = (DataSourceResolver) ds.defValue;
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
  public final void testLookupDefaultOrderMappings(TestContext context) {

    logger.info("Begin: Testing for Order Mappings to fetch default mappings if configuration Call fails");
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(PostGobiOrdersHelper.CONFIGURATION_ENDPOINT)) {
        req.response().setStatusCode(500).end("Unrecheable End point: " + req.path());
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
          DataSourceResolver ds = map.get(Mapping.Field.CURRENCY);
          context.assertEquals("//ListPrice/Currency", ds.from);
          context.assertEquals("USD", ds.defValue);

          context.assertNotNull(map.get(Mapping.Field.LIST_UNIT_PRICE_ELECTRONIC));
          ds = map.get(Mapping.Field.LIST_UNIT_PRICE_ELECTRONIC);
          context.assertEquals("//ListPrice/Amount", ds.from);
          context.assertEquals("0", ds.defValue);
          try {
            Double result = (Double) ds.translation.apply(ds.defValue.toString()).get();
            context.assertEquals(0.0, result);
          } catch (Exception e) {
            logger.error("Failed to execute translation LIST_PRICE", e);
          }

          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testLookupFirstMaterialTypes(TestContext context) {

    logger.info("Begin: Testing if all material type calls return empty, must use first in the list");
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(PostGobiOrdersHelper.MATERIAL_TYPES_ENDPOINT) && req.query().contains("*")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_materialType.json");
      } else {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/empty_materialType.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testDefaultMaterialTypes");
      PostGobiOrdersHelper pgoh = new PostGobiOrdersHelper(
          GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), null, okapiHeaders,
          vertx.getOrCreateContext());
      pgoh.lookupMaterialTypeId("unspecified")
        .thenAccept(list -> {
          context.assertNotNull(list);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testSuccessMapLookupExpenseClassId(TestContext context) {

    logger.info("Begin: Testing if all material type calls return empty, must use first in the list");
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(PostGobiOrdersHelper.EXPENSE_CLASS_ENDPOINT) && req.query().contains("Elec")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_expenseClasses.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_expenseClasses.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testDefaultExpenseClasses");
      PostGobiOrdersHelper pgoh = new PostGobiOrdersHelper(
        GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), null, okapiHeaders,
        vertx.getOrCreateContext());
      pgoh.lookupExpenseClassId("Elec")
        .thenAccept(list -> {
          context.assertNotNull(list);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testShouldReturnNullExpenseClassIdIfExpenseClassNotFound(TestContext context) {

    logger.info("Begin: Testing if all material type calls return empty, must use first in the list");
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(PostGobiOrdersHelper.EXPENSE_CLASS_ENDPOINT) && req.query().contains("Elec")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_expenseClasses.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_expenseClasses.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testDefaultExpenseClasses");
      PostGobiOrdersHelper pgoh = new PostGobiOrdersHelper(
        GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), null, okapiHeaders,
        vertx.getOrCreateContext());
      pgoh.lookupExpenseClassId("Prn")
        .thenAccept(list -> {
          context.assertNull(list);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

}

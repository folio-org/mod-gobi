package org.folio.rest.impl;

import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_BAD_REQUEST;
import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_XML;
import static org.folio.rest.utils.TestUtils.checkVertxContextCompletion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.InputStream;
import java.io.StringReader;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.DataSourceResolver;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.ResourcePaths;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.jaxrs.model.Errors;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.tools.utils.BinaryOutStream;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Document;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;


@ExtendWith(VertxExtension.class)
public class PostGobiOrdersHelperTest {

  private static final Logger logger = LogManager.getLogger(PostGobiOrdersHelperTest.class);
  Map<String, String> okapiHeaders = new HashMap<>();

  private Unmarshaller jaxbUnmarshaller;

  @BeforeEach
  public void setUp() throws Exception {
    jaxbUnmarshaller = JAXBContext.newInstance(GobiResponse.class).createUnmarshaller();
  }

  @Test
  void testHandleErrorHttpClientBadRequest(VertxTestContext context) throws Throwable {
    logger.info("Begin: Testing handleError on HttpException bad request");

    Throwable t = new Throwable("invalid foo");

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(400, response.result().getStatus());

      try {
        String body = new String(((BinaryOutStream) response.result().getEntity()).getData());
        GobiResponse gobiResp = (GobiResponse) jaxbUnmarshaller.unmarshal(new StringReader(body));

        assertEquals(CODE_BAD_REQUEST, gobiResp.getError().getCode());
        assertEquals(t.toString(), gobiResp.getError().getMessage());
        context.completeNow();
      } catch (JAXBException e) {
        fail(e.getMessage());
      }

    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(new HttpException(400, t, new Errors())));
    checkVertxContextCompletion(context);
  }

  @Test
  void testHandleErrorGobiPurchaseOrderParserException(VertxTestContext context) throws Throwable {
    logger.info("Begin: Testing handleError on HttpException bad request");

    String msg = "invalid gobi request xml";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(400, response.result().getStatus());

      try {
        String body = new String(((BinaryOutStream) response.result().getEntity()).getData());
        GobiResponse gobiResp = (GobiResponse) jaxbUnmarshaller.unmarshal(new StringReader(body));

        assertEquals(CODE_INVALID_XML, gobiResp.getError().getCode());
        assertEquals(msg, gobiResp.getError().getMessage());
        context.completeNow();
      } catch (JAXBException e) {
        fail(e.getMessage());
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(new GobiPurchaseOrderParserException(msg)));
    checkVertxContextCompletion(context);
  }


  @Test
  void testHandleErrorHttpClientUnauthorized(VertxTestContext context) throws Throwable {
    logger.info("Begin: Testing handleError on HttpException 401");

    String msg = "requires permission foo.bar.get";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(401, response.result().getStatus());
      assertEquals(msg, response.result().getEntity());
      context.completeNow();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(new HttpException(401, msg)));
    checkVertxContextCompletion(context);

  }

  @Test
  void testHandleErrorHttpClientInternalServerError(VertxTestContext context) throws Throwable {
    logger.info("Begin: Testing handleError on HttpException 500");

    String msg = "you zigged when you should have zagged";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(500, response.result().getStatus());
      assertEquals(msg, response.result().getEntity());
      context.completeNow();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(new HttpException(500, msg)));
    checkVertxContextCompletion(context);
  }

  @Test
  void testHandleErrorHttpClientNotImplemented(VertxTestContext context) throws Throwable {
    logger.info("Begin: Testing handleError on HttpException 501");

    String msg = "not implemented";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      Assertions.assertEquals(500, response.result().getStatus());
      Assertions.assertEquals(msg, response.result().getEntity());
      context.completeNow();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(new HttpException(501, msg)));
    checkVertxContextCompletion(context);
  }

  @Test
  void testHandleErrorGenericThrowable(VertxTestContext context) {
    logger.info("Begin: Testing handleError on generic Throwable");

    Throwable expected = new Throwable("whoops!");

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(500, response.result().getStatus());
      assertEquals(expected.getMessage(), response.result().getEntity());
      context.completeNow();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(expected);

  }

  @Test
  void testGetOrderType() throws Exception {
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
    InputStream data = PostGobiOrdersHelperTest.class.getClassLoader().getResourceAsStream(orderFile);
    Document doc = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder()
      .parse(data);

    Assertions.assertThrows(IllegalArgumentException.class, () -> PostGobiOrdersHelper.getOrderType(doc));
    logger.info("Got expected IllegalArgumentException for unknown order type");

  }

  @Test
  public final void testLookupOrderMappings(VertxTestContext context) {
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.CONFIGURATION_ENDPOINT)) {
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
      assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");
      PostGobiOrdersHelper pgoh = new PostGobiOrdersHelper(null, okapiHeaders, vertx.getOrCreateContext());
      pgoh.lookupOrderMappings(OrderMappings.OrderType.fromValue("ListedElectronicMonograph"))
        .thenAccept(map -> {
          assertNotNull(map);
          assertNotNull(map.get(Mapping.Field.CURRENCY));
          DataSourceResolver ds = map.get(Mapping.Field.CURRENCY);
          assertEquals("//ListPrice/Currency", ds.from);
          assertEquals("USD", ds.defValue);

          assertNotNull(map.get(Mapping.Field.LIST_UNIT_PRICE_ELECTRONIC));
          ds = map.get(Mapping.Field.LIST_UNIT_PRICE_ELECTRONIC);
          assertEquals("//ListPrice/Amount", ds.from);
          assertEquals("0", ds.defValue);
          try {
            Double result = (Double) ds.applyTranslation(ds.defValue).get();
            assertEquals(0.0, result);
          } catch (Exception e) {
            logger.error("Failed to execute translation LIST_PRICE", e);
          }

          assertNotNull((map.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE)));
          ds = map.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE);
          assertEquals("//NetPrice/Amount", ds.from);
          assertNotNull(ds.defValue);
          DataSourceResolver defVal = (DataSourceResolver) ds.defValue;
          assertEquals("//ListPrice/Amount//EstPrice", defVal.from);
          assertEquals("15.0", defVal.defValue);
          try {
            Double result = (Double) defVal.applyTranslation(defVal.defValue).get();
            assertEquals(15.0, result);
          } catch (Exception e) {
            logger.error("Failed to execute translation for ESTIMATED_ PRICE with recursive default mapping", e);
          }

          vertx.close(context.succeedingThenComplete());
        });
    });
  }

  @Test
  public final void testLookupDefaultOrderMappingsFailed() {
    var headers = Map.of("X-Okapi-Url", "http://invalid-host");
    PostGobiOrdersHelper pgoh = new PostGobiOrdersHelper(null, headers, Vertx.vertx().getOrCreateContext());

    logger.info("Begin: Testing for Order Mappings to fetch default mappings if configuration Call fails");

    var future = pgoh.lookupOrderMappings(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH);
    var exception = Assertions.assertThrows(CompletionException.class, future::join);

    assertEquals(UnknownHostException.class, exception.getCause().getClass());
  }
}

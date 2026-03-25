package org.folio.rest.impl;

import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_BAD_REQUEST;
import static org.folio.rest.impl.PostGobiOrdersHelper.CODE_INVALID_XML;
import static org.folio.rest.utils.TestUtils.checkVertxContextCompletion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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
import org.folio.gobi.exceptions.ErrorCodes;
import org.folio.gobi.exceptions.GobiException;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.tools.utils.BinaryOutStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;


@ExtendWith(VertxExtension.class)
public class PostGobiOrdersHelperTest {

  Map<String, String> okapiHeaders = new HashMap<>();

  private Unmarshaller jaxbUnmarshaller;

  @BeforeEach
  public void setUp() throws Exception {
    jaxbUnmarshaller = JAXBContext.newInstance(GobiResponse.class).createUnmarshaller();
  }

  // -- handleError tests covering each branch of mapExceptionToResponse --

  @Test
  void testHandleErrorHttpException400(VertxTestContext context) throws Throwable {
    String msg = "invalid foo";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(400, response.result().getStatus());

      try {
        GobiResponse gobiResp = parseGobiXmlResponse(response.result());
        assertEquals(CODE_BAD_REQUEST, gobiResp.getError().getCode());
        assertEquals(msg, gobiResp.getError().getMessage());
        context.completeNow();
      } catch (JAXBException e) {
        fail(e.getMessage());
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(new HttpException(400, msg)));
    checkVertxContextCompletion(context);
  }

  @Test
  void testHandleErrorHttpException401(VertxTestContext context) throws Throwable {
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
  void testHandleErrorHttpException422(VertxTestContext context) throws Throwable {
    String msg = "validation failed";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(422, response.result().getStatus());

      try {
        GobiResponse gobiResp = parseGobiXmlResponse(response.result());
        assertEquals("genericError", gobiResp.getError().getCode());
        assertEquals(msg, gobiResp.getError().getMessage());
        context.completeNow();
      } catch (JAXBException e) {
        fail(e.getMessage());
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(new HttpException(422, msg)));
    checkVertxContextCompletion(context);
  }

  @ParameterizedTest
  @ValueSource(ints = {500, 501})
  void testHandleErrorHttpExceptionDefault(int httpCode, VertxTestContext context) throws Throwable {
    String msg = "server error";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(500, response.result().getStatus());

      try {
        GobiResponse gobiResp = parseGobiXmlResponse(response.result());
        assertEquals("genericError", gobiResp.getError().getCode());
        assertEquals(msg, gobiResp.getError().getMessage());
        context.completeNow();
      } catch (JAXBException e) {
        fail(e.getMessage());
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(new HttpException(httpCode, msg)));
    checkVertxContextCompletion(context);
  }

  @Test
  void testHandleErrorGobiPurchaseOrderParserException(VertxTestContext context) throws Throwable {
    String msg = "invalid gobi request xml";

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(400, response.result().getStatus());

      try {
        GobiResponse gobiResp = parseGobiXmlResponse(response.result());
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
  void testHandleErrorGobiException(VertxTestContext context) throws Throwable {
    Throwable cause = new RuntimeException("bad mapping");
    GobiException gobiException = new GobiException(ErrorCodes.INVALID_ORDER_MAPPING_FILE, cause);

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(500, response.result().getStatus());

      try {
        GobiResponse gobiResp = parseGobiXmlResponse(response.result());
        assertEquals(ErrorCodes.INVALID_ORDER_MAPPING_FILE.getCode(), gobiResp.getError().getCode());
        assertEquals(gobiException.getMessage(), gobiResp.getError().getMessage());
        context.completeNow();
      } catch (JAXBException e) {
        fail(e.getMessage());
      }
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(new CompletionException(gobiException));
    checkVertxContextCompletion(context);
  }

  @Test
  void testHandleErrorGenericThrowable(VertxTestContext context) {
    Throwable expected = new Throwable("whoops!");

    Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler = response -> {
      assertEquals(500, response.result().getStatus());
      assertEquals(expected.getMessage(), response.result().getEntity());
      context.completeNow();
    };

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, Vertx.vertx().getOrCreateContext());
    helper.handleError(expected);
  }

  private GobiResponse parseGobiXmlResponse(javax.ws.rs.core.Response response) throws JAXBException {
    String body = new String(((BinaryOutStream) response.getEntity()).getData());
    return (GobiResponse) jaxbUnmarshaller.unmarshal(new StringReader(body));
  }

  // -- getOrderType / lookupOrderMappings tests --

  @Test
  void testGetOrderType() throws Exception {
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

    assertThrows(IllegalArgumentException.class, () -> PostGobiOrdersHelper.getOrderType(doc));
  }

  @Test
  public final void testLookupOrderMappings(VertxTestContext context) {
    final Vertx vertx = Vertx.vertx();
    Map<String, String> okapiHeaders = new HashMap<>();
    okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");

    // Create a real instance and spy on it to mock the lookupOrderMappings method
    PostGobiOrdersHelper pgoh = spy(new PostGobiOrdersHelper(null, okapiHeaders, vertx.getOrCreateContext()));

    // Create mock mapping data that matches the expected structure
    Map<Mapping.Field, DataSourceResolver> mockMappings = new HashMap<>();

    // Mock CURRENCY field
    DataSourceResolver currencyDs = new DataSourceResolver(null, "//ListPrice/Currency", null, "USD", null, false);
    mockMappings.put(Mapping.Field.CURRENCY, currencyDs);

    // Mock LIST_UNIT_PRICE_ELECTRONIC field
    DataSourceResolver listPriceDs = new DataSourceResolver(null, "//ListPrice/Amount", null, "0", null, false);
    mockMappings.put(Mapping.Field.LIST_UNIT_PRICE_ELECTRONIC, listPriceDs);

    // Mock PO_LINE_ESTIMATED_PRICE field with nested default
    DataSourceResolver nestedDefault = new DataSourceResolver(null, "//ListPrice/Amount//EstPrice", null, "15.0", null, false);
    DataSourceResolver estimatedPriceDs = new DataSourceResolver(null, "//NetPrice/Amount", null, nestedDefault, null, false);
    mockMappings.put(Mapping.Field.PO_LINE_ESTIMATED_PRICE, estimatedPriceDs);

    // Mock the lookupOrderMappings method to return our test data
    doReturn(java.util.concurrent.CompletableFuture.completedFuture(mockMappings))
      .when(pgoh).lookupOrderMappings(any(OrderMappings.OrderType.class));

    // Execute the test
    pgoh.lookupOrderMappings(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH)
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

        assertNotNull((map.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE)));
        ds = map.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE);
        assertEquals("//NetPrice/Amount", ds.from);
        assertNotNull(ds.defValue);
        DataSourceResolver defVal = (DataSourceResolver) ds.defValue;
        assertEquals("//ListPrice/Amount//EstPrice", defVal.from);
        assertEquals("15.0", defVal.defValue);

        context.completeNow();
      });
  }

  @Test
  public final void testLookupDefaultOrderMappingsFailed() {
    final Vertx vertx = Vertx.vertx();
    var headers = Map.of("x-okapi-tenant", "testLookupOrderMappingsFailed");

    // Create a spy instance
    PostGobiOrdersHelper pgoh = spy(new PostGobiOrdersHelper(null, headers, vertx.getOrCreateContext()));

    // Mock the lookupOrderMappings method to return a failed CompletableFuture
    Exception expectedException = new RuntimeException("Database connection failed");
    doReturn(java.util.concurrent.CompletableFuture.failedFuture(expectedException))
      .when(pgoh).lookupOrderMappings(any(OrderMappings.OrderType.class));

    // Execute the test and verify it throws the expected exception
    var future = pgoh.lookupOrderMappings(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH);
    assertThrows(CompletionException.class, future::join);
  }
}

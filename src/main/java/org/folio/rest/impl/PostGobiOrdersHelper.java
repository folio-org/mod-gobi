package org.folio.rest.impl;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.folio.gobi.LookupService.FIRST_ELEM;
import static org.folio.gobi.LookupService.QUERY;
import static org.folio.rest.ResourcePaths.ORDERS_BY_ID_ENDPOINT;
import static org.folio.rest.ResourcePaths.ORDERS_ENDPOINT;
import static org.folio.rest.jaxrs.resource.Gobi.PostGobiOrdersResponse.respond400WithApplicationXml;
import static org.folio.rest.jaxrs.resource.Gobi.PostGobiOrdersResponse.respond500WithApplicationXml;
import static org.folio.rest.jaxrs.resource.GobiOrdersCustomMappings.GetGobiOrdersCustomMappingsResponse.respond401WithTextPlain;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.dao.OrderMappingsDao;
import org.folio.dao.OrderMappingsDaoImpl;
import org.folio.gobi.DataSourceResolver;
import org.folio.rest.persist.PostgresClient;
import org.folio.gobi.GobiPurchaseOrderParser;
import org.folio.gobi.GobiResponseWriter;
import org.folio.gobi.HelperUtils;
import org.folio.gobi.LookupService;
import org.folio.gobi.Mapper;
import org.folio.gobi.MappingHelper;
import org.folio.gobi.OrdersSettingsRetriever;
import org.folio.gobi.domain.BindingResult;
import org.folio.gobi.exceptions.ErrorCodes;
import org.folio.gobi.exceptions.GobiException;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.core.RestClient;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.gobi.model.ResponseError;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.tools.utils.BinaryOutStream;
import org.folio.rest.tools.utils.TenantTool;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class PostGobiOrdersHelper {
  private static final Logger logger = LogManager.getLogger(PostGobiOrdersHelper.class);

  public static final String CODE_BAD_REQUEST = "BAD_REQUEST";
  public static final String CODE_INVALID_XML = "INVALID_XML";


  private final Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler;
  private final RestClient restClient;
  private final OrdersSettingsRetriever settingsRetriever;
  private final LookupService lookupService;
  private final MappingHelper mappingHelper;
  private final OrderMappingsDao orderMappingsDao;
  private final String tenantId;
  private final PostgresClient pgClient;

  public PostGobiOrdersHelper(Handler<AsyncResult<Response>> asyncResultHandler,
                              Map<String, String> okapiHeaders, Context ctx) {
    this.restClient = new RestClient(okapiHeaders, ctx);
    this.asyncResultHandler = asyncResultHandler;
    this.settingsRetriever = new OrdersSettingsRetriever(restClient);
    this.lookupService = new LookupService(restClient, settingsRetriever);
    this.mappingHelper = new MappingHelper(lookupService);
    this.tenantId = TenantTool.tenantId(okapiHeaders);
    this.pgClient = PostgresClient.getInstance(ctx.owner(), tenantId);
    this.orderMappingsDao = new OrderMappingsDaoImpl();
  }


  public CompletableFuture<CompositePurchaseOrder> mapToPurchaseOrder(Document doc) {
    final OrderMappings.OrderType orderType = getOrderType(doc);

    return lookupOrderMappings(orderType)
      .thenCompose(orderMappings -> {
        logger.info("Using Mappings {}", orderMappings);
        return new Mapper(lookupService).map(orderMappings, doc);
      })
      .thenApply(BindingResult::getResult);
  }


  public static OrderMappings.OrderType getOrderType(Document doc) {
    logger.debug("getOrderType:: Trying to get order type from '{}'", doc.getDoctype());
    final XPath xpath = XPathFactory.newInstance().newXPath();
    OrderMappings.OrderType orderType;

    String provided = null;
    try {
      Node node = (Node) xpath.evaluate(
        "//ListedElectronicMonograph|//ListedElectronicSerial|//ListedPrintMonograph|//ListedPrintSerial|//UnlistedPrintMonograph|//UnlistedPrintSerial",
        doc, XPathConstants.NODE);
      if (node != null) {
        provided = node.getNodeName();
        orderType = OrderMappings.OrderType.fromValue(provided);
      } else {
        logger.warn("getOrderType:: Could not find order type in provided '{}'", doc.getDoctype());
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      logger.error("Cannot determine order type", e);
      throw new IllegalArgumentException("Invalid order type: " + provided);
    }
    logger.info("getOrderType:: Order type is {} received from '{}'", orderType, doc.getDoctype());
    return orderType;
  }

  public CompletableFuture<Document> parseXML(String entity) {
    final GobiPurchaseOrderParser parser = GobiPurchaseOrderParser.getParser();

    try {
      return CompletableFuture.completedFuture(parser.parse(entity));
    } catch (GobiPurchaseOrderParserException e) {
      logger.error("Failed to parse GobiPurchaseOrder: {}", entity, e);
      return CompletableFuture.failedFuture(e);
    }
  }

  public CompletableFuture<Map<Mapping.Field, DataSourceResolver>> lookupOrderMappings(OrderMappings.OrderType orderType) {
    logger.debug("lookupOrderMappings:: Looking up mappings for orderType={}", orderType);

    return pgClient.withConn(conn -> orderMappingsDao.getByOrderType(orderType.value(), conn)
      .map(orderMappings -> {
        if (orderMappings == null) {
          logger.info("lookupOrderMappings:: Use default mapping for order type: {}", orderType);
          return mappingHelper.getDefaultMappingForOrderType(orderType);
        } else {
          logger.info("lookupOrderMappings:: Use custom mapping config from DB for order type: {}", orderType);
          return tryExtractOrderMappings(orderMappings);
        }
      }))
    .onFailure(t -> logger.error("Exception looking up custom order mappings for tenant '{}'", tenantId, t))
    .toCompletionStage()
    .toCompletableFuture();
  }


  private Map<Mapping.Field, DataSourceResolver> tryExtractOrderMappings(OrderMappings customMapping) {
    try {
      return mappingHelper.extractOrderMappings(customMapping);
    } catch (Exception e) {
      throw new GobiException(ErrorCodes.INVALID_ORDER_MAPPING_FILE, e);
    }
  }

  private CompletableFuture<String> placeOrder(CompositePurchaseOrder compPO) {
    try {
      return restClient.post(ORDERS_ENDPOINT, JsonObject.mapFrom(compPO))
        .map(body -> {
          logger.debug("Response from mod-orders: \n {}", body::encodePrettily);
          return body.getJsonArray("poLines").getJsonObject(FIRST_ELEM).getString("poLineNumber");
        })
        .toCompletionStage().toCompletableFuture();
    } catch (Exception e) {
      String errorMessage = String.format("Exception calling %s on %s", HttpMethod.POST, ORDERS_ENDPOINT);
      logger.error(errorMessage, e);
      return CompletableFuture.failedFuture(e);
    }
  }


  public CompletableFuture<String> getOrPlaceOrder(CompositePurchaseOrder compPO) {
    logger.debug("getOrPlaceOrder:: Trying to get order or place it with composite PO, its Line Number: {}", compPO.getPoNumber());
    return checkExistingOrder(compPO).thenCompose(isExisting -> {
        if (Boolean.TRUE.equals(isExisting)) {
          logger.info("getOrPlaceOrder:: Order already exists, retrieving the PO Line Number: {}", compPO.getPoNumber());
          return getExistingOrderById(compPO);
        }
        return CompletableFuture.completedFuture(compPO);
      })
      .thenCompose(compositePO -> {
        String poLineNumber = compositePO.getPoLines()
          .get(FIRST_ELEM)
          .getPoLineNumber();
        if (StringUtils.isEmpty(poLineNumber)) {
          return placeOrder(compositePO);
        }
        return CompletableFuture.completedFuture(poLineNumber);
      });
  }

  private CompletableFuture<Boolean> checkExistingOrder(CompositePurchaseOrder compPO) {
    String vendorRefNumber = compPO.getPoLines().get(FIRST_ELEM).getVendorDetail().getReferenceNumbers().get(FIRST_ELEM).getRefNumber();
    logger.debug("checkExistingOrder:: Trying to look for existing order with Vendor Reference Number: {}", vendorRefNumber);
    String query = HelperUtils.encodeValue(String.format("poLine.vendorDetail.referenceNumbers=\"refNumber\" : \"%s\"", vendorRefNumber));
    String endpoint = String.format(ORDERS_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenCompose(purchaseOrders -> {
        String orderId = HelperUtils.extractOrderId(purchaseOrders);
        if (StringUtils.isEmpty(orderId)) {
          logger.warn("checkExistingOrder:: No existing order found with Vendor Reference Number: {}", vendorRefNumber);
          return completedFuture(false);
        }
        CompositePurchaseOrder purchaseOrder = purchaseOrders.getJsonArray("purchaseOrders").getJsonObject(FIRST_ELEM).mapTo(CompositePurchaseOrder.class);
        compPO.setId(purchaseOrder.getId());
        // try to Open the Order, doing it asynchronously because irrespective of the result return the existing Order to GOBI
        if (purchaseOrder.getWorkflowStatus()
          .equals(CompositePurchaseOrder.WorkflowStatus.PENDING)) {
          purchaseOrder.setWorkflowStatus(CompositePurchaseOrder.WorkflowStatus.OPEN);

          return restClient.handlePutRequest(ORDERS_ENDPOINT + "/" + orderId, JsonObject.mapFrom(purchaseOrder)).toCompletionStage().toCompletableFuture()
            .exceptionally(e -> {
              logger.error("Retry to OPEN existing Order with id '{}' failed", orderId, e);
              return null;
            })
            .thenApply(v -> true);
        }
        return completedFuture(true);
      })
      .exceptionally(t -> {
        logger.error("Error looking up for existing Order with id: {}", compPO.getId(), t);
        return false;
      });
  }

  private CompletableFuture<CompositePurchaseOrder> getExistingOrderById(CompositePurchaseOrder compositePurchaseOrder) {
    logger.debug("getExistingOrderById:: Trying to retrieve existing Order with ID: {}", compositePurchaseOrder.getId());
    String endpoint = String.format(ORDERS_BY_ID_ENDPOINT, compositePurchaseOrder.getId());
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenCompose(order -> {
        CompositePurchaseOrder compPO = order.mapTo(CompositePurchaseOrder.class);
        String poLineNumber = compPO.getPoLines().get(FIRST_ELEM).getPoLineNumber();
        if (StringUtils.isEmpty(poLineNumber)) {
          logger.warn("getExistingOrderById:: No PO Line Number found for existing Order with ID: {}", compositePurchaseOrder.getId());
          return completedFuture(compositePurchaseOrder);
        }
        compositePurchaseOrder.getPoLines().get(FIRST_ELEM).setPoLineNumber(poLineNumber);
        return completedFuture(compositePurchaseOrder);
      })
      .exceptionally(t -> {
        logger.error("Error looking up for existing PO Line Number: {}", compositePurchaseOrder.getPoNumber(), t);
        return compositePurchaseOrder;
      });

  }

  public Void handleError(Throwable throwable) {
    logger.error("Exception placing order", throwable);
    Throwable cause = throwable.getCause();
    asyncResultHandler.handle(Future.succeededFuture(mapExceptionToResponse(cause != null ? cause : throwable)));
    return null;
  }

  private static Response mapExceptionToResponse(Throwable t) {

    String message = t.getMessage();

    if (t instanceof HttpException) {
      switch (((HttpException) t).getCode()) {
        case 400:
          return respond400WithApplicationXml(writeGobiResponse(CODE_BAD_REQUEST, message));
        case 401:
          return respond401WithTextPlain(message);
      }
    }

    if (t instanceof GobiPurchaseOrderParserException) {
      return respond400WithApplicationXml(writeGobiResponse(CODE_INVALID_XML, message));
    }

    if (t instanceof GobiException) {
      String errorCode = ((GobiException) t).getErrorCode().getCode();
      return respond500WithApplicationXml(writeGobiResponse(errorCode, message));
    }

    return respond500WithApplicationXml(message);
  }

  private static BinaryOutStream writeGobiResponse(String errorCode, String message) {
    GobiResponse response = new GobiResponse();
    response.setError(new ResponseError());
    response.getError().setCode(errorCode);
    response.getError().setMessage(message);
    return GobiResponseWriter.getWriter().write(response);
  }

}

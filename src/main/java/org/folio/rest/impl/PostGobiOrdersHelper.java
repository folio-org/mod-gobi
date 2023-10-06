package org.folio.rest.impl;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.folio.gobi.LookupService.CONFIGS;
import static org.folio.gobi.LookupService.FIRST_ELEM;
import static org.folio.gobi.LookupService.QUERY;
import static org.folio.rest.ResourcePaths.CONFIGURATION_ENDPOINT;
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
import org.folio.gobi.DataSourceResolver;
import org.folio.gobi.FieldMappingTranslatorResolver;
import org.folio.gobi.GobiPurchaseOrderParser;
import org.folio.gobi.GobiResponseWriter;
import org.folio.gobi.HelperUtils;
import org.folio.gobi.LookupService;
import org.folio.gobi.Mapper;
import org.folio.gobi.MappingHelper;
import org.folio.gobi.OrderMappingCache;
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

  private static final String CONFIGURATION_MODULE = "GOBI";
  private static final String CONFIGURATION_CONFIG_NAME = "orderMappings";
  private static final String CONFIGURATION_CODE = "gobi.order.";
  public static final String CODE_BAD_REQUEST = "BAD_REQUEST";
  public static final String CODE_INVALID_XML = "INVALID_XML";
  public static final String TENANT_HEADER = "X-Okapi-Tenant";


  private final Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler;
  private final RestClient restClient;
  private final LookupService lookupService;
  private final MappingHelper mappingHelper;

  public PostGobiOrdersHelper(Handler<AsyncResult<Response>> asyncResultHandler,
                              Map<String, String> okapiHeaders, Context ctx) {
    this.restClient = new RestClient(okapiHeaders, ctx);
    this.asyncResultHandler = asyncResultHandler;
    this.lookupService = new LookupService(restClient);
    FieldMappingTranslatorResolver fieldMappingTranslatorResolver = new FieldMappingTranslatorResolver(lookupService);
    this.mappingHelper = new MappingHelper(fieldMappingTranslatorResolver);
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
    final String query = HelperUtils.encodeValue(
      String.format("module==%s AND configName==%s AND code==%s",
        CONFIGURATION_MODULE,
        CONFIGURATION_CONFIG_NAME,
        CONFIGURATION_CODE + orderType));

    String endpoint = String.format(CONFIGURATION_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .map(jo -> {
        if (!jo.getJsonArray(CONFIGS).isEmpty()) {
          String config = jo.getJsonArray(CONFIGS).getJsonObject(0).getString("value");
          logger.info("lookupOrderMappings:: Use custom mapping config: \n {}", config);
          return tryExtractOrderMappings(orderType, jo);
        } else {
          String config = JsonObject.mapFrom(MappingHelper.getDefaultMappingByOrderType(orderType)).encode();
          logger.info("lookupOrderMappings:: Use default mapping config: \n {}", config);
          return getDefaultMappingsFromCache(orderType);
        }
      })
      .onFailure(t -> logger.error("Exception looking up custom order mappings for tenant '{}'", restClient.getTenantId(), t))
      .toCompletionStage()
      .toCompletableFuture();
  }

  /**
   * If retrieval of custom tenant mapping fails and there is no cached value
   * then the default mappings will be used for placing an order
   *
   * @param orderType
   * @return Map<Mapping.Field, org.folio.gobi.DataSourceResolver>
   */
  private Map<Mapping.Field, org.folio.gobi.DataSourceResolver> getDefaultMappingsFromCache(
    final OrderMappings.OrderType orderType) {

    logger.info("No custom Mappings found for tenant, using default mappings");
    String tenant = restClient.getTenantId();
    boolean cacheFound = OrderMappingCache.getInstance().containsKey(OrderMappingCache.computeKey(tenant, orderType));
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> mappings = cacheFound ?
      OrderMappingCache.getInstance().getValue(OrderMappingCache.computeKey(tenant, orderType)) : mappingHelper.getDefaultMappingForOrderType(orderType);

    if (!cacheFound)
      OrderMappingCache.getInstance().putValue(orderType.toString(), mappings);
    return mappings;
  }

  private Map<Mapping.Field, DataSourceResolver> tryExtractOrderMappings(OrderMappings.OrderType orderType, JsonObject jo) {
    try {
      return extractOrderMappings(orderType, jo);
    } catch (Exception e) {
      throw new GobiException(ErrorCodes.INVALID_ORDER_MAPPING_FILE, e);
    }
  }

  private Map<Mapping.Field, DataSourceResolver> extractOrderMappings(OrderMappings.OrderType orderType, JsonObject jo) {
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> mappings;
    String tenant = restClient.getTenantId();
    String tenantConfigKey = OrderMappingCache.computeKey(tenant, orderType, jo);

    if (OrderMappingCache.getInstance().containsKey(tenantConfigKey)) {
      mappings = OrderMappingCache.getInstance().getValue(tenantConfigKey);
    } else {
      // check if there is a key with an old mapping for this order type and tenant, if so delete it
      String tenantKey = OrderMappingCache.getInstance().getifContainsTenantconfigKey(tenant, orderType);
      if (tenantKey != null) {
        OrderMappingCache.getInstance().removeKey(tenantKey);
      }
      // extract the mappings and add it to cache
      mappings = mappingHelper.extractOrderMappings(orderType, jo);
      if (!mappings.isEmpty())
        OrderMappingCache.getInstance().putValue(tenantConfigKey, mappings);
    }
    return mappings;

  }

  private CompletableFuture<String> placeOrder(CompositePurchaseOrder compPO) {
    try {
      return restClient.post(ORDERS_ENDPOINT, JsonObject.mapFrom(compPO))
        .map(body -> {
          logger.info("Response from mod-orders: \n {}", body::encodePrettily);
          return body.getJsonArray("compositePoLines").getJsonObject(FIRST_ELEM).getString("poLineNumber");
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
        String poLineNumber = compositePO.getCompositePoLines()
          .get(FIRST_ELEM)
          .getPoLineNumber();
        if (StringUtils.isEmpty(poLineNumber)) {
          return placeOrder(compositePO);
        }
        return CompletableFuture.completedFuture(poLineNumber);
      });
  }

  private CompletableFuture<Boolean> checkExistingOrder(CompositePurchaseOrder compPO) {
    String vendorRefNumber = compPO.getCompositePoLines().get(FIRST_ELEM).getVendorDetail().getReferenceNumbers().get(FIRST_ELEM).getRefNumber();
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
        String poLineNumber = compPO.getCompositePoLines().get(FIRST_ELEM).getPoLineNumber();
        if (StringUtils.isEmpty(poLineNumber)) {
          logger.warn("getExistingOrderById:: No PO Line Number found for existing Order with ID: {}", compositePurchaseOrder.getId());
          return completedFuture(compositePurchaseOrder);
        }
        compositePurchaseOrder.getCompositePoLines().get(FIRST_ELEM).setPoLineNumber(poLineNumber);
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

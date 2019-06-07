package org.folio.rest.impl;

import static java.util.Objects.nonNull;
import static org.folio.rest.jaxrs.resource.Gobi.PostGobiOrdersResponse.respond400WithApplicationXml;
import static org.folio.rest.jaxrs.resource.Gobi.PostGobiOrdersResponse.respond401WithTextPlain;
import static org.folio.rest.jaxrs.resource.Gobi.PostGobiOrdersResponse.respond500WithTextPlain;
import static java.util.concurrent.CompletableFuture.completedFuture;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;
import org.apache.commons.lang.StringUtils;
import org.folio.gobi.DataSourceResolver;
import org.folio.gobi.GobiPurchaseOrderParser;
import org.folio.gobi.GobiResponseWriter;
import org.folio.gobi.HelperUtils;
import org.folio.gobi.Mapper;
import org.folio.gobi.MappingHelper;
import org.folio.gobi.OrderMappingCache;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.acq.model.Organization;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.gobi.model.ResponseError;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMappings;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PostGobiOrdersHelper {
  private static final Logger logger = LoggerFactory.getLogger(PostGobiOrdersHelper.class);

  static final String LOCATIONS_ENDPOINT = "/locations";
  static final String MATERIAL_TYPES_ENDPOINT = "/material-types";
  static final String PAYMENT_STATUS_ENDPOINT = "/payment_status";
  static final String GET_ORGANIZATION_ENDPOINT = "/organizations-storage/organizations";
  static final String CONFIGURATION_ENDPOINT = "/configurations/entries";
  static final String ORDERS_ENDPOINT = "/orders/composite-orders";
  static final String ORDERS_BY_ID_ENDPOINT = "/orders/composite-orders/%s";
  static final String FUNDS_ENDPOINT = "/finance-storage/funds";
  static final String IDENTIFIERS_ENDPOINT = "/identifier-types";
  private static final String QUERY = "?query=%s";

  private static final String CONFIGURATION_MODULE = "GOBI";
  private static final String CONFIGURATION_CONFIG_NAME = "orderMappings";
  private static final String CONFIGURATION_CODE = "gobi.order.";
  public static final String CODE_BAD_REQUEST = "BAD_REQUEST";
  public static final String CODE_INVALID_TOKEN = "INVALID_TOKEN";
  public static final String CODE_INVALID_XML = "INVALID_XML";
  public static final String CQL_CODE_STRING_FMT = "code==\"%s\"";
  public static final String TENANT_HEADER = "X-Okapi-Tenant";
  private static final String EXCEPTION_CALLING_ENDPOINT_MSG = "Exception calling {} {}";
  private static final String DEFAULT_LOOKUP_CODE = "*";
  private static final String UNSPECIFIED_MATERIAL_NAME = "unspecified";
  private static final String CHECK_ORGANIZATION_ISVENDOR = " and isVendor==true";

  private final HttpClientInterface httpClient;
  private final Context ctx;
  private final Map<String, String> okapiHeaders;
  private final Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler;

  public PostGobiOrdersHelper(HttpClientInterface httpClient,
      Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler, Map<String, String> okapiHeaders,
      Context ctx) {
    this.httpClient = httpClient;
    this.ctx = ctx;
    this.okapiHeaders = okapiHeaders;
    this.asyncResultHandler = asyncResultHandler;
  }


  public CompletableFuture<CompositePurchaseOrder> mapToPurchaseOrder(Document doc) {
    final OrderMappings.OrderType orderType = getOrderType(doc);
    VertxCompletableFuture<CompositePurchaseOrder> future = new VertxCompletableFuture<>(ctx);

      lookupOrderMappings(orderType).thenAccept(ordermappings -> {
       logger.info("Using Mappings {}",ordermappings.toString());
        new Mapper(ordermappings).map(doc)
          .thenAccept(future::complete);
      }).exceptionally(e -> {
        logger.error("Exception looking up Order mappings", e);
        future.completeExceptionally(e);
        return null;
      });
    return future;
  }


  public static OrderMappings.OrderType getOrderType(Document doc) {
    final XPath xpath = XPathFactory.newInstance().newXPath();
    OrderMappings.OrderType orderType;

    String provided = null;
    try {
      Node node = (Node) xpath.evaluate(
          "//ListedElectronicMonograph|//ListedElectronicSerial|//ListedPrintMonograph|//ListedPrintSerial|//UnlistedPrintMonograph|//UnlistedPrintSerial",
          doc, XPathConstants.NODE);
      if(node!=null){
        provided = node.getNodeName();
        orderType = OrderMappings.OrderType.fromValue(provided);
      }else {
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      logger.error("Cannot determine order type", e);
      throw new IllegalArgumentException("Invalid order type: " + provided);
    }
    logger.info("Order Type Recieved {}",orderType);
    return orderType;
  }

  public CompletableFuture<Document> parse(String entity) {
    VertxCompletableFuture<Document> future = new VertxCompletableFuture<>(ctx);
    final GobiPurchaseOrderParser parser = GobiPurchaseOrderParser.getParser();

    try {
      future.complete(parser.parse(entity));
    } catch (GobiPurchaseOrderParserException e) {
      logger.error("Failed to parse GobiPurchaseOrder", e);
      future.completeExceptionally(e);
    }
    return future;
  }

  public CompletableFuture<JsonObject> handleGetRequest(String endpoint) {
      CompletableFuture<JsonObject> future = new VertxCompletableFuture<>(ctx);
      try {
        logger.debug("Calling GET {}", endpoint);
        httpClient.request(HttpMethod.GET, endpoint, okapiHeaders)
          .thenApply(response -> {
            logger.debug("Validating response for GET {}", endpoint);
            return HelperUtils.verifyAndExtractBody(response);
          })
          .thenAccept(body -> {
            if (logger.isDebugEnabled()) {
              logger.debug("The response body for GET {}: {}", endpoint, nonNull(body) ? body.encodePrettily() : null);
            }
            future.complete(body);
          })
          .exceptionally(t -> {
            logger.error(EXCEPTION_CALLING_ENDPOINT_MSG, t, HttpMethod.GET, endpoint);
            future.completeExceptionally(t);
            return null;
          });
      } catch (Exception e) {
        logger.error(EXCEPTION_CALLING_ENDPOINT_MSG, e, HttpMethod.GET, endpoint);
        future.completeExceptionally(e);
      }
      return future;
    }

  /**
   * A common method to update an entry
   *
   * @param recordData json to use for update operation
   * @param endpoint endpoint
   */
  public static CompletableFuture<Void> handlePutRequest(String endpoint, JsonObject recordData, HttpClientInterface httpClient,
                                                         Context ctx, Map<String, String> okapiHeaders, Logger logger) {
    CompletableFuture<Void> future = new VertxCompletableFuture<>(ctx);
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Sending 'PUT {}' with body: {}", endpoint, recordData.encodePrettily());
      }
      httpClient
        .request(HttpMethod.PUT, recordData.toBuffer(), endpoint, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenAccept(response -> {
          logger.debug("'PUT {}' request successfully processed", endpoint);
          future.complete(null);
        })
        .exceptionally(e -> {
          future.completeExceptionally(e);
          logger.error("'PUT {}' request failed. Request body: {}", e, endpoint, recordData.encodePrettily());
          return null;
        });
    } catch (Exception e) {
      future.completeExceptionally(e);
    }

    return future;
  }

  /**
   * Use the provided location code. if one isn't provided, or the specified
   * type can't be found, fallback using the first one listed
   *
   * @param location
   * @return UUID of the location
   */
  public CompletableFuture<String> lookupLocationId(String location) {
    logger.info("Received location is {}", location);
    String query = HelperUtils.encodeValue(String.format(CQL_CODE_STRING_FMT, location), logger);
    String endpoint = String.format(LOCATIONS_ENDPOINT + QUERY, query);
    return handleGetRequest(endpoint)
      .thenCompose(locations -> {
        String locationId = HelperUtils.extractLocationId(locations);
        if (StringUtils.isEmpty(locationId)) {
          return completedFuture(null);
        }
        return completedFuture(locationId);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up location id", t);
        return null;
      });
  }

  /**
   * Use the provided materialType. if the specified type can't be found,
   * fallback to looking up "unspecified"(handled via default mappings).If that
   * cannot be found too, fallback to using the first one listed
   *
   * @param materialTypeCode
   */
  public CompletableFuture<String> lookupMaterialTypeId(String materialTypeCode) {
    String query = HelperUtils.encodeValue(String.format("name==%s", materialTypeCode), logger);
    String endpoint = String.format(MATERIAL_TYPES_ENDPOINT + QUERY, query);
    return handleGetRequest(endpoint)
      .thenCompose(materialTypes -> {
        String materialType = HelperUtils.extractMaterialTypeId(materialTypes);
        if (StringUtils.isEmpty(materialType)) {
          if (StringUtils.equalsIgnoreCase(materialTypeCode, UNSPECIFIED_MATERIAL_NAME)) {
            return lookupMaterialTypeId(DEFAULT_LOOKUP_CODE);
          } else {
            return completedFuture(null);
          }
        }
        return completedFuture(materialType);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up material-type id", t);
        return null;
      });
  }

  public CompletableFuture<Organization> lookupOrganization(String vendorCode) {
      String query = HelperUtils.encodeValue(String.format(CQL_CODE_STRING_FMT+CHECK_ORGANIZATION_ISVENDOR, vendorCode), logger);
      String endpoint = String.format(GET_ORGANIZATION_ENDPOINT+QUERY, query);

      return handleGetRequest(endpoint)
        .thenApply(resp ->
          Optional.ofNullable(resp.getJsonArray("organizations"))
          .flatMap(organizations -> organizations.stream().findFirst())
          .map(organization -> ((JsonObject) organization).mapTo(Organization.class))
          .orElse(null))
        .exceptionally(t -> {
          logger.error("Exception looking up Organization which is a vendor with code:"+vendorCode, t);
          return null;
        });
  }

  public CompletableFuture<String> lookupFundId(String fundCode) {
    String query = HelperUtils.encodeValue(String.format("code==%s", fundCode), logger);
    String endpoint = String.format(FUNDS_ENDPOINT + QUERY, query);
    return handleGetRequest(endpoint)
      .thenCompose(funds -> {
        String fundId = HelperUtils.extractIdOfFirst(funds, "funds");
        if (StringUtils.isEmpty(fundId)) {
          return lookupFundId(DEFAULT_LOOKUP_CODE);
        }
        return completedFuture(fundId);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up fund id", t);
        return null;
      });
  }

  /**
   * Use the provided product type.If the specified type can't be found, fallback using the first one listed
   *
   * @param productType
   * @return UUID of the productId Type
   */
  public CompletableFuture<String> lookupProductIdType(String productType) {
    logger.info("Received ProductType is {}", productType);
    String query = HelperUtils.encodeValue(String.format("name==%s", productType), logger);
    String endpoint = String.format(IDENTIFIERS_ENDPOINT + QUERY, query);
    return handleGetRequest(endpoint).thenCompose(productTypes -> {
      String productTypeId = HelperUtils.extractProductTypeId(productTypes);
      if (StringUtils.isEmpty(productTypeId)) {
        return lookupProductIdType(DEFAULT_LOOKUP_CODE);
      }
      return completedFuture(productTypeId);
    })
      .exceptionally(t -> {
        logger.error("Exception looking up productId type UUID", t);
        return null;
      });
  }

  public CompletableFuture<String> lookupMock(String data) {
    logger.info("Mocking the data lookup for: " + data);
    return CompletableFuture.completedFuture(UUID.randomUUID().toString());
  }

  public CompletableFuture<Map<Mapping.Field, DataSourceResolver>> lookupOrderMappings(OrderMappings.OrderType orderType) {
      final String query = HelperUtils.encodeValue(
          String.format("module==%s AND configName==%s AND code==%s",
              CONFIGURATION_MODULE,
              CONFIGURATION_CONFIG_NAME,
              CONFIGURATION_CODE+orderType), logger);

      String endpoint = String.format(CONFIGURATION_ENDPOINT+QUERY, query);
      return handleGetRequest(endpoint)
        .thenApply(jo ->  {
          if(!jo.getJsonArray("configs").isEmpty())
            return extractOrderMappings(orderType, jo);
          return getDefaultMappingsFromCache(orderType);
        })
        .exceptionally(t -> {
          logger.error("Exception looking up custom order mappings for tenant", t);
          String tenantKey=OrderMappingCache.getInstance().getifContainsTenantconfigKey(okapiHeaders.get(TENANT_HEADER), orderType);
          if(tenantKey!=null) {
            logger.info("Using the cached value of custom mappings");
            return OrderMappingCache.getInstance().getValue(tenantKey);
          }
          return getDefaultMappingsFromCache(orderType);
        });
  }

  /**
   * If retrieval of custom tenant mapping fails, and there is no cached value, then the default mappings will be used for placing an order
   *
   * @param orderType
   * @return Map<Mapping.Field, org.folio.gobi.DataSourceResolver>
   */
  private Map<Mapping.Field, org.folio.gobi.DataSourceResolver> getDefaultMappingsFromCache(
      final OrderMappings.OrderType orderType) {

    logger.info("No custom Mappings found for tenant, using default mappings");
    String tenant = okapiHeaders.get(TENANT_HEADER);
    boolean cacheFound = OrderMappingCache.getInstance().containsKey(OrderMappingCache.computeKey(tenant, orderType));
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> mappings =  cacheFound ?
         OrderMappingCache.getInstance().getValue(OrderMappingCache.computeKey(tenant, orderType)) : MappingHelper.getDefaultMappingForOrderType(this, orderType);

    if(!cacheFound)
      OrderMappingCache.getInstance().putValue(orderType.toString(), mappings);
    return mappings;
  }

  private Map<Mapping.Field, DataSourceResolver> extractOrderMappings(OrderMappings.OrderType orderType, JsonObject jo) {
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> mappings;
    String tenant = okapiHeaders.get(TENANT_HEADER);
    String tenantConfigKey=OrderMappingCache.computeKey(tenant, orderType, jo);

    if(OrderMappingCache.getInstance().containsKey(tenantConfigKey)){
      mappings = OrderMappingCache.getInstance().getValue(tenantConfigKey);
    } else {
      //check if there is a key with an old mapping for this order type and tenant, if so delete it
      String tenantKey=OrderMappingCache.getInstance().getifContainsTenantconfigKey(tenant, orderType);
      if(tenantKey!=null) {
        OrderMappingCache.getInstance().removeKey(tenantKey);
      }
      //extract the mappings and add it to cache
      mappings= MappingHelper.extractOrderMappings(orderType, jo, this);
      if(!mappings.isEmpty())
          OrderMappingCache.getInstance().putValue(tenantConfigKey, mappings);
    }
    return mappings;

  }

  private CompletableFuture<String> placeOrder(CompositePurchaseOrder compPO) {
    VertxCompletableFuture<String> future = new VertxCompletableFuture<>(ctx);
    try {
      httpClient.request(HttpMethod.POST, compPO, ORDERS_ENDPOINT, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenAccept(body -> {
          logger.info("Response from mod-orders: " + body.encodePrettily());
          future.complete(body.getJsonArray("compositePoLines").getJsonObject(0).getString("poLineNumber"));
        })
        .exceptionally(t -> {
          future.completeExceptionally(t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling {} on {}",HttpMethod.POST,ORDERS_ENDPOINT, e);
      future.completeExceptionally(e);
    }
    return future;
  }


  public CompletableFuture<String> getOrPlaceOrder(CompositePurchaseOrder compPO) {
    return checkExistingOrder(compPO).thenCompose(isExisting -> {
      if (isExisting) {
        logger.info("Order already exists, retrieving the PO Line Number", Json.encodePrettily(compPO));
        return getExistingOrderById(compPO);
      }
      return CompletableFuture.completedFuture(compPO);
    })
      .thenCompose(compositePO -> {
        String poLineNumber = compositePO.getCompositePoLines()
          .get(0)
          .getPoLineNumber();
        if (StringUtils.isEmpty(poLineNumber)) {
          return placeOrder(compositePO);
        }
        return CompletableFuture.completedFuture(poLineNumber);
      });
  }

  private CompletableFuture<Boolean> checkExistingOrder(CompositePurchaseOrder compPO){
    String vendorRefNumber = compPO.getCompositePoLines().get(0).getVendorDetail().getRefNumber();
    logger.info("Looking for exisiting order with Vendor Reference Number", vendorRefNumber);
    String query = HelperUtils.encodeValue(String.format("vendorDetail.refNumber=%s", vendorRefNumber), logger);
    String endpoint = String.format(ORDERS_ENDPOINT + QUERY, query);
    return handleGetRequest(endpoint)
      .thenCompose(purchaseOrders -> {
        String orderId = HelperUtils.extractOrderId(purchaseOrders);
        if (StringUtils.isEmpty(orderId)) {
          return completedFuture(false);
        }
        CompositePurchaseOrder purchaseOrder = purchaseOrders.getJsonArray("purchaseOrders").getJsonObject(0).mapTo(CompositePurchaseOrder.class);
        compPO.setId(purchaseOrder.getId());
        //try to Open the Order,doing it asynchronously because irrespective of the result return the existing Order to GOBI
        if (purchaseOrder.getWorkflowStatus()
          .equals(CompositePurchaseOrder.WorkflowStatus.PENDING)) {
          purchaseOrder.setWorkflowStatus(CompositePurchaseOrder.WorkflowStatus.OPEN);
          handlePutRequest(ORDERS_ENDPOINT + "/" + orderId, JsonObject.mapFrom(purchaseOrder), httpClient, ctx, okapiHeaders,
              logger).exceptionally(e -> {
                logger.error("Retry to OPEN existing Order failed", e);
                return null;
              });
        }
        return completedFuture(true);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up for existing Order", t);
        return false;
      });
  }

  private CompletableFuture<CompositePurchaseOrder> getExistingOrderById(CompositePurchaseOrder compositePurchaseOrder) {
    logger.info("Retrieving existing Order with ID ", compositePurchaseOrder.getId());
    String endpoint = String.format(ORDERS_BY_ID_ENDPOINT, compositePurchaseOrder.getId());
    return handleGetRequest(endpoint)
      .thenCompose(order -> {
        CompositePurchaseOrder compPO = order.mapTo(CompositePurchaseOrder.class);
        String poLineNumber = compPO.getCompositePoLines().get(0).getPoLineNumber();
        if (StringUtils.isEmpty(poLineNumber)) {
          return completedFuture(compositePurchaseOrder);
        }
        compositePurchaseOrder.getCompositePoLines().get(0).setPoLineNumber(poLineNumber);
        return completedFuture(compositePurchaseOrder);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up for existing PO Line Number", t);
        return compositePurchaseOrder;
      });

  }
  public Void handleError(Throwable throwable) {
    final javax.ws.rs.core.Response result;

    logger.error("Exception placing order", throwable.getCause());
    GobiResponse response = new GobiResponse();
    response.setError(new ResponseError());

    final Throwable t = throwable.getCause();
    if (t instanceof HttpException) {
      final int code = ((HttpException) t).getCode();
      final String message = ((HttpException) t).getMessage();
      switch (code) {
      case 400:
        response.getError().setCode(CODE_BAD_REQUEST);
        response.getError().setMessage(HelperUtils.truncate(t.getMessage(), 500));
        result = respond400WithApplicationXml(GobiResponseWriter.getWriter().write(response));
        break;
      case 500:
        result = respond500WithTextPlain(message);
        break;
      case 401:
        result = respond401WithTextPlain(message);
        break;
      default:
        result = respond500WithTextPlain(message);
      }
    } else if (t instanceof GobiPurchaseOrderParserException) {
      response.getError().setCode(CODE_INVALID_XML);
      response.getError().setMessage(HelperUtils.truncate(t.getMessage(), 500));
      result = respond400WithApplicationXml(GobiResponseWriter.getWriter().write(response));
    } else {
      result = respond500WithTextPlain(throwable.getMessage());
    }

    if (httpClient != null) {
      httpClient.closeClient();
    }
    asyncResultHandler.handle(Future.succeededFuture(result));
    return null;
  }
}

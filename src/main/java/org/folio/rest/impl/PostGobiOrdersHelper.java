package org.folio.rest.impl;

import static java.util.Objects.nonNull;
import static org.folio.rest.jaxrs.resource.Gobi.PostGobiOrdersResponse.respond400WithApplicationXml;
import static org.folio.rest.jaxrs.resource.Gobi.PostGobiOrdersResponse.respond500WithTextPlain;
import static org.folio.rest.jaxrs.resource.Gobi.PostGobiOrdersResponse.respond401WithTextPlain;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
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
import org.folio.rest.acq.model.Vendor;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.gobi.model.ResponseError;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMappings;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class PostGobiOrdersHelper {
  private static final Logger logger = LoggerFactory.getLogger(PostGobiOrdersHelper.class);

  static final String LOCATIONS_ENDPOINT = "/locations";
  static final String MATERIAL_TYPES_ENDPOINT = "/material-types";
  static final String PAYMENT_STATUS_ENDPOINT = "/payment_status";
  static final String GET_VENDORS_ENDPOINT = "/vendor-storage/vendors";
  static final String CONFIGURATION_ENDPOINT = "/configurations/entries";
  static final String ORDERS_ENDPOINT = "/orders/composite-orders";
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

  public CompletableFuture<Integer> getPurchaseOptionCode(Object s) {
    if (s != null) {
      return CompletableFuture.completedFuture(3);
    } else {
      return CompletableFuture.completedFuture(null);
    }
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

  public CompletableFuture<String> lookupLocationId(String location) {
    //Always getting the first location until GOBI responds with how to handle locations for various orders
      String query = HelperUtils.encodeValue(String.format(CQL_CODE_STRING_FMT, "*"), logger);
      String endpoint = String.format(LOCATIONS_ENDPOINT+QUERY, query);
      return handleGetRequest(endpoint)
        .thenApply(HelperUtils::extractLocationId)
        .exceptionally(t -> {
          logger.error("Exception looking up location id", t);
          return null;
        });

  }
  public CompletableFuture<List<String>> lookupMaterialTypeId(String materialType) {
      String query = HelperUtils.encodeValue(String.format("name==%s", materialType), logger);
      String endpoint = String.format(MATERIAL_TYPES_ENDPOINT+QUERY, query);
      return handleGetRequest(endpoint)
        .thenApply(HelperUtils::extractMaterialTypeId)
        .exceptionally(t -> {
          logger.error("Exception looking up material-type id", t);
          return null;
        });
  }

  public CompletableFuture<Vendor> lookupVendorId(String vendorCode) {
      String query = HelperUtils.encodeValue(String.format(CQL_CODE_STRING_FMT, vendorCode), logger);
      String endpoint = String.format(GET_VENDORS_ENDPOINT+QUERY, query);
      return handleGetRequest(endpoint)
        .thenApply(resp ->
          Optional.ofNullable(resp.getJsonArray("vendors"))
          .flatMap(vendors -> vendors.stream().findFirst())
          .map(vendor -> ((JsonObject) vendor).mapTo(Vendor.class))
          .orElse(null))
        .exceptionally(t -> {
          logger.error("Exception looking up vendor id", t);
          return null;
        });
  }


  public CompletableFuture<String> lookupPaymentStatusId(String paymentStatusCode) {
      String query = HelperUtils.encodeValue(String.format(CQL_CODE_STRING_FMT, paymentStatusCode), logger);
      String endpoint = String.format(PAYMENT_STATUS_ENDPOINT+QUERY, query);
      return handleGetRequest(endpoint)
        .thenApply(HelperUtils::extractPaymentStatusId)
        .exceptionally(t -> {
          logger.error("Exception looking up payment status id", t);
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

  public CompletableFuture<String> placeOrder(CompositePurchaseOrder compPO) {
    VertxCompletableFuture<String> future = new VertxCompletableFuture<>(ctx);
    try {
      httpClient.request(HttpMethod.POST, compPO, ORDERS_ENDPOINT, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenAccept(body -> {
          logger.info("Response from mod-orders: " + body.encodePrettily());
          future.complete(body.getJsonArray("compositePoLines").getJsonObject(0).getString("po_line_number"));
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

  public static JsonObject getClaims(String token) {
    String[] tokenPieces = token.split("\\.");
    if (tokenPieces.length > 1) {
      String encodedJson = tokenPieces[1];
      if (encodedJson == null) {
        return null;
      }

      String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
      return new JsonObject(decodedJson);
    }
    return null;
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

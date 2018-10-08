package org.folio.rest.impl;


import java.io.Reader;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.folio.gobi.DataSource;
import org.folio.gobi.GobiPurchaseOrderParser;
import org.folio.gobi.GobiResponseWriter;
import org.folio.gobi.HelperUtils;
import org.folio.gobi.Mapper;
import org.folio.gobi.MappingHelper;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.gobi.exceptions.HttpException;
import org.folio.gobi.exceptions.InvalidTokenException;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.gobi.model.ResponseError;
import org.folio.rest.jaxrs.resource.GOBIIntegrationServiceResource.PostGobiOrdersResponse;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMapping;
import org.folio.rest.mappings.model.OrderMapping.OrderType;
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

  private static final Logger logger = Logger.getLogger(PostGobiOrdersHelper.class);

  private static final String CONFIGURATION_MODULE = "GOBI";
  private static final String CONFIGURATION_CONFIG_NAME = "orderMappings";
  private static final String CONFIGURATION_CODE = "gobi.order.mappings";

  public static final String CODE_BAD_REQUEST = "BAD_REQUEST";
  public static final String CODE_INVALID_TOKEN = "INVALID_TOKEN";
  public static final String CODE_INVALID_XML = "INVALID_XML";

  
  
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
  
  
 
//  public CompletableFuture<CompositePurchaseOrder> defaultMapping(Document doc) {
//
//	// Map<OrderMapping.OrderType, Map<Mapping.Field, DataSource>>
//	  
//	  try {
//		  Map<OrderMapping.OrderType, Map<Mapping.Field, DataSource>> defaultMapping = 
//				  new LinkedHashMap<OrderMapping.OrderType, Map<Mapper.Field, DataSource>>();
//				  
//		  Map<Field, DataSource> mappings = new EnumMap<>(Field.class);
//		  Json.decodeValue(buf, clazz)
//		  JsonObject jo = new JsonObject();
//		  final JsonArray orderMappingsArray = jo.getJsonArray("orderMappings");	//get ordermappings array
//		  for(int i=0;i<orderMappingsArray.size();i++) {
//			  JsonObject jsonOrder = orderMappingsArray.getJsonObject(i);			//get ith json object in ordermapping array
//			  String orderType = jsonOrder.getString("orderType");					//get orderType
//			  JsonArray mappingsArray = jsonOrder.getJsonArray("mappings");			//get mappings array
//			  for(int j=0;j<mappingsArray.size();j++) {								//iterate through mappings array
//				  JsonObject mappingsObject = new JsonObject();						
//				  mappingsObject = mappingsArray.getJsonObject(j);					//get jth object in mappings array
//				  String field = mappingsObject.getString("field");					//get field
//				  // Field.ACCOUNT_NUMBER
//				  DataSource a = mappings.get(field);
//				  Map<String, Object> dataSourceMap = mappingsObject.getMap();		
//				  if(!dataSourceMap.isEmpty()) {
//					  if(dataSourceMap.containsKey("from")) {
//						  
//					  }
//				  }
//				  
//			  }
//		  }
//	  }
//	  catch (Exception e) {
//	      logger.error("Exception mapping request", e);
//	      //future.completeExceptionally(e);
//	    }
//	  return CompletableFuture.completedFuture(null);
//  }
  
  public CompletableFuture<CompositePurchaseOrder> map(Document doc) {
    final OrderMapping.OrderType orderType = getOrderType(doc);

    VertxCompletableFuture<CompositePurchaseOrder> future = new VertxCompletableFuture<>(ctx);
    
    try {
      Map<OrderType, Map<Mapping.Field, org.folio.gobi.DataSource>> defaultMapping = MappingHelper.defaultMapping();
      
      Map<Mapping.Field, org.folio.gobi.DataSource> mappings = defaultMapping.get(orderType);
      
      
//
//      mappings.put(Field.CREATED_BY, DataSource.builder()
//        //.withDefault(getUuid(okapiHeaders.get(RestVerticle.OKAPI_HEADER_TOKEN)))
//        .build());
//      mappings.put(Field.ACCOUNT_NUMBER, DataSource.builder()
//        .withFrom("//SubAccount")
//        .withDefault(0)
//        .build());
//      mappings.put(Field.ACQUISITION_METHOD, DataSource.builder()
//        .withDefault("mod-gobi")
//        .build());
//      mappings.put(Field.QUANTITY, DataSource.builder()
//        .withFrom("//Quantity")
//        .withDefault(1)
//        //.withTranslation(Mapper::toInteger)
//        .build());
//      mappings.put(Field.LIST_PRICE, DataSource.builder()
//        .withFrom("//ListPrice/Amount")
//        .withDefault(0d)
//        //.withTranslation(Mapper::toDouble)
//        .build());
//      mappings.put(Field.ESTIMATED_PRICE, DataSource.builder()
//        .withFrom("//NetPrice/Amount")
//        .withDefault(DataSource.builder()
//          .withFrom("//ListPrice/Amount|//Quantity")
//          .withCombinator(Mapper::multiply)
//          .withDefault(mappings.get(Field.LIST_PRICE))
//          //.withTranslation(Mapper::toDouble)
//          .build())
//        //.withTranslation(Mapper::toDouble)
//        .build());
//      mappings.put(Field.CURRENCY, DataSource.builder()
//        .withFrom("//ListPrice/Currency")
//        .withDefault("USD")
//        .build());
//      mappings.put(Field.FUND_CODE, DataSource.builder()
//        .withFrom("//FundCode")
//        .withDefault(0)
//        .build());
//      mappings.put(Field.TITLE, DataSource.builder()
//        .withFrom("//datafield[@tag='245']/*")
//        .withCombinator(Mapper::concat)
//        .build());
//      mappings.put(Field.RECEIVING_NOTE, DataSource.builder()
//        .withFrom("//LocalData[Description='LocalData2']/Value")
//        .build());
//      mappings.put(Field.REQUESTER, DataSource.builder()
//        .withFrom("//LocalData[Description='LocalData3']/Value")
//        .build());
//      mappings.put(Field.ACCESS_PROVIDER, DataSource.builder()
//        .withFrom("//PurchaseOrder/VendorPOCode")
//        .build());
//      mappings.put(Field.NOTE_FROM_VENDOR, DataSource.builder()
//        .withFrom("//PurchaseOrder/VendorCode")
//        .build());
//      mappings.put(Field.PRODUCT_ID, DataSource.builder()
//        .withFrom("//datafield[@tag='020']/subfield[@code='a']")
//        .build());
//      mappings.put(Field.MATERIAL_TYPE, DataSource.builder()
//        .withFrom("//LocalData[Description='LocalData1']/Value")
//        //.withTranslation(this::lookupMaterialTypeId)
//        .build());
//      mappings.put(Field.LOCATION, DataSource.builder()
//        .withFrom("//Location")
//        //.withTranslation(this::lookupLocationId)
//        .build());
//      mappings.put(Field.VENDOR_ID, DataSource.builder()
//        .withDefault("GOBI")
//        //.withTranslation(this::lookupVendorId)
//        .withTranslateDefault(true)
//        .build());
//      mappings.put(Field.INSTRUCTIONS, DataSource.builder()
//        .withFrom("//OrderNotes")
//        .withDefault("")
//        .build());
//      mappings.put(Field.USER_LIMIT, DataSource.builder()
//        .withFrom("//PurchaseOption/Code")
//        //.withTranslation(this::getPurchaseOptionCode)
//        .build());

      lookupOrderMappings(orderType).thenAccept(m -> {
        // Override the default mappings with the configured mappings

        mappings.putAll(m);
        new Mapper(mappings, this).map(doc)
          .thenAccept(future::complete);
      }).exceptionally(e -> {
        logger.error("Exception looking up mappings", e);
        future.completeExceptionally(e);
        return null;
      });
    } catch (Exception e) {
      logger.error("Exception mapping request", e);
      future.completeExceptionally(e);
    }

    return future;
  }

  public static OrderMapping.OrderType getOrderType(Document doc) {
    final XPath xpath = XPathFactory.newInstance().newXPath();
    OrderMapping.OrderType orderType;

    try {
      Node node = (Node) xpath.evaluate("//ListedElectronicMonograph|//ListedElectronicSerial|//ListedPrintMonograph|//ListedPrintSerial|//UnlistedPrintMonograph|//UnlistedPrintSerial", doc, XPathConstants.NODE);
      orderType = OrderMapping.OrderType.fromValue(node.getNodeName());
    } catch (Exception e) {
      logger.error("Cannot determine order type", e);
      orderType = null;
    }

    return orderType;
  }

  public CompletableFuture<Integer> getPurchaseOptionCode(Object s){
    if (s != null) {
      return CompletableFuture.completedFuture(3);
    } else {
      return CompletableFuture.completedFuture(null);
    }
  }

  public CompletableFuture<Document> parse(Reader entity) {
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

  public CompletableFuture<String> lookupLocationId(String location) {
    try {
      String query = HelperUtils.encodeValue(String.format("code==\"%s\"", location));
      return httpClient.request("/locations?query=" + query, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenApply(HelperUtils::extractLocationId)
        .exceptionally(t -> {
          logger.error("Exception looking up location id", t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling lookupLocationId", e);
      throw new CompletionException(e);
    }
  }

  public CompletableFuture<String> lookupMaterialTypeId(String materialType) {
    try {
      String query = HelperUtils.encodeValue(String.format("name==\"%s\"", materialType));
      return httpClient.request("/material-types?query=" + query, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenApply(HelperUtils::extractMaterialTypeId)
        .exceptionally(t -> {
          logger.error("Exception looking up material-type id", t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling lookupMaterialTypeId", e);

      throw new CompletionException(e);
    }
  }

  public CompletableFuture<String> lookupVendorId(String vendorCode) {
    try {
      String query = HelperUtils.encodeValue(String.format("code==\"%s\"", vendorCode));
      return httpClient.request(HttpMethod.GET, "/vendor?query=" + query, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenApply(HelperUtils::extractVendorId)
        .exceptionally(t -> {
          logger.error("Exception looking up vendor id", t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling lookupVendorId", e);
      throw new CompletionException(e);
    }
  }

  public CompletableFuture<String> lookupWorkflowStatusId(String workflowStatusCode) {
    try {
      String query = HelperUtils.encodeValue(String.format("code==\"%s\"", workflowStatusCode));
      return httpClient.request(HttpMethod.GET, "/workflow_status?query=" + query, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenApply(HelperUtils::extractWorkflowStatusId)
        .exceptionally(t -> {
          logger.error("Exception looking up workflow status id", t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling lookupWorkflowStatusId", e);
      throw new CompletionException(e);
    }
  }

  public CompletableFuture<String> lookupReceiptStatusId(String receiptStatusCode) {
    try {
      String query = HelperUtils.encodeValue(String.format("code==\"%s\"", receiptStatusCode));
      return httpClient.request(HttpMethod.GET, "/receipt_status?query=" + query, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenApply(HelperUtils::extractReceiptStatusId)
        .exceptionally(t -> {
          logger.error("Exception looking up receipt status id", t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling lookupReceiptStatusId", e);
      throw new CompletionException(e);
    }
  }

  public CompletableFuture<String> lookupPaymentStatusId(String paymentStatusCode) {
    try {
      String query = HelperUtils.encodeValue(String.format("code==\"%s\"", paymentStatusCode));
      return httpClient.request(HttpMethod.GET, "/payment_status?query=" + query, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenApply(HelperUtils::extractPaymentStatusId)
        .exceptionally(t -> {
          logger.error("Exception looking up payment status id", t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling lookupPaymentStatusId", e);
      throw new CompletionException(e);
    }
  }

  //TO DO - Needs implementation
  public CompletableFuture<String> lookupFundId(String fundCode) {
	    return CompletableFuture.completedFuture(UUID.randomUUID().toString());
	  }
  
  public CompletableFuture<String> lookupActivationStatusId(String activationStatusCode) {
    try {
      String query = HelperUtils.encodeValue(String.format("code==\"%s\"", activationStatusCode));
      return httpClient.request(HttpMethod.GET, "/activation_status?query=" + query, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenApply(HelperUtils::extractActivationStatusId)
        .exceptionally(t -> {
          logger.error("Exception looking up activation status id", t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling lookupActivationStatusId", e);
      throw new CompletionException(e);
    }
  }

  public CompletableFuture<Map<Mapping.Field, DataSource>> lookupOrderMappings(OrderMapping.OrderType orderType) {
    try {
      final String query = HelperUtils.encodeValue(
          String.format("(module==%s AND configName==%s AND code==%s)",
              CONFIGURATION_MODULE,
              CONFIGURATION_CONFIG_NAME,
              CONFIGURATION_CODE));
      return httpClient.request(HttpMethod.GET,
          "/configurations/entries?query=" + query, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenApply(jo -> HelperUtils.extractOrderMappings(orderType, jo))
        .exceptionally(t -> {
          logger.error("Exception looking up order mappings", t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling lookupOrderMappings", e);
      throw new CompletionException(e);
    }
  }

  public CompletableFuture<String> placeOrder(CompositePurchaseOrder compPO) {
    VertxCompletableFuture<String> future = new VertxCompletableFuture<>(ctx);
    try {
      httpClient.request(HttpMethod.POST, compPO, "/orders", okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .thenAccept(body -> {
          logger.info("Response from mod-orders: " + body.encodePrettily());
          future.complete(body.getJsonArray("po_lines").getJsonObject(0).getString("po_line_number"));
        })
        .exceptionally(t -> {
          future.completeExceptionally(t);
          return null;
        });
    } catch (Exception e) {
      logger.error("Exception calling POST /orders", e);
      future.completeExceptionally(e);
    }
    return future;
  }

  public static String getUuid(String okapiToken) throws InvalidTokenException {

    logger.info(okapiToken);
    if (okapiToken == null || okapiToken.equals("")) {
      throw new InvalidTokenException("x-okapi-tenant is NULL or empty");
    }

    JsonObject tokenJson = getClaims(okapiToken);
    if (tokenJson != null) {
      String userId = tokenJson.getString("user_id");

      if (userId == null || userId.equals("")) {
        throw new InvalidTokenException("user_id is not found in x-okapi-token");
      }
      return userId;

    } else {
      throw new InvalidTokenException("user_id is not found in x-okapi-token");
    }
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
    final Future<javax.ws.rs.core.Response> result;

    logger.error("Exception placing order", throwable.getCause());

    final Throwable t = throwable.getCause();
    if (t instanceof HttpException) {
      final int code = ((HttpException) t).getCode();
      final String message = ((HttpException) t).getMessage();
      switch (code) {
      case 400:
        GobiResponse response = new GobiResponse();
        response.setError(new ResponseError());
        response.getError().setCode(CODE_BAD_REQUEST);
        response.getError().setMessage(HelperUtils.truncate(t.getMessage(), 500));
        result = Future
          .succeededFuture(PostGobiOrdersResponse.withXmlBadRequest(GobiResponseWriter.getWriter().write(response)));
        break;
      case 500:
        result = Future.succeededFuture(PostGobiOrdersResponse.withPlainInternalServerError(message));
        break;
      case 401:
        result = Future.succeededFuture(PostGobiOrdersResponse.withPlainUnauthorized(message));
        break;
      default:
        result = Future.succeededFuture(PostGobiOrdersResponse.withPlainInternalServerError(message));
      }
    } else if (t instanceof GobiPurchaseOrderParserException) {
      GobiResponse response = new GobiResponse();
      response.setError(new ResponseError());
      response.getError().setCode(CODE_INVALID_XML);
      response.getError().setMessage(HelperUtils.truncate(t.getMessage(), 500));
      result = Future
        .succeededFuture(PostGobiOrdersResponse.withXmlBadRequest(GobiResponseWriter.getWriter().write(response)));
    } else if (t instanceof InvalidTokenException) {
      GobiResponse response = new GobiResponse();
      response.setError(new ResponseError());
      response.getError().setCode(CODE_INVALID_TOKEN);
      response.getError().setMessage(HelperUtils.truncate(t.getMessage(), 500));
      result = Future
        .succeededFuture(PostGobiOrdersResponse.withXmlBadRequest(GobiResponseWriter.getWriter().write(response)));
    } else {
      result = Future.succeededFuture(PostGobiOrdersResponse.withPlainInternalServerError(throwable.getMessage()));
    }

    if (httpClient != null) {
      httpClient.closeClient();
    }

    asyncResultHandler.handle(result);

    return null;
  }

}

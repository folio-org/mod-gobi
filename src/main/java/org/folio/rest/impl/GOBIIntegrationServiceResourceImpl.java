package org.folio.rest.impl;

import java.io.Reader;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import io.vertx.core.*;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.folio.gobi.GOBIResponseWriter;
import org.folio.gobi.HttpException;
import org.folio.gobi.PurchaseOrderParser;
import org.folio.gobi.PurchaseOrderParserException;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.GOBIResponse;
import org.folio.rest.jaxrs.model.PurchaseOrder;
import org.folio.rest.jaxrs.model.ResponseError;
import org.folio.rest.jaxrs.resource.GOBIIntegrationServiceResource;
import org.folio.rest.tools.client.HttpClientFactory;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.rest.tools.utils.TenantTool;

public class GOBIIntegrationServiceResourceImpl
    implements GOBIIntegrationServiceResource {

  @Override
  public void getGobiValidate(Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    asyncResultHandler.handle(Future.succeededFuture(GetGobiValidateResponse.withNoContent()));
  }

  @Override
  public void postGobiOrders(Reader entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    final GOBIResponse response = new GOBIResponse();
    final PurchaseOrderParser parser = PurchaseOrderParser.getParser();

    try {
      PurchaseOrder order = parser.parse(entity);
      getUuid(okapiHeaders.get(("x-okapi-token")));
      getVendorId(order.getOrder().getListedElectronicMonograph().getOrderDetail().getPurchaseOption().getVendorCode(), okapiHeaders);

    } catch (PurchaseOrderParserException e) {
      final ResponseError re = new ResponseError();
      re.setCode("INVALID_XML");
      re.setMessage(e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)));
      response.setError(re);
      asyncResultHandler.handle(Future.succeededFuture(PostGobiOrdersResponse.withXmlBadRequest(GOBIResponseWriter.getWriter().write(response))));
      return;
    }
    catch (IllegalArgumentException e){
      final ResponseError re = new ResponseError();
      re.setCode("INVALID_TOKEN");
      re.setMessage(e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)));
      response.setError(re);
      asyncResultHandler.handle(Future.succeededFuture(PostGobiOrdersResponse.withXmlBadRequest(GOBIResponseWriter.getWriter().write(response))));
      return;
    }

    final String poLineNumber = new Random().ints(16, 0, 9).mapToObj(Integer::toString).collect(Collectors.joining());
    response.setPoLineNumber(poLineNumber);

    asyncResultHandler.handle(Future.succeededFuture(PostGobiOrdersResponse.withXmlCreated(GOBIResponseWriter.getWriter().write(response))));
  }

  public static String getUuid(String okapiToken) {

    if (okapiToken == null || okapiToken.equals("") ){
      throw new IllegalArgumentException("x-okapi-tenant is NULL or empty");
    }

    JsonObject tokenJson = getClaims(okapiToken);
    if (tokenJson != null) {
      String userId = tokenJson.getString("user_id");

      if (userId  == null || userId.equals("") ){
        throw new IllegalArgumentException("user_id is not found in x-okapi-token");
      }
      return userId;

    } else {
      throw new IllegalArgumentException("user_id is not found in x-okapi-token");
    }
  }

  public static JsonObject getClaims(String token){
    String[] tokenPieces = token.split("\\.");
    if (tokenPieces.length > 1){
      String encodedJson = tokenPieces[1];
      if (encodedJson == null) {
        return null;
      }

      String decodedJson = new String(Base64.getDecoder().decode(encodedJson));
      return new JsonObject(decodedJson);
    }
    return null;
  }

  public CompletableFuture<String> getVendorId(String vendorCode, Map<String, String>  headers) throws Exception {

    if (vendorCode == null || vendorCode.equals("")){
      throw new IllegalArgumentException("vendor code is NULL or empty");
    }

    if (headers == null || headers.isEmpty()){
      throw new IllegalArgumentException("headers is NULL or empty");
    }

    HttpClientInterface httpClient = getHttpClient(headers);

    try {
      return httpClient.request("/vendor?query=code==" + vendorCode, headers)
        .thenApply(this::verifyAndExtractBody)
          .thenApply(this::extractVendorId);

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    finally {
      httpClient.closeClient(); //or pass it in
    }
  }

  public String extractVendorId(JsonObject obj) {
    JsonArray jsonArray = obj.getJsonArray("vendors");
    JsonObject item = jsonArray.getJsonObject(0);
    String id = item.getString("id");
    return id;
  }

  private HttpClientInterface getHttpClient(Map<String, String> okapiHeaders) {
    final String okapiURL = okapiHeaders.getOrDefault("X-Okapi-Url", "");
    final String tenantId = TenantTool.calculateTenantId(okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT));

    return HttpClientFactory.getHttpClient(okapiURL, tenantId);
  }

  private JsonObject verifyAndExtractBody(org.folio.rest.tools.client.Response response) {
    if (!org.folio.rest.tools.client.Response.isSuccess(response.getCode())) {
      throw new CompletionException(new HttpException(response.getCode(), response.getError().toString()));
    }

    return response.getBody();
  }
}

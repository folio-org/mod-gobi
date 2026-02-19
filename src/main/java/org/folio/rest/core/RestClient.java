package org.folio.rest.core;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.folio.rest.core.ExceptionUtil.getHttpException;
import java.util.Map;
import org.folio.okapi.common.WebClientFactory;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.utils.TenantTool;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RestClient {

  private final WebClient webClient;
  private final Context ctx;
  private final MultiMap okapiHeaders;
  private final String okapiURL;
  private final String tenantId;
  public static final String X_OKAPI_URL = "X-Okapi-Url";

  public RestClient(Map<String, String> okapiHeaders, Context ctx) {
    okapiURL = okapiHeaders.getOrDefault(X_OKAPI_URL, "");
    tenantId = TenantTool.calculateTenantId(okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT));
    webClient = WebClientFactory.getWebClient(ctx.owner());
    this.ctx = ctx;
    this.okapiHeaders = MultiMap.caseInsensitiveMultiMap().addAll(okapiHeaders);
  }

  public Context getCtx() {
    return ctx;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Future<JsonObject> handleGetRequest(String endpoint) {
    return webClient.getAbs(okapiURL + endpoint).putHeaders(okapiHeaders)
      .send()
      .compose(RestClient::convertHttpResponse)
      .map(HttpResponse::bodyAsJsonObject);
  }

  /**
   * A common method to update an entry
   *
   * @param recordData json to use for update operation
   * @param endpoint   endpoint
   */
  public Future<Void> handlePutRequest(String endpoint, JsonObject recordData) {
    return webClient.putAbs(okapiURL + endpoint).putHeaders(okapiHeaders)
      .sendJsonObject(recordData)
      .compose(RestClient::convertHttpResponse)
      .mapEmpty();
  }

  public Future<JsonObject> post(String endpoint, JsonObject recordData) {
    return webClient.postAbs(okapiURL + endpoint).putHeaders(okapiHeaders)
      .sendJsonObject(recordData)
      .compose(RestClient::convertHttpResponse)
      .map(HttpResponse::bodyAsJsonObject);
  }

  public Future<Void> delete(String endpointById) {
    log.debug("Tying to delete with using endpoint: {}", endpointById);
    return webClient.deleteAbs(okapiURL + endpointById)
      .putHeaders(okapiHeaders).putHeader("Accept", APPLICATION_JSON + ", " + TEXT_PLAIN)
      .send()
      .compose(RestClient::convertHttpResponse)
      .mapEmpty();
  }

  protected static <T> Future<HttpResponse<T>> convertHttpResponse(HttpResponse<T> response) {
    return HttpResponseExpectation.SC_SUCCESS.test(response)
      ? Future.succeededFuture(response)
      : Future.failedFuture(getHttpException(response.statusCode(), response.bodyAsString()));
  }

}

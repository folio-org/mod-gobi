package org.folio.rest.core;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.folio.rest.service.GobiCustomMappingsService.SEARCH_ENDPOINT;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.exceptions.HttpException;
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

public class RestClient {

  private static final Logger logger = LogManager.getLogger(RestClient.class);
  public static final String X_OKAPI_URL = "X-Okapi-Url";

  private final WebClient webClient;
  private final MultiMap okapiHeaders;
  private final String okapiURL;
  private final String tenantId;

  public RestClient(Map<String, String> okapiHeaders, Context ctx) {
    okapiURL = okapiHeaders.getOrDefault(X_OKAPI_URL, "");
    tenantId = TenantTool.calculateTenantId(okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT));
    webClient = WebClientFactory.getWebClient(ctx.owner());
    this.okapiHeaders = MultiMap.caseInsensitiveMultiMap().addAll(okapiHeaders);
  }

  public String getTenantId() {
    return tenantId;
  }

  public Future<JsonObject> handleGetRequest(String endpoint) {
    logger.debug("Trying to get '{}' object", endpoint);
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
    if (logger.isDebugEnabled()) {
      logger.debug("Trying to put '{}' object with data: {}", endpoint, recordData.encodePrettily());
    }
    return webClient.putAbs(okapiURL + endpoint).putHeaders(okapiHeaders)
      .sendJsonObject(recordData)
      .compose(RestClient::convertHttpResponse)
      .mapEmpty();
  }

  public Future<JsonObject> post(String endpoint, JsonObject recordData) {
    if (logger.isDebugEnabled()) {
      logger.debug("Trying to post '{}' object with body: {}", endpoint, recordData.encodePrettily());
    }
    return webClient.postAbs(okapiURL + endpoint).putHeaders(okapiHeaders)
      .sendJsonObject(recordData)
      .compose(RestClient::convertHttpResponse)
      .map(HttpResponse::bodyAsJsonObject);
  }

  public Future<Void> delete(String endpointById) {
    logger.debug("Tying to delete with using endpoint: {}", endpointById);
    return webClient.deleteAbs(okapiURL + endpointById)
      .putHeaders(okapiHeaders).putHeader("Accept", APPLICATION_JSON + ", " + TEXT_PLAIN)
      .send()
      .compose(RestClient::convertHttpResponse)
      .mapEmpty();
  }

  public Future<JsonObject> handleGetRequest(String baseEndpoint, String query, int offset, int limit) {
    String endpoint = String.format(SEARCH_ENDPOINT, baseEndpoint, limit, offset, buildQueryParam(query));
    return handleGetRequest(endpoint);
  }

  private static <T> Future<HttpResponse<T>> convertHttpResponse(HttpResponse<T> response) {
    return HttpResponseExpectation.SC_SUCCESS.test(response)
      ? Future.succeededFuture(response)
      : Future.failedFuture(new HttpException(response.statusCode(), response.bodyAsString()));
  }

  private static String buildQueryParam(String query) {
    return isEmpty(query) ? EMPTY : "&query=" + encodeQuery(query);
  }

  private static String encodeQuery(String query) {
    return URLEncoder.encode(query, StandardCharsets.UTF_8);
  }

}

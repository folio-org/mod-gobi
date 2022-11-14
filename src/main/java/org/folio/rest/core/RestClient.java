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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ErrorConverter;
import io.vertx.ext.web.client.predicate.ResponsePredicate;

public class RestClient {
  private static final Logger logger = LogManager.getLogger(RestClient.class);
  private final WebClient webClient;
  private final Context ctx;
  private final MultiMap okapiHeaders;
  private final String okapiURL;
  private final String tenantId;
  public static final String X_OKAPI_URL = "X-Okapi-Url";
  private static final ErrorConverter ERROR_CONVERTER = ErrorConverter.createFullBody(
      result -> new HttpException(result.response().statusCode(), result.response().bodyAsString()));
  private static final ResponsePredicate SUCCESS_RESPONSE_PREDICATE =
      ResponsePredicate.create(ResponsePredicate.SC_SUCCESS, ERROR_CONVERTER);

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
    logger.debug("Trying to get '{}' object", endpoint);
    return webClient.getAbs(okapiURL + endpoint).putHeaders(okapiHeaders)
        .expect(SUCCESS_RESPONSE_PREDICATE).send()
        .map(HttpResponse::bodyAsJsonObject);
  }

  /**
   * A common method to update an entry
   *
   * @param recordData json to use for update operation
   * @param endpoint endpoint
   */
  public Future<Void> handlePutRequest(String endpoint, JsonObject recordData) {
    logger.debug("Trying to put '{}' object with data: {}", endpoint, recordData.encodePrettily());
    return webClient.putAbs(okapiURL + endpoint).putHeaders(okapiHeaders)
        .expect(SUCCESS_RESPONSE_PREDICATE).sendJsonObject(recordData)
        .mapEmpty();
  }

  public Future<JsonObject> post(String endpoint, JsonObject recordData) {
    logger.debug("Trying to post '{}' object with body: {}", endpoint, recordData.encodePrettily());
    return webClient.postAbs(okapiURL + endpoint).putHeaders(okapiHeaders)
        .expect(SUCCESS_RESPONSE_PREDICATE).sendJsonObject(recordData)
        .map(HttpResponse::bodyAsJsonObject);
  }

  public Future<Void> delete(String endpointById) {
    logger.debug("Tying to delete with using endpoint: {}", endpointById);
    return webClient.deleteAbs(okapiURL + endpointById)
        .putHeaders(okapiHeaders).putHeader("Accept", APPLICATION_JSON + ", " + TEXT_PLAIN)
        .expect(SUCCESS_RESPONSE_PREDICATE).send()
        .mapEmpty();
  }

  public Future<JsonObject> handleGetRequest(String baseEndpoint, String query, int offset, int limit) {
    String endpoint = String.format(SEARCH_ENDPOINT, baseEndpoint, limit, offset, buildQueryParam(query));

    return handleGetRequest(endpoint);
  }

  public String buildQueryParam(String query) {
    return isEmpty(query) ? EMPTY : "&query=" + encodeQuery(query);
  }

  public String encodeQuery(String query) {
    return URLEncoder.encode(query, StandardCharsets.UTF_8);
  }

}

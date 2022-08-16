package org.folio.rest.core;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.folio.rest.service.GobiCustomMappingsService.SEARCH_ENDPOINT;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.completablefuture.FolioVertxCompletableFuture;
import org.folio.gobi.HelperUtils;
import org.folio.gobi.exceptions.ErrorCodes;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.client.HttpClientFactory;
import org.folio.rest.tools.client.Response;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.rest.tools.utils.TenantTool;

import io.vertx.core.Context;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class RestClient {
  private static final Logger logger = LogManager.getLogger(RestClient.class);
  private static final String EXCEPTION_CALLING_ENDPOINT_MSG = "Exception calling %s %s";
  private static final String ERROR_MESSAGE = "errorMessage";

  private final HttpClientInterface httpClient;
  private final Context ctx;
  private final Map<String, String> okapiHeaders;
  public static final String X_OKAPI_URL = "X-Okapi-Url";
  private static final String CALLING_ENDPOINT_MSG = "Sending {} {}";


  public RestClient(Map<String, String> okapiHeaders, Context ctx) {
    final String okapiURL = okapiHeaders.getOrDefault(X_OKAPI_URL, "");
    final String tenantId = TenantTool.calculateTenantId(okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT));

    this.httpClient = HttpClientFactory.getHttpClient(okapiURL, tenantId);
    this.ctx = ctx;
    this.okapiHeaders = okapiHeaders;
  }

  public Context getCtx() {
    return ctx;
  }

  public Map<String, String> getOkapiHeaders() {
    return Collections.unmodifiableMap(okapiHeaders);
  }

  public CompletableFuture<JsonObject> handleGetRequest(String endpoint) {
    FolioVertxCompletableFuture<JsonObject> future = new FolioVertxCompletableFuture<>(ctx);
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
          String errorMessage = String.format(EXCEPTION_CALLING_ENDPOINT_MSG, HttpMethod.GET, endpoint);
          logger.error(errorMessage, t);
          future.completeExceptionally(t);
          return null;
        });
    } catch (Exception e) {
      String errorMessage = String.format(EXCEPTION_CALLING_ENDPOINT_MSG, HttpMethod.GET, endpoint);
      logger.error(errorMessage, e);
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
  public CompletableFuture<Void> handlePutRequest(String endpoint, JsonObject recordData) {
    FolioVertxCompletableFuture<Void> future = new FolioVertxCompletableFuture<>(ctx);
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
          String errorMessage = String.format("'PUT %s' request failed. Request body: %s", endpoint, recordData.encodePrettily());
          logger.error(errorMessage, e);
          future.completeExceptionally(e);
          return null;
        });
    } catch (Exception e) {
      future.completeExceptionally(e);
    }

    return future;
  }

  public  CompletableFuture<JsonObject> post(String endpoint, JsonObject recordData) {
    CompletableFuture<JsonObject> future = new FolioVertxCompletableFuture<>(ctx);

    if (logger.isDebugEnabled()) {
      logger.debug("Sending 'POST {}' with body: {}", endpoint, recordData.encodePrettily());
    }

    try {
      httpClient
        .request(HttpMethod.POST, recordData.toBuffer(), endpoint, okapiHeaders)
        .thenApply(HelperUtils::verifyAndExtractBody)
        .handle((body, t) -> {
          if (nonNull(t)) {
            logger.error("'POST {}' request failed with error {}. Request body: {}", t.getCause(), endpoint, recordData.encodePrettily());
            future.completeExceptionally(t.getCause());
          } else {
            if (logger.isDebugEnabled()) {
              logger.debug("'POST {}' request successfully processed. Record with '{}' id has been created", endpoint, body);
            }
            future.complete(body);
          }
          return null;
        });
    } catch (Exception e) {
      logger.error("'POST {}' request failed with error {}. Request body: {}", e, endpoint, recordData.encodePrettily());
      future.completeExceptionally(e);
    }

    return future;
  }

  public CompletableFuture<Void> delete(String endpointById) {
    CompletableFuture<Void> future = new FolioVertxCompletableFuture<>(ctx);
    if (logger.isDebugEnabled()) {
      logger.debug(CALLING_ENDPOINT_MSG, HttpMethod.DELETE, endpointById);
    }
    setDefaultHeaders(httpClient);

    try {
      httpClient.request(HttpMethod.DELETE, endpointById, okapiHeaders)
        .thenAccept(this::verifyResponse)
        .handle((aVoid, t) -> {
          if (nonNull(t)) {
            logger.error(EXCEPTION_CALLING_ENDPOINT_MSG, HttpMethod.DELETE, endpointById);
            future.completeExceptionally(t.getCause());
          } else {
            future.complete(null);
          }
          return null;
        });
    } catch (Exception e) {
      logger.error(EXCEPTION_CALLING_ENDPOINT_MSG, HttpMethod.DELETE, endpointById);
      future.completeExceptionally(e);
    }

    return future;
  }

  public CompletableFuture<JsonObject> handleGetRequest(String baseEndpoint, String query, int offset, int limit) {
    String endpoint = String.format(SEARCH_ENDPOINT, baseEndpoint, limit, offset, buildQueryParam(query));

    return handleGetRequest(endpoint);
  }

  public String buildQueryParam(String query) {
    return isEmpty(query) ? EMPTY : "&query=" + encodeQuery(query);
  }

  public String encodeQuery(String query) {
    return URLEncoder.encode(query, StandardCharsets.UTF_8);
  }

  private void verifyResponse(Response response) {
    if (!Response.isSuccess(response.getCode())) {
      String errorMsg = response.getError().getString(ERROR_MESSAGE);
      HttpException httpException = getErrorByCode(errorMsg)
        .map(errorCode -> new HttpException(response.getCode(), errorCode))
        .orElse(new HttpException(response.getCode(), errorMsg));
      throw new CompletionException(httpException);
    }
  }

  private Optional<ErrorCodes> getErrorByCode(String errorCode){
    return EnumSet.allOf(ErrorCodes.class).stream()
      .filter(errorCodes -> errorCodes.getCode().equals(errorCode))
      .findAny();
  }

  private void setDefaultHeaders(HttpClientInterface httpClient) {
    httpClient.setDefaultHeaders(Collections.singletonMap("Accept", APPLICATION_JSON + ", " + TEXT_PLAIN));
  }

}

package org.folio.rest.core;

import static java.util.Objects.nonNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.completablefuture.FolioVertxCompletableFuture;
import org.folio.gobi.HelperUtils;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;

import io.vertx.core.Context;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class RestClient {
  private static final Logger logger = LogManager.getLogger(RestClient.class);
  private static final String EXCEPTION_CALLING_ENDPOINT_MSG = "Exception calling %s %s";

  private final HttpClientInterface httpClient;
  private final Context ctx;
  private final Map<String, String> okapiHeaders;

  public RestClient(HttpClientInterface httpClient, Map<String, String> okapiHeaders, Context ctx) {
    this.httpClient = httpClient;
    this.ctx = ctx;
    this.okapiHeaders = okapiHeaders;
  }

  public HttpClientInterface getHttpClient() {
    return httpClient;
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
}

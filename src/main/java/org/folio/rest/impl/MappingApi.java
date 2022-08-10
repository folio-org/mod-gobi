package org.folio.rest.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.RetrieveMappingDetailsService;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.resource.GobiOrdersMappings;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

public class MappingApi implements GobiOrdersMappings {

  private final RetrieveMappingDetailsService retrieveMappingDetailsService;
  private static final Logger logger = LogManager.getLogger(MappingApi.class);

  public MappingApi() {
    this.retrieveMappingDetailsService = new RetrieveMappingDetailsService();
  }

  @Override
  public void getGobiOrdersMappingsFields(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) {
    try {
      JsonArray fields = retrieveMappingDetailsService.retrieveFields();
      asyncResultHandler.handle(Future.succeededFuture(Response.ok(fields.getList(), APPLICATION_JSON).build()));
    } catch (Exception e) {
      logger.error(String.format("Error when getting mappings fields %s", e.getMessage()));
      asyncResultHandler.handle(Future.succeededFuture(Response.serverError().build()));
    }
  }

  @Override
  public void getGobiOrdersMappingsTranslators(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) {
    try {
      JsonArray translators = retrieveMappingDetailsService.retrieveTranslators();
      asyncResultHandler.handle(Future.succeededFuture(Response.ok(translators.getList(), APPLICATION_JSON).build()));
    } catch (Exception e) {
      logger.error(String.format("Error when getting mappings translators %s", e.getMessage()));
      asyncResultHandler.handle(Future.succeededFuture(Response.serverError().build()));
    }
  }

  @Override
  public void getGobiOrdersMappingsTypes(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) {
    try {
      List<OrderMappings.OrderType> types = retrieveMappingDetailsService.retrieveMappingsTypes();
      asyncResultHandler.handle(Future.succeededFuture(Response.ok(types, APPLICATION_JSON).build()));
    } catch (Exception e) {
      logger.error(String.format("Error when getting mappings types %s", e.getMessage()));
      asyncResultHandler.handle(Future.succeededFuture(Response.serverError().build()));
    }
  }
}

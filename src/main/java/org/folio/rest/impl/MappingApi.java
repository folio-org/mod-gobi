package org.folio.rest.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.MappingDetailsService;
import org.folio.rest.acq.model.FolioOrderFields;
import org.folio.rest.acq.model.FolioOrderTranslators;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.resource.GobiOrdersMappings;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class MappingApi implements GobiOrdersMappings {

  private final MappingDetailsService mappingDetailsService;
  private static final Logger logger = LogManager.getLogger(MappingApi.class);

  public MappingApi() {
    this.mappingDetailsService = new MappingDetailsService();
  }

  @Override
  public void getGobiOrdersMappingsFields(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) {
    try {
      FolioOrderFields fields = mappingDetailsService.retrieveFields();
      asyncResultHandler.handle(Future.succeededFuture(Response.ok(fields, APPLICATION_JSON).build()));
    } catch (Exception e) {
      logger.error(String.format("Error when getting mappings fields %s", e.getMessage()));
      asyncResultHandler.handle(Future.succeededFuture(Response.serverError().build()));
    }
  }

  @Override
  public void getGobiOrdersMappingsTranslators(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) {
    try {
      FolioOrderTranslators translators = mappingDetailsService.retrieveTranslators();
      asyncResultHandler.handle(Future.succeededFuture(Response.ok(translators, APPLICATION_JSON).build()));
    } catch (Exception e) {
      logger.error(String.format("Error when getting mappings translators %s", e.getMessage()));
      asyncResultHandler.handle(Future.succeededFuture(Response.serverError().build()));
    }
  }

  @Override
  public void getGobiOrdersMappingsTypes(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) {
    try {
      List<OrderMappings.OrderType> types = mappingDetailsService.retrieveMappingsTypes();
      asyncResultHandler.handle(Future.succeededFuture(Response.ok(types, APPLICATION_JSON).build()));
    } catch (Exception e) {
      logger.error(String.format("Error when getting mappings types %s", e.getMessage()));
      asyncResultHandler.handle(Future.succeededFuture(Response.serverError().build()));
    }
  }
}

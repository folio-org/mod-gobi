package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.gobi.HelperUtils.getEndpoint;
import static org.folio.rest.core.RestClient.X_OKAPI_URL;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.resource.GobiOrdersCustomMappings;
import org.folio.rest.service.GobiCustomMappingsService;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public class GobiOrdersCustomMappingsImpl extends BaseApi implements GobiOrdersCustomMappings {

  private static final Logger logger = LogManager.getLogger(GobiOrdersCustomMappingsImpl.class);
  private static final String GOBI_ORDERS_CUSTOM_MAPPINGS_LOCATION = getEndpoint(GobiOrdersCustomMappings.class) + "/%s";

  @Override
  public void getGobiOrdersCustomMappings(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    new GobiCustomMappingsService(okapiHeaders, vertxContext).getCustomMappingListByQuery(query, offset, limit)
      .thenAccept(orderMappingsViewCollection -> asyncResultHandler.handle(succeededFuture(buildOkResponse(orderMappingsViewCollection))))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));
  }


  @Override
  public void postGobiOrdersCustomMappings(String lang, OrderMappings orderMappings, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    new GobiCustomMappingsService(okapiHeaders, vertxContext).postCustomMapping(orderMappings)
      .thenAccept(createdCustomMapping -> asyncResultHandler.handle(succeededFuture(buildResponseWithLocation(okapiHeaders.get(X_OKAPI_URL),
              String.format(GOBI_ORDERS_CUSTOM_MAPPINGS_LOCATION, createdCustomMapping.getMappingType().value()), createdCustomMapping))))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));

  }

  @Override
  public void deleteGobiOrdersCustomMappingsByOrderType(String orderType, String lang, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    new GobiCustomMappingsService(okapiHeaders, vertxContext).deleteCustomMapping(orderType)
      .thenAccept(orderMappingsViewCollection -> asyncResultHandler.handle(succeededFuture(buildOkResponse(orderMappingsViewCollection))))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

  @Override
  public void putGobiOrdersCustomMappingsByOrderType(String orderType, String lang, OrderMappings orderMappings,
      Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    new GobiCustomMappingsService(okapiHeaders, vertxContext).putCustomMapping(orderType, orderMappings)
      .thenAccept(types -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

  @Override
  public void getGobiOrdersCustomMappingsByOrderType(String orderType, String lang, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    new GobiCustomMappingsService(okapiHeaders, vertxContext).getCustomMappingByOrderType(orderType)
      .thenAccept(type -> asyncResultHandler.handle(succeededFuture(buildOkResponse(type))))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

}

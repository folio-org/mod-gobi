package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.gobi.HelperUtils.getEndpoint;
import static org.folio.rest.core.RestClient.X_OKAPI_URL;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.rest.core.RestClient;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.resource.GobiOrdersCustomMappings;
import org.folio.rest.service.GobiCustomMappingsService;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public class GobiOrdersCustomMappingsImpl extends BaseApi implements GobiOrdersCustomMappings {
  private static final String GOBI_ORDERS_CUSTOM_MAPPINGS_LOCATION = getEndpoint(GobiOrdersCustomMappings.class) + "/%s";

  @Override
  public void getGobiOrdersCustomMappings(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    RestClient rc = new RestClient(okapiHeaders, vertxContext);
    new GobiCustomMappingsService(rc)
      .getCustomMappingListByQuery(offset, limit)
      .thenAccept(orderMappingsViewCollection -> asyncResultHandler.handle(succeededFuture(buildOkResponse(orderMappingsViewCollection))))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));
  }


  @Override
  public void postGobiOrdersCustomMappings(String lang, OrderMappings orderMappings, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    RestClient rc = new RestClient(okapiHeaders, vertxContext);
    new GobiCustomMappingsService(rc)
      .postCustomMapping(orderMappings)
      .thenAccept(createdCustomMapping -> asyncResultHandler.handle(succeededFuture(buildResponseWithLocation(okapiHeaders.get(X_OKAPI_URL),
              String.format(GOBI_ORDERS_CUSTOM_MAPPINGS_LOCATION, createdCustomMapping.getMappingType().value()), createdCustomMapping))))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));

  }

  @Override
  public void deleteGobiOrdersCustomMappingsByOrderType(String orderType, String lang, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    RestClient rc = new RestClient(okapiHeaders, vertxContext);
    new GobiCustomMappingsService(rc)
      .deleteCustomMapping(orderType)
      .thenAccept(orderMappingsViewCollection -> asyncResultHandler.handle(succeededFuture(buildOkResponse(orderMappingsViewCollection))))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

  @Override
  public void putGobiOrdersCustomMappingsByOrderType(String orderType, String lang, OrderMappings orderMappings,
      Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    RestClient rc = new RestClient(okapiHeaders, vertxContext);
    new GobiCustomMappingsService(rc)
      .putCustomMapping(orderType, orderMappings)
      .thenAccept(v -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

  @Override
  public void getGobiOrdersCustomMappingsByOrderType(String orderType, String lang, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    RestClient rc = new RestClient(okapiHeaders, vertxContext);
    new GobiCustomMappingsService(rc)
      .getCustomMappingByOrderType(orderType)
      .thenAccept(omv -> asyncResultHandler.handle(succeededFuture(buildOkResponse(omv))))
      .exceptionally(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

}

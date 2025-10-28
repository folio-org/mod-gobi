package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.rest.core.RestClient.X_OKAPI_URL;

import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.folio.dao.OrderMappingsDao;
import org.folio.dao.OrderMappingsDaoImpl;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.resource.GobiOrdersCustomMappings;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.service.GobiCustomMappingsService;
import org.folio.rest.tools.utils.TenantTool;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public class GobiOrdersCustomMappingsImpl extends BaseApi implements GobiOrdersCustomMappings {

  private static final String GOBI_ORDERS_CUSTOM_MAPPINGS_LOCATION = GobiOrdersCustomMappings.class.getAnnotation(Path.class).value() + "/%s";

  @Override
  public void getGobiOrdersCustomMappings(String query, String totalRecords, int offset, int limit, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    getCustomMappingsService(okapiHeaders, vertxContext)
      .getCustomMappingListByQuery(offset, limit)
      .onSuccess(orderMappingsViewCollection -> asyncResultHandler.handle(succeededFuture(buildOkResponse(orderMappingsViewCollection))))
      .onFailure(fail -> handleErrorResponse(asyncResultHandler, fail));
  }


  @Override
  @Validate
  public void postGobiOrdersCustomMappings(OrderMappings orderMappings, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    getCustomMappingsService(okapiHeaders, vertxContext)
      .postCustomMapping(orderMappings)
      .onSuccess(createdCustomMapping -> asyncResultHandler.handle(succeededFuture(buildResponseWithLocation(okapiHeaders.get(X_OKAPI_URL),
              String.format(GOBI_ORDERS_CUSTOM_MAPPINGS_LOCATION, createdCustomMapping.getMappingType().value()), createdCustomMapping))))
      .onFailure(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

  @Override
  public void deleteGobiOrdersCustomMappingsByOrderType(String orderType, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    getCustomMappingsService(okapiHeaders, vertxContext)
      .deleteCustomMapping(orderType)
      .onSuccess(orderMappingsViewCollection -> asyncResultHandler.handle(succeededFuture(buildOkResponse(orderMappingsViewCollection))))
      .onFailure(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

  @Override
  @Validate
  public void putGobiOrdersCustomMappingsByOrderType(String orderType, OrderMappings orderMappings,
      Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    getCustomMappingsService(okapiHeaders, vertxContext)
      .putCustomMapping(orderType, orderMappings)
      .onSuccess(v -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .onFailure(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

  @Override
  public void getGobiOrdersCustomMappingsByOrderType(String orderType, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    getCustomMappingsService(okapiHeaders, vertxContext)
      .getCustomMappingByOrderType(orderType)
      .onSuccess(omv -> asyncResultHandler.handle(succeededFuture(buildOkResponse(omv))))
      .onFailure(fail -> handleErrorResponse(asyncResultHandler, fail));
  }

  private GobiCustomMappingsService getCustomMappingsService(Map<String, String> okapiHeaders, Context vertxContext) {
    String tenantId = TenantTool.tenantId(okapiHeaders);
    PostgresClient pgClient = PostgresClient.getInstance(vertxContext.owner(), tenantId);
    OrderMappingsDao dao = new OrderMappingsDaoImpl();
    return new GobiCustomMappingsService(pgClient, dao);
  }
}

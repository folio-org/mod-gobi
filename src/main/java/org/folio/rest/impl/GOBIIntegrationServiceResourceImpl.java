package org.folio.rest.impl;

import java.io.Reader;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.folio.gobi.PurchaseOrderParser;
import org.folio.gobi.PurchaseOrderParserException;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Order;
import org.folio.rest.jaxrs.resource.GOBIIntegrationServiceResource;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class GOBIIntegrationServiceResourceImpl
    implements GOBIIntegrationServiceResource {

  @Validate
  @Override
  public void getGobiValidate(Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    asyncResultHandler.handle(Future.succeededFuture(GetGobiValidateResponse.withNoContent()));
  }

  @Validate
  @Override
  public void postGobiOrders(Reader entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    final PurchaseOrderParser parser = PurchaseOrderParser.getParser();

    try {
      parser.parse(entity);
    } catch (PurchaseOrderParserException e) {
      asyncResultHandler.handle(Future.succeededFuture(PostGobiOrdersResponse.withPlainBadRequest(e.getMessage())));
      return;
    }

    asyncResultHandler.handle(Future.succeededFuture(PostGobiOrdersResponse.withJsonCreated(new Order().withId(UUID.randomUUID().toString()))));
  }
}

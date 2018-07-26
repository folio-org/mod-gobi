package org.folio.rest.impl;

import java.io.Reader;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.folio.gobi.GOBIResponseWriter;
import org.folio.gobi.PurchaseOrderParser;
import org.folio.gobi.PurchaseOrderParserException;
import org.folio.rest.jaxrs.model.GOBIResponse;
import org.folio.rest.jaxrs.model.ResponseError;
import org.folio.rest.jaxrs.resource.GOBIIntegrationServiceResource;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class GOBIIntegrationServiceResourceImpl
    implements GOBIIntegrationServiceResource {

  @Override
  public void getGobiValidate(Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    asyncResultHandler.handle(Future.succeededFuture(GetGobiValidateResponse.withNoContent()));
  }

  @Override
  public void postGobiOrders(Reader entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    final GOBIResponse response = new GOBIResponse();
    final PurchaseOrderParser parser = PurchaseOrderParser.getParser();

    try {
      parser.parse(entity);
    } catch (PurchaseOrderParserException e) {
      final ResponseError re = new ResponseError();
      re.setCode("INVALID_XML");
      re.setMessage(e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)));
      response.setError(re);
      asyncResultHandler.handle(Future.succeededFuture(PostGobiOrdersResponse.withXmlBadRequest(GOBIResponseWriter.getWriter().write(response))));
      return;
    }

    final String poLineNumber = new Random().ints(16, 0, 9).mapToObj(Integer::toString).collect(Collectors.joining());
    response.setPoLineNumber(poLineNumber);

    asyncResultHandler.handle(Future.succeededFuture(PostGobiOrdersResponse.withXmlCreated(GOBIResponseWriter.getWriter().write(response))));
  }
}

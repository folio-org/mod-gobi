package org.folio.rest.impl;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.GobiResponseWriter;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.jaxrs.resource.Gobi;
import org.folio.rest.tools.utils.BinaryOutStream;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class GOBIIntegrationServiceResourceImpl implements Gobi {

  private static final Logger logger = LogManager.getLogger(GOBIIntegrationServiceResourceImpl.class);
  private static final String GET_DATA = "<test>GET - OK</test>";
  private static final String POST_DATA = "<test>POST - OK</test>";



  @Override
  public void getGobiValidate(Map<String, String> okapiHeaders,
      Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler, Context vertxContext) {
    BinaryOutStream binaryOutStream = new BinaryOutStream();
    binaryOutStream.setData(GET_DATA.getBytes(StandardCharsets.UTF_8));
    asyncResultHandler.handle(Future.succeededFuture(GetGobiValidateResponse.respond200WithApplicationXml(binaryOutStream)));
  }

  @Override
  public void postGobiOrders(String entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler, Context vertxContext) {

    PostGobiOrdersHelper helper = new PostGobiOrdersHelper(asyncResultHandler, okapiHeaders, vertxContext);

    logger.info("postGobiOrders:: Trying to parse request: {}", entity);
    helper.parse(entity)
      .thenCompose(gobiPO -> {
        logger.info("postGobiOrders:: Trying to create PO from request: {}", gobiPO);
        return helper.mapToPurchaseOrder(gobiPO);
      })
      .thenCompose(helper::getOrPlaceOrder)
      .thenAccept(poLineNumber -> {
        GobiResponse gobiResponse = new GobiResponse();
        gobiResponse.setPoLineNumber(poLineNumber);
        BinaryOutStream binaryOutStream = GobiResponseWriter.getWriter().write(gobiResponse);
        asyncResultHandler.handle(Future.succeededFuture(PostGobiOrdersResponse.respond201WithApplicationXml(binaryOutStream)));
      })
      .exceptionally(helper::handleError);
  }

  @Override
  public void postGobiValidate(Map<String, String> okapiHeaders, Handler<AsyncResult<javax.ws.rs.core.Response>> asyncResultHandler,
      Context vertxContext) {
    BinaryOutStream binaryOutStream = new BinaryOutStream();
    binaryOutStream.setData(POST_DATA.getBytes(StandardCharsets.UTF_8));

    asyncResultHandler.handle(Future.succeededFuture(PostGobiValidateResponse.respond200WithApplicationXml(binaryOutStream)));
  }

}

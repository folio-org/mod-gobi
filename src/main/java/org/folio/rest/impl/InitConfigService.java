package org.folio.rest.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.folio.gobi.MappingHelper;
import org.folio.rest.resource.interfaces.PostDeployVerticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class InitConfigService implements PostDeployVerticle {
  private static final Logger logger = Logger.getLogger(MappingHelper.class);
  
  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
    logger.info("Init Config Service");
    try {
      MappingHelper.defaultMapping();
    } catch (IOException e) {
      e.printStackTrace();
    }
    handler.handle(io.vertx.core.Future.succeededFuture(true));
  }

}

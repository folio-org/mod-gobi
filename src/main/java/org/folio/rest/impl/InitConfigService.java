package org.folio.rest.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.resource.interfaces.PostDeployVerticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

 public class InitConfigService implements PostDeployVerticle {
  private static final Logger logger = LogManager.getLogger(InitConfigService.class);

  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
    logger.info("Init Config Service");
    logger.info("Loading Default Mappings");
    handler.handle(io.vertx.core.Future.succeededFuture(true));
  }

}

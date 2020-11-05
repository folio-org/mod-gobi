 package org.folio.rest.impl;

 import org.folio.rest.resource.interfaces.PostDeployVerticle;

 import io.vertx.core.AsyncResult;
 import io.vertx.core.Context;
 import io.vertx.core.Handler;
 import io.vertx.core.Vertx;
 import io.vertx.core.logging.Logger;
 import io.vertx.core.logging.LoggerFactory;

public class InitConfigService implements PostDeployVerticle {
  private static final Logger logger = LoggerFactory.getLogger(InitConfigService.class);

  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
    logger.info("Init Config Service");
    logger.info("Loading Default Mappings");
    handler.handle(io.vertx.core.Future.succeededFuture(true));
  }

}

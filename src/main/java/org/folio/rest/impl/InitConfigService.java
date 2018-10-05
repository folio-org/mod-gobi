package org.folio.rest.impl;

import org.folio.gobi.MappingHelper;
import org.folio.rest.resource.interfaces.PostDeployVerticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class InitConfigService implements PostDeployVerticle {
	
	@Override
	  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {

	    System.out.println("Init Config Service");
	    /** hard code the secret key for now - in production env - change this to read from a secure place */

	    MappingHelper.defaultMapping();
	    handler.handle(io.vertx.core.Future.succeededFuture(true));
	  }

}

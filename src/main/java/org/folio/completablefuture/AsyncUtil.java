package org.folio.completablefuture;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class AsyncUtil {

  public static <T> Handler<AsyncResult<T>> asyncResultHandler(CompletableFuture<T> future) {
    Objects.requireNonNull(future);
    return result -> {
      if (result.succeeded()) {
        future.complete(result.result());
      } else {
        future.completeExceptionally(result.cause());
      }
    };
  }

  public static <T> CompletableFuture<T> executeBlocking(Context ctx, boolean isOrdered, Supplier<T> supplier) {
    Objects.requireNonNull(supplier);
    CompletableFuture<T> future = new CompletableFuture<>();
    ctx.owner().executeBlocking(blockingFeature -> {
      try {
        T result = supplier.get();
        blockingFeature.complete(result);
      } catch (Throwable e) {
        blockingFeature.fail(e);
      }
    }, isOrdered, asyncResultHandler(future));
    return future;
  }

  public static CompletableFuture<Void> executeBlocking(Context ctx, boolean isOrdered, Runnable runnable) {
    Objects.requireNonNull(runnable);
    CompletableFuture<Void> future = new CompletableFuture<>();
    ctx.owner().executeBlocking(blockingFeature -> {
      try {
        runnable.run();
        blockingFeature.complete(null);
      } catch (Throwable e) {
        blockingFeature.fail(e);
      }
    }, isOrdered, asyncResultHandler(future));
    return future;
  }
}

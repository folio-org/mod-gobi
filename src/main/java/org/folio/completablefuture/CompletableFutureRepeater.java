package org.folio.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CompletableFutureRepeater {
  /**
   * Run <code>task</code> repeatedly until it succeeds or it has run <code>max</code> times.
   * Return the result of the succeeding task, or the last run's failure if all runs fail.
   */
  public static <T> CompletableFuture<T> repeat(int max, Supplier<CompletableFuture<T>> task) {
    CompletableFuture<T> future = task.get();
    for (int i = 1; i < max; i++) {
      future = future.thenApply(CompletableFuture::completedFuture)
          .exceptionally(t -> task.get())
          .thenCompose(Function.identity());
    }
    return future;
  }
}

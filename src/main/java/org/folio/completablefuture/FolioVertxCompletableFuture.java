package org.folio.completablefuture;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.*;

/**
 *  * NOTICE:
 *  <p>
 *  Original class <a href="https://github.com/cescoffier/vertx-completable-future/blob/vertx-completable-future-0.1.2/src/main/java/me/escoffier/vertx/completablefuture/VertxCompletableFuture.java">VertxCompletableFuture</a>
 *  had been developed by <a href="https://github.com/cescoffier">Clement Escoffier</a>
 *  <br/>
 *  Original class had been copied from the repository above and updated with following changes: <br/>
 *    1. Renamed the class and related constructors from VertxCompletableFuture to FolioVertxCompletableFuture<br/>
 *    2. Removed unused methods from "Composite Future implementation" block<br/>
 *    3. Reformatted the code<br/><br/>
 *  </p>
 * An implementation of {@link CompletableFuture} for Vert.x.4.x.x  It differs in the way to handle async calls:
 * <p>
 * * {@link FolioVertxCompletableFuture} are attached to a Vert.x {@link Context}
 * * All operator methods returns {@link FolioVertxCompletableFuture}
 * * <em*async</em> method not passing an {@link Executor} are executed on the attached {@link Context}
 * * All non async method are executed on the current Thread (so not necessary on the attached {@link Context}
 * <p>
 * The class also offer bridges methods with Vert.x {@link Future}, and regular {@link CompletableFuture}.
 *
 * @param <T> the expected type of result
 */
@SuppressWarnings("WeakerAccess")
public class FolioVertxCompletableFuture<T> extends CompletableFuture<T> implements CompletionStage<T> {
    private final Executor executor;

    /**
     * The {@link Context} used by the future.
     */
    private final Context context;

    // ============= Constructors =============

    /**
     * Creates an instance of {@link FolioVertxCompletableFuture}, using the given {@link Context}.
     *
     * @param context the context
     */
    public FolioVertxCompletableFuture(Context context) {
        this.context = Objects.requireNonNull(context);
        this.executor = command -> context.runOnContext(v -> command.run());
    }

    /**
     * Creates a new {@link FolioVertxCompletableFuture} from the given context and given {@link CompletableFuture}.
     * The created {@link FolioVertxCompletableFuture} is completed successfully or not when the given completable future
     * completes successfully or not.
     *
     * @param context the context
     * @param future  the completable future
     */
    private FolioVertxCompletableFuture(Context context, CompletableFuture<T> future) {
        this(context);
        Objects.requireNonNull(future).whenComplete((res, err) -> {
            if (err != null) {
                completeExceptionally(err);
            } else {
                complete(res);
            }
        });
    }

    // ============= Factory methods (from) =============

     /**
     * Creates a {@link FolioVertxCompletableFuture} from the given {@link Context} and {@link CompletableFuture}.
     * <p>
     * The created {@link FolioVertxCompletableFuture} is completed successfully or not when the given future
     * completes successfully or not. The completion is called on the given {@link Context}, immediately if it is
     * already executing on the right context, asynchronously if not.
     *
     * @param context the context
     * @param future  the future
     * @param <T>     the type of result
     * @return the creation {@link FolioVertxCompletableFuture}
     */
    public static <T> FolioVertxCompletableFuture<T> from(Context context, CompletableFuture<T> future) {
        FolioVertxCompletableFuture<T> res = new FolioVertxCompletableFuture<>(Objects.requireNonNull(context));
        Objects.requireNonNull(future).whenComplete((result, error) -> {
            if (context == Vertx.currentContext()) {
                res.complete(result, error);
            } else {
                res.context.runOnContext(v -> res.complete(result, error));
            }
        });
        return res;
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the current Vert.x
     * {@link Context} with the value obtained by calling the given Supplier.
     * <p>
     * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
     * executor, but use the Vert.x context.
     *
     * @param context  the context in which the supplier is executed.
     * @param supplier a function returning the value to be used to complete the returned CompletableFuture
     * @param <T>      the function's return type
     * @return the new CompletableFuture
     */
    public static <T> FolioVertxCompletableFuture<T> supplyAsync(Context context, Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        FolioVertxCompletableFuture<T> future = new FolioVertxCompletableFuture<>(Objects.requireNonNull(context));
        context.runOnContext(v -> {
            try {
                future.complete(supplier.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the
     * current Vert.x {@link Context} after it runs the given action.
     * <p>
     * This method is different from {@link CompletableFuture#runAsync(Runnable)} as it does not use a fork join
     * executor, but use the Vert.x context.
     *
     * @param context  the context
     * @param runnable the action to run before completing the returned CompletableFuture
     * @return the new CompletableFuture
     */
    public static FolioVertxCompletableFuture<Void> runAsync(Context context, Runnable runnable) {
        Objects.requireNonNull(runnable);
        FolioVertxCompletableFuture<Void> future = new FolioVertxCompletableFuture<>(context);
        context.runOnContext(v -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the worker thread pool of
     * Vert.x
     * <p>
     * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
     * executor, but the worker thread pool.
     *
     * @param context  the context in which the supplier is executed.
     * @param supplier a function returning the value to be used to complete the returned CompletableFuture
     * @param <T>      the function's return type
     * @return the new CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyBlockingAsync(Context context, Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        FolioVertxCompletableFuture<T> future = new FolioVertxCompletableFuture<>(context);
        context.<T>executeBlocking(
                promise -> {
                    try {
                        promise.complete(supplier.get());
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                },
                ar -> {
                    if (ar.failed()) {
                        future.completeExceptionally(ar.cause());
                    } else {
                        future.complete(ar.result());
                    }
                }
        );
        return future;
    }

    // ============= Parallel composition methods =============

    /**
     * Returns a new CompletableFuture that is completed when all of the given CompletableFutures complete.  If any of
     * the given CompletableFutures complete exceptionally, then the returned CompletableFuture also does so, with a
     * CompletionException holding this exception as its cause.  Otherwise, the results, if any, of the given
     * CompletableFutures are not reflected in the returned CompletableFuture, but may be obtained by inspecting them
     * individually. If no CompletableFutures are provided, returns a CompletableFuture completed with the value
     * {@code null}.
     * <p>
     * <p>Among the applications of this method is to await completion
     * of a set of independent CompletableFutures before continuing a
     * program, as in: {@code CompletableFuture.allOf(c1, c2, c3).join();}.
     * <p>
     * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
     * stages into the Vert.x context.
     *
     * @param context the context
     * @param futures the CompletableFutures
     * @return a new CompletableFuture that is completed when all of the given CompletableFutures complete
     * @throws NullPointerException if the array or any of its elements are {@code null}
     */
    public static FolioVertxCompletableFuture<Void> allOf(Context context, CompletableFuture<?>... futures) {
        CompletableFuture<Void> all = CompletableFuture.allOf(futures);
        return FolioVertxCompletableFuture.from(context, all);
    }

    // ============= Composite Future implementation =============

    @Override
    public <U> FolioVertxCompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return new FolioVertxCompletableFuture<>(context, super.thenApply(fn));
    }

    @Override
    public FolioVertxCompletableFuture<Void> thenRun(Runnable action) {
        return new FolioVertxCompletableFuture<>(context, super.thenRun(action));
    }

    @Override
    public <U, V> FolioVertxCompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return new FolioVertxCompletableFuture<>(context, super.thenCombine(other, fn));
    }

    @Override
    public <U> FolioVertxCompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return new FolioVertxCompletableFuture<>(context, super.thenCompose(fn));
    }

    @Override
    public FolioVertxCompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return new FolioVertxCompletableFuture<>(context, super.whenComplete(action));
    }

    @Override
    public <U> FolioVertxCompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return new FolioVertxCompletableFuture<>(context, super.handle(fn));
    }

    @Override
    public <U> FolioVertxCompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return new FolioVertxCompletableFuture<>(context, super.thenApplyAsync(fn, executor));
    }

    @Override
    public FolioVertxCompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return new FolioVertxCompletableFuture<>(context, super.thenAccept(action));
    }


    @Override
    public FolioVertxCompletableFuture<T> toCompletableFuture() {
        return this;
    }

    // ============= other instance methods =============


    private void complete(T result, Throwable error) {
        if (error == null) {
            super.complete(result);
        } else {
            super.completeExceptionally(error);
        }
    }

}

package io.slinkydeveloper.debtsmanager.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public class FutureUtils {

  public static <T> Future<T> futurify(Consumer<Handler<AsyncResult<T>>> c) {
    Future<T> f = Future.future();
    c.accept(f.completer());
    return f;
  }
}

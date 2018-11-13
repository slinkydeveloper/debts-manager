package io.slinkydeveloper.debtsmanager.services.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceUtils {

  public static JsonObject buildJsonFromStatusMap(Map<String, Double> status) {
    return status.entrySet().stream().collect(JsonObject::new, (j, e) -> j.put(e.getKey(), e.getValue()), JsonObject::mergeIn);
  }

  public static <T> Handler<AsyncResult<T>> sendRetrievedObjectHandler(Handler<AsyncResult<OperationResponse>> resultHandler, Function<T, JsonObject> mapper) {
    return ar -> {
      if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
      if (ar.result() == null)
        resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(404).setStatusMessage("Not Found")));
      resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(mapper.apply(ar.result()))));
    };
  }

  public static <T> Handler<AsyncResult<List<T>>> sendRetrievedArrayHandler(Handler<AsyncResult<OperationResponse>> resultHandler, Function<T, JsonObject> mapper) {
    return ar -> {
      if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
      resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonArray(ar.result().stream().map(mapper).collect(Collectors.toList())))));
    };
  }

}

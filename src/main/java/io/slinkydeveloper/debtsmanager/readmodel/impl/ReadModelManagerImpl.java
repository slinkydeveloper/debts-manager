package io.slinkydeveloper.debtsmanager.readmodel.impl;

import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import org.slf4j.Logger;
import io.vertx.redis.RedisClient;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadModelManagerImpl implements ReadModelManager {

  private final RedisClient redisClient;
  private final String updateStatusLuaScript;

  private final static Logger log = LoggerFactory.getLogger(ReadModelManager.class);

  public ReadModelManagerImpl(RedisClient redisClient, String updateStatusLuaScript) {
    this.redisClient = redisClient;
    this.updateStatusLuaScript = updateStatusLuaScript;
  }

  @Override
  public void triggerRefreshFromTransactionUpdate(String transactionId, String from, String to, double oldValue, double newValue) {
    double difference = newValue - oldValue;
    updateCouple(transactionId, from, to, difference, -difference);
  }

  @Override
  public void triggerRefreshFromTransactionRemove(String transactionId, String from, String to, double value) {
    updateCouple(transactionId, from, to, -value, value);
  }

  @Override
  public void triggerRefreshFromTransactionCreation(String transactionId, String from, String to, double value) {
    updateCouple(transactionId, from, to, value, -value);
  }

  public void updateCouple(String transactionid, String from, String to, double fromToValue, double toFromValue) {
    this.redisClient(statusPrefix + from, to, fromToValue, updateResultHandler);
    this.redisClient.hincrbyfloat(statusPrefix + to, from, toFromValue, updateResultHandler);
    //TODO update with lua script use redisClient.evalScript()
  }

  @Override
  public void pushStatusCache(String username, Map<String, Double> status) {
    CompositeFuture //TODO update with transaction WATCH statusPrefix + username REMOVE and HSET
      .all(status.entrySet().stream().map(e -> futHset(statusPrefix + username, e.getKey(), e.getValue().toString())).collect(Collectors.toList()))
      .setHandler(ar -> {
        if (ar.failed()) log.error("Error pushing status cache to redis\n" + Arrays.deepToString(ar.cause().getStackTrace()));
      });
  }

  private Future<Long> futHset(String key, String field, String value) {
    Future<Long> fut = Future.succeededFuture();
    this.redisClient.hset(key, field, value, fut.completer());
    return fut;
  }

  private Handler<AsyncResult<JsonArray>> updateResultHandler(String transactionId) {
    return ar -> {
      if (ar.failed()) log.warn("Error during transaction update script execution. Maybe because of a race condition?", ar.cause());
      if (ar.result().getInteger(0) == 1)
        log.info("Successfully updated status read model from transaction {}", transactionId);
      else
        log.info("Model was already updated {}", transactionId);
    };
  }

}

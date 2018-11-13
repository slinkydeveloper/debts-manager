package io.slinkydeveloper.debtsmanager.persistence.impl;

import io.slinkydeveloper.debtsmanager.persistence.StatusCacheManager;
import io.slinkydeveloper.debtsmanager.persistence.StatusPersistence;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.redis.RedisClient;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusCacheManagerImpl implements StatusCacheManager {

  private final RedisClient redisClient;
  private final String statusPrefix;

  private final static Logger log = LoggerFactory.getLogger(StatusCacheManager.class);

  private final Handler<AsyncResult<String>> updateResultHandler = ar -> {
    if (ar.failed()) log.warn("Error getting status from db\n" + Arrays.deepToString(ar.cause().getStackTrace()));
  };

  public StatusCacheManagerImpl(RedisClient redisClient, String statusPrefix) {
    this.redisClient = redisClient;
    this.statusPrefix = statusPrefix;
  }

  @Override
  public void triggerRefreshFromTransactionUpdate(String from, String to, double oldValue, double newValue) {
    double difference = newValue - oldValue;
    updateCouple(from, to, difference, -difference);
  }

  @Override
  public void triggerRefreshFromTransactionRemove(String from, String to, double value) {
    updateCouple(from, to, -value, value);
  }

  @Override
  public void triggerRefreshFromTransactionCreation(String from, String to, double value) {
    updateCouple(from, to, value, -value);
  }

  public void updateCouple(String from, String to, double fromToValue, double toFromValue) {
    this.redisClient.hincrbyfloat(statusPrefix + from, to, fromToValue, updateResultHandler);
    this.redisClient.hincrbyfloat(statusPrefix + to, from, toFromValue, updateResultHandler);
  }

  @Override
  public void pushStatusCache(String username, Map<String, Double> status) {
    CompositeFuture
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

}

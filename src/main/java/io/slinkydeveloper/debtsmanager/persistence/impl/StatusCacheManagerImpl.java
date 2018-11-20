package io.slinkydeveloper.debtsmanager.persistence.impl;

import io.slinkydeveloper.debtsmanager.persistence.StatusCacheManager;
import io.slinkydeveloper.debtsmanager.readmodel.impl.DebtsManagerRedisCommands;
import io.slinkydeveloper.debtsmanager.utils.HashUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.slf4j.Logger;
import io.vertx.redis.RedisClient;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StatusCacheManagerImpl implements StatusCacheManager {

  private final RedisClient redisClient;

  private final static Logger log = LoggerFactory.getLogger(StatusCacheManager.class);

  private final Handler<AsyncResult<Boolean>> redisScriptsResultHandler = ar -> {
    if (ar.failed()) log.warn("Error getting status from db", ar.cause());
    else if (!ar.result()) log.info("Update script cannot update status");
  };

  public StatusCacheManagerImpl(RedisClient redisClient) {
    this.redisClient = redisClient;
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
    DebtsManagerRedisCommands.updateTransaction(
      redisClient,
      from,
      HashUtils.createHash(from, to, String.valueOf(fromToValue), String.valueOf(System.currentTimeMillis())),
      to,
      fromToValue
    ).setHandler(redisScriptsResultHandler);
    DebtsManagerRedisCommands.updateTransaction(
      redisClient,
      to,
      HashUtils.createHash(to, from, String.valueOf(toFromValue), String.valueOf(System.currentTimeMillis())),
      from,
      toFromValue
    ).setHandler(redisScriptsResultHandler);
  }

  @Override
  public void pushNewStatusCache(String username, Map<String, Double> status) {
    DebtsManagerRedisCommands.pushNewStatus(
      redisClient,
      username,
      HashUtils.createHash(username, status.toString(), String.valueOf(System.currentTimeMillis())),
      status
    ).setHandler(redisScriptsResultHandler);
  }

}

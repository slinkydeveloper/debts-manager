package io.slinkydeveloper.debtsmanager.persistence;

import io.slinkydeveloper.debtsmanager.persistence.impl.StatusCacheManagerImpl;
import io.vertx.redis.RedisClient;

import java.util.Map;

public interface StatusCacheManager {

  void triggerRefreshFromTransactionUpdate(String from, String to, double oldValue, double newValue);
  void triggerRefreshFromTransactionRemove(String from, String to, double value);
  void triggerRefreshFromTransactionCreation(String from, String to, double value);
  void triggerRebuildStatusCacheOfUser(String username);
  void pushStatusCache(String username, Map<String, Double> status);

  static StatusCacheManager create(RedisClient redisClient, String statusPrefix, StatusPersistence statusPersistence) {
    return new StatusCacheManagerImpl(redisClient, statusPrefix, statusPersistence);
  }

}

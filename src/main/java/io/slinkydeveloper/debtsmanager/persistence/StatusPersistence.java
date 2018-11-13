package io.slinkydeveloper.debtsmanager.persistence;

import io.reactiverse.pgclient.PgPool;
import io.slinkydeveloper.debtsmanager.persistence.impl.StatusPersistenceImpl;
import io.vertx.core.Future;
import io.vertx.redis.RedisClient;

import java.time.ZonedDateTime;
import java.util.Map;

public interface StatusPersistence {

  Future<Map<String, Double>> getStatus(String username);
  Future<Map<String, Double>> getStatusFromCache(String username);
  Future<Map<String, Double>> getStatusFromDb(String username);
  Future<Map<String, Double>> getStatusTill(String username, ZonedDateTime time);

  static StatusPersistence create(RedisClient redisClient, PgPool pgClient, String statusPrefix, String buildStatusQuery, String buildStatusBeforeQuery, StatusCacheManager statusCacheManager) {
    return new StatusPersistenceImpl(redisClient, pgClient, statusPrefix, buildStatusQuery, buildStatusBeforeQuery, statusCacheManager);
  }

}

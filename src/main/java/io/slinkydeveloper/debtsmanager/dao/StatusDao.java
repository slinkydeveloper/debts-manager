package io.slinkydeveloper.debtsmanager.dao;

import io.reactiverse.pgclient.PgPool;
import io.slinkydeveloper.debtsmanager.dao.impl.StatusDaoImpl;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManagerService;
import io.vertx.core.Future;
import io.vertx.redis.RedisClient;

import java.time.ZonedDateTime;
import java.util.Map;

public interface StatusDao {

  Future<Map<String, Double>> getStatus(String username);
  Future<Map<String, Double>> getStatusFromCache(String username);
  Future<Map<String, Double>> getStatusFromDb(String username);
  Future<Map<String, Double>> getStatusTill(String username, ZonedDateTime time);

  static StatusDao create(RedisClient redisClient, PgPool pgClient, ReadModelManagerService readModelManager) {
    return new StatusDaoImpl(redisClient, pgClient, readModelManager);
  }

}

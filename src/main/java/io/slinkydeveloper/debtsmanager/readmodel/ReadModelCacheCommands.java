package io.slinkydeveloper.debtsmanager.readmodel;

import io.vertx.core.Future;
import io.vertx.redis.RedisClient;

import java.util.Map;

public interface ReadModelCacheCommands {

  Future<Boolean> updateTransaction(String username, String command_id, String username_to, double value);
  Future<Boolean> pushNewStatus(String username, String command_id, Map<String, Double> status);
  RedisClient getRedisClient();

}

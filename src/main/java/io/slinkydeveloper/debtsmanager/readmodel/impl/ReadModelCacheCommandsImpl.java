package io.slinkydeveloper.debtsmanager.readmodel.impl;

import io.slinkydeveloper.debtsmanager.readmodel.ReadModelCacheCommands;
import io.vertx.core.Future;
import io.vertx.redis.RedisClient;
import io.vertx.redis.Script;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadModelCacheCommandsImpl implements ReadModelCacheCommands {

  private static class ScriptsHolder {
    static Script updateTransactionScript;
    static Script pushStatusScript;

    static {
      try {
        updateTransactionScript = Script.create(String.join("\n", Files.readAllLines(Paths.get(ReadModelCacheCommandsImpl.class.getClassLoader().getResource("redis_lua/update_transaction.lua").toURI()))));
        pushStatusScript = Script.create(String.join("\n", Files.readAllLines(Paths.get(ReadModelCacheCommandsImpl.class.getClassLoader().getResource("redis_lua/push_status.lua").toURI()))));
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
    }
  }

  private RedisClient redisClient;

  public ReadModelCacheCommandsImpl(RedisClient redisClient) {
    this.redisClient = redisClient;
  }

  public Future<Boolean> updateTransaction(String username, String command_id, String username_to, double value) {
    Future<Boolean> fut = Future.future();
    redisClient.evalScript(ScriptsHolder.updateTransactionScript, List.of(username, command_id, username_to, String.valueOf(value)), List.of(), ar -> {
      if (ar.succeeded()) fut.complete(ar.result().getInteger(0) == 1);
      else fut.fail(ar.cause());
    });
    return fut;
  }

  public Future<Boolean> pushNewStatus(String username, String command_id, Map<String, Double> status) {
    Future<Boolean> fut = Future.future();
    redisClient.evalScript(
      ScriptsHolder.pushStatusScript,
      List.of(username, command_id),
      status.entrySet().stream().flatMap(e -> Stream.of(e.getKey(), e.getValue().toString())).collect(Collectors.toList()),
      ar -> {
      if (ar.succeeded()) fut.complete(ar.result().getInteger(0) == 1);
      else fut.fail(ar.cause());
    });
    return fut;
  }

  @Override
  public RedisClient getRedisClient() {
    return redisClient;
  }

}

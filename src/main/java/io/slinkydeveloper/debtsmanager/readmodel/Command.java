package io.slinkydeveloper.debtsmanager.readmodel;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.redis.RedisClient;

@VertxGen
public interface Command {

  String getCommandId();

  void runCommand(RedisClient client, Handler<AsyncResult<Boolean>> resultHandler);

}

package io.slinkydeveloper.debtsmanager.readmodel;

import io.slinkydeveloper.debtsmanager.readmodel.command.UpdateStatusAfterTransactionUpdateCommand;
import io.slinkydeveloper.debtsmanager.readmodel.impl.ReadModelManagerImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

import java.util.Map;

@VertxGen
@ProxyGen
public interface ReadModelManager {

  void runCommand(JsonObject command, Handler<AsyncResult<Boolean>> resultHandler);

  static ReadModelManager create(RedisClient redisClient) {
    return new ReadModelManagerImpl(redisClient);
  }

  static ReadModelManager createProxy(Vertx vertx, String address) {
    return new ReadModelManagerVertxEBProxy(vertx, address);
  }
}

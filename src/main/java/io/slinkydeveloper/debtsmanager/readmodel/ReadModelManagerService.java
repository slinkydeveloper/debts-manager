package io.slinkydeveloper.debtsmanager.readmodel;

import io.slinkydeveloper.debtsmanager.readmodel.impl.ReadModelManagerImpl;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

@VertxGen
@ProxyGen
public interface ReadModelManagerService {

  void runCommand(JsonObject command, Handler<AsyncResult<Boolean>> resultHandler);

  static ReadModelManagerService create(RedisClient redisClient) {
    return new ReadModelManagerImpl(redisClient);
  }

  static ReadModelManagerService createClient(Vertx vertx, String address, CircuitBreaker circuitBreaker) {
    return new ReadModelManagerServiceClient(new ReadModelManagerVertxEBProxy(vertx, address), circuitBreaker);
  }
}

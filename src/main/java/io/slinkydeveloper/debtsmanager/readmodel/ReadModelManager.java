package io.slinkydeveloper.debtsmanager.readmodel;

import io.slinkydeveloper.debtsmanager.readmodel.impl.ReadModelManagerImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;

import java.util.Map;

@VertxGen
@ProxyGen
public interface ReadModelManager {

  static ReadModelManager create(RedisClient redisClient) {
    return new ReadModelManagerImpl(redisClient);
  }

  static ReadModelManager createProxy(Vertx vertx, String address) {
    return new ReadModelManagerEBProxy(vertx, address);
  }

  void runCommand(Command command, Handler<AsyncResult<Boolean>> resultHandler);

  void triggerRefreshFromTransactionUpdate(String transactionId, String from, String to, double oldValue, double newValue);
  void triggerRefreshFromTransactionRemove(String transactionId, String from, String to, double value);
  void triggerRefreshFromTransactionCreation(String transactionId, String from, String to, double value);
  void pushStatusCache(String username, Map<String, Double> status);
}

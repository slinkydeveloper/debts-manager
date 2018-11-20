package io.slinkydeveloper.debtsmanager.readmodel;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadModelManagerServiceClient implements ReadModelManagerService {

  private final ReadModelManagerVertxEBProxy proxy;
  private final CircuitBreaker circuitBreaker;

  private final static Logger log = LoggerFactory.getLogger(ReadModelManagerServiceClient.class);

  public ReadModelManagerServiceClient(ReadModelManagerVertxEBProxy proxy, CircuitBreaker circuitBreaker) {
    this.proxy = proxy;
    this.circuitBreaker = circuitBreaker;
  }

  @Override
  public void runCommand(JsonObject command, Handler<AsyncResult<Boolean>> resultHandler) {
    circuitBreaker.<Boolean>execute(fut -> proxy.runCommand(command, fut.completer())).setHandler(resultHandler::handle);
  }
}

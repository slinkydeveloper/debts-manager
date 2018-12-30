package io.slinkydeveloper.debtsmanager.readmodel.impl;

import io.slinkydeveloper.debtsmanager.readmodel.Command;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelCacheCommands;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManagerService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadModelManagerImpl implements ReadModelManagerService {

  private final RedisClient redisClient;
  private final ReadModelCacheCommands readModelCacheCommands;

  private final static Logger log = LoggerFactory.getLogger(ReadModelManagerService.class);

  public ReadModelManagerImpl(RedisClient redisClient) {
    this.redisClient = redisClient;
    this.readModelCacheCommands = new ReadModelCacheCommandsImpl(redisClient);
  }

  @Override
  public void runCommand(JsonObject jsonCommand, Handler<AsyncResult<Boolean>> resultHandler) {
    Command command = jsonCommand.mapTo(Command.class);
    command.execute(readModelCacheCommands).setHandler(ar -> {
      if (ar.failed()) {
        log.warn("Error during transaction script execution. Maybe because of a race condition?", ar.cause());
      } else {
        if (ar.result())
          log.info("Command completed with true result {}", command);
        else
          log.info("Command completed with false result {}", command);
      }
      resultHandler.handle(ar);
    });
  }
}

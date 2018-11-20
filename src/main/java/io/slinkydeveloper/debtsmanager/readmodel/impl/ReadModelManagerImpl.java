package io.slinkydeveloper.debtsmanager.readmodel.impl;

import io.slinkydeveloper.debtsmanager.readmodel.Command;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManager;
import io.slinkydeveloper.debtsmanager.readmodel.command.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import io.vertx.redis.RedisClient;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadModelManagerImpl implements ReadModelManager {

  private final RedisClient redisClient;

  private final static Logger log = LoggerFactory.getLogger(ReadModelManager.class);

  public ReadModelManagerImpl(RedisClient redisClient) {
    this.redisClient = redisClient;
  }

  private Future<Boolean> runUpdateStatusAfterTransactionUpdateCommand(UpdateStatusAfterTransactionUpdateCommand command) {
    double difference = command.getNewValue() - command.getOldValue();
    return CompositeFuture.all(
      DebtsManagerRedisCommands.updateTransaction(redisClient, command.getFrom(), command.getCommandId(), command.getTo(), difference),
      DebtsManagerRedisCommands.updateTransaction(redisClient, command.getTo(), command.getCommandId(), command.getFrom(), -difference)
    ).map(cf -> (Boolean) cf.resultAt(0) && (Boolean) cf.resultAt(1));
  }

  private Future<Boolean> runUpdateStatusAfterTransactionRemoveCommand(UpdateStatusAfterTransactionRemoveCommand command) {
    return CompositeFuture.all(
      DebtsManagerRedisCommands.updateTransaction(redisClient, command.getFrom(), command.getCommandId(), command.getTo(), -command.getValue()),
      DebtsManagerRedisCommands.updateTransaction(redisClient, command.getTo(), command.getCommandId(), command.getFrom(), command.getValue())
    ).map(cf -> (Boolean) cf.resultAt(0) && (Boolean) cf.resultAt(1));
  }

  private Future<Boolean> runUpdateStatusAfterTransactionCreationCommand(UpdateStatusAfterTransactionCreationCommand command) {
    return CompositeFuture.all(
      DebtsManagerRedisCommands.updateTransaction(redisClient, command.getFrom(), command.getCommandId(), command.getTo(), command.getValue()),
      DebtsManagerRedisCommands.updateTransaction(redisClient, command.getTo(), command.getCommandId(), command.getFrom(), -command.getValue())
    ).map(cf -> (Boolean) cf.resultAt(0) && (Boolean) cf.resultAt(1));
  }

  private Future<Boolean> runPushNewStatusCommand(PushNewStatusCommand command) {
    return DebtsManagerRedisCommands.pushNewStatus(redisClient, command.getUsername(), command.getCommandId(), command.getStatus());
  }

  @Override
  public void runCommand(JsonObject jsonCommand, Handler<AsyncResult<Boolean>> resultHandler) {
    Command command = jsonCommand.mapTo(Command.class);
    Future<Boolean> fut;
    if (command instanceof PushNewStatusCommand)
      fut = runPushNewStatusCommand((PushNewStatusCommand) command);
    else if (command instanceof UpdateStatusAfterTransactionCreationCommand)
      fut = runUpdateStatusAfterTransactionCreationCommand((UpdateStatusAfterTransactionCreationCommand) command);
    else if (command instanceof UpdateStatusAfterTransactionRemoveCommand)
      fut = runUpdateStatusAfterTransactionRemoveCommand((UpdateStatusAfterTransactionRemoveCommand) command);
    else if (command instanceof UpdateStatusAfterTransactionUpdateCommand)
      fut = runUpdateStatusAfterTransactionUpdateCommand((UpdateStatusAfterTransactionUpdateCommand) command);
    else {
      resultHandler.handle(Future.failedFuture("Command not exists"));
      return;
    }
    fut.setHandler(ar -> {
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

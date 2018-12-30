package io.slinkydeveloper.debtsmanager.readmodel.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.slinkydeveloper.debtsmanager.readmodel.Command;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelCacheCommands;
import io.slinkydeveloper.debtsmanager.utils.HashUtils;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.Future;
import joptsimple.internal.Strings;

public abstract class AbstractCommand implements Command {

  private String commandId;

  public AbstractCommand(String ...args) {
    this.commandId = HashUtils.createHash(Strings.join(args, ""), String.valueOf(System.currentTimeMillis()));
  }

  @JsonIgnore
  public abstract Future<Boolean> execute(ReadModelCacheCommands commands);

  @Override
  public String getCommandId() {
    return commandId;
  }

  @Fluent
  public AbstractCommand setCommandId(String commandId) {
    this.commandId = commandId;
    return this;
  }
}

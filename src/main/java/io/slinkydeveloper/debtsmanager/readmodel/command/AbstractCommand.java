package io.slinkydeveloper.debtsmanager.readmodel.command;

import io.slinkydeveloper.debtsmanager.readmodel.Command;
import io.slinkydeveloper.debtsmanager.utils.HashUtils;
import io.vertx.codegen.annotations.Fluent;
import joptsimple.internal.Strings;

public abstract class AbstractCommand implements Command {

  private String commandId;

  public AbstractCommand(String ...args) {
    this.commandId = HashUtils.createHash(new String[]{Strings.join(args, ""), String.valueOf(System.currentTimeMillis())});
  }

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

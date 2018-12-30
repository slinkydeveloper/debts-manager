package io.slinkydeveloper.debtsmanager.readmodel.command;

import io.slinkydeveloper.debtsmanager.readmodel.ReadModelCacheCommands;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

public class UpdateStatusAfterTransactionRemoveCommand extends AbstractCommand {

  private String transactionId;
  private String from;
  private String to;
  private double value;

  public UpdateStatusAfterTransactionRemoveCommand() {}

  public UpdateStatusAfterTransactionRemoveCommand(String transactionId, String from, String to, double value) {
    super(transactionId, from, to, String.valueOf(value));
    this.transactionId = transactionId;
    this.from = from;
    this.to = to;
    this.value = value;
  }

  public String getTransactionId() {
    return transactionId;
  }

  @Fluent
  public UpdateStatusAfterTransactionRemoveCommand setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public String getFrom() {
    return from;
  }

  @Fluent
  public UpdateStatusAfterTransactionRemoveCommand setFrom(String from) {
    this.from = from;
    return this;
  }

  public String getTo() {
    return to;
  }

  @Fluent
  public UpdateStatusAfterTransactionRemoveCommand setTo(String to) {
    this.to = to;
    return this;
  }

  public double getValue() {
    return value;
  }

  @Fluent
  public UpdateStatusAfterTransactionRemoveCommand setValue(double value) {
    this.value = value;
    return this;
  }

  @Override
  public Future<Boolean> execute(ReadModelCacheCommands commands) {
    return CompositeFuture.all(
      commands.updateTransaction(this.getFrom(), this.getCommandId(), this.getTo(), -this.getValue()),
      commands.updateTransaction(this.getTo(), this.getCommandId(), this.getFrom(), this.getValue())
    ).map(cf -> (Boolean) cf.resultAt(0) && (Boolean) cf.resultAt(1));
  }
}

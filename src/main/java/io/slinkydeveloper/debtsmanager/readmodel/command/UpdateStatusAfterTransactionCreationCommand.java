package io.slinkydeveloper.debtsmanager.readmodel.command;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

public class UpdateStatusAfterTransactionCreationCommand extends AbstractCommand {

  private String transactionId;
  private String from;
  private String to;
  private double value;

  public UpdateStatusAfterTransactionCreationCommand(String transactionId, String from, String to, double value) {
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
  public UpdateStatusAfterTransactionCreationCommand setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public String getFrom() {
    return from;
  }

  @Fluent
  public UpdateStatusAfterTransactionCreationCommand setFrom(String from) {
    this.from = from;
    return this;
  }

  public String getTo() {
    return to;
  }

  @Fluent
  public UpdateStatusAfterTransactionCreationCommand setTo(String to) {
    this.to = to;
    return this;
  }

  public double getValue() {
    return value;
  }

  @Fluent
  public UpdateStatusAfterTransactionCreationCommand setValue(double value) {
    this.value = value;
    return this;
  }
}

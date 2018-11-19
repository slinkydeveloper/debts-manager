package io.slinkydeveloper.debtsmanager.readmodel.command;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

@DataObject(generateConverter = true)
public class UpdateStatusAfterTransactionRemoveCommand extends AbstractCommand {

  private String transactionId;
  private String from;
  private String to;
  private double value;

  public UpdateStatusAfterTransactionRemoveCommand(String transactionId, String from, String to, double value) {
    super(transactionId, from, to, String.valueOf(value));
    this.transactionId = transactionId;
    this.from = from;
    this.to = to;
    this.value = value;
  }

  public UpdateStatusAfterTransactionRemoveCommand(JsonObject json) {
    UpdateStatusAfterTransactionRemoveCommandConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    UpdateStatusAfterTransactionRemoveCommandConverter.toJson(this, json);
    return json;
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
  public void runCommand(RedisClient client, Handler<AsyncResult<Void>> resultHandler) {
    //TODO
  }
}

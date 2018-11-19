package io.slinkydeveloper.debtsmanager.readmodel.command;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

@DataObject(generateConverter = true)
public class UpdateStatusAfterTransactionUpdateCommand extends AbstractCommand {

  private String transactionId;
  private String from;
  private String to;
  private double oldValue;
  private double newValue;

  public UpdateStatusAfterTransactionUpdateCommand(String transactionId, String from, String to, double oldValue, double newValue) {
    super(transactionId, from, to, String.valueOf(oldValue), String.valueOf(newValue));
    this.transactionId = transactionId;
    this.from = from;
    this.to = to;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public UpdateStatusAfterTransactionUpdateCommand(JsonObject json) {
    UpdateStatusAfterTransactionUpdateCommandConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    UpdateStatusAfterTransactionUpdateCommandConverter.toJson(this, json);
    return json;
  }

  public String getTransactionId() {
    return transactionId;
  }

  @Fluent
  public UpdateStatusAfterTransactionUpdateCommand setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public String getFrom() {
    return from;
  }

  @Fluent
  public UpdateStatusAfterTransactionUpdateCommand setFrom(String from) {
    this.from = from;
    return this;
  }

  public String getTo() {
    return to;
  }

  @Fluent
  public UpdateStatusAfterTransactionUpdateCommand setTo(String to) {
    this.to = to;
    return this;
  }

  public double getOldValue() {
    return oldValue;
  }

  @Fluent
  public UpdateStatusAfterTransactionUpdateCommand setOldValue(double oldValue) {
    this.oldValue = oldValue;
    return this;
  }

  public double getNewValue() {
    return newValue;
  }

  @Fluent
  public UpdateStatusAfterTransactionUpdateCommand setNewValue(double newValue) {
    this.newValue = newValue;
    return this;
  }

  @Override
  public void runCommand(RedisClient client, Handler<AsyncResult<Void>> resultHandler) {
    //TODO
  }
}

package io.slinkydeveloper.debtsmanager.readmodel.command;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

import java.util.Map;

@DataObject(generateConverter = true)
public class PushNewStatusCommand extends AbstractCommand {

  private String username;
  private Map<String, Double> status;

  public PushNewStatusCommand(String username, Map<String, Double> status) {
    super(username, status.toString());
    this.username = username;
    this.status = status;
  }

  public PushNewStatusCommand(JsonObject json) {
    PushNewStatusCommandConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PushNewStatusCommandConverter.toJson(this, json);
    return json;
  }

  public String getUsername() {
    return username;
  }

  @Fluent
  public PushNewStatusCommand setUsername(String username) {
    this.username = username;
    return this;
  }

  public Map<String, Double> getStatus() {
    return status;
  }

  @Fluent
  public PushNewStatusCommand setStatus(Map<String, Double> status) {
    this.status = status;
    return this;
  }

  @Override
  public void runCommand(RedisClient client, Handler<AsyncResult<Boolean>> resultHandler) {

  }
}

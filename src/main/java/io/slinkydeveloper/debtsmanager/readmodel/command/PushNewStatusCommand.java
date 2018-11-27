package io.slinkydeveloper.debtsmanager.readmodel.command;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

import java.util.Map;

public class PushNewStatusCommand extends AbstractCommand {

  private String username;
  private Map<String, Double> status;

  public PushNewStatusCommand() {}

  public PushNewStatusCommand(String username, Map<String, Double> status) {
    super(username, status.toString());
    this.username = username;
    this.status = status;
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
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
}

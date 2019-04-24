package io.slinkydeveloper.debtsmanager.readmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.slinkydeveloper.debtsmanager.readmodel.command.PushNewStatusCommand;
import io.slinkydeveloper.debtsmanager.readmodel.command.UpdateStatusAfterTransactionCreationCommand;
import io.slinkydeveloper.debtsmanager.readmodel.command.UpdateStatusAfterTransactionRemoveCommand;
import io.slinkydeveloper.debtsmanager.readmodel.command.UpdateStatusAfterTransactionUpdateCommand;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PushNewStatusCommand.class, name = "PushNewStatusCommand"),
  @JsonSubTypes.Type(value = UpdateStatusAfterTransactionCreationCommand.class, name = "UpdateStatusAfterTransactionCreationCommand"),
  @JsonSubTypes.Type(value = UpdateStatusAfterTransactionRemoveCommand.class, name = "UpdateStatusAfterTransactionRemoveCommand"),
  @JsonSubTypes.Type(value = UpdateStatusAfterTransactionUpdateCommand.class, name = "UpdateStatusAfterTransactionUpdateCommand"),
})
public interface Command {

  String getCommandId();

  default JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  @JsonIgnore
  public abstract Future<Boolean> execute(ReadModelCacheCommands commands);

}

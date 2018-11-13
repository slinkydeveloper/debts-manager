package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;

@DataObject(generateConverter = true, publicConverter = false)
public class UpdateTransaction {

  private Double value;
  private String description;

  public UpdateTransaction (
    Double value,
    String description
  ) {
    this.value = value;
    this.description = description;
  }

  public UpdateTransaction(JsonObject json) {
    UpdateTransactionConverter.fromJson(json, this);
  }

  public UpdateTransaction(UpdateTransaction other) {
    this.value = other.getValue();
    this.description = other.getDescription();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    UpdateTransactionConverter.toJson(this, json);
    return json;
  }

  @Fluent public UpdateTransaction setValue(Double value){
    this.value = value;
    return this;
  }
  public Double getValue() {
    return this.value;
  }

  @Fluent public UpdateTransaction setDescription(String description){
    this.description = description;
    return this;
  }
  public String getDescription() {
    return this.description;
  }

}

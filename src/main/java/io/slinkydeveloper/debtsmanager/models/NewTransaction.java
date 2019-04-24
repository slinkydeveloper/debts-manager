package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class NewTransaction {

  private String to;
  private double value;
  private String description;

  public NewTransaction (
    String to,
    double value,
    String description
  ) {
    this.to = to;
    this.value = value;
    this.description = description;
  }

  public NewTransaction(JsonObject json) {
    NewTransactionConverter.fromJson(json, this);
  }

  public NewTransaction(NewTransaction other) {
    this.to = other.getTo();
    this.value = other.getValue();
    this.description = other.getDescription();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    NewTransactionConverter.toJson(this, json);
    return json;
  }

  @Fluent public NewTransaction setTo(String to){
    this.to = to;
    return this;
  }
  public String getTo() {
    return this.to;
  }

  @Fluent public NewTransaction setValue(double value){
    this.value = value;
    return this;
  }
  public double getValue() {
    return this.value;
  }

  @Fluent public NewTransaction setDescription(String description){
    this.description = description;
    return this;
  }
  public String getDescription() {
    return this.description;
  }

}

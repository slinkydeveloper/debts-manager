package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true, publicConverter = false)
public class UpdateTransaction {

  private String from;
  private String to;
  private Float value;
  private String description;

  public UpdateTransaction (
    String from,
    String to,
    Float value,
    String description
  ) {
    this.from = from;
    this.to = to;
    this.value = value;
    this.description = description;
  }

  public UpdateTransaction(JsonObject json) {
    UpdateTransactionConverter.fromJson(json, this);
  }

  public UpdateTransaction(UpdateTransaction other) {
    this.from = other.getFrom();
    this.to = other.getTo();
    this.value = other.getValue();
    this.description = other.getDescription();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    UpdateTransactionConverter.toJson(this, json);
    return json;
  }

  @Fluent public UpdateTransaction setFrom(String from){
    this.from = from;
    return this;
  }
  public String getFrom() {
    return this.from;
  }

  @Fluent public UpdateTransaction setTo(String to){
    this.to = to;
    return this;
  }
  public String getTo() {
    return this.to;
  }

  @Fluent public UpdateTransaction setValue(Float value){
    this.value = value;
    return this;
  }
  public Float getValue() {
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

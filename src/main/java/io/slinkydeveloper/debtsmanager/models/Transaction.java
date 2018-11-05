package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true, publicConverter = false)
public class Transaction {

  private String from;
  private String to;
  private Float value;
  private String description;
  private String id;
  private String at;

  public Transaction (
    String from,
    String to,
    Float value,
    String description,
    String id,
    String at
  ) {
    this.from = from;
    this.to = to;
    this.value = value;
    this.description = description;
    this.id = id;
    this.at = at;
  }

  public Transaction(JsonObject json) {
    TransactionConverter.fromJson(json, this);
  }

  public Transaction(Transaction other) {
    this.from = other.getFrom();
    this.to = other.getTo();
    this.value = other.getValue();
    this.description = other.getDescription();
    this.id = other.getId();
    this.at = other.getAt();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    TransactionConverter.toJson(this, json);
    return json;
  }

  @Fluent public Transaction setFrom(String from){
    this.from = from;
    return this;
  }
  public String getFrom() {
    return this.from;
  }

  @Fluent public Transaction setTo(String to){
    this.to = to;
    return this;
  }
  public String getTo() {
    return this.to;
  }

  @Fluent public Transaction setValue(Float value){
    this.value = value;
    return this;
  }
  public Float getValue() {
    return this.value;
  }

  @Fluent public Transaction setDescription(String description){
    this.description = description;
    return this;
  }
  public String getDescription() {
    return this.description;
  }

  @Fluent public Transaction setId(String id){
    this.id = id;
    return this;
  }
  public String getId() {
    return this.id;
  }

  @Fluent public Transaction setAt(String at){
    this.at = at;
    return this;
  }
  public String getAt() {
    return this.at;
  }

}

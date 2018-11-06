package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;

@DataObject(generateConverter = true, publicConverter = false)
public class Transaction {

  private Float value;
  private String description;
  private String from;
  private String id;
  private String at;
  private String to;

  public Transaction (
    Float value,
    String description,
    String from,
    String id,
    String at
  ) {
    this.value = value;
    this.description = description;
    this.from = from;
    this.id = id;
    this.at = at;
  }

  public Transaction(JsonObject json) {
    TransactionConverter.fromJson(json, this);
  }

  public Transaction(Transaction other) {
    this.value = other.getValue();
    this.description = other.getDescription();
    this.from = other.getFrom();
    this.id = other.getId();
    this.at = other.getAt();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    TransactionConverter.toJson(this, json);
    return json;
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

  @Fluent public Transaction setFrom(String from){
    this.from = from;
    return this;
  }
  public String getFrom() {
    return this.from;
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

  @Fluent
  public Transaction setTo(String to) {
    this.to = to;
    return this;
  }
  public String getTo() {
    return to;
  }
}

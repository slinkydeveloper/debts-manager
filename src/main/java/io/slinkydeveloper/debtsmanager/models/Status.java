package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;

@DataObject(generateConverter = true, publicConverter = false)
public class Status {

  private List<Transaction> transactions;
  private Map<String, Double> users;

  public Status (
    List<Transaction> transactions,
    Map<String, Double> users
  ) {
    this.transactions = transactions;
    this.users = users;
  }

  public Status(JsonObject json) {
    StatusConverter.fromJson(json, this);
  }

  public Status(Status other) {
    this.transactions = other.getTransactions();
    this.users = other.getUsers();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    StatusConverter.toJson(this, json);
    return json;
  }

  @Fluent public Status setTransactions(List<Transaction> transactions){
    this.transactions = transactions;
    return this;
  }
  public List<Transaction> getTransactions() {
    return this.transactions;
  }

  @Fluent public Status setUsers(Map<String, Double>  users){
    this.users = users;
    return this;
  }
  public Map<String, Double> getUsers() {
    return this.users;
  }

}

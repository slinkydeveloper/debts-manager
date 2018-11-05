package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;
import java.util.List;

@DataObject(generateConverter = true, publicConverter = false)
public class GetConnectedUsers200Response {

  private List<String> allowedTo;
  private List<String> allowedFrom;

  public GetConnectedUsers200Response (
    List<String> allowedTo,
    List<String> allowedFrom
  ) {
    this.allowedTo = allowedTo;
    this.allowedFrom = allowedFrom;
  }

  public GetConnectedUsers200Response(JsonObject json) {
    GetConnectedUsers200ResponseConverter.fromJson(json, this);
  }

  public GetConnectedUsers200Response(GetConnectedUsers200Response other) {
    this.allowedTo = other.getAllowedTo();
    this.allowedFrom = other.getAllowedFrom();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GetConnectedUsers200ResponseConverter.toJson(this, json);
    return json;
  }

  @Fluent public GetConnectedUsers200Response setAllowedTo(List<String> allowedTo){
    this.allowedTo = allowedTo;
    return this;
  }
  public List<String> getAllowedTo() {
    return this.allowedTo;
  }

  @Fluent public GetConnectedUsers200Response setAllowedFrom(List<String> allowedFrom){
    this.allowedFrom = allowedFrom;
    return this;
  }
  public List<String> getAllowedFrom() {
    return this.allowedFrom;
  }

}

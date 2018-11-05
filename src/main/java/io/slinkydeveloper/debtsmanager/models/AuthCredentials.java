package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true, publicConverter = false)
public class AuthCredentials {

  private String username;
  private String password;

  public AuthCredentials(
    String username,
    String password
  ) {
    this.username = username;
    this.password = password;
  }

  public AuthCredentials(JsonObject json) {
    LoginRequestBodyConverter.fromJson(json, this);
  }

  public AuthCredentials(AuthCredentials other) {
    this.username = other.getUsername();
    this.password = other.getPassword();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    LoginRequestBodyConverter.toJson(this, json);
    return json;
  }

  @Fluent public AuthCredentials setUsername(@Nullable String username){
    this.username = username;
    return this;
  }
  @Nullable
  public String getUsername() {
    return this.username;
  }

  @Fluent public AuthCredentials setPassword(String password){
    this.password = password;
    return this;
  }
  public String getPassword() {
    return this.password;
  }

}

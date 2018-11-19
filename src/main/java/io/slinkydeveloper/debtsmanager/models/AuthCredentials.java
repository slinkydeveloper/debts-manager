package io.slinkydeveloper.debtsmanager.models;

import io.slinkydeveloper.debtsmanager.utils.HashUtils;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
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
    AuthCredentialsConverter.fromJson(json, this);
  }

  public AuthCredentials(AuthCredentials other) {
    this.username = other.getUsername();
    this.password = other.getPassword();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    AuthCredentialsConverter.toJson(this, json);
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

  @GenIgnore @Fluent public AuthCredentials hashPassword() {
    this.setPassword(HashUtils.createHash(new String[]{this.getPassword()}));
    return this;
  }
}

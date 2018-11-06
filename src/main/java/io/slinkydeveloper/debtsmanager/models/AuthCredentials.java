package io.slinkydeveloper.debtsmanager.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

  @GenIgnore @Fluent public AuthCredentials hashPassword() {
    this.setPassword(createPasswordHash(this.getPassword()));
    return this;
  }

  private static MessageDigest shaDigest;

  static {
    try {
      shaDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if(hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  private static String createPasswordHash(String password) {
    byte[] encodedhash = shaDigest.digest(password.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(encodedhash);
  }
}

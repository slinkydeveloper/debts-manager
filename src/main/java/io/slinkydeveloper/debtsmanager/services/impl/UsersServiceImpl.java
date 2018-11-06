package io.slinkydeveloper.debtsmanager.services.impl;

import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.api.*;

import io.slinkydeveloper.debtsmanager.models.*;
import io.slinkydeveloper.debtsmanager.services.UsersService;

import java.util.List;

public class UsersServiceImpl implements UsersService {

  private final Vertx vertx;
  private final UserPersistence persistence;
  private final JWTAuth auth;

  public UsersServiceImpl(Vertx vertx, UserPersistence persistence, JWTAuth auth) {
    this.vertx = vertx;
    this.persistence = persistence;
    this.auth = auth;
  }

  @Override
  public void login(AuthCredentials body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    persistence.userExists(body).setHandler(ar -> {
      if (ar.succeeded()) {
        if (ar.result()) {
          resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(generateToken(body.getUsername())))));
        } else {
          resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(400).setStatusMessage("Bad Request").setPayload(Buffer.buffer("Wrong username or password"))));
        }
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void register(AuthCredentials body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    persistence.addUser(body).setHandler(ar -> {
      if (ar.succeeded()) {
        resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(generateToken(body.getUsername())))));
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getConnectedUsers(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    CompositeFuture.all(
      persistence.getAllowedTo(context.getUser().getString("username")),
      persistence.getAllowedFrom(context.getUser().getString("username"))
    ).setHandler(ar -> {
      if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
      resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(
        new JsonObject()
          .put("allowedTo", new JsonArray((List<String>)ar.result().resultAt(0)))
          .put("allowedFrom", new JsonArray((List<String>)ar.result().resultAt(1)))
      )));
    });
  }

  @Override
  public void getUsers(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    persistence.getUsersList().setHandler(ar -> {
      if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
      resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonArray(ar.result()))));
    });
  }

  @Override
  public void connectUser(String body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    persistence.addUserConnection(body, context.getUser().getString("username")).setHandler(ar -> {
      if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
      resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(200).setStatusMessage("OK")));
    });
  }

  private String generateToken(String username) {
    return auth.generateToken(
      new JsonObject().put("username", username),
      new JWTOptions().setExpiresInMinutes(60).setIssuer("Debts Manager Backend").setSubject("Debts Manager API")
    );
  }

}

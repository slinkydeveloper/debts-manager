package io.slinkydeveloper.debtsmanager.services.impl;

import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.slinkydeveloper.debtsmanager.services.UsersService;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

public class UsersServiceImpl implements UsersService {

  private final Vertx vertx;
  private final UserPersistence persistence;
  private final JWTAuth auth;

  private final static Logger log = LoggerFactory.getLogger(UsersService.class);

  public UsersServiceImpl(Vertx vertx, UserPersistence persistence, JWTAuth auth) {
    this.vertx = vertx;
    this.persistence = persistence;
    this.auth = auth;
  }

  @Override
  public void login(AuthCredentials body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    body.hashPassword();
    persistence.userExists(body).setHandler(ar -> {
      if (ar.succeeded()) {
        if (ar.result()) {
          log.info("Logged user {}", body.getUsername());
          resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(generateToken(body.getUsername())))));
        } else {
          resultHandler.handle(Future.succeededFuture(
            new OperationResponse()
              .setStatusCode(400)
              .setStatusMessage("Bad Request")
              .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
              .setPayload(Buffer.buffer("Wrong username or password")))
          );
        }
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void register(AuthCredentials body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    body.hashPassword();
    persistence.addUser(body).setHandler(ar -> {
      if (ar.succeeded()) {
        if (!ar.result()) {
          log.warn("User is trying to register again: " + body.getUsername());
          resultHandler.handle(Future.succeededFuture(
            new OperationResponse()
              .setStatusCode(400)
              .setStatusMessage("Bad Request")
              .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
              .setPayload(Buffer.buffer("User " + body.getUsername() + " already exists")))
          );
        } else {
          log.info("User successfully registered: {}", body.getUsername());
          resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithPlainText(Buffer.buffer(generateToken(body.getUsername())))));
        }
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
  public void getUsers(String filter, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    if (filter != null && !filter.isEmpty()) {
      persistence.getUsersList(filter).setHandler(ServiceUtils.sendRetrievedArrayHandler(resultHandler, s -> s));
    } else {
      persistence.getUsersList().setHandler(ServiceUtils.sendRetrievedArrayHandler(resultHandler, s -> s));
    }
  }

  @Override
  public void connectUser(String userToConnect, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
    persistence.addUserConnection(userToConnect, context.getUser().getString("username")).setHandler(ar -> {
      if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
      resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(200).setStatusMessage("OK")));
    });
  }

  private String generateToken(String username) {
    return auth.generateToken(
      new JsonObject().put("username", username),
      new JWTOptions().setExpiresInMinutes(60).setIssuer("Debts Manager Backend").setSubject("Debts Manager API").setAlgorithm("RS256")
    );
  }

}

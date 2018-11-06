package io.slinkydeveloper.debtsmanager.services;

import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.api.*;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

import io.slinkydeveloper.debtsmanager.models.*;
import io.slinkydeveloper.debtsmanager.services.impl.UsersServiceImpl;

@WebApiServiceGen
public interface UsersService {

  static UsersService create(Vertx vertx, UserPersistence persistence, JWTAuth auth) {
    return new UsersServiceImpl(vertx, persistence, auth);
  }

  void login(
    AuthCredentials body,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void register(
    AuthCredentials body,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void getConnectedUsers(
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void getUsers(
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void connectUser(String body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}

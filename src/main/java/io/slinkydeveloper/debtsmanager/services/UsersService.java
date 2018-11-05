package io.slinkydeveloper.debtsmanager.services;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.*;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

import java.util.List;
import java.util.Map;

import io.slinkydeveloper.debtsmanager.models.*;
import io.slinkydeveloper.debtsmanager.services.impl.UsersServiceImpl;

@WebApiServiceGen
public interface UsersService {

  static UsersService create(Vertx vertx) {
    return new UsersServiceImpl(vertx);
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

}

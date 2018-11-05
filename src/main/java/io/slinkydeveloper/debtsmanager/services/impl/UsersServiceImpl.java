package io.slinkydeveloper.debtsmanager.services.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.*;

import java.util.List;
import java.util.Map;

import io.slinkydeveloper.debtsmanager.models.*;
import io.slinkydeveloper.debtsmanager.services.UsersService;

public class UsersServiceImpl implements UsersService {

  private Vertx vertx;

  public UsersServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void login(AuthCredentials body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

  }

  @Override
  public void register(AuthCredentials body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

  }

  @Override
  public void getConnectedUsers(
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    // Write your business logic here
    resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(501).setStatusMessage("Not Implemented")));
  }

  @Override
  public void getUsers(
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    // Write your business logic here
    resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(501).setStatusMessage("Not Implemented")));
  }

}

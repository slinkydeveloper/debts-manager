package io.slinkydeveloper.debtsmanager.services;

import io.slinkydeveloper.debtsmanager.dao.UserDao;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.services.impl.UsersServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface UsersService {

  static UsersService create(UserDao userDao, JWTAuth auth) {
    return new UsersServiceImpl(userDao, auth);
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
    String filter, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void connectUser(String userToConnect, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}

package io.slinkydeveloper.debtsmanager.services;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.*;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

import java.util.List;
import java.util.Map;

import io.slinkydeveloper.debtsmanager.models.*;
import io.slinkydeveloper.debtsmanager.services.impl.TransactionsServiceImpl;

@WebApiServiceGen
public interface TransactionsService {

  static TransactionsService create(Vertx vertx) {
    return new TransactionsServiceImpl(vertx);
  }

  void getTransactions(
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void createTransaction(
    NewTransaction body,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void getTransaction(
    String transactionId,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void updateTransaction(
    String transactionId,
    UpdateTransaction body,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void deleteTransaction(
    String transactionId,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

  void getUserStatus(
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}

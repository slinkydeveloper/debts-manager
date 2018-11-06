package io.slinkydeveloper.debtsmanager.services;

import io.slinkydeveloper.debtsmanager.persistence.StatusPersistence;
import io.slinkydeveloper.debtsmanager.persistence.TransactionPersistence;
import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.*;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

import io.slinkydeveloper.debtsmanager.models.*;
import io.slinkydeveloper.debtsmanager.services.impl.TransactionsServiceImpl;

@WebApiServiceGen
public interface TransactionsService {

  static TransactionsService create(Vertx vertx, StatusPersistence statusPersistence, TransactionPersistence transactionPersistence, UserPersistence userPersistence) {
    return new TransactionsServiceImpl(vertx, statusPersistence, transactionPersistence, userPersistence);
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

  void getUserStatus(String till, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
}

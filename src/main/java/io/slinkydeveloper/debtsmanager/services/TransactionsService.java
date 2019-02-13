package io.slinkydeveloper.debtsmanager.services;

import io.slinkydeveloper.debtsmanager.dao.TransactionDao;
import io.slinkydeveloper.debtsmanager.dao.UserDao;
import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.UpdateTransaction;
import io.slinkydeveloper.debtsmanager.services.impl.TransactionsServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface TransactionsService {

  static TransactionsService create(TransactionDao transactionPersistence, UserDao userPersistence) {
    return new TransactionsServiceImpl(transactionPersistence, userPersistence);
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
}

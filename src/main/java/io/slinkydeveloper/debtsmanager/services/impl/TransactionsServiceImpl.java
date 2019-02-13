package io.slinkydeveloper.debtsmanager.services.impl;

import io.slinkydeveloper.debtsmanager.dao.TransactionDao;
import io.slinkydeveloper.debtsmanager.dao.UserDao;
import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.slinkydeveloper.debtsmanager.models.UpdateTransaction;
import io.slinkydeveloper.debtsmanager.services.TransactionsService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class TransactionsServiceImpl implements TransactionsService {

  private final TransactionDao transactionDao;
  private final UserDao userDao;

  public TransactionsServiceImpl(TransactionDao transactionDao, UserDao userDao) {
    this.transactionDao = transactionDao;
    this.userDao = userDao;
  }

  @Override
  public void getTransactions(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    transactionDao
      .getTransactionsByUser(context.getUser().getString("username"))
      .setHandler(ServiceUtils.sendRetrievedArrayHandler(resultHandler, Transaction::toJson));
  }

  @Override
  public void createTransaction(NewTransaction body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    userDao.isAllowed(context.getUser().getString("username"), body.getTo()).setHandler(ar -> {
      if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
      if (!ar.result()) resultHandler.handle(Future.succeededFuture(
        new OperationResponse().setStatusCode(403).setStatusMessage("Forbidden").setPayload(Buffer.buffer("You need " + body.getTo() + " authorization to add a new transaction with him as recipient"))
      ));
      else
        transactionDao.newTransaction(body, context.getUser().getString("username")).setHandler(newAr -> {
          if (newAr.failed()) resultHandler.handle(Future.failedFuture(newAr.cause()));
          else resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(newAr.result().toJson())));
        });
    });
  }

  @Override
  public void getTransaction(String transactionId, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    transactionDao
      .getTransaction(transactionId)
      .setHandler(ServiceUtils.sendRetrievedObjectHandler(resultHandler, Transaction::toJson));
  }

  @Override
  public void updateTransaction(
    String transactionId,
    UpdateTransaction body,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    if (body.getDescription() == null && body.getValue() == null)
      resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(400).setStatusMessage("Bad Request")));
    else
      transactionDao.getTransaction(transactionId).setHandler(getAr -> {
        if (getAr.failed()) resultHandler.handle(Future.failedFuture(getAr.cause()));
        if (getAr.result() == null) resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(404).setStatusMessage("Not Found")));
        if (!getAr.result().getFrom().equals(context.getUser().getString("username")))
          resultHandler.handle(Future.succeededFuture(new OperationResponse()
            .setStatusCode(403)
            .setStatusMessage("Forbidden")
            .setPayload(Buffer.buffer("You are not authorized to update " + transactionId + " because you are not the transaction creator"))));
        transactionDao.updateTransaction(transactionId, body, getAr.result()).setHandler(ar -> {
          if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
          resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(200).setStatusMessage("OK")));
        });
      });
  }

  @Override
  public void deleteTransaction(
    String transactionId,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    transactionDao.getTransaction(transactionId).setHandler(getAr -> {
      if (getAr.failed()) resultHandler.handle(Future.failedFuture(getAr.cause()));
      if (getAr.result() == null) resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(404).setStatusMessage("Not Found")));
      if (!getAr.result().getFrom().equals(context.getUser().getString("username")))
        resultHandler.handle(Future.succeededFuture(new OperationResponse()
          .setStatusCode(403)
          .setStatusMessage("Forbidden")
          .setPayload(Buffer.buffer("You are not authorized to remove " + transactionId + " because you are not the transaction creator"))));
      transactionDao.removeTransaction(transactionId).setHandler(ar -> {
        if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
        resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(200).setStatusMessage("OK")));
      });
    });
  }

}

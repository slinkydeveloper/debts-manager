package io.slinkydeveloper.debtsmanager.services.impl;

import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.slinkydeveloper.debtsmanager.models.UpdateTransaction;
import io.slinkydeveloper.debtsmanager.persistence.StatusPersistence;
import io.slinkydeveloper.debtsmanager.persistence.TransactionPersistence;
import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.slinkydeveloper.debtsmanager.services.TransactionsService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

import java.time.ZonedDateTime;
import java.util.Map;

public class TransactionsServiceImpl implements TransactionsService {

  private final Vertx vertx;
  private final StatusPersistence statusPersistence;
  private final TransactionPersistence transactionPersistence;
  private final UserPersistence userPersistence;

  public TransactionsServiceImpl(Vertx vertx, StatusPersistence statusPersistence, TransactionPersistence transactionPersistence, UserPersistence userPersistence) {
    this.vertx = vertx;
    this.statusPersistence = statusPersistence;
    this.transactionPersistence = transactionPersistence;
    this.userPersistence = userPersistence;
  }

  @Override
  public void getTransactions(OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    transactionPersistence
      .getTransactionsByUser(context.getUser().getString("username"))
      .setHandler(ServiceUtils.sendRetrievedArrayHandler(resultHandler, Transaction::toJson));
  }

  @Override
  public void createTransaction(NewTransaction body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    userPersistence.isAllowed(context.getUser().getString("username"), body.getTo()).setHandler(ar -> {
      if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
      if (!ar.result()) resultHandler.handle(Future.succeededFuture(
        new OperationResponse().setStatusCode(403).setStatusMessage("Forbidden").setPayload(Buffer.buffer("You need " + body.getTo() + " authorization to add a new transaction with him as recipient"))
      ));
      else
        transactionPersistence.newTransaction(body, context.getUser().getString("username")).setHandler(newAr -> {
          if (newAr.failed()) resultHandler.handle(Future.failedFuture(newAr.cause()));
          resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(newAr.result().toJson())));
        });
    });
  }

  @Override
  public void getTransaction(String transactionId, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    transactionPersistence
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
      transactionPersistence.getTransaction(transactionId).setHandler(getAr -> {
        if (getAr.failed()) resultHandler.handle(Future.failedFuture(getAr.cause()));
        if (getAr.result() == null) resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(404).setStatusMessage("Not Found")));
        if (!getAr.result().getFrom().equals(context.getUser().getString("username")))
          resultHandler.handle(Future.succeededFuture(new OperationResponse()
            .setStatusCode(403)
            .setStatusMessage("Forbidden")
            .setPayload(Buffer.buffer("You are not authorized to update " + transactionId + " because you are not the transaction creator"))));
        transactionPersistence.updateTransaction(transactionId, body).setHandler(ar -> {
          if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
          resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(200).setStatusMessage("OK")));
        });
      });
  }

  @Override
  public void deleteTransaction(
    String transactionId,
    OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    transactionPersistence.getTransaction(transactionId).setHandler(getAr -> {
      if (getAr.failed()) resultHandler.handle(Future.failedFuture(getAr.cause()));
      if (getAr.result() == null) resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(404).setStatusMessage("Not Found")));
      if (!getAr.result().getFrom().equals(context.getUser().getString("username")))
        resultHandler.handle(Future.succeededFuture(new OperationResponse()
          .setStatusCode(403)
          .setStatusMessage("Forbidden")
          .setPayload(Buffer.buffer("You are not authorized to remove " + transactionId + " because you are not the transaction creator"))));
      transactionPersistence.removeTransaction(transactionId).setHandler(ar -> {
        if (ar.failed()) resultHandler.handle(Future.failedFuture(ar.cause()));
        resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusCode(200).setStatusMessage("OK")));
      });
    });
  }

  @Override
  public void getUserStatus(String till, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    if (till == null)
      statusPersistence
        .getStatus(context.getUser().getString("username"))
        .setHandler(ServiceUtils.sendRetrievedObjectHandler(resultHandler, ServiceUtils::buildJsonFromStatusMap));
    else
      statusPersistence
        .getStatusTill(context.getUser().getString("username"), ZonedDateTime.parse(till))
        .setHandler(ServiceUtils.sendRetrievedObjectHandler(resultHandler, ServiceUtils::buildJsonFromStatusMap));
  }

}

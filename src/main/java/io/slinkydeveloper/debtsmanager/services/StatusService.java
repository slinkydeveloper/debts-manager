package io.slinkydeveloper.debtsmanager.services;

import io.slinkydeveloper.debtsmanager.persistence.StatusRetriever;
import io.slinkydeveloper.debtsmanager.services.impl.StatusServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface StatusService {

  static StatusService create(Vertx vertx, StatusRetriever statusRetriever) {
    return new StatusServiceImpl(vertx, statusRetriever);
  }

  void getUserStatus(String till, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}

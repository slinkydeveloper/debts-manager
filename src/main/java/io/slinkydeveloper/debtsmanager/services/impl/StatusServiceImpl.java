package io.slinkydeveloper.debtsmanager.services.impl;

import io.slinkydeveloper.debtsmanager.persistence.StatusRetriever;
import io.slinkydeveloper.debtsmanager.services.StatusService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

import java.time.ZonedDateTime;

public class StatusServiceImpl implements StatusService {

  private final Vertx vertx;
  private final StatusRetriever statusRetriever;

  public StatusServiceImpl(Vertx vertx, StatusRetriever statusRetriever) {
    this.vertx = vertx;
    this.statusRetriever = statusRetriever;
  }

  @Override
  public void getUserStatus(String till, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    if (till == null)
      statusRetriever
        .getStatus(context.getUser().getString("username"))
        .setHandler(ServiceUtils.sendRetrievedObjectHandler(resultHandler, ServiceUtils::buildJsonFromStatusMap));
    else
      statusRetriever
        .getStatusTill(context.getUser().getString("username"), ZonedDateTime.parse(till))
        .setHandler(ServiceUtils.sendRetrievedObjectHandler(resultHandler, ServiceUtils::buildJsonFromStatusMap));
  }
}

package io.slinkydeveloper.debtsmanager.services.impl;

import io.slinkydeveloper.debtsmanager.dao.StatusDao;
import io.slinkydeveloper.debtsmanager.services.StatusService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

import java.time.ZonedDateTime;

public class StatusServiceImpl implements StatusService {

  private final Vertx vertx;
  private final StatusDao statusDao;

  public StatusServiceImpl(Vertx vertx, StatusDao statusDao) {
    this.vertx = vertx;
    this.statusDao = statusDao;
  }

  @Override
  public void getUserStatus(String till, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler){
    if (till == null)
      statusDao
        .getStatus(context.getUser().getString("username"))
        .setHandler(ServiceUtils.sendRetrievedObjectHandler(resultHandler, ServiceUtils::buildJsonFromStatusMap));
    else
      statusDao
        .getStatusTill(context.getUser().getString("username"), ZonedDateTime.parse(till))
        .setHandler(ServiceUtils.sendRetrievedObjectHandler(resultHandler, ServiceUtils::buildJsonFromStatusMap));
  }
}

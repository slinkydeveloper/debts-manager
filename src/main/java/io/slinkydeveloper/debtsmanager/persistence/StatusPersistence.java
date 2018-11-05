package io.slinkydeveloper.debtsmanager.persistence;

import io.slinkydeveloper.debtsmanager.models.Status;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.vertx.core.Future;

import java.time.ZonedDateTime;

public interface StatusPersistence {

  Future<Status> getStatus(String username);
  Future<Status> getStatusTill(String username, ZonedDateTime time);
  void triggerStatusRefreshFromTransactionUpdate(Transaction transaction);
  void triggerStatusRefreshFromTransactionRemove(Transaction transaction);
  void triggerStatusRefreshFromTransactionCreation(Transaction transaction);

}

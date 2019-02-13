package io.slinkydeveloper.debtsmanager.persistence;

import io.reactiverse.pgclient.PgPool;
import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.slinkydeveloper.debtsmanager.models.UpdateTransaction;
import io.slinkydeveloper.debtsmanager.persistence.impl.TransactionDaoImpl;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManagerService;
import io.vertx.core.Future;

import java.util.List;

public interface TransactionDao {

  Future<List<Transaction>> getTransactionsByUser(String username);
  Future<Transaction> getTransaction(String id);
  Future<Transaction> newTransaction(NewTransaction transaction, String from);
  Future<Void> updateTransaction(String id, UpdateTransaction updateTransaction, Transaction oldTransaction);
  Future<Void> removeTransaction(String id);

  static TransactionDao create(PgPool client, ReadModelManagerService readModelManager) {
    return new TransactionDaoImpl(client, readModelManager);
  }

}

package io.slinkydeveloper.debtsmanager.persistence;

import io.reactiverse.pgclient.PgPool;
import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.slinkydeveloper.debtsmanager.models.UpdateTransaction;
import io.slinkydeveloper.debtsmanager.persistence.impl.TransactionPersistenceImpl;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManager;
import io.vertx.core.Future;

import java.util.List;

public interface TransactionPersistence {

  Future<List<Transaction>> getTransactionsByUser(String username);
  Future<Transaction> getTransaction(String id);
  Future<Transaction> newTransaction(NewTransaction transaction, String from);
  Future<Void> updateTransaction(String id, UpdateTransaction transaction);
  Future<Void> removeTransaction(String id);

  static TransactionPersistence create(PgPool client, ReadModelManager statusCacheManager) {
    return new TransactionPersistenceImpl(client, statusCacheManager);
  }

}

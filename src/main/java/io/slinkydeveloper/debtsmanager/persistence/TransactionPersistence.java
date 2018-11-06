package io.slinkydeveloper.debtsmanager.persistence;

import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.slinkydeveloper.debtsmanager.models.UpdateTransaction;
import io.vertx.core.Future;

import java.util.List;

public interface TransactionPersistence {

  Future<List<Transaction>> getTransactionsByUser(String username);
  Future<Transaction> getTransaction(String id);
  Future<Transaction> newTransaction(NewTransaction transaction, String from);
  Future<Void> updateTransaction(String id, UpdateTransaction transaction);
  Future<Void> removeTransaction(String id);

}

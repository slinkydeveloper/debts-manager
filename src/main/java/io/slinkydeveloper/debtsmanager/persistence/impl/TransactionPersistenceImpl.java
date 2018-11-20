package io.slinkydeveloper.debtsmanager.persistence.impl;

import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.slinkydeveloper.debtsmanager.models.UpdateTransaction;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManagerService;
import io.slinkydeveloper.debtsmanager.persistence.TransactionPersistence;
import io.slinkydeveloper.debtsmanager.readmodel.command.UpdateStatusAfterTransactionCreationCommand;
import io.slinkydeveloper.debtsmanager.readmodel.command.UpdateStatusAfterTransactionUpdateCommand;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TransactionPersistenceImpl implements TransactionPersistence {

  private final PgPool client;
  private final ReadModelManagerService readModelManager;

  private final static Logger log = LoggerFactory.getLogger(TransactionPersistenceImpl.class);
  private final static Handler<AsyncResult<Boolean>> READ_MODEL_MANAGER_RESULT_HANDLER = ar -> {
    if (ar.failed()) log.warn("Unable to update read model", ar.cause());
  };

  public TransactionPersistenceImpl(PgPool client, ReadModelManagerService readModelManager) {
    this.client = client;
    this.readModelManager = readModelManager;
  }

  @Override
  public Future<List<Transaction>> getTransactionsByUser(String username) {
    Future<List<Transaction>> fut = Future.future();
    client.preparedQuery("SELECT * FROM \"transaction\" WHERE \"transaction\".\"to\"=$1 OR \"transaction\".\"from\"=$1", Tuple.of(username), ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      List<Transaction> result = StreamSupport.stream(ar.result().spliterator(), false).map(this::mapRowToTransaction).collect(Collectors.toList());
      fut.complete(result);
    });
    return fut;
  }

  @Override
  public Future<Transaction> getTransaction(String id) {
    Future<Transaction> fut = Future.future();
    client.preparedQuery("SELECT * FROM \"transaction\" WHERE id=$1", Tuple.of(id), ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      if (ar.result().rowCount() != 1) fut.complete(null);
      fut.complete(mapRowToTransaction(ar.result().iterator().next()));
    });
    return fut;
  }

  @Override
  public Future<Transaction> newTransaction(NewTransaction transaction, String from) {
    Future<Transaction> fut = Future.future();
    client.preparedQuery("INSERT INTO \"transaction\" (description, \"from\", \"to\", at, value) VALUES ($1, $2, $3, current_timestamp, $4) ON CONFLICT DO NOTHING RETURNING *",
      Tuple.of(transaction.getDescription(), from, transaction.getTo(), transaction.getValue()), ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      Transaction t = mapRowToTransaction(ar.result().iterator().next());
      readModelManager.runCommand(new UpdateStatusAfterTransactionCreationCommand(t.getId(), from, transaction.getTo(), transaction.getValue()).toJson(), READ_MODEL_MANAGER_RESULT_HANDLER);
      fut.complete(t);
    });
    return fut;
  }

  @Override
  public Future<Void> updateTransaction(String id, UpdateTransaction updateTransaction) {
    Future<Void> fut = Future.future();
    String query;
    Tuple tuple;
    if (updateTransaction.getDescription() != null && updateTransaction.getValue() != null) {
      query = "UPDATE \"transaction\" SET description=$1, value=$2 WHERE id=$3 RETURNING *";
      tuple = Tuple.of(updateTransaction.getDescription(), updateTransaction.getValue(), id);
    } else if (updateTransaction.getDescription() != null) {
      query = "UPDATE \"transaction\" SET description=$1 WHERE id=$3";
      tuple = Tuple.of(updateTransaction.getDescription(), id);
    } else {
      query = "UPDATE \"transaction\" SET value=$2 WHERE id=$3 RETURNING *";
      tuple = Tuple.of(updateTransaction.getValue(), id);
    }
    client.preparedQuery(query, tuple, ar -> {
        if (ar.failed()) fut.fail(ar.cause());
        if (updateTransaction.getValue() != null) {
          Transaction oldT = mapRowToTransaction(ar.result().iterator().next());
          readModelManager
            .runCommand(
              new UpdateStatusAfterTransactionUpdateCommand(id, oldT.getFrom(), oldT.getTo(), oldT.getValue(), updateTransaction.getValue()).toJson(),
              READ_MODEL_MANAGER_RESULT_HANDLER
            );
        }
        fut.complete();
    });
    return fut;
  }

  @Override
  public Future<Void> removeTransaction(String id) {
    Future<Void> fut = Future.future();
    client.preparedQuery("DELETE FROM \"transaction\" WHERE id=$1 RETURNING *", Tuple.of(id), ar -> {
        if (ar.failed()) fut.fail(ar.cause());
        Transaction t = mapRowToTransaction(ar.result().iterator().next());
      readModelManager.runCommand(new UpdateStatusAfterTransactionCreationCommand(t.getId(), t.getFrom(), t.getTo(), t.getValue()).toJson(), READ_MODEL_MANAGER_RESULT_HANDLER);
        fut.complete();
    });
    return fut;
  }

  private Transaction mapRowToTransaction(Row row) {
    return new Transaction(
      row.getInteger("id").toString(),
      row.getString("from"),
      row.getString("to"),
      row.getDouble("value"),
      row.getString("description"),
      row.getString("at")
    );
  }
}

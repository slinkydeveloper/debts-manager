package io.slinkydeveloper.debtsmanager.persistence.impl;

import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Tuple;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserPersistenceImpl implements UserPersistence {

  PgPool client;

  public UserPersistenceImpl(PgPool client) {
    this.client = client;
  }

  @Override
  public Future<Boolean> addUser(AuthCredentials user) {
    Future<Boolean> fut = Future.future();
    client.preparedQuery("INSERT INTO \"user\" (username, password) VALUES ($1, $2) ON CONFLICT DO NOTHING", Tuple.of(user.getUsername(), user.getPassword()), ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      fut.complete(ar.result().rowCount() == 1);
    });
    return fut;
  }

  @Override
  public Future<Boolean> userExists(AuthCredentials user) {
    Future<Boolean> fut = Future.future();
    client.preparedQuery("SELECT username FROM \"user\" WHERE username=$1 AND password=$2", Tuple.of(user.getUsername(), user.getPassword()), ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      fut.complete(ar.result().rowCount() == 1);
    });
    return fut;
  }

  @Override
  public Future<List<String>> getUsersList() {
    Future<List<String>> fut = Future.future();
    client.query("SELECT username FROM \"user\"", generateUserListHandler(fut));
    return fut;
  }

  @Override
  public Future<Void> addUserConnection(String from, String to) {
    Future<Void> fut = Future.future();
    client.preparedQuery("INSERT INTO userrelationship (\"from\", \"to\") VALUES ($1, $2) ON CONFLICT DO NOTHING", Tuple.of(from, to), ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      fut.complete();
    });
    return fut;
  }

  @Override
  public Future<List<String>> getAllowedFrom(String username) {
    Future<List<String>> fut = Future.future();
    client.preparedQuery("SELECT \"from\" FROM userrelationship WHERE \"to\"=$1", Tuple.of(username), generateUserListHandler(fut));
    return fut;
  }

  @Override
  public Future<List<String>> getAllowedTo(String username) {
    Future<List<String>> fut = Future.future();
    client.preparedQuery("SELECT \"to\" FROM userrelationship WHERE \"from\"=$1", Tuple.of(username), generateUserListHandler(fut));
    return fut;
  }

  @Override
  public Future<Boolean> isAllowed(String from, String to) {
    Future<Boolean> fut = Future.future();
    client.preparedQuery("SELECT \"to\" FROM userrelationship WHERE \"from\"=$1 AND \"to\"=$2", Tuple.of(from, to), ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      fut.complete(ar.result().rowCount() == 1);
    });
    return fut;
  }

  private Handler<AsyncResult<PgRowSet>> generateUserListHandler(Future<List<String>> fut) {
    return ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      List<String> result = StreamSupport.stream(ar.result().spliterator(), false).map(row -> row.getString(0)).collect(Collectors.toList());
      fut.complete(result);
    };
  }
}

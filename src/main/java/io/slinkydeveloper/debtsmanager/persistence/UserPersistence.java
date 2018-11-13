package io.slinkydeveloper.debtsmanager.persistence;

import io.reactiverse.pgclient.PgPool;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.persistence.impl.UserPersistenceImpl;
import io.vertx.core.Future;

import java.util.List;

public interface UserPersistence {

  Future<Void> addUser(AuthCredentials user);
  Future<Boolean> userExists(AuthCredentials user);
  Future<List<String>> getUsersList();
  Future<Void> addUserConnection(String from, String to);
  Future<List<String>> getAllowedFrom(String username);
  Future<List<String>> getAllowedTo(String username);
  Future<Boolean> isAllowed(String from, String to);

  static UserPersistence create(PgPool client) {
    return new UserPersistenceImpl(client);
  }

}

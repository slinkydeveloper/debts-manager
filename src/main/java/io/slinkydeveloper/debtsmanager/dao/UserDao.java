package io.slinkydeveloper.debtsmanager.dao;

import io.reactiverse.pgclient.PgPool;
import io.slinkydeveloper.debtsmanager.dao.impl.UserDaoImpl;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.vertx.core.Future;

import java.util.List;

public interface UserDao {

  Future<Boolean> addUser(AuthCredentials user);
  Future<Boolean> userExists(AuthCredentials user);
  Future<List<String>> getUsersList();
  Future<List<String>> getUsersList(String filter);
  Future<Void> addUserConnection(String from, String to);
  Future<List<String>> getAllowedFrom(String username);
  Future<List<String>> getAllowedTo(String username);
  Future<Boolean> isAllowed(String from, String to);

  static UserDao create(PgPool client) {
    return new UserDaoImpl(client);
  }

}

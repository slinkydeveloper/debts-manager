package io.slinkydeveloper.debtsmanager.persistence;

import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.vertx.core.Future;

import java.util.List;

public interface UserPersistence {

  Future<Void> addUser(AuthCredentials user);
  Future<Void> userExists(AuthCredentials user);
  Future<List<String>> getUsersList();
  Future<Void> addUserConnection(String from, String to);
  Future<List<String>> getAllowedFrom(String username);
  Future<List<String>> getAllowedTo(String username);
  Future<Boolean> isAllowed(String from, String to);

}

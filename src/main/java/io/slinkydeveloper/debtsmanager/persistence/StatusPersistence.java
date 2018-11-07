package io.slinkydeveloper.debtsmanager.persistence;

import io.vertx.core.Future;

import java.time.ZonedDateTime;
import java.util.Map;

public interface StatusPersistence {

  Future<Map<String, Double>> getStatus(String username);
  Future<Map<String, Double>> getStatusTill(String username, ZonedDateTime time);

}

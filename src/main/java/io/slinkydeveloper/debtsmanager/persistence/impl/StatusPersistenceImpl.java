package io.slinkydeveloper.debtsmanager.persistence.impl;

import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.slinkydeveloper.debtsmanager.persistence.StatusPersistence;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class StatusPersistenceImpl implements StatusPersistence {

  private final RedisClient redisClient;
  private final PgPool pgClient;
  private final String buildStatusQuery;
  private final String buildStatusBeforeQuery;

  public StatusPersistenceImpl(RedisClient redisClient, PgPool pgClient, String buildStatusQuery, String buildStatusBeforeQuery) {
    this.redisClient = redisClient;
    this.pgClient = pgClient;
    this.buildStatusQuery = buildStatusQuery;
    this.buildStatusBeforeQuery = buildStatusBeforeQuery;
  }

  @Override
  public Future<Map<String, Double>> getStatus(String username) {
    Future<Map<String, Double>> future = Future.future();
    redisClient.get(username, ar -> {
      if (ar.failed()) future.fail(ar.cause());
      if (ar.result() == null) {
        // Nothing in redis cache!

      }
      Map<String, Double> status = new JsonObject(ar.result()).mapTo(Map.class);
      future.complete(status);
    });
    return future;
  }

  @Override
  public Future<Map<String, Double>> getStatusTill(String username, ZonedDateTime time) {
    Future<Map<String, Double>> fut = Future.future();
    pgClient.preparedQuery(buildStatusBeforeQuery, Tuple.of(username, time.withZoneSameInstant(ZoneId.of("UTC"))), ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      fut.complete(buildStatusFromPgRowSet(ar.result()));
    });
    return fut;
  }

  private Map<String, Double> buildStatusFromPgRowSet(PgRowSet set) {
    Map<String, Double> result = new HashMap<>();
    for (Row row : set) {
      result.put(row.getString("username"), row.getDouble("total"));
    }
    return result;
  }
}

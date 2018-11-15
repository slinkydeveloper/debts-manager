package io.slinkydeveloper.debtsmanager.persistence.impl;

import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.slinkydeveloper.debtsmanager.persistence.StatusCacheManager;
import io.slinkydeveloper.debtsmanager.persistence.StatusPersistence;
import io.vertx.core.Future;
import io.vertx.redis.RedisClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StatusPersistenceImpl implements StatusPersistence {

  private final RedisClient redisClient;
  private final PgPool pgClient;
  private final String statusPrefix;
  private final String buildStatusQuery;
  private final String buildStatusBeforeQuery;

  private final StatusCacheManager statusCacheManager;

  private final static Collector<Row, ?, Map<String, Double>> statusCollector = Collectors.toMap(
    row -> row.getString("username"),
    row -> row.getDouble("total")
  );

  public StatusPersistenceImpl(RedisClient redisClient, PgPool pgClient, String statusPrefix, String buildStatusQuery, String buildStatusBeforeQuery, StatusCacheManager statusCacheManager) {
    this.redisClient = redisClient;
    this.pgClient = pgClient;
    this.statusPrefix = statusPrefix;
    this.buildStatusQuery = buildStatusQuery;
    this.buildStatusBeforeQuery = buildStatusBeforeQuery;
    this.statusCacheManager = statusCacheManager;
  }

  @Override
  public Future<Map<String, Double>> getStatus(String username) {
    return getStatusFromCache(username).compose(map -> {
      if (map == null) {
        Future<Map<String, Double>> fut = Future.succeededFuture();
        return getStatusFromDb(username).setHandler(ar -> {
          if (ar.succeeded()) {
            statusCacheManager.pushStatusCache(username, ar.result());
            fut.complete(ar.result());
          } else fut.fail(ar.cause());
        });
      } else {
        return Future.succeededFuture(map);
      }
    });
  }

  @Override
  public Future<Map<String, Double>> getStatusFromCache(String username) {
    Future<Map<String, Double>> future = Future.future();
    redisClient.hgetall(statusPrefix + username, ar -> {
      if (ar.failed()) future.fail(ar.cause());
      Map<String, Double> status = StatusUtils.mapToStatusMap(ar.result());
      future.complete(status);
    });
    return future;
  }

  @Override
  public Future<Map<String, Double>> getStatusFromDb(String username) {
    Future<Map<String, Double>> fut = Future.future();
    pgClient.preparedQuery(buildStatusQuery, statusCollector, ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      fut.complete(ar.result().value());
    });
    return fut;
  }

  @Override
  public Future<Map<String, Double>> getStatusTill(String username, ZonedDateTime time) {
    Future<Map<String, Double>> fut = Future.future();
    pgClient.preparedQuery(buildStatusBeforeQuery, Tuple.of(username, time.withZoneSameInstant(ZoneId.of("UTC"))), statusCollector, ar -> {
      if (ar.failed()) fut.fail(ar.cause());
      fut.complete(ar.result().value());
    });
    return fut;
  }
}

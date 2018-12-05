package io.slinkydeveloper.debtsmanager.persistence.impl;

import io.reactiverse.pgclient.*;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManagerService;
import io.slinkydeveloper.debtsmanager.persistence.StatusPersistence;
import io.slinkydeveloper.debtsmanager.readmodel.command.PushNewStatusCommand;
import io.slinkydeveloper.debtsmanager.utils.FutureUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static io.slinkydeveloper.debtsmanager.utils.FutureUtils.futurify;

public class StatusPersistenceImpl implements StatusPersistence {

  private static String buildStatusQuery;
  private static String buildStatusBeforeQuery;

  static {
    try {
      buildStatusQuery = String.join("\n", Files.readAllLines(Paths.get(StatusPersistenceImpl.class.getClassLoader().getResource("sql/build_status_query.sql").toURI())));
      buildStatusBeforeQuery = String.join("\n", Files.readAllLines(Paths.get(StatusPersistenceImpl.class.getClassLoader().getResource("sql/build_status_before_query.sql").toURI())));
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
  }

  private final RedisClient redisClient;
  private final PgPool pgClient;

  private final ReadModelManagerService readModelManager;

  private final static Logger log = LoggerFactory.getLogger(StatusPersistenceImpl.class);
  private final static Handler<AsyncResult<Boolean>> READ_MODEL_MANAGER_RESULT_HANDLER = ar -> {
    if (ar.failed()) log.warn("Unable to update read model", ar.cause());
  };

  private final static Collector<Row, ?, Map<String, Double>> statusCollector = Collectors.toMap(
    row -> row.getString("username"),
    row -> row.getDouble("total")
  );

  public StatusPersistenceImpl(RedisClient redisClient, PgPool pgClient, ReadModelManagerService readModelManager) {
    this.redisClient = redisClient;
    this.pgClient = pgClient;
    this.readModelManager = readModelManager;
  }

  @Override
  public Future<Map<String, Double>> getStatus(String username) {
    return getStatusFromCache(username).compose(map -> {
      if (map == null) {
        Future<Map<String, Double>> fut = Future.future();
        getStatusFromDb(username).setHandler(ar -> {
          if (ar.succeeded()) {
            readModelManager.runCommand(new PushNewStatusCommand(username, ar.result()).toJson(), READ_MODEL_MANAGER_RESULT_HANDLER);
            fut.complete(ar.result());
          } else fut.fail(ar.cause());
        });
        return fut;
      } else {
        return Future.succeededFuture(map);
      }
    });
  }

  @Override
  public Future<Map<String, Double>> getStatusFromCache(String username) {
    return FutureUtils
      .<Long>futurify(h -> redisClient.scard("commands:" + username, h))
      .compose(n ->
        (n == 0) ?
          Future.succeededFuture() :
          FutureUtils.<JsonObject>futurify(h -> redisClient.hgetall("status:" + username, h)).map(StatusUtils::mapToStatusMap)
      );
  }

  @Override
  public Future<Map<String, Double>> getStatusFromDb(String username) {
    return FutureUtils
      .<PgResult<Map<String, Double>>>futurify(h -> pgClient.preparedQuery(buildStatusQuery, Tuple.of(username), statusCollector, h))
      .map(PgResult::value);
  }

  @Override
  public Future<Map<String, Double>> getStatusTill(String username, ZonedDateTime time) {
    return FutureUtils
      .<PgResult<Map<String, Double>>>futurify(h -> pgClient.preparedQuery(buildStatusBeforeQuery, Tuple.of(username, time.withZoneSameInstant(ZoneId.of("UTC"))), statusCollector, h))
      .map(PgResult::value);
  }
}

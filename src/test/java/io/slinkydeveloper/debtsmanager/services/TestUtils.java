package io.slinkydeveloper.debtsmanager.services;

import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgRowSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.junit5.VertxTestContext;
import io.vertx.redis.RedisClient;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {

  public static Future<Void> wipeDb(PgPool pgClient) {
    Future<PgRowSet> fut = Future.future();
    pgClient.query("TRUNCATE \"user\", \"userrelationship\", \"transaction\"", fut.completer());
    return fut.map(p -> null);
  }

  public static Future<Void> wipeRedis(RedisClient redisClient) {
    Future<String> fut = Future.future();
    redisClient.flushdb(fut.completer());
    return fut.map(p -> null);
  }

  public static void assertSuccessResponse(OperationResponse actual) {
    assertEquals(Integer.valueOf(200), actual.getStatusCode());
    assertEquals("OK", actual.getStatusMessage());
  }

  public static void assertSuccessResponse(String expectedContentType, OperationResponse actual) {
    assertEquals(Integer.valueOf(200), actual.getStatusCode());
    assertEquals("OK", actual.getStatusMessage());
    assertEquals(expectedContentType, actual.getHeaders().get("content-type"));
  }

  public static void assertTextResponse(int expectedStatusCode, String expectedStatusMessage, String expectedResult, OperationResponse actual) {
    assertEquals(Integer.valueOf(expectedStatusCode), actual.getStatusCode());
    assertEquals(expectedStatusMessage, actual.getStatusMessage());
    assertEquals("text/plain", actual.getHeaders().get("content-type"));
    assertEquals(expectedResult, actual.getPayload().toString());
  }

  public static void assertJsonResponse(int expectedStatusCode, String expectedStatusMessage, JsonObject expectedResult, OperationResponse actual) {
    assertEquals(Integer.valueOf(expectedStatusCode), actual.getStatusCode());
    assertEquals(expectedStatusMessage, actual.getStatusMessage());
    assertEquals("application/json", actual.getHeaders().get("content-type"));
    assertEquals(expectedResult, actual.getPayload().toJsonObject());
  }

  public static void assertJsonResponse(int expectedStatusCode, String expectedStatusMessage, JsonArray expectedResult, OperationResponse actual) {
    assertEquals(Integer.valueOf(expectedStatusCode), actual.getStatusCode());
    assertEquals(expectedStatusMessage, actual.getStatusMessage());
    assertEquals("application/json", actual.getHeaders().get("content-type"));
    assertEquals(expectedResult, actual.getPayload().toJsonArray());
  }

}

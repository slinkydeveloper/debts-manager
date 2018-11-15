package io.slinkydeveloper.debtsmanager.services;

import io.reactiverse.pgclient.PgPool;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.junit5.VertxTestContext;
import io.vertx.redis.RedisClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {

  public static void wipeDb(PgPool pgClient, VertxTestContext testContext) {
    pgClient.query("TRUNCATE \"user\", \"userrelationship\", \"transaction\"", testContext.succeeding(r -> testContext.completeNow()));
  }

  public static void wipeRedis(RedisClient redisClient, VertxTestContext testContext) {
    redisClient.flushdb(testContext.succeeding(r -> testContext.completeNow()));
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
    assertEquals("text/plain", actual.getHeaders().get("content-type"));
    assertEquals(expectedResult, actual.getPayload().toJsonObject());
  }

  public static void assertJsonResponse(int expectedStatusCode, String expectedStatusMessage, JsonArray expectedResult, OperationResponse actual) {
    assertEquals(Integer.valueOf(expectedStatusCode), actual.getStatusCode());
    assertEquals(expectedStatusMessage, actual.getStatusMessage());
    assertEquals("text/plain", actual.getHeaders().get("content-type"));
    assertEquals(expectedResult, actual.getPayload().toJsonArray());
  }

}

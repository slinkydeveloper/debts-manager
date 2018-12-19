package io.slinkydeveloper.debtsmanager.services;

import io.reactiverse.pgclient.*;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static io.slinkydeveloper.debtsmanager.services.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UsersService Test
 */
@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersServiceTest extends BaseServicesTest {

  private PgPool pgClient;
  private UserPersistence userPersistence;
  private JWTAuth auth;
  private OperationRequest loggedContext;

  @Override
  @BeforeAll
  public void beforeAll(Vertx vertx, VertxTestContext testContext) throws Exception {
    super.beforeAll(vertx, testContext);
    pgClient = PgClient.pool(vertx,
      new PgPoolOptions()
        .setPort(environment.getServicePort("postgres", POSTGRESQL_PORT))
        .setHost(environment.getServiceHost("postgres", POSTGRESQL_PORT))
        .setDatabase("debts-manager")
        .setUser("postgres")
        .setPassword("postgres")
    );
    userPersistence = UserPersistence.create(pgClient);
    vertx.fileSystem().readFile("jwk.json", testContext.succeeding(buf -> {
      auth = JWTAuth.create(vertx, new JWTAuthOptions().addJwk(buf.toJsonObject()));
      usersService = UsersService.create(vertx, userPersistence, auth);
      testContext.completeNow();
    }));
  }

  @BeforeEach
  public void before(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    testContext.assertComplete(
      wipeDb(pgClient)
        .compose(v -> registerBeforeTestLogin(new AuthCredentials("francesco", "francesco"),  testContext))
    ).setHandler(ar -> {
      loggedContext = new OperationRequest().setUser(new JsonObject().put("username", "francesco"));
      testContext.completeNow();
    });
    testContext.awaitCompletion(1000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void getUsers(VertxTestContext test) {
    Checkpoint checkNonFiltered = test.checkpoint();
    Checkpoint checkFiltered = test.checkpoint();
    pgClient.preparedQuery(
      "INSERT INTO \"user\" (username, password) VALUES ($1, $2), ($3, $4), ($5, $6)",
      Tuple.of("slinky", "slinky", "paolorossi", "paolorossi", "mariorossi", "mariorossi"), test.succeeding(rows -> {
        usersService.getUsers(null, loggedContext, test.succeeding(op -> {
          test.verify(() -> {
            assertJsonResponse(200, "OK", new JsonArray().add("francesco").add("slinky").add("paolorossi").add("mariorossi"), op);
          });
          checkNonFiltered.flag();
        }));
        usersService.getUsers("rossi", loggedContext, test.succeeding(op -> {
          test.verify(() -> {
            assertJsonResponse(200, "OK", new JsonArray().add("paolorossi").add("mariorossi"), op);
          });
          checkFiltered.flag();
        }));
      }));
  }

  @Test
  public void connectUser(VertxTestContext test) {
    pgClient.preparedQuery(
      "INSERT INTO \"user\" (username, password) VALUES ($1, $2)",
      Tuple.of("slinky", "slinky"), test.succeeding(insertResult -> {
        usersService.connectUser("slinky", loggedContext, test.succeeding(op -> {
          test.verify(() -> {
            assertSuccessResponse(op);
          });
          pgClient.preparedQuery(
            "SELECT * FROM userrelationship", test.succeeding(rows -> {
              test.verify(() -> {
                assertEquals(1, rows.rowCount());
                Row row = rows.iterator().next();
                assertEquals("slinky", row.getString("from"));
                assertEquals("francesco", row.getString("to"));
              });
              test.completeNow();
            })
          );
        }));
      }));
  }

  @Test
  public void getConnectedUsersWithEmptyResponse(VertxTestContext test) {
    usersService.getConnectedUsers(loggedContext, test.succeeding(op -> {
      test.verify(() ->
        assertJsonResponse(200, "OK", new JsonObject().put("allowedFrom", new JsonArray()).put("allowedTo", new JsonArray()), op)
      );
      test.completeNow();
    }));
  }

  @Test
  public void getConnectedUsers(VertxTestContext test) {
    pgClient.preparedQuery(
      "INSERT INTO \"user\" (username, password) VALUES ($1, $2), ($3, $4)",
      Tuple.of("slinky", "slinky", "mariorossi", "mariorossi"), test.succeeding(insert1Result -> {
        pgClient.preparedQuery(
          "INSERT INTO \"userrelationship\" (\"from\", \"to\") VALUES ($1, $2), ($3, $4), ($5, $6)",
          Tuple.of("francesco", "slinky", "slinky", "francesco", "mariorossi", "francesco"), test.succeeding(insert2Result -> {
            usersService.getConnectedUsers(loggedContext, test.succeeding(op -> {
              test.verify(() ->
                assertJsonResponse(200, "OK", new JsonObject()
                    .put("allowedFrom", new JsonArray().add("slinky").add("mariorossi"))
                    .put("allowedTo", new JsonArray().add("slinky")),
                  op)
              );
              test.completeNow();
            }));
          }));
      }));
  }

}

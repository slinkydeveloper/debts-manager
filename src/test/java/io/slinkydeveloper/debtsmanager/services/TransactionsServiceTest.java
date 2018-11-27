package io.slinkydeveloper.debtsmanager.services;

import io.reactiverse.pgclient.*;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.slinkydeveloper.debtsmanager.persistence.StatusPersistence;
import io.slinkydeveloper.debtsmanager.persistence.TransactionPersistence;
import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.slinkydeveloper.debtsmanager.persistence.impl.StatusUtils;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManagerService;
import io.slinkydeveloper.debtsmanager.readmodel.command.PushNewStatusCommand;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.slinkydeveloper.debtsmanager.services.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UsersService Test
 */
@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionsServiceTest extends BaseServicesTest {

  private PgPool pgClient;
  private RedisClient redisClient;
  private UserPersistence userPersistence;
  private TransactionPersistence transactionPersistence;
  private StatusPersistence statusPersistence;
  private TransactionsService transactionsService;
  private ReadModelManagerService readModelManagerService;

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
    redisClient = RedisClient.create(vertx,
      new RedisOptions()
        .setPort(environment.getServicePort("redis", REDIS_PORT))
        .setHost(environment.getServiceHost("redis", REDIS_PORT))
    );
    readModelManagerService = ReadModelManagerService.create(redisClient);
    userPersistence = UserPersistence.create(pgClient);
    transactionPersistence = TransactionPersistence.create(pgClient, readModelManagerService);
    statusPersistence = StatusPersistence.create(redisClient, pgClient, readModelManagerService);
    transactionsService = TransactionsService.create(vertx, statusPersistence, transactionPersistence, userPersistence);
    vertx.fileSystem().readFile("jwk.json", testContext.succeeding(buf -> {
      auth = JWTAuth.create(vertx, new JWTAuthOptions().addJwk(buf.toJsonObject()));
      usersService = UsersService.create(vertx, userPersistence, auth);
      testContext.completeNow();
    }));
  }

  @BeforeEach
  public void before(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    testContext.assertComplete(
        CompositeFuture
          .all(wipeDb(pgClient), wipeRedis(redisClient))
          .compose(v -> registerBeforeTestLogin(new AuthCredentials("francesco", "francesco"),  testContext))
    ).setHandler(ar -> {
      loggedContext = new OperationRequest().setUser(new JsonObject().put("username", "francesco"));
      testContext.completeNow();
    });
  }

  @Test
  public void createTransaction(VertxTestContext test) throws InterruptedException {
    Checkpoint statusCheck = test.checkpoint();
    Checkpoint commandsCheck = test.checkpoint();
    pgClient.preparedQuery(
      "INSERT INTO \"user\" (username, password) VALUES ($1, $2)",
      Tuple.of("slinky", "slinky"), test.succeeding(rows ->
        pgClient.preparedQuery(
          "INSERT INTO \"userrelationship\" (\"from\", \"to\") VALUES ($1, $2)",
          Tuple.of("francesco", "slinky"), test.succeeding(insert2Result -> {
            NewTransaction transactionBody = new NewTransaction("slinky", +20, "test");
            transactionsService.createTransaction(transactionBody, loggedContext, test.succeeding(res -> {
                test.verify(() -> {
                  assertSuccessResponse("application/json", res);
                  Transaction transaction = new Transaction(res.getPayload().toJsonObject());
                  assertNotNull(transaction.getId());
                  assertNotNull(transaction.getAt());
                  assertEquals("francesco",  transaction.getFrom());
                  assertEquals("slinky", transaction.getTo());
                  assertEquals(20, transaction.getValue());
                  assertEquals("test", transaction.getDescription());
                });
                redisClient.exists("status:francesco", test.succeeding(l -> {
                  test.verify(() -> assertEquals(0, l.longValue()));
                  statusCheck.flag();
                }));
                redisClient.exists("commands:francesco", test.succeeding(l -> {
                  test.verify(() -> assertEquals(0, l.longValue()));
                  commandsCheck.flag();
                }));
              })
            );
          })
        )
      )
    );
    test.awaitCompletion(1000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void createTransactionFailForMissingRelationship(VertxTestContext test) {
    Checkpoint statusCheck = test.checkpoint();
    Checkpoint commandsCheck = test.checkpoint();
    pgClient.preparedQuery(
      "INSERT INTO \"user\" (username, password) VALUES ($1, $2)",
      Tuple.of("slinky", "slinky"), test.succeeding(rows ->
        pgClient.preparedQuery(
          "INSERT INTO \"userrelationship\" (\"from\", \"to\") VALUES ($1, $2)",
          Tuple.of("slinky", "francesco"), test.succeeding(insert2Result -> {
            NewTransaction transactionBody = new NewTransaction("slinky", +20, "test");
            transactionsService.createTransaction(
              transactionBody, loggedContext, test.succeeding(res -> {
                test.verify(() -> {
                  assertEquals(403, res.getStatusCode().intValue());
                  assertEquals("Forbidden", res.getStatusMessage());
                  assertEquals("You need slinky authorization to add a new transaction with him as recipient",  res.getPayload().toString());
                });
                redisClient.exists("status:francesco", test.succeeding(l -> {
                  test.verify(() -> assertEquals(0, l.longValue()));
                  statusCheck.flag();
                }));
                redisClient.exists("commands:francesco", test.succeeding(l -> {
                  test.verify(() -> assertEquals(0, l.longValue()));
                  commandsCheck.flag();
                }));
              })
            );
          })
        )
      )
    );
  }

  @Test
  public void createTransactionAndUpdateStatus(Vertx vertx, VertxTestContext test) throws InterruptedException {
    Checkpoint statusFrancescoCheck = test.checkpoint();
    Checkpoint commandsFrancescoCheck = test.checkpoint();
    Checkpoint statusSlinkyCheck = test.checkpoint();
    Checkpoint commandsSlinkyCheck = test.checkpoint();
    Map<String, Double> fakeStatus = new HashMap<>();
    fakeStatus.put("fake", 10d);
    test.assertComplete(CompositeFuture.all(
      TestUtils.<Boolean>futurify(h -> readModelManagerService.runCommand(new PushNewStatusCommand("slinky", fakeStatus).toJson(), h)),
      TestUtils.<Boolean>futurify(h -> readModelManagerService.runCommand(new PushNewStatusCommand("francesco", fakeStatus).toJson(), h)),
      TestUtils.<PgRowSet>futurify(h -> pgClient.preparedQuery("INSERT INTO \"user\" (username, password) VALUES ($1, $2)", Tuple.of("slinky", "slinky"),  h))
        .compose(r -> TestUtils.<PgRowSet>futurify(h -> pgClient.preparedQuery("INSERT INTO \"userrelationship\" (\"from\", \"to\") VALUES ($1, $2)", Tuple.of("francesco", "slinky"), h)))
    )).setHandler(ar -> {
      NewTransaction transactionBody = new NewTransaction("slinky", +20, "test");
      transactionsService.createTransaction(transactionBody, loggedContext, test.succeeding(res -> {
          test.verify(() -> {
            assertSuccessResponse("application/json", res);
            Transaction transaction = new Transaction(res.getPayload().toJsonObject());
            assertNotNull(transaction.getId());
            assertNotNull(transaction.getAt());
            assertEquals("francesco",  transaction.getFrom());
            assertEquals("slinky", transaction.getTo());
            assertEquals(20, transaction.getValue());
            assertEquals("test", transaction.getDescription());
          });
          vertx.setTimer(500, l -> {
            redisClient.hgetall("status:francesco", test.succeeding(o -> {
              test.verify(() -> {
                assertEquals(2, o.size());
                assertEquals(20, StatusUtils.mapToStatusMap(o).get("slinky").doubleValue());
              });
              statusFrancescoCheck.flag();
            }));
            redisClient.smembers("commands:francesco", test.succeeding(a -> {
              test.verify(() -> assertEquals(2, a.size()));
              commandsFrancescoCheck.flag();
            }));
            redisClient.hgetall("status:slinky", test.succeeding(o -> {
              test.verify(() -> {
                assertEquals(2, o.size());
                assertEquals(-20, StatusUtils.mapToStatusMap(o).get("francesco").doubleValue());
              });
              statusSlinkyCheck.flag();
            }));
            redisClient.smembers("commands:slinky", test.succeeding(a -> {
              test.verify(() -> assertEquals(2, a.size()));
              commandsSlinkyCheck.flag();
            }));
          });
        })
      );
    });
    test.awaitCompletion(2000, TimeUnit.MILLISECONDS);
  }

}

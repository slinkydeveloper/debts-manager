package io.slinkydeveloper.debtsmanager.services;

import io.reactiverse.pgclient.*;
import io.slinkydeveloper.debtsmanager.dao.StatusDao;
import io.slinkydeveloper.debtsmanager.dao.TransactionDao;
import io.slinkydeveloper.debtsmanager.dao.UserDao;
import io.slinkydeveloper.debtsmanager.dao.impl.DaoUtils;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.models.NewTransaction;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.slinkydeveloper.debtsmanager.models.UpdateTransaction;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManagerService;
import io.slinkydeveloper.debtsmanager.readmodel.command.PushNewStatusCommand;
import io.slinkydeveloper.debtsmanager.utils.FutureUtils;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
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
  private UserDao userPersistence;
  private TransactionDao transactionPersistence;
  private StatusDao statusPersistence;
  private TransactionsService transactionsService;
  private StatusService statusService;
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
    userPersistence = UserDao.create(pgClient);
    transactionPersistence = TransactionDao.create(pgClient, readModelManagerService);
    statusPersistence = StatusDao.create(redisClient, pgClient, readModelManagerService);
    transactionsService = TransactionsService.create(transactionPersistence, userPersistence);
    statusService = StatusService.create(vertx, statusPersistence);
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
      FutureUtils.<Boolean>futurify(h -> readModelManagerService.runCommand(new PushNewStatusCommand("slinky", fakeStatus).toJson(), h)),
      FutureUtils.<Boolean>futurify(h -> readModelManagerService.runCommand(new PushNewStatusCommand("francesco", fakeStatus).toJson(), h)),
      FutureUtils.<PgRowSet>futurify(h -> pgClient.preparedQuery("INSERT INTO \"user\" (username, password) VALUES ($1, $2)", Tuple.of("slinky", "slinky"),  h))
        .compose(r -> FutureUtils.<PgRowSet>futurify(h -> pgClient.preparedQuery("INSERT INTO \"userrelationship\" (\"from\", \"to\") VALUES ($1, $2)", Tuple.of("francesco", "slinky"), h)))
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
                assertEquals(20, DaoUtils.mapToStatusMap(o).get("slinky").doubleValue());
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
                assertEquals(-20, DaoUtils.mapToStatusMap(o).get("francesco").doubleValue());
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

  @Test
  public void getUserStatusEmpty(VertxTestContext test) throws InterruptedException {
    statusService.getUserStatus(null, loggedContext, test.succeeding(res -> {
        test.verify(() -> {
          assertJsonResponse(200, "OK", new JsonObject(), res);
        });
        test.completeNow();
      })
    );
    test.awaitCompletion(1000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void getUserStatusMustCreateCache(Vertx vertx, VertxTestContext test) throws InterruptedException {
    Checkpoint statusFrancescoCheck = test.checkpoint();
    Checkpoint statusFrancescoRedisCheck = test.checkpoint();
    Checkpoint statusSlinkyCheck = test.checkpoint();
    Checkpoint statusSlinkyRedisCheck = test.checkpoint();
    OperationRequest slinkyLoggedContext = new OperationRequest().setUser(new JsonObject().put("username", "slinky"));

    FutureUtils.<PgRowSet>futurify(h -> pgClient.preparedQuery("INSERT INTO \"user\" (username, password) VALUES ($1, $2)", Tuple.of("slinky", "slinky"),  h))
      .compose(r -> FutureUtils.<PgRowSet>futurify(h -> pgClient.preparedQuery("INSERT INTO \"userrelationship\" (\"from\", \"to\") VALUES ($1, $2), ($2, $1)", Tuple.of("francesco", "slinky"), h)))
      .compose(cf -> CompositeFuture.all(
          FutureUtils.<OperationResponse>futurify(h -> transactionsService.createTransaction(new NewTransaction("slinky", +20, "test0"), loggedContext, h)),
          FutureUtils.<OperationResponse>futurify(h -> transactionsService.createTransaction(new NewTransaction("francesco", +10, "test1"), slinkyLoggedContext, h))
        )
    ).setHandler(test.succeeding(cf -> {
      statusService.getUserStatus(null, loggedContext, test.succeeding(opResponse -> {
        test.verify(() ->
          assertJsonResponse(200, "OK", new JsonObject().put("slinky", 10), opResponse)
        );
        statusFrancescoCheck.flag();
      }));
      statusService.getUserStatus(null, slinkyLoggedContext, test.succeeding(opResponse -> {
        test.verify(() ->
          assertJsonResponse(200, "OK", new JsonObject().put("francesco", -10), opResponse)
        );
        statusSlinkyCheck.flag();
      }));
      vertx.setTimer(500,  l -> {
        statusPersistence.getStatusFromCache("francesco").setHandler(test.succeeding(map -> {
          test.verify(() -> assertEquals(Double.valueOf(10), map.get("slinky")));
          statusFrancescoRedisCheck.flag();
        }));
        statusPersistence.getStatusFromCache("slinky").setHandler(test.succeeding(map -> {
          test.verify(() -> assertEquals(Double.valueOf(-10), map.get("francesco")));
          statusSlinkyRedisCheck.flag();
        }));
      });
    }));
    test.awaitCompletion(2000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void updateTransactionAndUpdateStatus(Vertx vertx, VertxTestContext test) throws InterruptedException {
    Checkpoint statusFrancescoCheck = test.checkpoint();
    Checkpoint commandsFrancescoCheck = test.checkpoint();
    Checkpoint statusSlinkyCheck = test.checkpoint();
    Checkpoint commandsSlinkyCheck = test.checkpoint();
    OperationRequest slinkyLoggedContext = new OperationRequest().setUser(new JsonObject().put("username", "slinky"));

    Future<OperationResponse> bootstrapTestFuture = FutureUtils.<PgRowSet>futurify(h -> pgClient.preparedQuery("INSERT INTO \"user\" (username, password) VALUES ($1, $2)", Tuple.of("slinky", "slinky"),  h))
      .compose(r -> FutureUtils.<PgRowSet>futurify(h -> pgClient.preparedQuery("INSERT INTO \"userrelationship\" (\"from\", \"to\") VALUES ($1, $2)", Tuple.of("francesco", "slinky"), h)))
      .compose(r -> CompositeFuture.all(
        FutureUtils.<OperationResponse>futurify(h -> statusService.getUserStatus(null, loggedContext, h)),
        FutureUtils.<OperationResponse>futurify(h -> statusService.getUserStatus(null, slinkyLoggedContext, h))
      ))
      .compose(r -> FutureUtils.<OperationResponse>futurify(h -> transactionsService.createTransaction(new NewTransaction("slinky", +20, "test"), loggedContext, h)))
      .compose(t -> FutureUtils.futurify(h -> transactionsService.updateTransaction(t.getPayload().toJsonObject().getString("id"), new UpdateTransaction(+10d, "newtest"), loggedContext, h)));

    test.assertComplete(bootstrapTestFuture)
      .setHandler(ar -> {
        test.verify(() -> {
          assertSuccessResponse(ar.result());
        });
        vertx.setTimer(500, l -> {
          redisClient.hgetall("status:francesco", test.succeeding(o -> {
            test.verify(() -> {
              assertEquals(1, o.size());
              assertEquals(10, DaoUtils.mapToStatusMap(o).get("slinky").doubleValue());
            });
            statusFrancescoCheck.flag();
          }));
          redisClient.smembers("commands:francesco", test.succeeding(a -> {
            test.verify(() -> assertEquals(3, a.size()));
            commandsFrancescoCheck.flag();
          }));
          redisClient.hgetall("status:slinky", test.succeeding(o -> {
            test.verify(() -> {
              assertEquals(1, o.size());
              assertEquals(-10, DaoUtils.mapToStatusMap(o).get("francesco").doubleValue());
            });
            statusSlinkyCheck.flag();
          }));
          redisClient.smembers("commands:slinky", test.succeeding(a -> {
            test.verify(() -> assertEquals(3, a.size()));
            commandsSlinkyCheck.flag();
          }));
        });
      });
    test.awaitCompletion(2000, TimeUnit.MILLISECONDS);
  }

}

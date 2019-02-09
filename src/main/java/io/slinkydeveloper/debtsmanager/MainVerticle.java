package io.slinkydeveloper.debtsmanager;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import io.slinkydeveloper.debtsmanager.dao.StatusDao;
import io.slinkydeveloper.debtsmanager.dao.TransactionDao;
import io.slinkydeveloper.debtsmanager.dao.UserDao;
import io.slinkydeveloper.debtsmanager.readmodel.ReadModelManagerService;
import io.slinkydeveloper.debtsmanager.services.TransactionsService;
import io.slinkydeveloper.debtsmanager.services.UsersService;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.ArrayList;
import java.util.List;

public class MainVerticle extends AbstractVerticle {

  HttpServer server;
  ServiceBinder serviceBinder;

  List<MessageConsumer<JsonObject>> registeredConsumers;

  /**
   * This method constructs the router factory, mounts services and handlers and starts the http server with built router
   * @return
   */
  private Future<Void> startHttpServer(JWTAuth auth) {
    Future<Void> future = Future.future();
    OpenAPI3RouterFactory.create(this.vertx, "debts_manager_api.yaml", openAPI3RouterFactoryAsyncResult -> {
      if (openAPI3RouterFactoryAsyncResult.succeeded()) {
        OpenAPI3RouterFactory routerFactory = openAPI3RouterFactoryAsyncResult.result();

        // Enable automatic response when ValidationException is thrown
        routerFactory.setOptions(new RouterFactoryOptions().setMountValidationFailureHandler(true));

        // Add gloabl handler
        routerFactory.addGlobalHandler(LoggerHandler.create());

        // Mount services on event bus based on extensions
        routerFactory.mountServicesFromExtensions();

        // Add security handlers
        routerFactory.addSecurityHandler("loggedUserToken", JWTAuthHandler.create(auth));

        // Generate the router
        Router router = routerFactory.getRouter();
        server = vertx.createHttpServer(new HttpServerOptions().setPort(config().getInteger("http-server-port", 8080)).setHost(config().getString("http-server-host", "localhost")));
        server.requestHandler(router).listen();
        future.complete();
      } else {
        // Something went wrong during router factory initialization
        future.fail(openAPI3RouterFactoryAsyncResult.cause());
      }
    });
    return future;
  }

  @Override
  public void start(Future<Void> future) {
    String jwkPath = config().getString("jwkPath", "jwk.json");
    loadResource(jwkPath).setHandler(ar -> {
      if (ar.failed()) future.fail(ar.cause());
      else {
        RedisClient redisClient = RedisClient.create(vertx,
          new RedisOptions()
            .setHost(config().getString("redis-address", "localhost"))
            .setPort(config().getInteger("redis-port", 6379))
        );
        PgPool pgClient = PgClient.pool(vertx,
          new PgPoolOptions()
            .setPort(config().getInteger("pg-port", 6379))
            .setHost(config().getString("pg-address", "localhost"))
            .setDatabase(config().getString("pg-db", "debts-manager"))
            .setUser(config().getString("pg-username", "postgres"))
            .setPassword(config().getString("pg-password", "postgres"))
        );
        CircuitBreaker readModelManagerCircuitBreaker = CircuitBreaker.create(
          "read-model-manager",
          vertx,
          new CircuitBreakerOptions()
            .setMaxFailures(5)
            .setMaxRetries(5)
            .setTimeout(2000)
            .setResetTimeout(10000)
        );
        ReadModelManagerService readModelManagerProxy = ReadModelManagerService.createClient(vertx, "read_model_manager.debts_manager", readModelManagerCircuitBreaker);
        UserDao userPersistence = UserDao.create(pgClient);
        TransactionDao transactionPersistence = TransactionDao.create(pgClient, readModelManagerProxy);
        StatusDao statusPersistence = StatusDao.create(
          redisClient,
          pgClient,
          readModelManagerProxy
        );
        JsonObject jwkObject = ar.result().toJsonObject();
        JWTAuth auth = JWTAuth.create(vertx, new JWTAuthOptions().addJwk(jwkObject));
        startServices(userPersistence, transactionPersistence, statusPersistence, redisClient, auth);
        startHttpServer(auth).setHandler(future.completer());
      }
    });
  }

  /**
   * This method closes the http server and unregister all services loaded to Event Bus
   */
  @Override
  public void stop(){
    this.server.close();
    registeredConsumers.forEach(c -> serviceBinder.unregister(c));
  }

  private Future<Buffer> loadResource(String path) {
    Future<Buffer> fut = Future.future();
    vertx.fileSystem().readFile(path, fut.completer());
    return fut;
  }

  private void startServices(UserDao userPersistence, TransactionDao transactionPersistence, StatusDao statusPersistence, RedisClient redisClient, JWTAuth auth) {
    serviceBinder = new ServiceBinder(vertx);

    registeredConsumers = new ArrayList<>();

    TransactionsService transactionsService = TransactionsService.create(vertx, statusPersistence, transactionPersistence, userPersistence);
    registeredConsumers.add(
      serviceBinder
        .setAddress("transactions.debts_manager")
        .register(TransactionsService.class, transactionsService)
    );
    UsersService usersService = UsersService.create(vertx, userPersistence, auth);
    registeredConsumers.add(
      serviceBinder
        .setAddress("users.debts_manager")
        .register(UsersService.class, usersService)
    );
    ReadModelManagerService readModelManager = ReadModelManagerService.create(redisClient);
    registeredConsumers.add(
      serviceBinder
        .setAddress("read_model_manager.debts_manager")
        .register(ReadModelManagerService.class, readModelManager)
    );
  }

}

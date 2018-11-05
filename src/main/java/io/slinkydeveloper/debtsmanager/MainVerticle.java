package io.slinkydeveloper.debtsmanager;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.Router;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.serviceproxy.ServiceBinder;

import io.slinkydeveloper.debtsmanager.services.*;

import java.util.List;

public class MainVerticle extends AbstractVerticle {

  HttpServer server;
  ServiceBinder serviceBinder;

  List<MessageConsumer<JsonObject>> registeredConsumers;

  private JWTAuth createJwtAuth(JsonObject jwk) {
    return JWTAuth.create(vertx,
      new JWTAuthOptions().addJwk(jwk)
    );
  }

  /**
   * This method starts all services
   */
  private void startServices() {
    serviceBinder = new ServiceBinder(vertx);

    TransactionsService transactionsService = TransactionsService.create(vertx);
    registeredConsumers.add(
      serviceBinder
        .setAddress("transactions.debts_manager")
        .register(TransactionsService.class, transactionsService)
    );
    UsersService usersService = UsersService.create(vertx);
    registeredConsumers.add(
      serviceBinder
        .setAddress("users.debts_manager")
        .register(UsersService.class, usersService)
    );
  }

  /**
   * This method constructs the router factory, mounts services and handlers and starts the http server with built router
   * @return
   */
  private Future<Void> startHttpServer(JWTAuth auth) {
    Future<Void> future = Future.future();
    OpenAPI3RouterFactory.create(this.vertx, getClass().getResource("/openapi.yaml").getFile(), openAPI3RouterFactoryAsyncResult -> {
      if (openAPI3RouterFactoryAsyncResult.succeeded()) {
        OpenAPI3RouterFactory routerFactory = openAPI3RouterFactoryAsyncResult.result();

        // Enable automatic response when ValidationException is thrown
        routerFactory.setOptions(new RouterFactoryOptions().setMountValidationFailureHandler(true));

        // Mount services on event bus based on extensions
        routerFactory.mountServicesFromExtensions();

        // Add security handlers
        routerFactory.addSecurityHandler("loggedUserToken", JWTAuthHandler.create(auth));

        // Generate the router
        Router router = routerFactory.getRouter();
        server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
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
    vertx.fileSystem().readFile(jwkPath, ar -> {
      if (ar.failed()) throw new IllegalArgumentException("Cannot find jwk at " + jwkPath);
      JsonObject jwkObject = ar.result().toJsonObject();
      JWTAuth auth = createJwtAuth(jwkObject);
      startServices();
      startHttpServer(auth).setHandler(future.completer());
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

}

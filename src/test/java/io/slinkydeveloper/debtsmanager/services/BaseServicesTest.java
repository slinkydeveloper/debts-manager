package io.slinkydeveloper.debtsmanager.services;

import io.reactiverse.pgclient.PgPool;
import io.slinkydeveloper.debtsmanager.BaseTestWithEnvironment;
import io.slinkydeveloper.debtsmanager.models.AuthCredentials;
import io.slinkydeveloper.debtsmanager.persistence.UserPersistence;
import io.vertx.core.Future;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.junit5.VertxTestContext;

import static io.slinkydeveloper.debtsmanager.services.TestUtils.assertSuccessResponse;

public class BaseServicesTest extends BaseTestWithEnvironment {

  protected UsersService usersService;

  protected Future<String> registerBeforeTestLogin(AuthCredentials credentials, VertxTestContext test) {
    Future<String> fut = Future.future();
    usersService.register(new AuthCredentials(credentials), new OperationRequest(), test.succeeding(operationResponse -> {
      test.verify(() -> {
        assertSuccessResponse("text/plain", operationResponse);
      });
      fut.complete(operationResponse.getPayload().toString());
    }));
    return fut;
  }
}

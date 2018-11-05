package io.slinkydeveloper.debtsmanager.services;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.api.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.slinkydeveloper.debtsmanager.models.*;

/**
 * LoginService Test
 */
@RunWith(VertxUnitRunner.class)
public class LoginServiceTest {

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  LoginService loginService;

  @Before
  public void before(TestContext context) {
    Vertx vertx = rule.vertx();
    loginService = LoginService.create(vertx);
    //TODO add some test initialization code like security token retrieval
  }

  @After
  public void after(TestContext context) {
    //TODO add some test end code like session destroy
  }

  @Test
  public void loginTest(TestContext context) {
    Async async = context.async(2);
    AuthCredentials body;

    // TODO set parameters for 200 response test
    body = null;
    loginService.login(body, new OperationRequest(), ar -> {
      if (ar.succeeded()) {
        OperationResponse result = ar.result();
        context.assertEquals(200, result.getStatusCode());
        //TODO add your asserts
      } else {
        context.fail("Operation failed with " + ar.cause().toString());
      }
      async.countDown();
    });

    // TODO set parameters for 400 response test
    body = null;
    loginService.login(body, new OperationRequest(), ar -> {
      if (ar.succeeded()) {
        OperationResponse result = ar.result();
        context.assertEquals(400, result.getStatusCode());
        //TODO add your asserts
      } else {
        context.fail("Operation failed with " + ar.cause().toString());
      }
      async.countDown();
    });
  }


}

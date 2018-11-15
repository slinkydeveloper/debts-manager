//package io.slinkydeveloper.debtsmanager.services;
//
//import io.vertx.core.Vertx;
//import io.vertx.ext.unit.Async;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.RunTestOnContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import io.vertx.ext.web.api.*;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import io.slinkydeveloper.debtsmanager.models.*;
//
///**
// * TransactionsService Test
// */
//@RunWith(VertxUnitRunner.class)
//public class TransactionsServiceTest {
//
//  @Rule
//  public RunTestOnContext rule = new RunTestOnContext();
//
//  TransactionsService transactionsService;
//
//  @Before
//  public void before(TestContext context) {
//    Vertx vertx = rule.vertx();
//    transactionsService = TransactionsService.create(vertx);
//    //TODO add some test initialization code like security token retrieval
//  }
//
//  @After
//  public void after(TestContext context) {
//    //TODO add some test end code like session destroy
//  }
//
//  @Test
//  public void getTransactionsTest(TestContext context) {
//    Async async = context.async(2);
//
//    // TODO set parameters for 200 response test
//    transactionsService.getTransactions(new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(200, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 401 response test
//    transactionsService.getTransactions(new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(401, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//  }
//
//  @Test
//  public void createTransactionTest(TestContext context) {
//    Async async = context.async(2);
//    NewTransaction body;
//
//    // TODO set parameters for 201 response test
//    body = null;
//    transactionsService.createTransaction(body, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(201, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 401 response test
//    body = null;
//    transactionsService.createTransaction(body, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(401, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//  }
//
//  @Test
//  public void getTransactionTest(TestContext context) {
//    Async async = context.async(3);
//    String transactionId;
//
//    // TODO set parameters for 200 response test
//    transactionId = null;
//    transactionsService.getTransaction(transactionId, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(200, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 401 response test
//    transactionId = null;
//    transactionsService.getTransaction(transactionId, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(401, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 403 response test
//    transactionId = null;
//    transactionsService.getTransaction(transactionId, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(403, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//  }
//
//  @Test
//  public void updateTransactionTest(TestContext context) {
//    Async async = context.async(3);
//    String transactionId;
//    UpdateTransaction body;
//
//    // TODO set parameters for 202 response test
//    transactionId = null;
//    body = null;
//    transactionsService.updateTransaction(transactionId, body, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(202, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 401 response test
//    transactionId = null;
//    body = null;
//    transactionsService.updateTransaction(transactionId, body, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(401, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 403 response test
//    transactionId = null;
//    body = null;
//    transactionsService.updateTransaction(transactionId, body, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(403, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//  }
//
//  @Test
//  public void deleteTransactionTest(TestContext context) {
//    Async async = context.async(3);
//    String transactionId;
//
//    // TODO set parameters for 204 response test
//    transactionId = null;
//    transactionsService.deleteTransaction(transactionId, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(204, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 401 response test
//    transactionId = null;
//    transactionsService.deleteTransaction(transactionId, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(401, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 403 response test
//    transactionId = null;
//    transactionsService.deleteTransaction(transactionId, new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(403, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//  }
//
//  @Test
//  public void getUserStatusTest(TestContext context) {
//    Async async = context.async(2);
//
//    // TODO set parameters for 200 response test
//    transactionsService.getUserStatus(new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(200, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//
//    // TODO set parameters for 401 response test
//    transactionsService.getUserStatus(new OperationRequest(), ar -> {
//      if (ar.succeeded()) {
//        OperationResponse result = ar.result();
//        context.assertEquals(401, result.getStatusCode());
//        //TODO add your asserts
//      } else {
//        context.fail("Operation failed with " + ar.cause().toString());
//      }
//      async.countDown();
//    });
//  }
//
//
//}

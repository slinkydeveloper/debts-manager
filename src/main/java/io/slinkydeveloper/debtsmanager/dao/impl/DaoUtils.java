package io.slinkydeveloper.debtsmanager.dao.impl;

import io.reactiverse.pgclient.Row;
import io.slinkydeveloper.debtsmanager.models.Transaction;
import io.vertx.core.json.JsonObject;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DaoUtils {

  protected static Collector<Row, ?, Map<String, Transaction>> transactionCollector = Collectors.toMap(
    row -> row.getInteger("id").toString(),
    row -> new Transaction(
    row.getInteger("id").toString(),
      row.getString("from"),
        row.getString("to"),
        row.getDouble("value"),
        row.getString("description"),
        row.getLocalDateTime("at").toString()
    )
  );

  protected final static Collector<Row, ?, Map<String, Double>> statusCollector = Collectors.toMap(
    row -> row.getString("username"),
    row -> row.getDouble("total")
  );

  public static Map<String, Double> mapToStatusMap(JsonObject status) {
    if (status == null) return null;
    return status
      .getMap()
      .entrySet()
      .stream()
      .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), Double.parseDouble(e.getValue().toString())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}

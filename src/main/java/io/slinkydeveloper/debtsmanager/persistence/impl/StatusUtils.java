package io.slinkydeveloper.debtsmanager.persistence.impl;

import io.vertx.core.json.JsonObject;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusUtils {

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

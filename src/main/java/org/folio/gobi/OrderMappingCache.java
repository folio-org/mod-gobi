package org.folio.gobi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMappings;

import io.vertx.core.json.JsonObject;

public class OrderMappingCache {
  private Map<String, Map<Mapping.Field, DataSourceResolver>> cache;

  private static final OrderMappingCache INSTANCE = new OrderMappingCache();

  private OrderMappingCache() {
    this.cache = new ConcurrentHashMap<>(100);
  }

  public static OrderMappingCache getInstance() {
    return INSTANCE;
  }

  public Map<Mapping.Field, DataSourceResolver> getValue(String key) {
    return cache.get(key);
  }

  public boolean containsKey(String key) {
    return cache.containsKey(key);
  }

  // This method returns the key which matches the format
  // "tenant:ordertype:config"
  public String getifContainsTenantconfigKey(String tenant, OrderMappings.OrderType orderType) {
    List<String> keys = cache.keySet()
        .stream()
        .filter(entry -> entry.startsWith(String.format("%s:%s", tenant, orderType.toString()))
            && entry.split(":").length == 3)
        .collect(Collectors.toList());
    if (!keys.isEmpty())
      return keys.get(0);

    return null;
  }

  public void putValue(String key, Map<Mapping.Field, DataSourceResolver> mapping) {
    cache.put(key, mapping);
  }

  public static String computeKey(String tenant, OrderMappings.OrderType orderType, JsonObject jo) {
    return String.format("%s:%s:%s", tenant, orderType.toString(), jo.toString());

  }

  public static String computeKey(String tenant, OrderMappings.OrderType orderType) {
    return String.format("%s-%s", tenant, orderType.toString());

  }

  public void removeKey(String key) {
    cache.remove(key);
  }

}

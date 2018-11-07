package org.folio.gobi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMappings;

import io.vertx.core.json.JsonObject;

public class OrderMappingCache {
  private Map<String, Map<Mapping.Field, DataSourceResolver>> cache;
  
  private static final OrderMappingCache INSTANCE = new OrderMappingCache();
  private OrderMappingCache() {
    this.cache =  new ConcurrentHashMap<>(100);
  }

  public static OrderMappingCache getInstance(){
    return INSTANCE;
  }
  
  public Map<Mapping.Field, DataSourceResolver> getValue(String key)  {    
    return cache.get(key);
  }
  
  public boolean containsKey(String key)  {    
    return cache.containsKey(key);
  }


  public void putValue(String key, Map<Mapping.Field, DataSourceResolver> mapping){
    cache.put(key, mapping);
  }
  
  public static String computeKey(String tenant,OrderMappings.OrderType orderType,JsonObject jo){
    return String.format("%s:%s:%s", tenant, orderType.toString(), jo.toString());
    
  }

}

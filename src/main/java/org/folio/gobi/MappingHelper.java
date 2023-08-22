package org.folio.gobi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.Mapper.NodeCombinator;
import org.folio.gobi.Mapper.Translation;
import org.folio.rest.impl.PostGobiOrdersHelper;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.w3c.dom.NodeList;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MappingHelper {
  private static final Logger logger = LogManager.getLogger(MappingHelper.class);
  private final FieldMappingTranslatorResolver fieldMappingTranslatorResolver;

  public MappingHelper(FieldMappingTranslatorResolver fieldMappingTranslatorResolver) {
    this.fieldMappingTranslatorResolver = fieldMappingTranslatorResolver;
  }

  private static final Map<OrderMappings.OrderType, OrderMappings> defaultMappings = new EnumMap<>(OrderMappings.OrderType.class);

  static {
    for (OrderMappings.OrderType orderType : OrderMappings.OrderType.values()) {
      defaultMappings.put(orderType, Json.decodeValue(readMappingsFile(orderType.toString() + ".json"), OrderMappings.class));
    }
  }

  public Map<Mapping.Field, DataSourceResolver> getDefaultMappingForOrderType(OrderMappings.OrderType orderType) {
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping = new EnumMap<>(Mapping.Field.class);
    List<Mapping> mappingsList = defaultMappings.get(orderType).getMappings();

    for (Mapping mapping : mappingsList) {
      Mapping.Field field = mapping.getField();
      org.folio.gobi.DataSourceResolver dataSource = getDS(mapping, fieldDataSourceMapping);
      fieldDataSourceMapping.put(field, dataSource);
    }

    return fieldDataSourceMapping;
  }

  public Object getDefaultValue(org.folio.rest.jaxrs.model.DataSource dataSource,
      Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping) {
    Object ret = null;
    if (dataSource.getDefault() != null) {
      ret = dataSource.getDefault();
    } else if (dataSource.getFromOtherField() != null) {
      String otherField = dataSource.getFromOtherField().value();
      ret = fieldDataSourceMapping.get(Mapping.Field.valueOf(otherField));
    } else if (dataSource.getDefaultMapping() != null) {
      ret = getDS(dataSource.getDefaultMapping(), fieldDataSourceMapping);
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  public org.folio.gobi.DataSourceResolver getDS(Mapping mapping,
      Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping) {

    Object defaultValue = getDefaultValue(mapping.getDataSource(), fieldDataSourceMapping);
    Boolean translateDefault = mapping.getDataSource().getTranslateDefault();
    String dataSourceFrom = mapping.getDataSource().getFrom();

    org.folio.rest.jaxrs.model.DataSource.Combinator combinator = mapping.getDataSource().getCombinator();
    NodeCombinator nc = null;
    if (combinator != null) {
      try {
        Method combinatorMethod = Mapper.class.getMethod(combinator.toString(), NodeList.class);
        nc = data -> {
          try {
            return (String) combinatorMethod.invoke(null, data);
          } catch (Exception e) {
            String errorMessage = String.format("Unable to invoke combinator method: %s", combinator);
            logger.error(errorMessage, e);
          }
          return null;
        };
      } catch (NoSuchMethodException e) {
        String errorMessage = String.format("Combinator method not found: %s", combinator);
        logger.error(errorMessage, e);
      }
    }

    org.folio.rest.jaxrs.model.DataSource.Translation translation = mapping.getDataSource().getTranslation();
    Translation<?> t = fieldMappingTranslatorResolver.resolve(translation);
    return org.folio.gobi.DataSourceResolver.builder()
        .withFrom(dataSourceFrom)
        .withDefault(defaultValue)
        .withTranslation(t)
        .withTranslateDefault(translateDefault != null && translateDefault)
        .withCombinator(nc)
        .build();

  }

  public Map<Mapping.Field, DataSourceResolver> extractOrderMappings(OrderMappings.OrderType orderType, JsonObject jo) {
    final Map<Mapping.Field, DataSourceResolver> map = new EnumMap<>(Mapping.Field.class);

    final JsonArray configs = jo.getJsonArray("configs");

    if (!configs.isEmpty()) {
      final String mappingsString = configs.getJsonObject(0).getString("value");
      final OrderMappings orderMapping = Json.decodeValue(mappingsString, OrderMappings.class);

      final List<Mapping> orderMappingList = orderMapping.getMappings();

      if (orderMappingList != null) {
        for (Mapping mapping : orderMappingList) {
          logger.info("Mapping exists for type: {} , field: {}", orderType, mapping.getField());
          map.put(mapping.getField(), getDS(mapping, map));
        }
      }
    }
    return map;
  }

  public static String readMappingsFile(final String path) {
    try (InputStream is = PostGobiOrdersHelper.class.getClassLoader().getResourceAsStream(path)) {
      if (is != null) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      String errorMessage = String.format("Unable to read configuration in file: %s", path);
      logger.error(errorMessage, e);
    }
    return StringUtils.EMPTY;
  }

  public static OrderMappings getDefaultMappingByOrderType(OrderMappings.OrderType orderType) {
    return defaultMappings.get(orderType);
  }
}

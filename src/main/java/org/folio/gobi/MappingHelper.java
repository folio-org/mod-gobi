package org.folio.gobi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.folio.gobi.Mapper.NodeCombinator;
import org.folio.gobi.Mapper.Translation;
import org.folio.rest.impl.PostGobiOrdersHelper;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.Mapping.Field;
import org.folio.rest.mappings.model.OrderMappings;
import org.folio.rest.mappings.model.OrderMappings.OrderType;
import org.w3c.dom.NodeList;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MappingHelper {
  private static final Logger logger = Logger.getLogger(MappingHelper.class);

  

  private MappingHelper() {
    throw new IllegalStateException("MappingHelper class cannot be instantiated");
  }

  
  public static Map<Mapping.Field, org.folio.gobi.DataSource> defaultMappingForOrderType(PostGobiOrdersHelper postGobiOrdersHelper, OrderType orderType) {
    final String PATH = orderType+".json";
    final OrderMappings defaultMappings = Json.decodeValue(readMappingsFile(PATH), OrderMappings.class);
    Map<Mapping.Field, org.folio.gobi.DataSource> fieldDataSourceMapping = new EnumMap<>(Mapping.Field.class);

      List<Mapping> mappingsList = defaultMappings.getMappings();

      for(Mapping mapping: mappingsList)
      {
        Mapping.Field field = mapping.getField(); // get field
        org.folio.gobi.DataSource dataSource = getDS(mapping, fieldDataSourceMapping, postGobiOrdersHelper);
        fieldDataSourceMapping.put(field, dataSource);
      }
    

    logger.info(defaultMappings.toString());
    return fieldDataSourceMapping;
  }

  public static Object getDefaultValue(org.folio.rest.mappings.model.DataSource dataSource,
      Map<Field, org.folio.gobi.DataSource> fieldDataSourceMapping, PostGobiOrdersHelper postGobiOrdersHelper) {
    Object ret = null;
    if (dataSource.getDefault() != null) {
      ret = dataSource.getDefault();
    } else if (dataSource.getFromOtherField() != null) {
      String otherField = dataSource.getFromOtherField().value();
      ret = fieldDataSourceMapping.get(Field.valueOf(otherField));
    } else if (dataSource.getDefaultMapping() != null) {
      ret = getDS(dataSource.getDefaultMapping(), fieldDataSourceMapping, postGobiOrdersHelper);
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static org.folio.gobi.DataSource getDS(Mapping mapping,
      Map<Field, org.folio.gobi.DataSource> fieldDataSourceMapping, PostGobiOrdersHelper postGobiOrdersHelper) {

    Object defaultValue = getDefaultValue(mapping.getDataSource(), fieldDataSourceMapping, postGobiOrdersHelper);
    Boolean translateDefault = mapping.getDataSource().getTranslateDefault();
    String dataSourceFrom = mapping.getDataSource().getFrom();

    org.folio.rest.mappings.model.DataSource.Combinator combinator = mapping.getDataSource().getCombinator();
    NodeCombinator nc = null;
    if (combinator != null) {
      try {
        Method combinatorMethod = Mapper.class.getMethod(combinator.toString(), NodeList.class);
        nc = data -> {
          try {
            return (String) combinatorMethod.invoke(null, data);
          } catch (Exception e) {
            logger.error("Unable to invoke combinator method: " + combinator, e);
          }
          return null;
        };
      } catch (NoSuchMethodException e) {
        logger.error("Combinator method not found: " + combinator, e);
      }
    }

    org.folio.rest.mappings.model.DataSource.Translation translation = mapping.getDataSource().getTranslation();
    Translation<?> t = null;
    if (translation != null) {

      t = data -> {
        Object translatedValue;
        try {
          switch (translation) {
          case GET_PURCHASE_OPTION_CODE:
            translatedValue = postGobiOrdersHelper.getPurchaseOptionCode(data);
            break;
          case LOOKUP_MOCK:
            translatedValue = postGobiOrdersHelper.lookupMock(data);
            break;
          case LOOKUP_LOCATION_ID:
            translatedValue = postGobiOrdersHelper.lookupLocationId(data);
            break;
          case LOOKUP_MATERIAL_TYPE_ID:
            translatedValue = postGobiOrdersHelper.lookupMaterialTypeId(data);
            break;
          case LOOKUP_VENDOR_ID:
            translatedValue = postGobiOrdersHelper.lookupVendorId(data);
            break;
          case TO_DATE:
            translatedValue = Mapper.toDate(data);
            break;
          case TO_DOUBLE:
            translatedValue = Mapper.toDouble(data);
            break;
          case TO_INTEGER:
            translatedValue = Mapper.toInteger(data);
            break;
          case TO_BOOLEAN:
            translatedValue = Mapper.toBoolean(data);
            break;
          default:
            throw new IllegalArgumentException("No such Translation available: " + translation);
          }
          return (CompletableFuture<Object>) translatedValue;
        } catch (Exception e) {
          logger.error("Exception in Mapperhelper", e);
        }
        return null;
      };
    }

    return org.folio.gobi.DataSource.builder()
      .withFrom(dataSourceFrom)
      .withDefault(defaultValue)
      .withTranslation(t)
      .withTranslateDefault(translateDefault != null && translateDefault.booleanValue())
      .withCombinator(nc)
      .build();

  }

  public static Map<Mapping.Field, DataSource> extractOrderMappings(OrderMappings.OrderType orderType, JsonObject jo,
      PostGobiOrdersHelper postGobiOrdersHelper) {
    final Map<Mapping.Field, DataSource> map = new EnumMap<>(Mapping.Field.class);

    final JsonArray configs = jo.getJsonArray("configs");

    if (!configs.isEmpty()) {
      final String mappingsString = configs.getJsonObject(0).getString("value");
      final OrderMappings orderMapping= Json.decodeValue(mappingsString, OrderMappings.class);

      final List<Mapping> orderMappingList = orderMapping.getMappings();

      if (orderMappingList != null) {
        for (Mapping mapping : orderMappingList) {
          logger.info("Mapping exists for type: " + orderType.value() + ", field: " + mapping.getField());
          map.put(mapping.getField(), getDS(mapping, map, postGobiOrdersHelper));
        }
      }
    }
    return map;
  }


  public static String readMappingsFile(final String path) {
    try {
      final InputStream is = PostGobiOrdersHelper.class.getClassLoader().getResourceAsStream(path);
      if (is != null) {
        return IOUtils.toString(is, "UTF-8");
      } else {
        return "";
      }
    } catch (IOException e) {
      logger.error(String.format("Unable to read mock configuration in %s file", path), e);
    }
    return "";
  }
}

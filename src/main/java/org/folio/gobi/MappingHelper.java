package org.folio.gobi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
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
import org.folio.rest.mappings.model.Mappings;
import org.folio.rest.mappings.model.OrderMapping;
import org.folio.rest.mappings.model.OrderMapping.OrderType;
import org.w3c.dom.NodeList;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MappingHelper {
  private static final Logger logger = Logger.getLogger(MappingHelper.class);

  public static final String PATH = "data-mapping.json";

  private MappingHelper() {
    throw new IllegalStateException("MappingHelper class cannot be instantiated");
  }

  private static final Mappings defaultMappings = Json.decodeValue(readMappingsFile(PATH), Mappings.class);

  public static Map<OrderType, Map<Field, DataSource>> defaultMapping(PostGobiOrdersHelper postGobiOrdersHelper) {

    Map<OrderType, Map<Mapping.Field, org.folio.gobi.DataSource>> defaultMapping = new EnumMap<>(OrderType.class);

    // get orderMappings list
    final List<OrderMapping> orderMappingList = defaultMappings.getOrderMappings();

    // iterate through orderMappings list
    for (OrderMapping orderMapping : orderMappingList) {
      Map<Mapping.Field, org.folio.gobi.DataSource> fieldDataSourceMapping = new EnumMap<>(Mapping.Field.class);

      // get orderType from orderMapping
      OrderType orderType = orderMapping.getOrderType();

      // get mappings list
      List<Mapping> mappingsList = orderMapping.getMappings();

      for (int i = 0; i < mappingsList.size(); i++) { // iterate
        Mapping mapping = mappingsList.get(i); // get mapping
        Mapping.Field field = mapping.getField(); // get field
        org.folio.gobi.DataSource dataSource = getDS(mapping, fieldDataSourceMapping, postGobiOrdersHelper);
        fieldDataSourceMapping.put(field, dataSource);
      }
      defaultMapping.put(orderType, fieldDataSourceMapping);
    }

    logger.info(defaultMappings.toString());
    return defaultMapping;
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
          case LOOKUP_ACTIVATION_STATUS_ID:
            translatedValue = postGobiOrdersHelper.lookupActivationStatusId(data);
            break;
          case LOOKUP_FUND_ID:
            translatedValue = postGobiOrdersHelper.lookupFundId(data);
            break;
          case LOOKUP_LOCATION_ID:
            translatedValue = postGobiOrdersHelper.lookupLocationId(data);
            break;
          case LOOKUP_MATERIAL_TYPE_ID:
            translatedValue = postGobiOrdersHelper.lookupMaterialTypeId(data);
            break;
          case LOOKUP_PAYMENT_STATUS_ID:
            translatedValue = postGobiOrdersHelper.lookupPaymentStatusId(data);
            break;
          case LOOKUP_RECEIPT_STATUS_ID:
            translatedValue = postGobiOrdersHelper.lookupReceiptStatusId(data);
            break;
          case LOOKUP_VENDOR_ID:
            translatedValue = postGobiOrdersHelper.lookupVendorId(data);
            break;
          case LOOKUP_WORKFLOW_STATUS_ID:
            translatedValue = postGobiOrdersHelper.lookupWorkflowStatusId(data);
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

  public static Map<Mapping.Field, DataSource> extractOrderMappings(OrderMapping.OrderType orderType, JsonObject jo,
      PostGobiOrdersHelper postGobiOrdersHelper) {
    final Map<Mapping.Field, DataSource> map = new EnumMap<>(Mapping.Field.class);

    final JsonArray configs = jo.getJsonArray("configs");

    if (!configs.isEmpty()) {
      final String mappingsString = configs.getJsonObject(0).getString("value");
      final Mappings mappings = Json.decodeValue(mappingsString, Mappings.class);

      final List<OrderMapping> orderMappingList = mappings.getOrderMappings();

      if (orderMappingList != null) {
        for (Mapping mapping : getMappingsByType(orderMappingList, orderType)) {
          logger.info("Mapping exists for type: " + orderType.value() + ", field: " + mapping.getField());
          map.put(mapping.getField(), getDS(mapping, map, postGobiOrdersHelper));
        }
      }
    }
    return map;
  }

  public static List<Mapping> getMappingsByType(List<OrderMapping> mappings, OrderType type) {
    List<Mapping> ret = new ArrayList<>();
    for (OrderMapping orderMapping : mappings) {
      if (orderMapping.getOrderType() == type) {
        ret.addAll(orderMapping.getMappings());
      }
    }
    return ret;
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

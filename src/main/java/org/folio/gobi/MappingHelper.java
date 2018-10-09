package org.folio.gobi;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.folio.rest.mappings.model.Mapping.Field;
import org.folio.gobi.Mapper.NodeCombinator;
import org.folio.gobi.Mapper.Translation;
import org.folio.rest.impl.PostGobiOrdersHelper;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.Mappings;
import org.folio.rest.mappings.model.OrderMapping;
import org.folio.rest.mappings.model.OrderMapping.OrderType;
import org.w3c.dom.NodeList;
import io.vertx.core.json.Json;

public class MappingHelper {
  private static final Logger logger = Logger.getLogger(MappingHelper.class);

  private static final String path = "data-mapping.json";

  public static Map<OrderType, Map<Field, DataSource>> defaultMapping() {

    Map<OrderType, Map<Mapping.Field, org.folio.gobi.DataSource>> defaultMapping = new LinkedHashMap<>();

    String jsonAsString = readMappingsFile(path);
    final Mappings mappings = Json.decodeValue(jsonAsString, Mappings.class);
    final List<OrderMapping> orderMappingList = mappings.getOrderMappings(); // get orderMappings list
    for (OrderMapping orderMapping : orderMappingList) { // iterate through orderMappings list
      Map<Mapping.Field, org.folio.gobi.DataSource> fieldDataSourceMapping = new LinkedHashMap<>();
      OrderType orderType = orderMapping.getOrderType(); // get orderType from orderMapping
      List<Mapping> mappingsList = orderMapping.getMappings(); // get mappings list
      for (int i = 0; i < mappingsList.size(); i++) { // iterate
        Mapping mapping = mappingsList.get(i); // get mapping
        Mapping.Field field = mapping.getField(); // get field
        org.folio.gobi.DataSource dataSource = getDS(mapping, fieldDataSourceMapping);
        fieldDataSourceMapping.put(field, dataSource);
      }
      defaultMapping.put(orderType, fieldDataSourceMapping);
    }
    logger.info(mappings.toString());
    return defaultMapping;
  }

  public static org.folio.gobi.DataSource getDS(Mapping mapping,
      Map<Field, org.folio.gobi.DataSource> fieldDataSourceMapping) {
    List<String> PostGobiOrdersHelperList = Arrays.asList("lookupLocationId", "lookupMaterialTypeId", "lookupVendorId",
        "lookupWorkflowStatusId", "lookupReceiptStatusId", "lookupPaymentStatusId", "lookupActivationStatusId", "lookupFundId");

    org.folio.gobi.DataSource ds;

    Object defaultValue = new Object();
    if (mapping.getDataSource().getDefault() != null) {
      defaultValue = mapping.getDataSource().getDefault();
    } else if (mapping.getDataSource().getFromOtherField() != null) {
      String otherField = mapping.getDataSource().getFromOtherField().value();
      defaultValue = fieldDataSourceMapping.get(Field.valueOf(otherField));
    } else if (mapping.getDataSource().getDefaultMapping() != null) {
      defaultValue = getDS(mapping.getDataSource().getDefaultMapping(), fieldDataSourceMapping);
    }

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
    Method translationMethod;
    if (translation != null) {
      try {
        if (PostGobiOrdersHelperList.contains(translation.toString())) {
          translationMethod = PostGobiOrdersHelper.class.getMethod(translation.toString(), String.class);
        } else {
          translationMethod = Mapper.class.getMethod(translation.toString(), String.class, PostGobiOrdersHelper.class);
        }

        t = (data, postGobiHelper) -> {
          try {
            return (CompletableFuture<Object>) translationMethod.invoke(null, data, postGobiHelper);
          } catch (Exception e) {
            logger.error("Unable to invoke translation method: " + translation, e);
          }
          return null;
        };
      } catch (NoSuchMethodException e) {
        logger.error("Translation method not found: " + translation, e);
      }
    }
    ds = org.folio.gobi.DataSource.builder().withFrom(dataSourceFrom).withDefault(defaultValue).withTranslation(t)
        .withTranslateDefault(true).withCombinator(nc).build();

    return ds;
  }

  public static String readMappingsFile(final String path) {
    try {
      final InputStream is = PostGobiOrdersHelper.class.getClassLoader().getResourceAsStream(path);
      if (is != null) {
        return IOUtils.toString(is, "UTF-8");
      } else {
        return "";
      }
    } catch (Throwable e) {
      logger.error(String.format("Unable to read mock configuration in %s file", path));
    }
    return "";
  }
}

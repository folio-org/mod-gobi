package org.folio.gobi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
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

  private static final String PATH = "data-mapping.json";

  private MappingHelper() {
    throw new IllegalStateException("MappingHelper class cannot be instantiated");
  }

  public static Map<OrderType, Map<Field, DataSource>> defaultMapping() throws IOException {

    Map<OrderType, Map<Mapping.Field, org.folio.gobi.DataSource>> defaultMapping = new LinkedHashMap<>();

    String jsonAsString = readMappingsFile(PATH);
    if (jsonAsString != "" && jsonAsString != null) {
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
    } else
      return defaultMapping;
  }

  @SuppressWarnings("unchecked")
  public static org.folio.gobi.DataSource getDS(Mapping mapping,
      Map<Field, org.folio.gobi.DataSource> fieldDataSourceMapping) {

    Object defaultValue = null;
    if (mapping.getDataSource().getDefault() != null) {
      defaultValue = mapping.getDataSource().getDefault();
    } else if (mapping.getDataSource().getFromOtherField() != null) {
      String otherField = mapping.getDataSource().getFromOtherField().value();
      defaultValue = fieldDataSourceMapping.get(Field.valueOf(otherField));
    } else if (mapping.getDataSource().getDefaultMapping() != null) {
      defaultValue = getDS(mapping.getDataSource().getDefaultMapping(), fieldDataSourceMapping);
    }

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

      t = (data, postGobiHelper) -> {
        Object translatedValue;
        try {

          switch (translation) {
          case GET_PURCHASE_OPTION_CODE:
            translatedValue = postGobiHelper.getPurchaseOptionCode(data);
            break;
          case LOOKUP_ACTIVATION_STATUS_ID:
            translatedValue = postGobiHelper.lookupActivationStatusId(data);
            break;
          case LOOKUP_FUND_ID:
            translatedValue = postGobiHelper.lookupFundId(data);
            break;
          case LOOKUP_LOCATION_ID:
            translatedValue = postGobiHelper.lookupLocationId(data);
            break;
          case LOOKUP_MATERIAL_TYPE_ID:
            translatedValue = postGobiHelper.lookupMaterialTypeId(data);
            break;
          case LOOKUP_PAYMENT_STATUS_ID:
            translatedValue = postGobiHelper.lookupPaymentStatusId(data);
            break;
          case LOOKUP_RECEIPT_STATUS_ID:
            translatedValue = postGobiHelper.lookupReceiptStatusId(data);
            break;
          case LOOKUP_VENDOR_ID:
            translatedValue = postGobiHelper.lookupVendorId(data);
            break;
          case LOOKUP_WORKFLOW_STATUS_ID:
            translatedValue = postGobiHelper.lookupWorkflowStatusId(data);
            break;
          case TO_DATE:
            translatedValue = Mapper.toDate(data, postGobiHelper);
            break;
          case TO_DOUBLE:
            translatedValue = Mapper.toDouble(data, postGobiHelper);
            break;
          case TO_INTEGER:
            translatedValue = Mapper.toInteger(data, postGobiHelper);
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

    return org.folio.gobi.DataSource.builder().withFrom(dataSourceFrom).withDefault(defaultValue).withTranslation(t)
        .withTranslateDefault(translateDefault == null ? false : translateDefault.booleanValue()).withCombinator(nc).build();

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

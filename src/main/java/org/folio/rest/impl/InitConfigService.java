package org.folio.rest.impl;

import static java.util.stream.Collectors.groupingBy;
import static org.folio.rest.impl.Constants.ORDERS_MAPPING_CONFIGURATION_DEFAULT;
import static org.folio.rest.impl.Constants.ORDERS_MAPPING_CONFIGURATION_PROPERTY_NAME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.mappings.model.OrderMappingConfig;
import org.folio.rest.mappings.model.OrdersMappingConfiguration;
import org.folio.rest.resource.interfaces.PostDeployVerticle;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

 public class InitConfigService implements PostDeployVerticle {
  private static final Logger logger = LogManager.getLogger(InitConfigService.class);

  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
    logger.info("Init Config Service");
    logger.info("Loading Default Mappings");
    try {
      final List<OrderMappingConfig> routingConfiguration = initApiConfiguration(System.getProperty(ORDERS_MAPPING_CONFIGURATION_PROPERTY_NAME));

      Map<OrderMappingConfig.OrderType, List<OrderMappingConfig>> groupedRouting = routingConfiguration.stream()
        .collect(groupingBy(OrderMappingConfig::getOrderType));

    } catch (Exception e) {
      logger.error("Failed to load api configuration", e);
    }

    handler.handle(io.vertx.core.Future.succeededFuture(true));
  }

  private List<OrderMappingConfig> initApiConfiguration(String apiConfigurationPropFile) throws IOException {

    OrdersMappingConfiguration apiConfiguration = null;
    final Pattern isURL = Pattern.compile("(?i)^http[s]?://.*");
    ObjectMapper mapper = new ObjectMapper();

    if (apiConfigurationPropFile != null) {
      URL url = null;
      try {
        if (isURL.matcher(apiConfigurationPropFile).matches()) {
          url = new URL(apiConfigurationPropFile);
        }

        try (InputStream in = url == null ? new FileInputStream(apiConfigurationPropFile) : url.openStream()) {

          apiConfiguration = mapper.readValue(in, OrdersMappingConfiguration.class);
          logger.info("ApiConfiguration has been loaded from file {}", apiConfigurationPropFile);
        }
      } catch (Exception e) {
        logger.warn("Failed to load ApiConfiguration from {}", apiConfigurationPropFile, e);
      }
    } else {
      logger.warn("No api configuration file specified. Using default {}", ORDERS_MAPPING_CONFIGURATION_DEFAULT);
      apiConfiguration = mapper
        .readValue(ClassLoader.getSystemClassLoader().getResource(ORDERS_MAPPING_CONFIGURATION_DEFAULT), OrdersMappingConfiguration.class);
    }

    return Objects.requireNonNull(apiConfiguration).getOrderMappingConfigs();
  }
}

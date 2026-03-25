package org.folio.rest.impl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.service.ConfigurationMigrationService;
import io.vertx.core.Context;
import io.vertx.core.Future;

public class TenantReferenceAPI extends TenantAPI {

  private static final Logger log = LogManager.getLogger(TenantReferenceAPI.class);

  private final ConfigurationMigrationService configurationMigrationService = new ConfigurationMigrationService();

  @Override
  public Future<Integer> loadData(TenantAttributes attributes, String tenantId,
      Map<String, String> headers, Context vertxContext) {
    log.info("Loading tenant data for tenantId: {}", tenantId);
    return super.loadData(attributes, tenantId, headers, vertxContext)
      .compose(loaded -> configurationMigrationService.migrateConfigurationData(attributes, tenantId, headers, vertxContext)
        .map(loaded));
  }
}
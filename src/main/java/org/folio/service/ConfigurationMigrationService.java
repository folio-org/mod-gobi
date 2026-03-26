package org.folio.service;

import static org.folio.gobi.HelperUtils.encodeValue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.dbschema.Versioned;
import org.folio.gobi.exceptions.HttpException;
import org.folio.okapi.common.WebClientFactory;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.persist.PostgresClient;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.sqlclient.Tuple;

public class ConfigurationMigrationService {

  private static final Logger log = LogManager.getLogger(ConfigurationMigrationService.class);

  private static final String OKAPI_URL = "x-okapi-url";
  private static final String CONFIGURATIONS_ENTRIES_ENDPOINT = "/configurations/entries";
  private static final String ORDER_MAPPINGS_TABLE = "order_mappings";
  private static final String MIGRATION_TARGET_VERSION = "3.1.0";
  private static final List<String> MIGRATED_CODES = List.of(
    "gobi.order.ListedElectronicMonograph",
    "gobi.order.ListedPrintMonograph",
    "gobi.order.UnlistedPrintMonograph"
  );

  public Future<Void> migrateConfigurationData(TenantAttributes attributes, String tenantId,
      Map<String, String> headers, Context vertxContext) {
    if (!isMigrationNeeded(attributes)) {
      log.info("Configuration migration is not needed for moduleFrom={}, moduleTo={}",
        attributes.getModuleFrom(), attributes.getModuleTo());
      return Future.succeededFuture();
    }

    log.info("Attempting to migrate configuration data from mod-configuration for tenant: {}", tenantId);

    return fetchConfigurationEntries(headers, vertxContext)
      .compose(configs -> {
        if (configs == null || configs.isEmpty()) {
          log.info("No configuration entries found to migrate");
          return Future.succeededFuture();
        }
        return insertConfigurationData(configs, tenantId, vertxContext);
      })
      .recover(throwable -> {
        log.warn("Failed to migrate configuration data from mod-configuration. "
          + "This is expected if mod-configuration is not deployed.", throwable);
        return Future.succeededFuture();
      });
  }

  private Future<JsonArray> fetchConfigurationEntries(Map<String, String> headers, Context vertxContext) {
    String okapiUrl = headers.get(OKAPI_URL);
    if (okapiUrl == null || okapiUrl.isEmpty()) {
      log.warn("No x-okapi-url header found, cannot call mod-configuration");
      return Future.succeededFuture(null);
    }

    String endpoint = okapiUrl + CONFIGURATIONS_ENTRIES_ENDPOINT
      + "?query=" + encodeValue("module==GOBI") + "&limit=1000";
    WebClient client = WebClientFactory.getWebClient(vertxContext.owner());
    MultiMap caseInsensitiveHeaders = MultiMap.caseInsensitiveMultiMap().addAll(headers);

    return client.getAbs(endpoint)
      .putHeaders(caseInsensitiveHeaders)
      .send()
      .map(response -> {
        if (!HttpResponseExpectation.SC_SUCCESS.test(response)) {
          throw new HttpException(response.statusCode(), "Failed to fetch configuration entries from mod-configuration");
        }
        JsonArray configs = response.bodyAsJsonObject().getJsonArray("configs");
        log.info("Fetched {} configuration entries from mod-configuration",
          configs != null ? configs.size() : 0);
        return configs;
      });
  }

  private boolean isMigrationNeeded(TenantAttributes attributes) {
    String moduleFrom = attributes.getModuleFrom();
    String moduleTo = attributes.getModuleTo();
    if (moduleFrom == null || moduleTo == null) {
      return false;
    }
    return isNewForVersion(moduleFrom, MIGRATION_TARGET_VERSION)
      && !isNewForVersion(moduleTo, MIGRATION_TARGET_VERSION);
  }

  // Uses the same version comparison as schema.json's fromModuleVersion.
  // Correctly handles SNAPSHOT suffixes, e.g. 3.1.0-SNAPSHOT.123 is treated as 3.1.0.
  private boolean isNewForVersion(String moduleId, String version) {
    var since = new Versioned() { };
    since.setFromModuleVersion(version);
    return since.isNewForThisInstall(moduleId);
  }

  private Future<Void> insertConfigurationData(JsonArray configs, String tenantId, Context vertxContext) {
    PostgresClient pgClient = PostgresClient.getInstance(vertxContext.owner(), tenantId);
    String schemaName = pgClient.getSchemaName();
    Future<Void> future = Future.succeededFuture();

    for (int i = 0; i < configs.size(); i++) {
      JsonObject config = configs.getJsonObject(i);
      String code = config.getString("code");
      if (MIGRATED_CODES.contains(code)) {
        future = future.compose(v -> insertOrderMapping(pgClient, schemaName, config));
      }
    }

    return future;
  }

  private Future<Void> insertOrderMapping(PostgresClient pgClient, String schemaName, JsonObject config) {
    String id = config.getString("id");
    JsonObject valueJson = new JsonObject(config.getString("value"));
    JsonObject mappingJsonb = new JsonObject()
      .put("orderType", valueJson.getValue("orderType"))
      .put("mappings", valueJson.getValue("mappings"));

    String sql = "INSERT INTO " + schemaName + "." + ORDER_MAPPINGS_TABLE + " (id, jsonb) VALUES ($1, $2::jsonb) "
      + "ON CONFLICT (lower(" + schemaName + ".f_unaccent(jsonb->>'orderType'::text))) DO NOTHING";

    return pgClient.execute(sql, Tuple.of(UUID.fromString(id), mappingJsonb.encode()))
      .onSuccess(rows -> log.info("Successfully migrated order mapping with id: {}", id))
      .onFailure(e -> log.error("Failed to insert order mapping with id: {}", id, e))
      .mapEmpty();
  }
}
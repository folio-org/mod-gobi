package org.folio.service;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.folio.okapi.common.WebClientFactory;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.persist.PostgresClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.sqlclient.Tuple;

class ConfigurationMigrationServiceTest {

  private static final String TENANT_ID = "diku";
  private static final String OKAPI_URL = "http://localhost:9130";
  private static final String MODULE_FROM_BEFORE_TARGET = "mod-gobi-2.0.0";
  private static final String MODULE_TO_TARGET = "mod-gobi-3.1.0";

  private ConfigurationMigrationService service;

  @Mock private WebClient webClient;
  @Mock private HttpRequest<Buffer> httpRequest;
  @Mock private HttpResponse<Buffer> httpResponse;
  @Mock private PostgresClient pgClient;
  @Mock private Context vertxContext;
  @Mock private Vertx vertx;

  private MockedStatic<WebClientFactory> webClientFactoryMock;
  private MockedStatic<PostgresClient> postgresClientMock;

  private Map<String, String> headers;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this).close();
    service = new ConfigurationMigrationService();

    when(vertxContext.owner()).thenReturn(vertx);

    headers = new HashMap<>();
    headers.put("x-okapi-url", OKAPI_URL);
    headers.put(OKAPI_HEADER_TENANT, TENANT_ID);

    webClientFactoryMock = mockStatic(WebClientFactory.class);
    postgresClientMock = mockStatic(PostgresClient.class);
    postgresClientMock.when(() -> PostgresClient.getInstance(vertx, TENANT_ID))
      .thenReturn(pgClient);
    when(pgClient.getSchemaName()).thenReturn(TENANT_ID + "_mod_gobi");
  }

  @AfterEach
  void tearDown() {
    webClientFactoryMock.close();
    postgresClientMock.close();
  }

  @Test
  void migrateConfigurationData_freshInstall_migrationTriggered() {
    var attributes = new TenantAttributes()
      .withModuleTo(MODULE_TO_TARGET);

    mockWebClient();
    mockHttpResponse(200, new JsonObject()
      .put("configs", new JsonArray())
      .put("totalRecords", 0));

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
  }

  @Test
  void migrateConfigurationData_alreadyAtTargetVersion_skipped() {
    var attributes = new TenantAttributes()
      .withModuleFrom("mod-gobi-3.1.0")
      .withModuleTo("mod-gobi-3.2.0");

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
  }

  @Test
  void migrateConfigurationData_pastTargetVersion_skipped() {
    var attributes = new TenantAttributes()
      .withModuleFrom("mod-gobi-4.0.0")
      .withModuleTo("mod-gobi-4.1.0");

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
  }

  @Test
  void migrateConfigurationData_snapshotModuleFrom_migrationTriggered() {
    var attributes = new TenantAttributes()
      .withModuleFrom("mod-gobi-2.0.0-SNAPSHOT.123")
      .withModuleTo(MODULE_TO_TARGET);

    mockWebClient();
    mockHttpResponse(200, new JsonObject()
      .put("configs", new JsonArray())
      .put("totalRecords", 0));

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
  }

  @Test
  void migrateConfigurationData_noOkapiUrl_skipped() {
    var attributes = new TenantAttributes()
      .withModuleFrom(MODULE_FROM_BEFORE_TARGET)
      .withModuleTo(MODULE_TO_TARGET);

    Map<String, String> headersNoUrl = new HashMap<>();
    headersNoUrl.put(OKAPI_HEADER_TENANT, TENANT_ID);

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headersNoUrl, vertxContext);

    assertTrue(result.succeeded());
  }

  @Test
  void migrateConfigurationData_orderMappingEntry_insertedIntoOrderMappingsTable() {
    var attributes = new TenantAttributes()
      .withModuleFrom(MODULE_FROM_BEFORE_TARGET)
      .withModuleTo(MODULE_TO_TARGET);

    String mappingId = UUID.randomUUID().toString();
    var valueJson = new JsonObject()
      .put("orderType", "ListedElectronicMonograph")
      .put("mappings", new JsonArray().add(new JsonObject().put("field", "TITLE")));

    var configEntry = new JsonObject()
      .put("id", mappingId)
      .put("module", "GOBI")
      .put("code", "gobi.order.ListedElectronicMonograph")
      .put("value", valueJson.encode());

    mockWebClient();
    mockHttpResponse(200, new JsonObject()
      .put("configs", new JsonArray().add(configEntry))
      .put("totalRecords", 1));
    mockPgExecuteSuccess();

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());

    var sqlCaptor = ArgumentCaptor.forClass(String.class);
    var tupleCaptor = ArgumentCaptor.forClass(Tuple.class);
    verify(pgClient).execute(sqlCaptor.capture(), tupleCaptor.capture());

    String sql = sqlCaptor.getValue();
    assertTrue(sql.contains("INSERT INTO " + TENANT_ID + "_mod_gobi.order_mappings"));
    assertTrue(sql.contains("ON CONFLICT"));

    JsonObject tupleJson = tupleCaptor.getValue().get(JsonObject.class, 1);
    assertEquals("ListedElectronicMonograph", tupleJson.getString("orderType"));
  }

  @Test
  void migrateConfigurationData_multipleMappings_allInserted() {
    var attributes = new TenantAttributes()
      .withModuleFrom(MODULE_FROM_BEFORE_TARGET)
      .withModuleTo(MODULE_TO_TARGET);

    var entry1 = createConfigEntry("gobi.order.ListedElectronicMonograph", "ListedElectronicMonograph");
    var entry2 = createConfigEntry("gobi.order.ListedPrintMonograph", "ListedPrintMonograph");

    mockWebClient();
    mockHttpResponse(200, new JsonObject()
      .put("configs", new JsonArray().add(entry1).add(entry2))
      .put("totalRecords", 2));
    mockPgExecuteSuccess();

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
    verify(pgClient, times(2)).execute(anyString(), any(Tuple.class));
  }

  @Test
  void migrateConfigurationData_nonMigratedCode_skippedEntry() {
    var attributes = new TenantAttributes()
      .withModuleFrom(MODULE_FROM_BEFORE_TARGET)
      .withModuleTo(MODULE_TO_TARGET);

    var migratedEntry = createConfigEntry("gobi.order.ListedPrintMonograph", "ListedPrintMonograph");

    var skippedEntry = new JsonObject()
      .put("id", UUID.randomUUID().toString())
      .put("module", "GOBI")
      .put("code", "gobi.some.other.config")
      .put("value", "some-value");

    mockWebClient();
    mockHttpResponse(200, new JsonObject()
      .put("configs", new JsonArray().add(migratedEntry).add(skippedEntry))
      .put("totalRecords", 2));
    mockPgExecuteSuccess();

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
    verify(pgClient, times(1)).execute(anyString(), any(Tuple.class));
  }

  @Test
  void migrateConfigurationData_emptyConfigResponse_noDbInserts() {
    var attributes = new TenantAttributes()
      .withModuleFrom(MODULE_FROM_BEFORE_TARGET)
      .withModuleTo(MODULE_TO_TARGET);

    mockWebClient();
    mockHttpResponse(200, new JsonObject()
      .put("configs", new JsonArray())
      .put("totalRecords", 0));

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
    verify(pgClient, never()).execute(anyString(), any(Tuple.class));
  }

  @Test
  void migrateConfigurationData_httpCallFails_recoveredGracefully() {
    var attributes = new TenantAttributes()
      .withModuleFrom(MODULE_FROM_BEFORE_TARGET)
      .withModuleTo(MODULE_TO_TARGET);

    mockWebClient();
    when(httpRequest.send())
      .thenReturn(Future.failedFuture(new RuntimeException("Connection refused")));

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
    verify(pgClient, never()).execute(anyString(), any(Tuple.class));
  }

  @Test
  void migrateConfigurationData_httpErrorResponse_recoveredGracefully() {
    var attributes = new TenantAttributes()
      .withModuleFrom(MODULE_FROM_BEFORE_TARGET)
      .withModuleTo(MODULE_TO_TARGET);

    mockWebClient();
    mockHttpResponse(403, new JsonObject());

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
    verify(pgClient, never()).execute(anyString(), any(Tuple.class));
  }

  @Test
  void migrateConfigurationData_dbInsertFails_recoveredGracefully() {
    var attributes = new TenantAttributes()
      .withModuleFrom(MODULE_FROM_BEFORE_TARGET)
      .withModuleTo(MODULE_TO_TARGET);

    var configEntry = createConfigEntry("gobi.order.UnlistedPrintMonograph", "UnlistedPrintMonograph");

    mockWebClient();
    mockHttpResponse(200, new JsonObject()
      .put("configs", new JsonArray().add(configEntry))
      .put("totalRecords", 1));
    when(pgClient.execute(anyString(), any(Tuple.class)))
      .thenReturn(Future.failedFuture(new RuntimeException("DB connection error")));

    var result = service.migrateConfigurationData(attributes, TENANT_ID, headers, vertxContext);

    assertTrue(result.succeeded());
  }

  private static JsonObject createConfigEntry(String code, String orderType) {
    return new JsonObject()
      .put("id", UUID.randomUUID().toString())
      .put("module", "GOBI")
      .put("code", code)
      .put("value", new JsonObject()
        .put("orderType", orderType)
        .put("mappings", new JsonArray())
        .encode());
  }

  private void mockWebClient() {
    webClientFactoryMock.when(() -> WebClientFactory.getWebClient(any(Vertx.class)))
      .thenReturn(webClient);
    when(webClient.getAbs(anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeaders(any())).thenReturn(httpRequest);
  }

  private void mockHttpResponse(int statusCode, JsonObject body) {
    when(httpResponse.statusCode()).thenReturn(statusCode);
    when(httpResponse.bodyAsJsonObject()).thenReturn(body);
    when(httpRequest.send()).thenReturn(Future.succeededFuture(httpResponse));
  }

  private void mockPgExecuteSuccess() {
    when(pgClient.execute(anyString(), any(Tuple.class)))
      .thenReturn(Future.succeededFuture());
  }
}

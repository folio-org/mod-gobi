package org.folio.gobi;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.rest.utils.TestUtils.getMockData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.folio.gobi.domain.OrdersSettingKey;
import org.folio.rest.acq.model.Setting;
import org.folio.rest.core.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.json.JsonObject;

public class LookupServiceTest {

  @Mock
  RestClient restClient;
  @Mock
  OrdersSettingsRetriever settingsRetriever;

  @BeforeEach
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testSuccessLookupFundIdByFundCodeWithExpenseClass() throws IOException {
    var funds = getMockData("PostGobiOrdersHelper/valid_funds.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(funds))).when(restClient).handleGetRequest(anyString());
    var jsonFund = lookupService.lookupFundId("AFRICAHIST:Elec").join();
    assertNotNull(jsonFund);
  }

  @Test
  void testShouldReturnNullFundIfFundIdNotFound() throws IOException {
    var funds = getMockData("PostGobiOrdersHelper/empty_funds.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(funds))).when(restClient).handleGetRequest(anyString());
    var jsonFund = lookupService.lookupFundId("NONEXISTENT").join();
    assertNull(jsonFund);
  }

  @Test
  void testLookupFirstMaterialTypes() throws IOException {
    var materialTypes = getMockData("PostGobiOrdersHelper/valid_materialType.json");

    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(materialTypes))).when(restClient).handleGetRequest(anyString());
    var jsonMaterialTypes = lookupService.lookupMaterialTypeId("unspecified").join();
    assertNotNull(jsonMaterialTypes);

  }

  @Test
  void testLookupEmptyMaterialTypes() throws IOException {
    var materialTypes = getMockData("PostGobiOrdersHelper/empty_materialType.json");

    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(materialTypes))).when(restClient).handleGetRequest(anyString());
    var jsonMaterialTypes = lookupService.lookupMaterialTypeId("unspecified").join();
    assertNull(jsonMaterialTypes);

  }


  @Test
  void testSuccessMapLookupExpenseClassId() throws IOException {
    var expClasses = getMockData("PostGobiOrdersHelper/valid_expenseClasses.json");

    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(expClasses))).when(restClient).handleGetRequest(anyString());
    var jsonExpClasses = lookupService.lookupExpenseClassId("Elec").join();

    assertNotNull(jsonExpClasses);

  }

  @Test
  void testShouldReturnNullExpenseClassIdIfExpenseClassNotFound() throws IOException {
    var suf = getMockData("PostGobiOrdersHelper/empty_expenseClasses.json");

    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(suf))).when(restClient).handleGetRequest(anyString());
    var jsonSuf = lookupService.lookupExpenseClassId("not_exists").join();

    assertNull(jsonSuf);
  }

  @Test
  void testSuccessMapLookupSuffixId() throws IOException {
    var suf = getMockData("PostGobiOrdersHelper/valid_suffix_collection.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);
    doReturn(succeededFuture(new JsonObject(suf))).when(restClient).handleGetRequest(anyString());
    var jsonSuf = lookupService.lookupSuffix("Suf").join();

    assertNotNull(jsonSuf);
  }

  @Test
  void testShouldReturnNullIdIfSuffixNotFound() throws IOException {
    var suf = getMockData("PostGobiOrdersHelper/empty_suffixes.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);
    doReturn(succeededFuture(new JsonObject(suf))).when(restClient).handleGetRequest(anyString());
    var jsonSuf = lookupService.lookupSuffix("Suf").join();

    assertNull(jsonSuf);
  }

  @Test
  void testSuccessMapLookupPrefixId() throws IOException {
    var pref = getMockData("PostGobiOrdersHelper/valid_prefix_collection.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);
    doReturn(succeededFuture(new JsonObject(pref)))
      .when(restClient).handleGetRequest(anyString());
    var jsonPref = lookupService.lookupPrefix("pref")      .join();

    assertNotNull(jsonPref);
  }

  @Test
  void testShouldReturnNullIdIfPrefixNotFound() throws IOException {
    var pref = getMockData("PostGobiOrdersHelper/empty_prefixes.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);
    doReturn(succeededFuture(new JsonObject(pref))).when(restClient)
      .handleGetRequest(anyString());
    var jsonPref = lookupService.lookupPrefix("pref")
      .join();

    assertNull(jsonPref);

  }

  @Test
  void testSuccessMapLookupAddressId() throws IOException {
    var address = getMockData("PostGobiOrdersHelper/valid_address_collection.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);
    doReturn(succeededFuture(new JsonObject(address)))
      .when(restClient).handleGetRequest(argThat(endpoint -> endpoint.equals("/tenant-addresses")));

    var jsonAddress = lookupService.lookupConfigAddress("address").join();

    assertNotNull(jsonAddress);
  }

  @Test
  void testShouldReturnNullIdIfAddressNotFound() throws IOException {
    var address = getMockData("PostGobiOrdersHelper/empty_address.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);
    doReturn(succeededFuture(new JsonObject(address)))
      .when(restClient).handleGetRequest(argThat(endpoint -> endpoint.equals("/tenant-addresses")));

    var jsonAddress = lookupService.lookupConfigAddress("address").join();

    assertNull(jsonAddress);
  }

  @Test
  void testSuccessMapLookupPoLineId() throws IOException {
    var poline = getMockData("PostGobiOrdersHelper/valid_po_line_collection.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);
    doReturn(succeededFuture(new JsonObject(poline)))
      .when(restClient).handleGetRequest(anyString());
    var jsonPoline = lookupService.lookupLinkedPackage("line").join();

    assertNotNull(jsonPoline);
  }

  @Test
  void testShouldReturnNullIdIfPoLineNotFound() throws IOException {

    var poline = getMockData("PostGobiOrdersHelper/empty_po_lines.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);
    doReturn(succeededFuture(new JsonObject(poline)))
      .when(restClient).handleGetRequest(anyString());
    var jsonPoline = lookupService.lookupLinkedPackage("line").join();

    assertNull(jsonPoline);
  }

  @Test
  void testLookupLocationIdWithExactMatchAndCentralOrderingEnabled() throws IOException {
    var locations = getMockData("PostGobiOrdersHelper/valid_locations.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(locations))).when(restClient).handleGetRequest(anyString());
    mockCentralOrdering(true);

    var result = lookupService.lookupLocationId("MAIN").join();

    assertNotNull(result);
    assertEquals("loc-1-id", result.locationId());
    assertEquals("tenant1", result.tenantId()); // Should include tenantId when central ordering is enabled
  }

  @Test
  void testLookupLocationIdWithExactMatchAndCentralOrderingDisabled() throws IOException {
    var locations = getMockData("PostGobiOrdersHelper/valid_locations.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(locations))).when(restClient).handleGetRequest(anyString());
    mockCentralOrdering(false);

    var result = lookupService.lookupLocationId("MAIN").join();

    assertNotNull(result);
    assertEquals("loc-1-id", result.locationId());
    assertNull(result.tenantId()); // Should exclude tenantId when central ordering is disabled
  }

  @Test
  void testLookupLocationIdWithExactMatchAndNoSettings() throws IOException {
    var locations = getMockData("PostGobiOrdersHelper/valid_locations.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(locations))).when(restClient).handleGetRequest(anyString());
    mockNoSettings();

    var result = lookupService.lookupLocationId("SCIENCE").join();

    assertNotNull(result);
    assertEquals("loc-2-id", result.locationId());
    assertNull(result.tenantId()); // Should exclude tenantId when no settings found (default behavior)
  }

  @Test
  void testLookupLocationIdWithDefaultCodeAndTenantMatch() throws IOException {
    var locations = getMockData("PostGobiOrdersHelper/valid_locations.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    when(restClient.getTenantId()).thenReturn("tenant1");
    doReturn(succeededFuture(new JsonObject(locations))).when(restClient).handleGetRequest(anyString());
    mockCentralOrdering(true);

    var result = lookupService.lookupLocationId("*").join();

    assertNotNull(result);
    assertEquals("loc-1-id", result.locationId()); // Should return first location with matching tenant
    assertEquals("tenant1", result.tenantId());
  }

  @Test
  void testLookupLocationIdWithDefaultCodeAndDifferentTenantMatch() throws IOException {
    var locations = getMockData("PostGobiOrdersHelper/valid_locations.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    when(restClient.getTenantId()).thenReturn("tenant2");
    doReturn(succeededFuture(new JsonObject(locations))).when(restClient).handleGetRequest(anyString());
    mockCentralOrdering(false);

    var result = lookupService.lookupLocationId("*").join();

    assertNotNull(result);
    assertEquals("loc-3-id", result.locationId()); // Should return location with tenant2
    assertNull(result.tenantId()); // Central ordering disabled
  }

  @Test
  void testLookupLocationIdWithNonExistentLocation() throws IOException {
    var locations = getMockData("PostGobiOrdersHelper/valid_locations.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(locations))).when(restClient).handleGetRequest(anyString());

    var result = lookupService.lookupLocationId("NONEXISTENT").join();

    assertNull(result);
  }

  @Test
  void testLookupLocationIdWithEmptyLocations() throws IOException {
    var locations = getMockData("PostGobiOrdersHelper/empty_locations.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    doReturn(succeededFuture(new JsonObject(locations))).when(restClient).handleGetRequest(anyString());

    var result = lookupService.lookupLocationId("MAIN").join();

    assertNull(result);
  }

  @Test
  void testLookupLocationIdWithDefaultCodeNoTenantMatch() throws IOException {
    var locations = getMockData("PostGobiOrdersHelper/valid_locations.json");
    LookupService lookupService = new LookupService(restClient, settingsRetriever);

    when(restClient.getTenantId()).thenReturn("nonexistent-tenant");
    doReturn(succeededFuture(new JsonObject(locations))).when(restClient).handleGetRequest(anyString());

    var result = lookupService.lookupLocationId("*").join();

    assertNull(result); // Should return null when no tenant match found for default code
  }

  private void mockCentralOrdering(boolean enabled) {
    Setting setting = new Setting();
    setting.setKey("ALLOW_ORDERING_WITH_AFFILIATED_LOCATIONS");
    setting.setValue(String.valueOf(enabled));
    when(settingsRetriever.getSettingByKey(any(OrdersSettingKey.class))).thenReturn(CompletableFuture.completedFuture(setting));
  }

  private void mockNoSettings() {
    when(settingsRetriever.getSettingByKey(any(OrdersSettingKey.class))).thenReturn(CompletableFuture.completedFuture(null));
  }
}

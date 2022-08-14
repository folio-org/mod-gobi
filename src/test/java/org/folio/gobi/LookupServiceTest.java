package org.folio.gobi;

import static org.folio.rest.utils.TestUtils.getMockData;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.core.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.json.JsonObject;

public class LookupServiceTest {

  @Mock
  RestClient restClient;

  @BeforeEach
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testSuccessLookupFundIdByFundCodeWithExpenseClass() throws IOException {
    var funds = getMockData("PostGobiOrdersHelper/valid_funds.json");
    LookupService lookupService = new LookupService(restClient);

    doReturn(CompletableFuture.completedFuture(new JsonObject(funds))).when(restClient).handleGetRequest(anyString());
    var jsonFund = lookupService.lookupFundId("AFRICAHIST:Elec").join();
    assertNotNull(jsonFund);
  }

  @Test
  void testShouldReturnNullFundIfFundIdNotFound() throws IOException {
    var funds = getMockData("PostGobiOrdersHelper/empty_funds.json");
    LookupService lookupService = new LookupService(restClient);

    doReturn(CompletableFuture.completedFuture(new JsonObject(funds))).when(restClient).handleGetRequest(anyString());
    var jsonFund = lookupService.lookupFundId("NONEXISTENT").join();
    assertNull(jsonFund);
  }

  @Test
  void testLookupFirstMaterialTypes() throws IOException {
    var materialTypes = getMockData("PostGobiOrdersHelper/valid_materialType.json");

    LookupService lookupService = new LookupService(restClient);

    doReturn(CompletableFuture.completedFuture(new JsonObject(materialTypes))).when(restClient).handleGetRequest(anyString());
    var jsonMaterialTypes = lookupService.lookupMaterialTypeId("unspecified").join();
    assertNotNull(jsonMaterialTypes);

  }

  @Test
  void testLookupEmptyMaterialTypes() throws IOException {
    var materialTypes = getMockData("PostGobiOrdersHelper/empty_materialType.json");

    LookupService lookupService = new LookupService(restClient);

    doReturn(CompletableFuture.completedFuture(new JsonObject(materialTypes))).when(restClient).handleGetRequest(anyString());
    var jsonMaterialTypes = lookupService.lookupMaterialTypeId("unspecified").join();
    assertNull(jsonMaterialTypes);

  }


  @Test
  void testSuccessMapLookupExpenseClassId() throws IOException {
    var expClasses = getMockData("PostGobiOrdersHelper/valid_expenseClasses.json");

    LookupService lookupService = new LookupService(restClient);

    doReturn(CompletableFuture.completedFuture(new JsonObject(expClasses))).when(restClient).handleGetRequest(anyString());
    var jsonExpClasses = lookupService.lookupExpenseClassId("Elec").join();

    assertNotNull(jsonExpClasses);

  }

  @Test
  void testShouldReturnNullExpenseClassIdIfExpenseClassNotFound() throws IOException {
    var suf = getMockData("PostGobiOrdersHelper/empty_expenseClasses.json");

    LookupService lookupService = new LookupService(restClient);

    doReturn(CompletableFuture.completedFuture(new JsonObject(suf))).when(restClient).handleGetRequest(anyString());
    var jsonSuf = lookupService.lookupExpenseClassId("not_exists").join();

    assertNull(jsonSuf);
  }

  @Test
  void testSuccessMapLookupSuffixId() throws IOException {
    var suf = getMockData("PostGobiOrdersHelper/valid_suffix_collection.json");
    LookupService lookupService = new LookupService(restClient);
    doReturn(CompletableFuture.completedFuture(new JsonObject(suf))).when(restClient).handleGetRequest(anyString());
    var jsonSuf = lookupService.lookupSuffix("Suf").join();

    assertNotNull(jsonSuf);
  }

  @Test
  void testShouldReturnNullIdIfSuffixNotFound() throws IOException {
    var suf = getMockData("PostGobiOrdersHelper/empty_suffixes.json");
    LookupService lookupService = new LookupService(restClient);
    doReturn(CompletableFuture.completedFuture(new JsonObject(suf))).when(restClient).handleGetRequest(anyString());
    var jsonSuf = lookupService.lookupSuffix("Suf").join();

    assertNull(jsonSuf);
  }

  @Test
  void testSuccessMapLookupPrefixId() throws IOException {
    var pref = getMockData("PostGobiOrdersHelper/valid_prefix_collection.json");
    LookupService lookupService = new LookupService(restClient);
    doReturn(CompletableFuture.completedFuture(new JsonObject(pref)))
      .when(restClient).handleGetRequest(anyString());
    var jsonPref = lookupService.lookupPrefix("pref")      .join();

    assertNotNull(jsonPref);
  }

  @Test
  void testShouldReturnNullIdIfPrefixNotFound() throws IOException {
    var pref = getMockData("PostGobiOrdersHelper/empty_prefixes.json");
    LookupService lookupService = new LookupService(restClient);
    doReturn(CompletableFuture.completedFuture(new JsonObject(pref))).when(restClient)
      .handleGetRequest(anyString());
    var jsonPref = lookupService.lookupPrefix("pref")
      .join();

    assertNull(jsonPref);

  }

  @Test
  void testSuccessMapLookupAddressId() throws IOException {
    var address = getMockData("PostGobiOrdersHelper/valid_address_collection.json");
    LookupService lookupService = new LookupService(restClient);
    doReturn(CompletableFuture.completedFuture(new JsonObject(address)))
      .when(restClient).handleGetRequest(anyString());
    var jsonAddress = lookupService.lookupConfigAddress("address").join();

    assertNotNull(jsonAddress);
  }

  @Test
  void testShouldReturnNullIdIfAddressNotFound() throws IOException {
    var address = getMockData("PostGobiOrdersHelper/empty_address.json");
    LookupService lookupService = new LookupService(restClient);
    doReturn(CompletableFuture.completedFuture(new JsonObject(address)))
      .when(restClient).handleGetRequest(anyString());
    var jsonAddress = lookupService.lookupConfigAddress("address").join();

    assertNull(jsonAddress);
  }

  @Test
  void testSuccessMapLookupPoLineId() throws IOException {
    var poline = getMockData("PostGobiOrdersHelper/valid_po_line_collection.json");
    LookupService lookupService = new LookupService(restClient);
    doReturn(CompletableFuture.completedFuture(new JsonObject(poline)))
      .when(restClient).handleGetRequest(anyString());
    var jsonPoline = lookupService.lookupLinkedPackage("line").join();

    assertNotNull(jsonPoline);
  }

  @Test
  void testShouldReturnNullIdIfPoLineNotFound() throws IOException {

    var poline = getMockData("PostGobiOrdersHelper/empty_po_lines.json");
    LookupService lookupService = new LookupService(restClient);
    doReturn(CompletableFuture.completedFuture(new JsonObject(poline)))
      .when(restClient).handleGetRequest(anyString());
    var jsonPoline = lookupService.lookupLinkedPackage("line").join();

    assertNull(jsonPoline);
  }

}

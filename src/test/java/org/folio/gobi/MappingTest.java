package org.folio.gobi;

import static org.folio.rest.jaxrs.model.Mapping.Field.BILL_TO;
import static org.folio.rest.jaxrs.model.Mapping.Field.EXCHANGE_RATE;
import static org.folio.rest.jaxrs.model.Mapping.Field.LINKED_PACKAGE;
import static org.folio.rest.jaxrs.model.Mapping.Field.LOCATION;
import static org.folio.rest.jaxrs.model.Mapping.Field.PO_LINE_ORDER_FORMAT;
import static org.folio.rest.jaxrs.model.Mapping.Field.PREFIX;
import static org.folio.rest.jaxrs.model.Mapping.Field.SHIP_TO;
import static org.folio.rest.jaxrs.model.Mapping.Field.SUFFIX;
import static org.folio.rest.jaxrs.model.Mapping.Field.SUPPRESS_INSTANCE_FROM_DISCOVERY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.domain.LocationTranslationResult;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.acq.model.Location;
import org.folio.rest.acq.model.PoLine;
import org.folio.rest.jaxrs.model.DataSource.Translation;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import io.vertx.core.json.Json;

public class MappingTest {

  private static final Logger logger = LogManager.getLogger(MappingTest.class);
  public static final String TEST_DATA_PATH = "Mapping/testdata.xml";

  private final String poListedPrintMonograph = "Mapping/modgobi152_po_listed_print_monograph.xml";
  private final String listedPrintMonographMapping = "Mapping/modgobi152_ListedPrintMonograph.json";

  private Document doc;

  @BeforeEach
  void setUp() throws Exception {
    InputStream data = this.getClass().getClassLoader().getResourceAsStream(TEST_DATA_PATH);
    doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
  }

  @Test
  void testBasicXPath() throws Exception {
    logger.info("begin: Test Mapping - xpath evalutation");
    assertEquals("Hello World", DataSourceResolver.builder().withFrom("//Doo/Dah").build().resolve(doc).get());
    assertEquals("DIT", DataSourceResolver.builder().withFrom("//Bar[@attr='dit']").build().resolve(doc).get());
  }

  @Test
  void testDefaults() throws Exception {
    logger.info("begin: Test Mapping - defaults");
    // default to a string literal
    assertEquals("PKD", DataSourceResolver.builder().withFrom("//Doo/Dud").withDefault("PKD").build().resolve(doc).get());

    // default to an integer literal
    assertEquals(1, DataSourceResolver.builder().withFrom("//Bar[@attr='one']").withDefault(1).build().resolve(doc).get());

    // default to another mapping
    DataSourceResolver defMapping = DataSourceResolver.builder().withFrom("//Bar[@attr='dat']").build();
    assertEquals("DAT", DataSourceResolver.builder().withFrom("//DAT").withDefault(defMapping).build().resolve(doc).get());

    // default to another mapping (multiple levels)
    DataSourceResolver defMapping1 = DataSourceResolver.builder().withFrom("//Bar[@attr='dot']").build();
    DataSourceResolver defMapping2 = DataSourceResolver.builder().withFrom("//DAT").withDefault(defMapping1).build();
    assertEquals("DOT", DataSourceResolver.builder().withFrom("//DIT").withDefault(defMapping2).build().resolve(doc).get());
  }

  @Test
  void testCombinators() throws Exception {
    logger.info("begin: Test Mapping - combinators");

    assertEquals("DITDATDOT", DataSourceResolver.builder().withFrom("//Bar").build().resolve(doc).get());
    assertEquals(4.5d, DataSourceResolver.builder()
      .withFrom("//Zap | //Zop")
      .withCombinator(Mapper::multiply)
      .withTranslation(Translation.TO_DOUBLE)
      .build()
      .resolve(doc)
      .get());
  }

  @Test
  void testTranslations() throws Exception {
    logger.info("begin: Test Mapping - translations");

    assertEquals(1.5d,
      DataSourceResolver.builder().withFrom("//Zap").withTranslation(Translation.TO_DOUBLE).build().resolve(doc).get());
    assertEquals(90210,
      DataSourceResolver.builder().withFrom("//Zip").withTranslation(Translation.TO_INTEGER).build().resolve(doc).get());
  }

  @Test
  void testLocationTranslationAndSuppressInstanceFromDiscovery() throws Exception {
    // Given
    LookupService lookupService = Mockito.mock(LookupService.class);
    String locationId = UUID.randomUUID().toString();
    String tenantId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(new LocationTranslationResult(locationId, tenantId)))
      .when(lookupService).lookupLocationId("locationCode");

    InputStream data = this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonograph);
    Document gobiOrder = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);

    String actualListedPrintJson = MappingHelper.readMappingsFile(listedPrintMonographMapping);
    Map<Mapping.Field, List<Mapping>> fieldMappingMap = Json.decodeValue(actualListedPrintJson, OrderMappings.class)
      .getMappings().stream().collect(Collectors.groupingBy(Mapping::getField));

    String locationFrom = fieldMappingMap.get(LOCATION).getFirst().getDataSource().getFrom();
    String suppressInstanceFromDiscovery = fieldMappingMap.get(SUPPRESS_INSTANCE_FROM_DISCOVERY).getFirst().getDataSource().getFrom();

    Map<Mapping.Field, DataSourceResolver> mappings = new EnumMap<>(Mapping.Field.class);
    mappings.put(PO_LINE_ORDER_FORMAT,  DataSourceResolver.builder().withLookupService(lookupService).withDefault("Physical Resource").build());
    mappings.put(LOCATION,  DataSourceResolver.builder().withLookupService(lookupService).withFrom(locationFrom).withTranslation(Translation.LOOKUP_LOCATION_ID).build());
    mappings.put(SUPPRESS_INSTANCE_FROM_DISCOVERY,  DataSourceResolver.builder().withLookupService(lookupService).withFrom(suppressInstanceFromDiscovery).build());

    // When
    Mapper mapper = new Mapper(lookupService);
    var bindingResult = mapper.map(mappings, gobiOrder).get();
    CompositePurchaseOrder compPO = bindingResult.getResult();
    PoLine pol = compPO.getPoLines().getFirst();

    // Then
    Location location = pol.getLocations().getFirst();
    assertEquals(locationId, location.getLocationId());
    assertEquals(tenantId, location.getTenantId());
    assertTrue(pol.getSuppressInstanceFromDiscovery());
  }

  @Test
  void testExceptionInApplyDefault() {
    logger.info("begin: Test Exception in applyDefault()");
    DataSourceResolver defMapping = DataSourceResolver.builder().withFrom("//Bar[@attr='dat']").build();

    Assertions.assertThrows(CompletionException.class,
      () -> DataSourceResolver.builder().withDefault(defMapping).build().resolve(null).get());
  }

  @Test
  void testSuccessLookupMappingOrdersPOListedPrintMonographWithNewAddedLookupsModGobi152() throws Exception {
    // Given
    LookupService lookupService = Mockito.mock(LookupService.class);
    String packageId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(packageId)).when(lookupService).lookupLinkedPackage("PO_6733180275-1");
    String sufId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(sufId)).when(lookupService).lookupSuffix("suf");
    String prefId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(prefId)).when(lookupService).lookupPrefix("pref");
    String vendorId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(vendorId)).when(lookupService).lookupOrganization("GOBI");
    Double exchangeRate = 2.3;

    InputStream data = this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonograph);
    Document gobiOrder = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);

    String actualListedPrintJson = MappingHelper.readMappingsFile(listedPrintMonographMapping);
    Map<Mapping.Field, List<Mapping>> fieldMappingMap = Json.decodeValue(actualListedPrintJson, OrderMappings.class)
      .getMappings().stream().collect(Collectors.groupingBy(Mapping::getField));

    String billToFrom = fieldMappingMap.get(BILL_TO).getFirst().getDataSource().getFrom();
    String shipToFrom = fieldMappingMap.get(SHIP_TO).getFirst().getDataSource().getFrom();
    String suffixFrom = fieldMappingMap.get(SUFFIX).getFirst().getDataSource().getFrom();
    String prefixFrom = fieldMappingMap.get(PREFIX).getFirst().getDataSource().getFrom();
    String linkedPackageFrom = fieldMappingMap.get(LINKED_PACKAGE).getFirst().getDataSource().getFrom();
    String exchangeRateFrom = fieldMappingMap.get(EXCHANGE_RATE).getFirst().getDataSource().getFrom();

    Map<Mapping.Field, DataSourceResolver> mappings = new EnumMap<>(Mapping.Field.class);
    mappings.put(BILL_TO,  DataSourceResolver.builder().withLookupService(lookupService).withFrom(billToFrom).withTranslation(Translation.LOOKUP_ORGANIZATION).withTranslateDefault(false).build());
    mappings.put(SHIP_TO,  DataSourceResolver.builder().withLookupService(lookupService).withFrom(shipToFrom).withTranslation(Translation.LOOKUP_ORGANIZATION).withTranslateDefault(false).build());
    mappings.put(SUFFIX,  DataSourceResolver.builder().withLookupService(lookupService).withFrom(suffixFrom).withTranslation(Translation.LOOKUP_SUFFIX).withTranslateDefault(false).build());
    mappings.put(PREFIX,  DataSourceResolver.builder().withLookupService(lookupService).withFrom(prefixFrom).withTranslation(Translation.LOOKUP_PREFIX).withTranslateDefault(false).build());
    mappings.put(LINKED_PACKAGE,  DataSourceResolver.builder().withLookupService(lookupService).withFrom(linkedPackageFrom).withTranslation(Translation.LOOKUP_LINKED_PACKAGE).withTranslateDefault(false).build());
    mappings.put(PO_LINE_ORDER_FORMAT,  DataSourceResolver.builder().withLookupService(lookupService).withDefault("Physical Resource").build());
    mappings.put(EXCHANGE_RATE,  DataSourceResolver.builder().withLookupService(lookupService).withFrom(exchangeRateFrom).build());
    // When
    Mapper mapper = new Mapper(lookupService);
    var bindingResult = mapper.map(mappings, gobiOrder).get();
    CompositePurchaseOrder compPO = bindingResult.getResult();
    PoLine pol = compPO.getPoLines().getFirst();
    // Then
    assertThat(pol.getOrderFormat(), is(PoLine.OrderFormat.PHYSICAL_RESOURCE));
    assertThat(compPO.getPoNumberSuffix(), equalTo(sufId));
    assertThat(compPO.getPoNumberPrefix(), equalTo(prefId));
    assertThat(compPO.getBillTo(), equalTo(vendorId));
    assertThat(compPO.getShipTo(), equalTo(vendorId));
    assertThat(pol.getPackagePoLineId(), equalTo(packageId));
    assertThat(pol.getCost().getExchangeRate(), equalTo(exchangeRate));
  }

  @Test
  void shouldCreateOrderInPendingStatusIfErrorsOccurredInTheLookupMappingModGobi152() throws Exception {
    // Given
    LookupService lookupService = Mockito.mock(LookupService.class);
    String packageId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(packageId)).when(lookupService).lookupLinkedPackage("PO_6733180275-1");
    String sufId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(sufId)).when(lookupService).lookupSuffix("suf");
    Mockito.doThrow(new CompletionException(new RuntimeException())).when(lookupService).lookupPrefix("pref");
    String vendorId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(vendorId)).when(lookupService).lookupOrganization("GOBI");

    InputStream data = this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonograph);
    Document gobiOrder = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);

    String actualListedPrintJson = MappingHelper.readMappingsFile(listedPrintMonographMapping);
    Map<Mapping.Field, List<Mapping>> fieldMappingMap = Json.decodeValue(actualListedPrintJson, OrderMappings.class)
      .getMappings().stream().collect(Collectors.groupingBy(Mapping::getField));

    String billToFrom = fieldMappingMap.get(BILL_TO).getFirst().getDataSource().getFrom();
    String shipToFrom = fieldMappingMap.get(SHIP_TO).getFirst().getDataSource().getFrom();
    String suffixFrom = fieldMappingMap.get(SUFFIX).getFirst().getDataSource().getFrom();
    String prefixFrom = fieldMappingMap.get(PREFIX).getFirst().getDataSource().getFrom();
    String linkedPackageFrom = fieldMappingMap.get(LINKED_PACKAGE).getFirst().getDataSource().getFrom();
    String exchangeRateFrom = fieldMappingMap.get(EXCHANGE_RATE).getFirst().getDataSource().getFrom();

    Map<Mapping.Field, DataSourceResolver> mappings = new EnumMap<>(Mapping.Field.class);
    mappings.put(BILL_TO,  DataSourceResolver.builder().withFrom(billToFrom).withTranslation(Translation.LOOKUP_ORGANIZATION).withTranslateDefault(false).build());
    mappings.put(SHIP_TO,  DataSourceResolver.builder().withFrom(shipToFrom).withTranslation(Translation.LOOKUP_ORGANIZATION).withTranslateDefault(false).build());
    mappings.put(SUFFIX,  DataSourceResolver.builder().withFrom(suffixFrom).withTranslation(Translation.LOOKUP_SUFFIX).withTranslateDefault(false).build());
    mappings.put(PREFIX,  DataSourceResolver.builder().withFrom(prefixFrom).withTranslation(Translation.LOOKUP_PREFIX).withTranslateDefault(false).build());
    mappings.put(LINKED_PACKAGE,  DataSourceResolver.builder().withFrom(linkedPackageFrom).withTranslation(Translation.LOOKUP_LINKED_PACKAGE).withTranslateDefault(false).build());
    mappings.put(PO_LINE_ORDER_FORMAT,  DataSourceResolver.builder().withDefault("Physical Resource").build());
    mappings.put(EXCHANGE_RATE,  DataSourceResolver.builder().withFrom(exchangeRateFrom).build());
    // When
    Mapper mapper = new Mapper(lookupService);
    var bindingResult = mapper.map(mappings, gobiOrder).get();
    CompositePurchaseOrder compPO = bindingResult.getResult();
    PoLine pol = compPO.getPoLines().getFirst();
    // Then
    assertThat(pol.getOrderFormat(), is(PoLine.OrderFormat.PHYSICAL_RESOURCE));
    assertThat(compPO.getWorkflowStatus(), equalTo(CompositePurchaseOrder.WorkflowStatus.PENDING));
    assertNotNull(bindingResult.getError(PREFIX));
  }

  @Test
  void shouldCreateOrderInPendingStatusIfValueNotFoundInTheLookupMappingModGobi152() throws Exception {
    // Given
    LookupService lookupService = Mockito.mock(LookupService.class);
    String packageId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(packageId)).when(lookupService).lookupLinkedPackage("PO_6733180275-1");
    String sufId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(sufId)).when(lookupService).lookupSuffix("suf");
    Mockito.doReturn(CompletableFuture.completedFuture(null)).when(lookupService).lookupPrefix("pref");
    String vendorId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(vendorId)).when(lookupService).lookupOrganization("GOBI");

    InputStream data = this.getClass().getClassLoader().getResourceAsStream(poListedPrintMonograph);
    Document gobiOrder = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);

    String actualListedPrintJson = MappingHelper.readMappingsFile(listedPrintMonographMapping);
    Map<Mapping.Field, List<Mapping>> fieldMappingMap = Json.decodeValue(actualListedPrintJson, OrderMappings.class)
      .getMappings().stream().collect(Collectors.groupingBy(Mapping::getField));

    String billToFrom = fieldMappingMap.get(BILL_TO).getFirst().getDataSource().getFrom();
    String shipToFrom = fieldMappingMap.get(SHIP_TO).getFirst().getDataSource().getFrom();
    String suffixFrom = fieldMappingMap.get(SUFFIX).getFirst().getDataSource().getFrom();
    String prefixFrom = fieldMappingMap.get(PREFIX).getFirst().getDataSource().getFrom();
    String linkedPackageFrom = fieldMappingMap.get(LINKED_PACKAGE).getFirst().getDataSource().getFrom();
    String exchangeRateFrom = fieldMappingMap.get(EXCHANGE_RATE).getFirst().getDataSource().getFrom();

    Map<Mapping.Field, DataSourceResolver> mappings = new EnumMap<>(Mapping.Field.class);
    mappings.put(BILL_TO,  DataSourceResolver.builder().withFrom(billToFrom).withTranslation(Translation.LOOKUP_ORGANIZATION).withTranslateDefault(false).build());
    mappings.put(SHIP_TO,  DataSourceResolver.builder().withFrom(shipToFrom).withTranslation(Translation.LOOKUP_ORGANIZATION).withTranslateDefault(false).build());
    mappings.put(SUFFIX,  DataSourceResolver.builder().withFrom(suffixFrom).withTranslation(Translation.LOOKUP_SUFFIX).withTranslateDefault(false).build());
    mappings.put(PREFIX,  DataSourceResolver.builder().withFrom(prefixFrom).withTranslation(Translation.LOOKUP_PREFIX).withTranslateDefault(false).build());
    mappings.put(LINKED_PACKAGE,  DataSourceResolver.builder().withFrom(linkedPackageFrom).withTranslation(Translation.LOOKUP_LINKED_PACKAGE).withTranslateDefault(false).build());
    mappings.put(PO_LINE_ORDER_FORMAT,  DataSourceResolver.builder().withDefault("Physical Resource").build());
    mappings.put(EXCHANGE_RATE,  DataSourceResolver.builder().withFrom(exchangeRateFrom).build());
    // When
    Mapper mapper = new Mapper(lookupService);
    var bindingResult = mapper.map(mappings, gobiOrder).get();
    CompositePurchaseOrder compPO = bindingResult.getResult();
    PoLine pol = compPO.getPoLines().getFirst();
    // Then
    assertThat(pol.getOrderFormat(), is(PoLine.OrderFormat.PHYSICAL_RESOURCE));
    assertThat(compPO.getWorkflowStatus(), equalTo(CompositePurchaseOrder.WorkflowStatus.PENDING));
    assertNotNull(bindingResult.getError(PREFIX));
  }
}

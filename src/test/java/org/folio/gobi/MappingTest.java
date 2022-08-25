package org.folio.gobi;

import static org.folio.rest.jaxrs.model.Mapping.Field.EXCHANGE_RATE;
import static org.folio.rest.jaxrs.model.Mapping.Field.LINKED_PACKAGE;
import static org.folio.rest.jaxrs.model.Mapping.Field.PO_LINE_ORDER_FORMAT;
import static org.folio.rest.jaxrs.model.Mapping.Field.PREFIX;
import static org.folio.rest.jaxrs.model.Mapping.Field.SUFFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.acq.model.CompositePoLine;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import io.vertx.core.json.Json;

public class MappingTest {

  private static final Logger logger = LogManager.getLogger(MappingTest.class);

  private final String MODGOBI152_PO_LISTED_PRINT_MONOGRAPH_PATH = "Mapping/modgobi152_po_listed_print_monograph.xml";
  private final String MODGOBI152_LISTED_PRINT_MONOGRAPH_MAPPING = "Mapping/modgobi152_ListedPrintMonograph.json";

  public static final String testdataPath = "Mapping/testdata.xml";
  private Document doc;
  private int port = NetworkUtils.nextFreePort();
  private Map<String, String> okapiHeaders = new HashMap<>();

  @BeforeEach
  public void setUp() throws Exception {
    InputStream data = this.getClass().getClassLoader().getResourceAsStream(testdataPath);
    doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
    okapiHeaders.put("x-okapi-url", "http://localhost:" + port);
    okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");
  }

  @AfterEach
  public void tearDown() {
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
      .withTranslation(Mapper::toDouble)
      .build()
      .resolve(doc)
      .get());
  }

  @Test
  void testTranslations() throws Exception {
    logger.info("begin: Test Mapping - translations");

    assertEquals("HELLO WORLD",
        DataSourceResolver.builder().withFrom("//Doo/Dah").withTranslation(this::toUpper).build().resolve(doc).get());
    assertEquals(1.5d,
        DataSourceResolver.builder().withFrom("//Zap").withTranslation(Mapper::toDouble).build().resolve(doc).get());
    assertEquals(90210,
        DataSourceResolver.builder().withFrom("//Zip").withTranslation(Mapper::toInteger).build().resolve(doc).get());
  }

  @Test
  void testExceptionInTranslator() {
   Assertions.assertThrows(ExecutionException.class, () -> DataSourceResolver.builder().withFrom("//Zip").withTranslation(this::throwException).build().resolve(doc).get());
  }

  @Test
   void testExceptionInApplyDefault() throws Exception {
    logger.info("begin: Test Exception in applyDefault()");
    DataSourceResolver defMapping = DataSourceResolver.builder().withFrom("//Bar[@attr='dat']").build();

    Assertions.assertThrows(CompletionException.class,
      () -> DataSourceResolver.builder().withDefault(defMapping).build().resolve(null).get());
  }

  @Test
  void testSuccessLookupMappingOrdersPOListedPrintMonographWithNewAddedLookupsModGobi152() throws Exception {
    //Given
    LookupService lookupService = Mockito.mock(LookupService.class);
    String packageId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(packageId)).when(lookupService).lookupMock(eq("PO_6733180275-1"));
    String sufId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(sufId)).when(lookupService).lookupMock(eq("suf"));
    String prefId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(prefId)).when(lookupService).lookupMock(eq("pref"));
    String vendorId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(vendorId)).when(lookupService).lookupMock(eq("GOBI"));
    Double exchangeRate = 2.3;

    InputStream data = this.getClass().getClassLoader().getResourceAsStream(MODGOBI152_PO_LISTED_PRINT_MONOGRAPH_PATH);
    Document gobiOrder = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);

    String actualListedPrintJson = MappingHelper.readMappingsFile(MODGOBI152_LISTED_PRINT_MONOGRAPH_MAPPING);
    Map<Mapping.Field, List<Mapping>> fieldMappingMap = Json.decodeValue(actualListedPrintJson, OrderMappings.class)
            .getMappings().stream().collect(Collectors.groupingBy(Mapping::getField));

    String suffixFrom = fieldMappingMap.get(SUFFIX).get(0).getDataSource().getFrom();
    String prefixFrom = fieldMappingMap.get(PREFIX).get(0).getDataSource().getFrom();
    String linkedPackageFrom = fieldMappingMap.get(LINKED_PACKAGE).get(0).getDataSource().getFrom();
    String exchangeRateFrom = fieldMappingMap.get(EXCHANGE_RATE).get(0).getDataSource().getFrom();

    Map<Mapping.Field, DataSourceResolver> mappings = new EnumMap<>(Mapping.Field.class);
    mappings.put(SUFFIX,  DataSourceResolver.builder().withFrom(suffixFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(PREFIX,  DataSourceResolver.builder().withFrom(prefixFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(LINKED_PACKAGE,  DataSourceResolver.builder().withFrom(linkedPackageFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(PO_LINE_ORDER_FORMAT,  DataSourceResolver.builder().withDefault("Physical Resource").build());
    mappings.put(EXCHANGE_RATE,  DataSourceResolver.builder().withFrom(exchangeRateFrom).build());
    //When
    Mapper mapper = new Mapper(lookupService);
    var bindingResult = mapper.map(mappings, gobiOrder).get();
    CompositePurchaseOrder compPO = bindingResult.getResult();
    CompositePoLine pol = compPO.getCompositePoLines().get(0);
    //Then
    assertThat(pol.getOrderFormat(), is(CompositePoLine.OrderFormat.PHYSICAL_RESOURCE));
    assertThat(compPO.getPoNumberSuffix(), equalTo(sufId));
    assertThat(compPO.getPoNumberPrefix(), equalTo(prefId));
    assertThat(pol.getPackagePoLineId(), equalTo(packageId));
    assertThat(pol.getCost().getExchangeRate(), equalTo(exchangeRate));
  }

  @Test
  void shouldCreateOrderInPendingStatusIfErrorsOccurredInTheLookupMappingModGobi152() throws Exception {
    //Given
    LookupService lookupService = Mockito.mock(LookupService.class);
    String packageId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(packageId)).when(lookupService).lookupMock(eq("PO_6733180275-1"));
    String sufId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(sufId)).when(lookupService).lookupMock(eq("suf"));
    Mockito.doThrow(new CompletionException(new RuntimeException())).when(lookupService).lookupMock(eq("pref"));
    String vendorId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(vendorId)).when(lookupService).lookupMock(eq("GOBI"));

    InputStream data = this.getClass().getClassLoader().getResourceAsStream(MODGOBI152_PO_LISTED_PRINT_MONOGRAPH_PATH);
    Document gobiOrder = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);

    String actualListedPrintJson = MappingHelper.readMappingsFile(MODGOBI152_LISTED_PRINT_MONOGRAPH_MAPPING);
    Map<Mapping.Field, List<Mapping>> fieldMappingMap = Json.decodeValue(actualListedPrintJson, OrderMappings.class)
      .getMappings().stream().collect(Collectors.groupingBy(Mapping::getField));

    String suffixFrom = fieldMappingMap.get(SUFFIX).get(0).getDataSource().getFrom();
    String prefixFrom = fieldMappingMap.get(PREFIX).get(0).getDataSource().getFrom();
    String linkedPackageFrom = fieldMappingMap.get(LINKED_PACKAGE).get(0).getDataSource().getFrom();
    String exchangeRateFrom = fieldMappingMap.get(EXCHANGE_RATE).get(0).getDataSource().getFrom();

    Map<Mapping.Field, DataSourceResolver> mappings = new EnumMap<>(Mapping.Field.class);
    mappings.put(SUFFIX,  DataSourceResolver.builder().withFrom(suffixFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(PREFIX,  DataSourceResolver.builder().withFrom(prefixFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(LINKED_PACKAGE,  DataSourceResolver.builder().withFrom(linkedPackageFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(PO_LINE_ORDER_FORMAT,  DataSourceResolver.builder().withDefault("Physical Resource").build());
    mappings.put(EXCHANGE_RATE,  DataSourceResolver.builder().withFrom(exchangeRateFrom).build());
    //When
    Mapper mapper = new Mapper(lookupService);
    var bindingResult = mapper.map(mappings, gobiOrder).get();
    CompositePurchaseOrder compPO = bindingResult.getResult();
    CompositePoLine pol = compPO.getCompositePoLines().get(0);
    //Then
    assertThat(pol.getOrderFormat(), is(CompositePoLine.OrderFormat.PHYSICAL_RESOURCE));
    assertThat(compPO.getWorkflowStatus(), equalTo(CompositePurchaseOrder.WorkflowStatus.PENDING));
    assertNotNull(bindingResult.getError(PREFIX));
  }

  @Test
  void shouldCreateOrderInPendingStatusIfValueNotFoundInTheLookupMappingModGobi152() throws Exception {
    //Given
    LookupService lookupService = Mockito.mock(LookupService.class);
    String packageId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(packageId)).when(lookupService).lookupMock(eq("PO_6733180275-1"));
    String sufId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(sufId)).when(lookupService).lookupMock(eq("suf"));
    Mockito.doReturn(CompletableFuture.completedFuture(null)).when(lookupService).lookupMock(eq("pref"));
    String vendorId = UUID.randomUUID().toString();
    Mockito.doReturn(CompletableFuture.completedFuture(vendorId)).when(lookupService).lookupMock(eq("GOBI"));

    InputStream data = this.getClass().getClassLoader().getResourceAsStream(MODGOBI152_PO_LISTED_PRINT_MONOGRAPH_PATH);
    Document gobiOrder = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);

    String actualListedPrintJson = MappingHelper.readMappingsFile(MODGOBI152_LISTED_PRINT_MONOGRAPH_MAPPING);
    Map<Mapping.Field, List<Mapping>> fieldMappingMap = Json.decodeValue(actualListedPrintJson, OrderMappings.class)
      .getMappings().stream().collect(Collectors.groupingBy(Mapping::getField));

    String suffixFrom = fieldMappingMap.get(SUFFIX).get(0).getDataSource().getFrom();
    String prefixFrom = fieldMappingMap.get(PREFIX).get(0).getDataSource().getFrom();
    String linkedPackageFrom = fieldMappingMap.get(LINKED_PACKAGE).get(0).getDataSource().getFrom();
    String exchangeRateFrom = fieldMappingMap.get(EXCHANGE_RATE).get(0).getDataSource().getFrom();

    Map<Mapping.Field, DataSourceResolver> mappings = new EnumMap<>(Mapping.Field.class);
    mappings.put(SUFFIX,  DataSourceResolver.builder().withFrom(suffixFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(PREFIX,  DataSourceResolver.builder().withFrom(prefixFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(LINKED_PACKAGE,  DataSourceResolver.builder().withFrom(linkedPackageFrom).withTranslation(lookupService::lookupMock).withTranslateDefault(false).build());
    mappings.put(PO_LINE_ORDER_FORMAT,  DataSourceResolver.builder().withDefault("Physical Resource").build());
    mappings.put(EXCHANGE_RATE,  DataSourceResolver.builder().withFrom(exchangeRateFrom).build());
    //When
    Mapper mapper = new Mapper(lookupService);
    var bindingResult = mapper.map(mappings, gobiOrder).get();
    CompositePurchaseOrder compPO = bindingResult.getResult();
    CompositePoLine pol = compPO.getCompositePoLines().get(0);
    //Then
    assertThat(pol.getOrderFormat(), is(CompositePoLine.OrderFormat.PHYSICAL_RESOURCE));
    assertThat(compPO.getWorkflowStatus(), equalTo(CompositePurchaseOrder.WorkflowStatus.PENDING));
    assertNotNull(bindingResult.getError(PREFIX));
  }

  private CompletableFuture<String> toUpper(String s) {
    String ret = s != null ? s.toUpperCase() : null;
    return CompletableFuture.completedFuture(ret);
  }

  private CompletableFuture<String> throwException(String s) {
    CompletableFuture<String> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> future.completeExceptionally(new Throwable("Whoops!")));
    return future;
  }
}

package org.folio.gobi;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappings.OrderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.folio.rest.jaxrs.model.Mapping.Field.ACCESS_PROVIDER;
import static org.folio.rest.jaxrs.model.Mapping.Field.EXPENSE_CLASS;
import static org.folio.rest.jaxrs.model.Mapping.Field.LINKED_PACKAGE;
import static org.folio.rest.jaxrs.model.Mapping.Field.PREFIX;
import static org.folio.rest.jaxrs.model.Mapping.Field.SUFFIX;
import static org.folio.rest.jaxrs.model.Mapping.Field.TITLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MappingHelperTest {

  private final Logger logger = LogManager.getLogger(MappingHelper.class);
  private final String LISTED_PRINT_PATH = "MappingHelper/ListedPrintMonograph.json";
  private final String UNLISTED_PRINT_PATH = "MappingHelper/UnlistedPrintMonograph.json";

  private final String EXPECTEDORDERTYPE1 = "ListedPrintMonograph";
  private final String EXPECTEDORDERTYPE2 = "UnlistedPrintMonograph";

  private String actualListedPrintJson = null;
  private String actualUnlistedPrintJson = null;
  private OrderMappings actualUnlistedPrintMappings = null;
  private OrderMappings actuallistedPrintMappings = null;
  private Mapping mappingAccessProvider = null;
  private Mapping mappingExpenseClass = null;
  private Mapping mappingCombinatorNotExist = null;
  private Mapping mapping2 = null;
  private Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping = new EnumMap<>(Mapping.Field.class);
  private LookupService lookupService = Mockito.mock(LookupService.class);
  private FieldMappingTranslatorResolver fieldMappingTranslatorResolver = new FieldMappingTranslatorResolver(lookupService);
  private MappingHelper mappingHelper = new MappingHelper(fieldMappingTranslatorResolver);

  @BeforeEach
  public void setUp() {
   logger.info("Begin: SetUp by initializing a default mapping");

   JsonObject expectedListedPrintMonographJsonObj = new JsonObject();
   expectedListedPrintMonographJsonObj.put("orderType", "ListedPrintMonograph");

   JsonObject accessProvider = new JsonObject().put("field", ACCESS_PROVIDER.value())
      .put("dataSource",
        new JsonObject().put("from", "//PurchaseOption/VendorPOCode").put("translation", "lookupOrganization"));

    JsonObject prefixId = new JsonObject().put("field", PREFIX.value())
      .put("dataSource",
        new JsonObject().put("from", "//LocalData[Description='LocalData1']/Value").put("translation", "lookupPrefix"));

    JsonObject suffixId = new JsonObject().put("field", SUFFIX.value())
      .put("dataSource",
        new JsonObject().put("from", "//LocalData[Description='LocalData2']/Value").put("translation", "lookupSuffix"));
    expectedListedPrintMonographJsonObj.put("mappings",
      new JsonArray().add(suffixId));

    JsonObject expenseClass = new JsonObject().put("field", EXPENSE_CLASS.value())
      .put("dataSource",
        new JsonObject().put("from", "//LocalData[Description='LocalData5']/Value").put("translation", "lookupExpenseClassId"));

    JsonObject linkedPackageId = new JsonObject().put("field", LINKED_PACKAGE.value())
      .put("dataSource",
        new JsonObject().put("from", "//LocalData[Description='LocalData6']/Value").put("translation", "lookupLinkedPackage"));


    JsonObject combinatorTitle = new JsonObject().put("field", TITLE.value())
      .put("dataSource",
        new JsonObject().put("from", "//datafield[@tag='245']/*").put("combinator", "concat"));

    expectedListedPrintMonographJsonObj.put("mappings",
      new JsonArray().add(accessProvider).add(combinatorTitle).add(linkedPackageId).add(expenseClass)
                        .add(suffixId).add(prefixId));

    String expectedListedPrintMonographJson = expectedListedPrintMonographJsonObj.toString();

    JsonObject expectedUnListedPrintMonographJsonObj = new JsonObject();
    expectedUnListedPrintMonographJsonObj.put("orderType", "UnlistedPrintMonograph");
    expectedUnListedPrintMonographJsonObj.put("mappings",
        new JsonArray().add(new JsonObject().put("field", "PRODUCT_ID")
          .put("dataSource",
              new JsonObject().put("from", "//datafield[@tag='020']/subfield[@code='a']")))
          .add(new JsonObject().put("field", "CONTRIBUTOR")
            .put("dataSource",
                new JsonObject().put("from", "//datafield[@tag='100']/*").put("combinator", "concat"))));

    String expectedUnListedPrintMonographJson = expectedUnListedPrintMonographJsonObj.toString();

    actualListedPrintJson = MappingHelper.readMappingsFile(LISTED_PRINT_PATH);
    assertEquals(expectedListedPrintMonographJson, actualListedPrintJson.replaceAll("\\s", ""));

    actuallistedPrintMappings = Json.decodeValue(actualListedPrintJson, OrderMappings.class);

    actualUnlistedPrintJson = MappingHelper.readMappingsFile(UNLISTED_PRINT_PATH);
    assertEquals(expectedUnListedPrintMonographJson,
        actualUnlistedPrintJson.replaceAll("\\s", ""));

    actualUnlistedPrintMappings = Json.decodeValue(actualUnlistedPrintJson, OrderMappings.class);


    OrderType orderType1 = actuallistedPrintMappings.getOrderType();
    OrderType orderType2 = actualUnlistedPrintMappings.getOrderType();
    assertEquals(EXPECTEDORDERTYPE1, orderType1.toString());
    assertEquals(EXPECTEDORDERTYPE2, orderType2.toString());

    List<Mapping> mappingsList1 = actuallistedPrintMappings.getMappings();
    List<Mapping> mappingsList2 = actualUnlistedPrintMappings.getMappings();
    mappingAccessProvider = mappingsList1.get(0);
    mappingExpenseClass = mappingsList1.get(3);
    mappingCombinatorNotExist = mappingsList1.get(2);
    mappingCombinatorNotExist.getDataSource().setCombinator(null);

    mapping2 = mappingsList2.get(0);
    assertEquals(8, mappingsList1.size());
    assertEquals(2, mappingsList2.size());
    assertEquals(Mapping.Field.ACCESS_PROVIDER.value(), mappingAccessProvider.getField().toString());
    assertEquals(EXPENSE_CLASS.value(), mappingExpenseClass.getField().toString());

    assertEquals("PRODUCT_ID", mapping2.getField().toString());
  }

  @Test()
  void testNullFilePath() {
    logger.info("Begin: Testing for failure when default mappings filepath is null");
    Assertions.assertThrows(NullPointerException.class, () -> MappingHelper.readMappingsFile(null));
  }

  @Test()
  void testHelperUtilsExtractSubAccount() {
    logger.info("Begin: Testing for failure when AccountNo is null");
    Assertions.assertThrows(NullPointerException.class, () -> HelperUtils.normalizeSubAccout(null));
  }

  @Test
  void testMapperNodeListMultiply() {
    logger.info("Begin: Testing for null when NodeList is null");
    Assertions.assertNull(Mapper.multiply(null));
  }

  @Test
  void testMapperNodeListConcat() {
    logger.info("Begin: Testing for null when NodeList is null");
    Assertions.assertNull(Mapper.concat(null));
  }

  @Test
  void testMappingHelperDefaultMappingGetDSMapping1() {
    logger.info("Begin: Testing for default mapping when it returns a DataSource mapping for OrderType ListedPrintMonograph");
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping1 = new LinkedHashMap<>();
    org.folio.gobi.DataSourceResolver dataSource = mappingHelper.getDS(mappingAccessProvider, fieldDataSourceMapping1);
    assertEquals("//PurchaseOption/VendorPOCode", dataSource.from);
    assertFalse(dataSource.translateDefValue);
  }

  @Test
  void testMappingHelperDefaultMappingGetDSMapping2() {
    logger.info(
        "Begin: Testing for default mapping when it returns a DataSource mapping for OrderType UnlistedPrintMonograph");
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping2 = new LinkedHashMap<>();
    org.folio.gobi.DataSourceResolver dataSource = mappingHelper.getDS(mapping2, fieldDataSourceMapping2);
    assertEquals("//datafield[@tag='020']/subfield[@code='a']", dataSource.from);
    assertFalse(dataSource.translateDefValue);
    assertNotNull(dataSource.combinator);
  }

  @Test
  void readMappingsFile_mappingFileNotFound() {
    assertEquals("", MappingHelper.readMappingsFile("errorType"));
  }

  @Test
  void readMappingsFile_IOException() {
    new MockUp<IOUtils>() {
      @Mock
      String toString(final InputStream input, final Charset encoding) throws IOException {
        throw new IOException("IOException");
      }
    };

    assertEquals("", MappingHelper.readMappingsFile(OrderType.LISTED_PRINT_MONOGRAPH.value() + ".json"));
  }

  @Test
  void testShouldSuccessMapDiffLookups() {
    Map<Mapping.Field, List<Mapping>> mappings = actuallistedPrintMappings.getMappings().stream().collect(groupingBy(Mapping::getField));
    org.folio.gobi.DataSourceResolver dataSourcePrefix = mappingHelper.getDS(mappings.get(PREFIX).get(0), fieldDataSourceMapping);

    assertEquals("//LocalData[Description='LocalData1']/Value", dataSourcePrefix.from);
    assertNotNull(dataSourcePrefix.translation);
    org.folio.gobi.DataSourceResolver dataSourceSuffix = mappingHelper.getDS(mappings.get(SUFFIX).get(0), fieldDataSourceMapping);
    assertEquals("//LocalData[Description='LocalData2']/Value", dataSourceSuffix.from);
    assertNotNull(dataSourceSuffix.translation);
    org.folio.gobi.DataSourceResolver dataSourceLinkedPackage = mappingHelper.getDS(mappings.get(LINKED_PACKAGE).get(0), fieldDataSourceMapping);
    assertNotNull(dataSourceLinkedPackage.translation);
    assertEquals("//LocalData[Description='LocalData6']/Value", dataSourceLinkedPackage.from);
  }

  @Test
  void testShouldSuccessMapExpenseClass() {
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping = new LinkedHashMap<>();
    org.folio.gobi.DataSourceResolver dataSource = mappingHelper.getDS(mappingExpenseClass, fieldDataSourceMapping);
    assertEquals("//LocalData[Description='LocalData5']/Value", dataSource.from);
    assertNotNull(dataSource.translation);
    assertFalse(dataSource.translateDefValue);
  }
}

package org.folio.gobi;

import static org.folio.rest.mappings.model.Mapping.Field.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMappings;
import org.folio.rest.mappings.model.OrderMappings.OrderType;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import mockit.Mock;
import mockit.MockUp;

public class MappingHelperTest {

  private static final Logger logger = LogManager.getLogger(MappingHelper.class);
  private static final String LISTED_PRINT_PATH = "MappingHelper/ListedPrintMonograph.json";
  private static final String UNLISTED_PRINT_PATH = "MappingHelper/UnlistedPrintMonograph.json";

  private static final String EXPECTEDORDERTYPE1 = "ListedPrintMonograph";
  private static final String EXPECTEDORDERTYPE2 = "UnlistedPrintMonograph";

  private static String actualListedPrintJson = null;
  private static String actualUnlistedPrintJson = null;
  private static OrderMappings actualUnlistedPrintMappings = null;
  private static OrderMappings actuallistedPrintMappings = null;
  private static Mapping mappingAccessProvider = null;
  private static Mapping mappingExpenseClass = null;
  private static Mapping mappingCombinatorNotExist = null;
  private static Mapping mapping2 = null;

  @Before
  public void setUp() {
    logger.info("Begin: SetUp by initializing a default mapping");

    JsonObject expectedListedPrintMonographJsonObj = new JsonObject();
    expectedListedPrintMonographJsonObj.put("orderType", "ListedPrintMonograph");

   JsonObject accessProvider = new JsonObject().put("field", ACCESS_PROVIDER.value())
      .put("dataSource",
        new JsonObject().put("from", "//PurchaseOption/VendorPOCode").put("translation", "lookupOrganization"));

    JsonObject expenseClass = new JsonObject().put("field", EXPENSE_CLASS.value())
      .put("dataSource",
        new JsonObject().put("from", "//LocalData[Description='LocalData5']/Value").put("translation", "lookupExpenseClassId"));;
    expectedListedPrintMonographJsonObj.put("mappings",
        new JsonArray().add(accessProvider).add(expenseClass));

    JsonObject combinatorTitle = new JsonObject().put("field", TITLE.value())
      .put("dataSource",
        new JsonObject().put("from", "//datafield[@tag='245']/*").put("combinator", "concat"));;
    expectedListedPrintMonographJsonObj.put("mappings",
      new JsonArray().add(accessProvider).add(expenseClass).add(combinatorTitle));


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
    mappingExpenseClass = mappingsList1.get(1);
    mappingCombinatorNotExist = mappingsList1.get(2);
    mappingCombinatorNotExist.getDataSource().setCombinator(null);

    mapping2 = mappingsList2.get(0);
    assertEquals(3, mappingsList1.size());
    assertEquals(2, mappingsList2.size());
    assertEquals(Mapping.Field.ACCESS_PROVIDER.value(), mappingAccessProvider.getField().toString());
    assertEquals(EXPENSE_CLASS.value(), mappingExpenseClass.getField().toString());

    assertEquals("PRODUCT_ID", mapping2.getField().toString());
  }

  @Test(expected=NullPointerException.class)
  public void testNullFilePath() {
    logger.info("Begin: Testing for failure when default mappings filepath is null");
    fail(MappingHelper.readMappingsFile(null));
  }

  @Test(expected=NullPointerException.class)
  public void testHelperUtilsExtractSubAccount() {
    logger.info("Begin: Testing for failure when AccountNo is null");
    assertNull(HelperUtils.extractSubAccount(null));
  }

  @Test
  public void testMapperNodeListMultiply() {
    logger.info("Begin: Testing for null when NodeList is null");
    assertNull(Mapper.multiply(null));
  }

  @Test
  public void testMappingHelperDefaultMappingGetDSMapping1() {
    logger.info(
        "Begin: Testing for default mapping when it returns a DataSource mapping for OrderType ListedPrintMonograph");
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping1 = new LinkedHashMap<>();
    org.folio.gobi.DataSourceResolver dataSource = MappingHelper.getDS(mappingAccessProvider, fieldDataSourceMapping1, null);
    assertEquals("//PurchaseOption/VendorPOCode", dataSource.from);
    assertFalse(dataSource.translateDefValue);
  }

  @Test
  public void testMappingHelperDefaultMappingGetDSMapping2() {
    logger.info(
        "Begin: Testing for default mapping when it returns a DataSource mapping for OrderType UnlistedPrintMonograph");
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping2 = new LinkedHashMap<>();
    org.folio.gobi.DataSourceResolver dataSource = MappingHelper.getDS(mapping2, fieldDataSourceMapping2, null);
    assertEquals("//datafield[@tag='020']/subfield[@code='a']", dataSource.from);
    assertFalse(dataSource.translateDefValue);
    assertNotNull(dataSource.combinator);
  }

  @Test
  public void readMappingsFile_mappingFileNotFound() {
    assertEquals("", MappingHelper.readMappingsFile("errorType"));
  }

  @Test
  public void readMappingsFile_IOException() {
    new MockUp<IOUtils>() {
      @Mock
      String toString(final InputStream input, final Charset encoding) throws IOException {
        throw new IOException("IOException");
      }
    };

    assertEquals("", MappingHelper.readMappingsFile(OrderType.LISTED_PRINT_MONOGRAPH.value() + ".json"));
  }

  @Test
  public void testShouldSuccessMapExpenseClass() {
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping = new LinkedHashMap<>();
    org.folio.gobi.DataSourceResolver dataSource = MappingHelper.getDS(mappingExpenseClass, fieldDataSourceMapping, null);
    assertEquals("//LocalData[Description='LocalData5']/Value", dataSource.from);
    assertFalse(dataSource.translateDefValue);
  }
}

package org.folio.gobi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.OrderMappings;
import org.folio.rest.mappings.model.OrderMappings.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.runner.Description;

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
  private static Mapping mapping1 = null;
  private static Mapping mapping2 = null;

  @Before
  public void setUp() {
    logger.info("Begin: SetUp by initializing a default mapping");

    JsonObject expectedListedPrintMonographJsonObj = new JsonObject();
    expectedListedPrintMonographJsonObj.put("orderType", "ListedPrintMonograph");
    expectedListedPrintMonographJsonObj.put("mappings",
        new JsonArray().add(new JsonObject().put("field", "ACCESS_PROVIDER")
          .put("dataSource",
              new JsonObject().put("from", "//PurchaseOption/VendorPOCode").put("translation", "lookupOrganization"))));

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
    mapping1 = mappingsList1.get(0);
    mapping2 = mappingsList2.get(0);
    assertEquals(1, mappingsList1.size());
    assertEquals(2, mappingsList2.size());
    assertEquals("ACCESS_PROVIDER", mapping1.getField().toString());
    assertEquals("PRODUCT_ID", mapping2.getField().toString());
  }

  @Test(expected=NullPointerException.class)
  public void testNullFilePath() {
    logger.info("Begin: Testing for failure when default mappings filepath is null");
    fail(MappingHelper.readMappingsFile(null));
  }

  @Test
  public void testMappingHelperDefaultMappingGetDSMapping1() {
    logger.info(
        "Begin: Testing for default mapping when it returns a DataSource mapping for OrderType ListedPrintMonograph");
    Map<Mapping.Field, org.folio.gobi.DataSourceResolver> fieldDataSourceMapping1 = new LinkedHashMap<>();
    org.folio.gobi.DataSourceResolver dataSource = MappingHelper.getDS(mapping1, fieldDataSourceMapping1, null);
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
}

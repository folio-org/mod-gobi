package org.folio.gobi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.folio.gobi.MappingHelper;
import org.folio.rest.mappings.model.Mapping;
import org.folio.rest.mappings.model.Mappings;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.folio.rest.mappings.model.OrderMapping;
import org.folio.rest.mappings.model.OrderMapping.OrderType;

import io.vertx.core.json.Json;

public class MappingHelperTest {

  private static final Logger logger = LoggerFactory.getLogger(MappingHelper.class);
  private static final String PATH = "MappingHelper/default-mappings.json";
  private static final String EXPECTEDORDERTYPE1 = "ListedPrintMonograph";
  private static final String EXPECTEDORDERTYPE2 = "UnlistedPrintMonograph";
  
  private static String actualDefaultMappingJson = null;
  private static Mappings actualMappings = null;
  private static Mapping mapping1 = null;
  private static Mapping mapping2 = null;
  
  @Before
  public void setUp() throws Exception {
    logger.info("Begin: SetUp by initializing a default mapping");
    
    String expectedDefaultMappingJson = "{\n" + 
        "  \"orderMappings\":\n" + 
        "  [\n" + 
        "    {\n" + 
        "      \"orderType\": \"ListedPrintMonograph\",\n" + 
        "      \"mappings\": [\n" + 
        "        {\n" + 
        "          \"field\": \"ACCESS_PROVIDER\",\n" + 
        "          \"dataSource\": {\n" + 
        "            \"from\": \"//PurchaseOption/VendorPOCode\",\n" + 
        "            \"translation\": \"lookupVendorId\"\n" + 
        "          }\n" + 
        "        },  \n" + 
        "        {\n" + 
        "          \"field\": \"USER_LIMIT\",\n" + 
        "          \"dataSource\": {\n" + 
        "            \"from\": \"//PurchaseOption/Code\",\n" + 
        "            \"translation\": \"getPurchaseOptionCode\"\n" + 
        "          }\n" + 
        "        }\n" + 
        "      ]\n" + 
        "    },\n" + 
        "    {\n" + 
        "      \"orderType\": \"UnlistedPrintMonograph\",\n" + 
        "      \"mappings\": [\n" + 
        "        {\n" + 
        "          \"field\": \"PRODUCT_ID\",\n" + 
        "          \"dataSource\": {\n" + 
        "            \"from\": \"//datafield[@tag='020']/subfield[@code='a']\"\n" + 
        "          }\n" + 
        "        },\n" + 
        "        {\n" + 
        "          \"field\": \"USER_LIMIT\",\n" + 
        "          \"dataSource\": {\n" + 
        "            \"from\": \"//PurchaseOption/Code\",\n" + 
        "            \"translation\": \"getPurchaseOptionCode\"\n" + 
        "          }\n" + 
        "        },\n" + 
        "        {\n" + 
        "          \"field\": \"CONTRIBUTOR\",\n" + 
        "          \"dataSource\": {\n" + 
        "            \"from\": \"//datafield[@tag='100']/*\",\n" + 
        "            \"combinator\": \"concat\"\n" + 
        "          }\n" + 
        "        } \n" + 
        "      ]\n" + 
        "    } \n" + 
        "  ]\n" + 
        "} \n";
    
    actualDefaultMappingJson = MappingHelper.readMappingsFile(PATH);
    assertEquals(expectedDefaultMappingJson, actualDefaultMappingJson);
    actualMappings = Json.decodeValue(actualDefaultMappingJson, Mappings.class);
    final List<OrderMapping> orderMappingList = actualMappings.getOrderMappings(); // get orderMappings list
    OrderType orderType1 = orderMappingList.get(0).getOrderType();
    OrderType orderType2 = orderMappingList.get(1).getOrderType();
    assertEquals(EXPECTEDORDERTYPE1, orderType1.toString());
    assertEquals(EXPECTEDORDERTYPE2, orderType2.toString());
    List<Mapping> mappingsList1 = orderMappingList.get(0).getMappings();
    List<Mapping> mappingsList2 = orderMappingList.get(1).getMappings();
    mapping1 = mappingsList1.get(0);
    mapping2 = mappingsList2.get(0);
    assertEquals(2, mappingsList1.size());
    assertEquals(3, mappingsList2.size());
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
    Map<Mapping.Field, org.folio.gobi.DataSource> fieldDataSourceMapping1 = new LinkedHashMap<>();
    org.folio.gobi.DataSource dataSource = MappingHelper.getDS(mapping1, fieldDataSourceMapping1, null);
    assertEquals("//PurchaseOption/VendorPOCode", dataSource.from);
    assertEquals(false, dataSource.translateDefValue);
  }

  @Test
  public void testMappingHelperDefaultMappingGetDSMapping2() {
    logger.info(
        "Begin: Testing for default mapping when it returns a DataSource mapping for OrderType UnlistedPrintMonograph");
    Map<Mapping.Field, org.folio.gobi.DataSource> fieldDataSourceMapping2 = new LinkedHashMap<>();
    org.folio.gobi.DataSource dataSource = MappingHelper.getDS(mapping2, fieldDataSourceMapping2, null);
    assertEquals("//datafield[@tag='020']/subfield[@code='a']", dataSource.from);
    assertEquals(false, dataSource.translateDefValue);
    assertNotNull(dataSource.combinator);
  }
}

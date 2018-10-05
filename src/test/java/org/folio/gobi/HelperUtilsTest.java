package org.folio.gobi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.folio.gobi.Mapper.Field;
import org.folio.rest.impl.PostGobiOrdersHelper;
import org.folio.rest.mappings.model.OrderMapping;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.impl.JsonParserImpl;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

public class HelperUtilsTest {

  private static final Logger logger = LoggerFactory.getLogger(HelperUtilsTest.class);

  @Test
  public void testExtractVendorUid() {

    logger.info("Begin: Testing for extracting valid Uid when only 1 vendor is in response");

    String vendorId = "9db7d395-a664-4c56-8e2f-f015413bc783";
    JsonArray vendorArray = new JsonArray();
    JsonObject vendor = new JsonObject();
    vendor.put("id", vendorId);
    vendor.put("name", "GOBI");
    vendor.put("code", "EBR");
    vendor.put("description", "This is Yankee Book Peddler");
    vendor.put("vendor_status", "Active");

    vendorArray.add(vendor);
    JsonObject vendorResponse = new JsonObject();

    vendorResponse.put("vendors", vendorArray);

    String extracteVendorId = HelperUtils.extractVendorId(vendorResponse);
    assertEquals(vendorId, extracteVendorId);
  }

  @Test
  public void testExtractVendorUidWithMultipleVendors() {

    logger.info(
        "Begin: Testing for extracting valid Uid when multiple vendors are returned, expecting it to grab the first one");

    String vendorId = "9db7d395-a664-4c56-8e2f-f015413bc783";
    JsonArray vendorArray = new JsonArray();

    for (int i = 0; i < 2; i++) {
      JsonObject vendor = new JsonObject();
      vendor.put("id", vendorId + i);
      vendor.put("name", "GOBI" + i);
      vendor.put("code", "EBR" + i);
      vendor.put("description", "This is Yankee Book Peddler" + i);
      vendor.put("vendor_status", i % 2 == 0 ? "Active" : "Inactive");

      vendorArray.add(vendor);
    }

    JsonObject vendorResponse = new JsonObject();

    vendorResponse.put("vendors", vendorArray);

    String extracteVendorId = HelperUtils.extractVendorId(vendorResponse);
    assertEquals(vendorId + "0", extracteVendorId);
  }

  @Test
  public final void testReadJsonAndCreateDefaultMapping() throws Exception {
	  
	  //final String testdataPath = "ConfigData/test_default_mapping.json";
	  //Assuming we will get a json object below is test to extract default mappings from json file
	  /*
		{
		    "orderMappings":[
	        {
	            "orderType":"ListedPrintMonograph",
	            "mappings":[{
	                "field":"ACCOUNT_NUMBER",
	                "dataSource":{
	                    "from":"//SubAccount",
	                    "default":"0"
	                }
	            }]
	        }]
		}
	   */
	  Map<String, Object> dataSourceMap1 = new LinkedHashMap<String, Object>();
	  dataSourceMap1.put("from", "//SubAccount");
	  dataSourceMap1.put("default", "0");
	  JsonObject dataSourceObj1 = new JsonObject(dataSourceMap1);
	  JsonObject mappingsObj1 = new JsonObject();
	  mappingsObj1.put("field", "ACCOUNT_NUMBER");
	  mappingsObj1.put("dataSource", dataSourceObj1);
	  JsonArray mappingsArray1 = new JsonArray();
	  mappingsArray1.add(mappingsObj1);
	  JsonObject orderMappingsObj1 = new JsonObject();
	  orderMappingsObj1.put("orderType", "ListedPrintMonograph");
	  
	  /*
		{
	    	"orderMappings":[
	        {
	            "orderType":"ListedPrintMonograph",
	            "mappings":[{
					"field": "ACQUISITION_METHOD",
					"dataSource": {
						"default": "Purchase at Vendor System"
					}
	        	}]
			}]
		}
	   */
	  Map<String, Object> dataSourceMap2 = new LinkedHashMap<String, Object>();
	  dataSourceMap2.put("default", "Purchase at Vendor System");
	  JsonObject dataSourceObj2 = new JsonObject(dataSourceMap2);
	  JsonObject mappingsObj2 = new JsonObject();
	  mappingsObj2.put("field", "ACQUISITION_METHOD");
	  mappingsObj2.put("dataSource", dataSourceObj2);
	  JsonArray mappingsArray2 = new JsonArray();
	  mappingsArray2.add(mappingsObj2);
	  
	  orderMappingsObj1.put("mappings", mappingsArray1);
	  //orderMappingsObj1.put("mappings", mappingsArray2);
	  
	  JsonArray orderMappingsArray1 = new JsonArray();
	  orderMappingsArray1.add(orderMappingsObj1);
	  JsonObject Obj = new JsonObject();
	  
	  
	  Obj.put("orderMappings", orderMappingsArray1);


  }
  
  @Test
  public final void testExtractOrderMappingWithOtherField() throws Exception {
    org.folio.rest.mappings.model.DataSource jsonDs = new org.folio.rest.mappings.model.DataSource();

    String jsonFrom = "//abc/de/fg";
    String listPriceFrom = "//hi/jk/l";
    String listPriceVal = "22.5";

    jsonDs.setFrom(jsonFrom);
    jsonDs.setFromOtherField(org.folio.rest.mappings.model.DataSource.FromOtherField.LIST_PRICE);

    Map<Mapper.Field, DataSource> map = new EnumMap<>(Mapper.Field.class);
    DataSource dataSrc = DataSource.builder()
      .withFrom(listPriceFrom)
      .withDefault(listPriceVal)
      .build();

    map.put(Mapper.Field.LIST_PRICE, dataSrc);

    DataSource outputDs = HelperUtils.extractOrderMapping(jsonDs, map);
    assertNotNull(outputDs);
    assertEquals(jsonFrom, outputDs.from);

    assertNotNull(outputDs.defValue);
    DataSource otherFieldDs = (DataSource) outputDs.defValue;
    assertEquals(listPriceVal, otherFieldDs.defValue);
    assertEquals(listPriceFrom, otherFieldDs.from);
  }
}

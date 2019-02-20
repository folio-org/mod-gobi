package org.folio.gobi;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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

}

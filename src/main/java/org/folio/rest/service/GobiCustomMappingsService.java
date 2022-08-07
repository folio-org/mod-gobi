package org.folio.rest.service;

import static java.util.stream.Collectors.toList;
import static org.folio.rest.ResourcePaths.CONFIGURATION_ENDPOINT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.folio.rest.client.ConfigurationsClient;
import org.folio.rest.core.RestClient;
import org.folio.rest.jaxrs.model.Config;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappingsView;

import io.vertx.core.Context;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.OrderMappingsViewCollection;

/*import lombok.extern.log4j.Log4j;

@Log4j*/
public class GobiCustomMappingsService {
  private final RestClient restClient;
  public static final String SEARCH_ENDPOINT = "%s?limit=%s&offset=%s%s";

  ConfigurationsClient configurationsClient;
  public GobiCustomMappingsService(Map<String, String> okapiHeaders, Context vertxContext) {
    this.restClient = new RestClient(okapiHeaders, vertxContext);
  }

  public CompletableFuture<OrderMappingsViewCollection> getCustomMappingListByQuery(String query, int offset, int limit) {
    query = "configName==GOBI AND configName==orderMappings";
    return restClient.handleGetRequest(CONFIGURATION_ENDPOINT, query, offset, limit)
      .thenApply(this::buildOrderMappingsViewCollectionResponse);
  }
/*  {
    "module": "GOBI",
    "configName": "orderMappings",
    "code": "gobi.order.ListedPrintMonograph",
    "description": "GOBI order mappings",
    "enabled": true,
    "value": "{\n  \"orderType\": \"ListedPrintMonograph\",\n  \"mappings\": [\n    {\n      \"field\": \"ACQUISITION_METHOD\",\n      \"dataSource\": {\n        \"default\": \"Purchase At Vendor System\"\n      }\n    },\n    {\n      \"field\": \"APPROVED\",\n      \"dataSource\": {\n        \"default\": \"true\",\n        \"translation\": \"toBoolean\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"CLAIMED\",\n      \"dataSource\": {\n        \"default\": \"true\",\n        \"translation\": \"toBoolean\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"COLLECTION\",\n      \"dataSource\": {\n        \"default\": \"false\",\n        \"translation\": \"toBoolean\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"CONTRIBUTOR\",\n      \"dataSource\": {\n        \"from\": \"//datafield[@tag='100']/*\",\n        \"combinator\": \"concat\"\n      }\n    },\n    {\n      \"field\": \"CONTRIBUTOR_NAME_TYPE\",\n      \"dataSource\": {\n        \"default\": \"Personal name\",\n        \"translation\": \"lookupContributorNameTypeId\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"CURRENCY\",\n      \"dataSource\": {\n        \"from\": \"//ListPrice/Currency\",\n        \"default\": \"USD\"\n      }\n    },\n    {\n      \"field\": \"DATE_ORDERED\",\n      \"dataSource\": {\n        \"from\": \"//OrderPlaced\",\n        \"translation\": \"toDate\"\n      }\n    },\n    {\n      \"field\": \"FUND_ID\",\n      \"dataSource\": {\n        \"from\": \"//FundCode\",\n        \"translation\": \"lookupFundId\"\n      }\n    },\n    {\n      \"field\": \"FUND_CODE\",\n      \"dataSource\": {\n        \"from\": \"//FundCode\"\n      }\n    },\n    {\n      \"field\": \"FUND_PERCENTAGE\",\n      \"dataSource\": {\n        \"default\": \"100\",\n        \"translation\": \"toDouble\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"VENDOR_INSTRUCTIONS\",\n      \"dataSource\": {\n        \"from\": \"//OrderNotes\",\n        \"default\": \"N/A\"\n      }\n    },\n    {\n      \"field\": \"LIST_UNIT_PRICE\",\n      \"dataSource\": {\n        \"from\": \"//ListPrice/Amount\",\n        \"default\": \"0\",\n        \"translation\": \"toDouble\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"LOCATION\",\n      \"dataSource\": {\n        \"from\": \"//Location\",\n        \"default\": \"*\",\n        \"translation\": \"lookupLocationId\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"MANUAL_PO\",\n      \"dataSource\": {\n        \"default\": \"false\",\n        \"translation\": \"toBoolean\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"ORDER_TYPE\",\n      \"dataSource\": {\n        \"default\": \"One-Time\"\n      }\n    },\n    {\n      \"field\": \"PO_LINE_ORDER_FORMAT\",\n      \"dataSource\": {\n        \"default\": \"Physical Resource\"\n      }\n    },\n    {\n      \"field\": \"PO_LINE_PAYMENT_STATUS\",\n      \"dataSource\": {\n        \"default\": \"Awaiting Payment\"\n      }\n    },\n    {\n      \"field\": \"PO_LINE_RECEIPT_STATUS\",\n      \"dataSource\": {\n        \"default\": \"Awaiting Receipt\"\n      }\n    },\n    {\n      \"field\": \"PRODUCT_ID\",\n      \"dataSource\": {\n        \"from\": \"//datafield[@tag='020']/subfield[@code='a']\",\n        \"translation\": \"truncateISBNQualifier\"\n      }\n    },\n    {\n      \"field\": \"PRODUCT_ID_TYPE\",\n      \"dataSource\": {\n        \"default\": \"ISBN\",\n        \"translation\": \"lookupProductIdType\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"PRODUCT_QUALIFIER\",\n      \"dataSource\": {\n        \"from\": \"//datafield[@tag='020']/subfield[@code='q']\",\n        \"defaultMapping\": {\n          \"dataSource\": {\n            \"from\": \"//datafield[@tag='020']/subfield[@code='a']\",\n            \"translation\": \"separateISBNQualifier\"\n          }\n        }\n      }\n    },\n    {\n      \"field\": \"PUBLICATION_DATE\",\n      \"dataSource\": {\n        \"from\": \"//datafield[@tag='260']/subfield[@code='c']\"\n      }\n    },\n    {\n      \"field\": \"PUBLISHER\",\n      \"dataSource\": {\n        \"from\": \"//datafield[@tag='260']/subfield[@code='b']\"\n      }\n    },\n    {\n      \"field\": \"QUANTITY_PHYSICAL\",\n      \"dataSource\": {\n        \"from\": \"//Quantity\",\n        \"default\": \"1\",\n        \"translation\": \"toInteger\"\n      }\n    },\n    {\n      \"field\": \"SOURCE\",\n      \"dataSource\": {\n        \"default\": \"API\"\n      }\n    },\n    {\n      \"field\": \"TITLE\",\n      \"dataSource\": {\n        \"from\": \"//datafield[@tag='245']/*\",\n        \"combinator\": \"concat\"\n      }\n    },\n    {\n      \"field\": \"VENDOR\",\n      \"dataSource\": {\n        \"default\": \"GOBI\",\n        \"translation\": \"lookupOrganization\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"MATERIAL_SUPPLIER\",\n      \"dataSource\": {\n        \"default\": \"GOBI\",\n        \"translation\": \"lookupOrganization\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"VENDOR_ACCOUNT\",\n      \"dataSource\": {\n        \"from\": \"//SubAccount\",\n        \"default\": \"0\"\n      }\n    },\n    {\n      \"field\": \"VENDOR_REF_NO\",\n      \"dataSource\": {\n        \"from\": \"//YBPOrderKey\"\n      }\n    },\n    {\n      \"field\": \"VENDOR_REF_NO_TYPE\",\n      \"dataSource\": {\n        \"default\": \"Vendor order reference number\"\n      }\n    },\n    {\n      \"field\": \"WORKFLOW_STATUS\",\n      \"dataSource\": {\n        \"default\": \"Open\"\n      }\n    },\n    {\n      \"field\": \"MATERIAL_TYPE\",\n      \"dataSource\": {\n        \"from\": \"//LocalData[Description='LocalData1']/Value\",\n        \"default\": \"unspecified\",\n        \"translation\": \"lookupMaterialTypeId\",\n        \"translateDefault\": true\n      }\n    },\n    {\n      \"field\": \"LINKED_PACKAGE\",\n      \"dataSource\": {\n        \"from\": \"//LocalData[Description='LocalData2']/Value\",\n        \"translation\": \"lookupLinkedPackage\"\n      }\n    },\n    {\n      \"field\": \"SHIP_TO\",\n      \"dataSource\": {\n        \"from\": \"//LocalData[Description='LocalData3']/Value\",\n        \"translation\": \"lookupConfigAddress\"\n      }\n    },\n    {\n      \"field\": \"PREFIX\",\n      \"dataSource\": {\n        \"from\": \"//LocalData[Description='LocalData4']/Value\",\n        \"translation\": \"lookupPrefix\"\n      }\n    }\n  ]\n}"
  } */
  public CompletableFuture<OrderMappingsView> getCustomMappingByOrderType(String orderType) {
    var query = String.format("configName==GOBI AND configName==orderMappings + code==gobi.order.%s", orderType);
    return restClient.handleGetRequest(CONFIGURATION_ENDPOINT, query, 0, 1)
      .thenApply(this::buildOrderMappingsViewResponse);

  }

  private OrderMappingsView buildOrderMappingsViewResponse(JsonObject entries) {
    var asd = entries.getJsonArray("configs").getJsonObject(0);
    if (asd != null) {
      var om = Json.decodeValue(asd.mapTo(Config.class).getValue(), OrderMappings.class);
      new OrderMappingsView().withMappingType(OrderMappingsView.MappingType.CUSTOM).withMapping(null);
    }
    return new OrderMappingsView();
  }

  private OrderMappingsViewCollection buildOrderMappingsViewCollectionResponse(JsonObject configs) {
    List<OrderMappings> defaultMappings = loadDefaultMappings();
    var omvc = new OrderMappingsViewCollection();
    List<Config> configList = configs.getJsonArray("configs")
      .stream()
      .map(JsonObject.class::cast)
      .map(json -> json.mapTo(Config.class))
      .collect(toList());

/*
    final String mappingsString = conf.getValue();
    final org.folio.rest.mappings.model.OrderMappings orderMapping = Json.decodeValue(mappingsString, org.folio.rest.mappings.model.OrderMappings.class);
*/

  //  final List<Mapping> orderMappingList = orderMapping.getMappings();
    List<OrderMappingsView> omv = configList.stream()
      .map(conf -> new OrderMappingsView()
        .withMappingType(OrderMappingsView.MappingType.CUSTOM)
        .withMapping(null))
      .collect(toList());

    omvc.withOrderMappingsViews(omv);
    return omvc;
  }

  private List<OrderMappings> loadDefaultMappings() {
    return Arrays.stream(OrderMappings.OrderType.values())
      .map(this::loadDefaultMappingByType)
      .collect(toList());

  }

  private org.folio.rest.jaxrs.model.OrderMappings loadDefaultMappingByType(OrderMappings.OrderType orderType) {
    StringBuilder sb = new StringBuilder();
    try (Stream<String> lines = Files.lines(Paths.get(orderType.value() + ".json"))) {
      lines.forEach(sb::append);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Json.decodeValue(sb.toString(), OrderMappings.class);
  }


  private OrderMappingsView.MappingType resolveMappingType(String code) {
    return null;
  }

  public CompletableFuture<OrderMappingsView> postCustomMapping(OrderMappings orderMappingsView) {
    return restClient.post("", JsonObject.mapFrom(orderMappingsView))
      .thenApply(json -> json.mapTo(OrderMappingsView.class));
  }

  public CompletableFuture<Void> putCustomMapping(String orderType, OrderMappings orderMappingsView) {
    return restClient.handlePutRequest("", JsonObject.mapFrom(orderMappingsView));
  }

  public CompletableFuture<Void> deleteCustomMapping(String id) {
    return restClient.delete(CONFIGURATION_ENDPOINT + "/", id);
  }
}

package org.folio.gobi;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.folio.gobi.HelperUtils.extractFundCode;
import static org.folio.rest.ResourcePaths.ACQUISITION_METHOD_ENDPOINT;
import static org.folio.rest.ResourcePaths.ACQUISITION_UNIT_ENDPOINT;
import static org.folio.rest.ResourcePaths.CONFIGURATION_ENDPOINT;
import static org.folio.rest.ResourcePaths.CONTRIBUTOR_NAME_TYPES_ENDPOINT;
import static org.folio.rest.ResourcePaths.EXPENSE_CLASS_ENDPOINT;
import static org.folio.rest.ResourcePaths.FUNDS_ENDPOINT;
import static org.folio.rest.ResourcePaths.GET_ORGANIZATION_ENDPOINT;
import static org.folio.rest.ResourcePaths.IDENTIFIERS_ENDPOINT;
import static org.folio.rest.ResourcePaths.LOCATIONS_ENDPOINT;
import static org.folio.rest.ResourcePaths.MATERIAL_TYPES_ENDPOINT;
import static org.folio.rest.ResourcePaths.ORDER_LINES_ENDPOINT;
import static org.folio.rest.ResourcePaths.PREFIXES_ENDPOINT;
import static org.folio.rest.ResourcePaths.SUFFIXES_ENDPOINT;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.acq.model.AcquisitionMethod;
import org.folio.rest.acq.model.AcquisitionsUnit;
import org.folio.rest.acq.model.Organization;
import org.folio.rest.core.RestClient;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class LookupService {
  private static final Logger logger = LogManager.getLogger(LookupService.class);

  public static final String QUERY = "?query=%s";
  public static final String LIMIT_1 = "&limit=1";
  public static final String DEFAULT_LOOKUP_CODE = "*";
  public static final String CQL_CODE_STRING_FMT = "code==\"%s\"";
  public static final String CQL_NAME_STRING_FMT = "name==\"%s\"";
  public static final String ACQ_METHODS_NAME = "acquisitionMethods";
  public static final String DEFAULT_ACQ_METHOD_VALUE = "Purchase At Vendor System";
  public static final String ACQ_METHODS_QUERY = "value==(%s OR "+ DEFAULT_ACQ_METHOD_VALUE +")";
  public static final String CONFIGURATION_ADDRESS_QUERY = "module==(TENANT or tenant) AND configName==tenant.addresses AND value==*%s*";
  public static final String PO_LINE_NUMBER_QUERY = "poLineNumber==%s";
  public static final String UNSPECIFIED_MATERIAL_NAME = "unspecified";
  public static final String CHECK_ORGANIZATION_ISVENDOR = " and isVendor==true";
  public static final String CHECK_ACQ_UNIT_IS_NOT_DELETED = " and isDeleted==false";
  public static final String CQL_NAME_CRITERIA = "name==%s";
  public static final int FIRST_ELEM = 0;
  public static final String CONFIGS = "configs";
  public static final String PREFIXES = "prefixes";
  public static final String SUFFIXES = "suffixes";
  public static final String PO_LINES = "poLines";
  public static final String ID = "id";
  public static final String NAME = "name";

  private final RestClient restClient;

  public LookupService(RestClient restClient) {
    this.restClient = restClient;
  }
  /**
   * Use the provided location code. if one isn't provided, or the specified
   * type can't be found, fallback using the first one listed
   *
   * @param location
   * @return UUID of the location
   */
  public CompletableFuture<String> lookupLocationId(String location) {
    logger.info("Received location is {}", location);
    String query = HelperUtils.encodeValue(String.format(CQL_CODE_STRING_FMT, location));
    String endpoint = String.format(LOCATIONS_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenCompose(locations -> {
        String locationId = HelperUtils.extractLocationId(locations);
        if (StringUtils.isEmpty(locationId)) {
          return completedFuture(null);
        }
        return completedFuture(locationId);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up location id", t);
        return null;
      });
  }

  /**
   * Use the provided materialType. if the specified type can't be found,
   * fallback to looking up "unspecified"(handled via default mappings).If that
   * cannot be found too, fallback to using the first one listed
   *
   * @param materialTypeCode
   */
  public CompletableFuture<String> lookupMaterialTypeId(String materialTypeCode) {
    String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, materialTypeCode));
    String endpoint = String.format(MATERIAL_TYPES_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenCompose(materialTypes -> {
        String materialType = HelperUtils.extractMaterialTypeId(materialTypes);
        if (StringUtils.isEmpty(materialType)) {
          if (StringUtils.equalsIgnoreCase(materialTypeCode, UNSPECIFIED_MATERIAL_NAME)) {
            return lookupMaterialTypeId(DEFAULT_LOOKUP_CODE);
          } else {
            return completedFuture(null);
          }
        }
        return completedFuture(materialType);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up material-type id", t);
        return null;
      });
  }

  public CompletableFuture<Organization> lookupOrganization(String vendorCode) {
    String query = HelperUtils.encodeValue(String.format(CQL_CODE_STRING_FMT+CHECK_ORGANIZATION_ISVENDOR, vendorCode));
    String endpoint = String.format(GET_ORGANIZATION_ENDPOINT + QUERY, query);

    return restClient.handleGetRequest(endpoint)
      .thenApply(resp ->
        Optional.ofNullable(resp.getJsonArray("organizations"))
          .flatMap(organizations -> organizations.stream().findFirst())
          .map(organization -> ((JsonObject) organization).mapTo(Organization.class))
          .orElse(null))
      .exceptionally(t -> {
        String errorMessage = String.format("Exception looking up Organization which is a vendor with code: %s", vendorCode);
        logger.error(errorMessage, t);
        return null;
      });
  }

  public CompletableFuture<String> lookupFundId(String fundCode) {
    String query = HelperUtils.encodeValue(String.format("code==%s", extractFundCode(fundCode)));
    String endpoint = String.format(FUNDS_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenApply(funds -> {
        String fundId = HelperUtils.extractIdOfFirst(funds, "funds");
        if (StringUtils.isEmpty(fundId)) {
          return null;
        }
        return fundId;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up fund id", t);
        return null;
      });
  }

  /**
   * Use the product type specified in mappings.If the specified type can't be found, fallback using the first one listed
   *
   * @param productType
   * @return UUID of the productId Type
   */
  public CompletableFuture<String> lookupProductIdType(String productType) {
    logger.info("Received ProductType is {}", productType);
    String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, productType));
    String endpoint = String.format(IDENTIFIERS_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).thenCompose(productTypes -> {
        String productTypeId = HelperUtils.extractProductTypeId(productTypes);
        if (StringUtils.isEmpty(productTypeId)) {
          // the productType is already a default value in the mappings, so fallback to first one
          return DEFAULT_LOOKUP_CODE.equals(productType) ? completedFuture(null) : lookupProductIdType(DEFAULT_LOOKUP_CODE);
        }
        return completedFuture(productTypeId);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up productId type UUID", t);
        return null;
      });
  }

  /**
   * Use the contributor name type specified in mappings. If the specified type can't be found, fallback using the first one listed
   *
   * @param name contributor type name
   * @return UUID of the contributor name type
   */
  public CompletableFuture<String> lookupContributorNameTypeId(String name) {
    logger.info("Received contributorNameType is {}", name);
    String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, name));
    String endpoint = String.format(CONTRIBUTOR_NAME_TYPES_ENDPOINT + QUERY + LIMIT_1, query);
    return restClient.handleGetRequest(endpoint).thenApply(HelperUtils::extractContributorNameTypeId)
      .thenCompose(typeId -> {
        if (StringUtils.isEmpty(typeId)) {
          if (DEFAULT_LOOKUP_CODE.equals(name)) {
            logger.error("No contributorNameTypes are available");
            return completedFuture(null);
          }
          // the type is already a default value in the mappings, so fallback to first one
          return lookupContributorNameTypeId(DEFAULT_LOOKUP_CODE);
        }
        return completedFuture(typeId);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up contributorNameType type UUID", t);
        return null;
      });
  }

  public CompletableFuture<String> lookupExpenseClassId(String expenseClassCode) {
    String query = HelperUtils.encodeValue(String.format("code==%s", expenseClassCode));
    String endpoint = String.format(EXPENSE_CLASS_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenApply(funds -> {
        String expenseClassId = HelperUtils.extractIdOfFirst(funds, "expenseClasses");
        if (StringUtils.isEmpty(expenseClassId)) {
          return null;
        }
        return expenseClassId;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up expense class id", t);
        return null;
      });
  }

  public CompletableFuture<String> lookupAcquisitionUnitDefault(String data) {
    String query = HelperUtils.encodeValue(String.format(CQL_NAME_STRING_FMT + CHECK_ACQ_UNIT_IS_NOT_DELETED, data));
    String endpoint = String.format(ACQUISITION_UNIT_ENDPOINT + QUERY, query);

    return restClient.handleGetRequest(endpoint)
      .thenApply(resp ->
        Optional.ofNullable(resp.getJsonArray("acquisitionsUnits"))
          .flatMap(acquisitionUnits -> acquisitionUnits.stream().findFirst())
          .map(organization -> ((JsonObject) organization).mapTo(AcquisitionsUnit.class).getId())
          .orElse(null))
      .exceptionally(t -> {
        String errorMessage = String.format("Exception looking up Acquisition unit for a vendor with name: %s", data);
        logger.error(errorMessage, t);
        return null;
      });

  }

  public CompletableFuture<String> lookupConfigAddress(String shipToName) {
    final String query = HelperUtils.encodeValue(String.format(CONFIGURATION_ADDRESS_QUERY, shipToName));
    String endpoint = String.format(CONFIGURATION_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenApply(addressConfig ->  {
        JsonArray addressJsonArray = addressConfig.getJsonArray(CONFIGS);
        if(!addressJsonArray.isEmpty()) {
          JsonObject address = addressJsonArray.getJsonObject(FIRST_ELEM);
          return address.getString(ID);
        }
        return null;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up address ID from configuration", t);
        return null;
      });
  }

  public CompletableFuture<String> lookupPrefix(String prefixName) {
    final String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, prefixName));
    String endpoint = String.format(PREFIXES_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenApply(prefixes ->  {
        JsonArray prefixesJsonArray = prefixes.getJsonArray(PREFIXES);
        if(!prefixesJsonArray.isEmpty()) {
          JsonObject address = prefixesJsonArray.getJsonObject(FIRST_ELEM);
          return address.getString(NAME);
        }
        return null;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up prefix ID", t);
        return null;
      });
  }

  public CompletableFuture<String> lookupSuffix(String suffixName) {
    final String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, suffixName));
    String endpoint = String.format(SUFFIXES_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenApply(suffixes ->  {
        JsonArray suffixesJsonArray = suffixes.getJsonArray(SUFFIXES);
        if(!suffixesJsonArray.isEmpty()) {
          JsonObject address = suffixesJsonArray.getJsonObject(FIRST_ELEM);
          return address.getString(NAME);
        }
        return null;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up suffix ID ", t);
        return null;
      });
  }

  public CompletableFuture<String> lookupLinkedPackage(String linkedPackageLineNumber) {
    final String query = HelperUtils.encodeValue(String.format(PO_LINE_NUMBER_QUERY, linkedPackageLineNumber));
    String endpoint = String.format(ORDER_LINES_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenApply(addressConfig ->  {
        JsonArray addressJsonArray = addressConfig.getJsonArray(PO_LINES);
        if(!addressJsonArray.isEmpty()) {
          JsonObject address = addressJsonArray.getJsonObject(FIRST_ELEM);
          return address.getString(ID);
        }
        return null;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up address ID from configuration", t);
        return null;
      });
  }

  public CompletableFuture<String> separateISBNQualifier(String data) {
    String[] productIDArr = data.trim().split("\\s+",2);
    if (productIDArr.length > 1) {
      return completedFuture(productIDArr[1]);
    }
    return completedFuture(null);
  }

  public CompletableFuture<String>  truncateISBNQualifier(String data) {
    String[] productIDArr = data.trim().split("\\s+",2);
    if(productIDArr.length > 1) {
      return completedFuture(productIDArr[FIRST_ELEM]);
    }
    return completedFuture(data);
  }

  public CompletableFuture<String> lookupAcquisitionMethodId(String acquisitionMethod) {
    String query = HelperUtils.encodeValue(String.format(ACQ_METHODS_QUERY, acquisitionMethod));
    String endpoint = String.format(ACQUISITION_METHOD_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint)
      .thenApply(acquisitionMethods -> {
        Map<String, AcquisitionMethod> acqMethods = acquisitionMethods.getJsonArray(ACQ_METHODS_NAME).stream()
          .map(obj -> ((JsonObject) obj).mapTo(AcquisitionMethod.class))
          .collect(toMap(AcquisitionMethod::getValue, identity()));

        return Optional.ofNullable(acqMethods.get(acquisitionMethod))
          .map(AcquisitionMethod::getId)
          .orElse(acqMethods.get(DEFAULT_ACQ_METHOD_VALUE).getId());
      })
      .exceptionally(t -> {
        logger.error("Exception looking up acquisition method id", t);
        return null;
      });
  }


  public CompletableFuture<String> lookupMock(String data) {
    logger.info("Mocking the data lookup for: {}", data);
    return CompletableFuture.completedFuture(UUID.randomUUID()
      .toString());
  }
}

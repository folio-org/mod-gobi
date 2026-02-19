package org.folio.gobi;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.folio.rest.ResourcePaths.ACQUISITION_METHOD_ENDPOINT;
import static org.folio.rest.ResourcePaths.ACQUISITION_UNIT_ENDPOINT;
import static org.folio.rest.ResourcePaths.TENANT_ADDRESSES_ENDPOINT;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.domain.LocationTranslationResult;
import org.folio.gobi.domain.OrdersSettingKey;
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
  public static final String PO_LINE_NUMBER_QUERY = "poLineNumber==%s";
  public static final String UNSPECIFIED_MATERIAL_NAME = "unspecified";
  public static final String CHECK_ORGANIZATION_ISVENDOR = " and isVendor==true";
  public static final String CHECK_ACQ_UNIT_IS_NOT_DELETED = " and isDeleted==false";
  public static final String CQL_NAME_CRITERIA = "name==%s";
  public static final int FIRST_ELEM = 0;
  public static final String ADDRESSES = "addresses";
  public static final String PREFIXES = "prefixes";
  public static final String SUFFIXES = "suffixes";
  public static final String PO_LINES = "poLines";
  public static final String ID = "id";
  public static final String NAME = "name";

  private final RestClient restClient;
  private final OrdersSettingsRetriever settingsRetriever;

  public LookupService(RestClient restClient, OrdersSettingsRetriever settingsRetriever) {
    this.restClient = restClient;
    this.settingsRetriever = settingsRetriever;
  }
  /**
   * Use the provided location code.
   * If one isn't provided, or the specified
   * type can't be found, fallback using the first one listed
   * Checks CENTRAL_ORDERING_ENABLED setting to determine whether to include tenantId in the result.
   *
   * @param locationCode code of location object
   * @return UUID of the location object with conditional tenantId based on settings
   */
  public CompletableFuture<LocationTranslationResult> lookupLocationId(String locationCode) {
    logger.debug("lookupLocationId:: Trying to look up locationId by location '{}'", locationCode);
    String tenantIdFromHeaders = restClient.getTenantId();
    return restClient.handleGetRequest(LOCATIONS_ENDPOINT).toCompletionStage().toCompletableFuture()
      .thenCompose(locations -> {
        JsonArray locationsJsonArray = locations.getJsonArray("locations");
        for (int i = 0; i < locationsJsonArray.size(); i++) {
          JsonObject locationObject = locationsJsonArray.getJsonObject(i);
          String code = locationObject.getString("code");
          String locationId = locationObject.getString("id");
          String tenantId = locationObject.getString("tenantId");

          if (Objects.equals(locationCode, code)) {
            logger.info("lookupLocationId:: found locationId: {}, tenantId: {}", locationId, tenantId);
            return getLookupLocationResult(locationId, tenantId);
          } else if (DEFAULT_LOOKUP_CODE.equals(locationCode) && StringUtils.equals(tenantIdFromHeaders, tenantId)) {
            logger.info("lookupLocationId:: found first available locationId: {} that matches tenantId: {} "
                + "for default lookup code: {}", locationId, tenantId, DEFAULT_LOOKUP_CODE);
            return getLookupLocationResult(locationId, tenantId);
          }
        }
        logger.warn("lookupLocationId:: Location '{}' not found", locationCode);
        return completedFuture(null);
      })
      .exceptionally(t -> {
        logger.error("Error while searching for location '{}'", locationCode, t);
        return null;
      });
  }

  private CompletableFuture<LocationTranslationResult> getLookupLocationResult(String locationId, String tenantId) {
    return settingsRetriever.getSettingByKey(OrdersSettingKey.CENTRAL_ORDERING_ENABLED)
      .thenApply(setting -> {
        if (setting != null && Boolean.TRUE.toString().equalsIgnoreCase(setting.getValue())) {
          return new LocationTranslationResult(locationId, tenantId);
        } else {
          return new LocationTranslationResult(locationId, null);
        }
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
    logger.debug("lookupMaterialTypeId:: Trying to look up materialTypeId by materialTypeCode: {}", materialTypeCode);
    String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, materialTypeCode));
    String endpoint = String.format(MATERIAL_TYPES_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenCompose(materialTypes -> {
        String materialType = HelperUtils.extractMaterialTypeId(materialTypes);
        if (StringUtils.isEmpty(materialType)) {
          logger.warn("lookupMaterialTypeId:: MaterialTypeCode: {} not found", materialTypeCode);
          if (StringUtils.equalsIgnoreCase(materialTypeCode, UNSPECIFIED_MATERIAL_NAME)) {
            return lookupMaterialTypeId(DEFAULT_LOOKUP_CODE);
          } else {
            return completedFuture(null);
          }
        }
        logger.info("lookupMaterialTypeId:: found materialType id: {}", materialType);
        return completedFuture(materialType);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up materialType id with materialTypeCode: {} ", materialTypeCode, t);
        return null;
      });
  }

  public CompletableFuture<Organization> lookupOrganization(String vendorCode) {
    logger.debug("lookupOrganization:: Trying to look up organization by vendorCode: {}", vendorCode);
    String query = HelperUtils.encodeValue(String.format(CQL_CODE_STRING_FMT+CHECK_ORGANIZATION_ISVENDOR, vendorCode));
    String endpoint = String.format(GET_ORGANIZATION_ENDPOINT + QUERY, query);

    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(resp ->
        Optional.ofNullable(resp.getJsonArray("organizations"))
          .flatMap(organizations -> organizations.stream().findFirst())
          .map(organization -> ((JsonObject) organization).mapTo(Organization.class))
          .orElse(null))
      .exceptionally(t -> {
        logger.error("Exception looking up Organization which is a vendor with code: {}", vendorCode, t);
        return null;
      });
  }

  public CompletableFuture<String> lookupFundId(String fundCode) {
    logger.debug("lookupFundId:: Trying to look up fundId by fundCode: {}", fundCode);
    String query = HelperUtils.encodeValue(String.format("code==%s", HelperUtils.extractFundCode(fundCode)));
    String endpoint = String.format(FUNDS_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(funds -> {
        String fundId = HelperUtils.extractIdOfFirst(funds, "funds");
        if (StringUtils.isEmpty(fundId)) {
          logger.warn("lookupFundId:: FundCode: {} not found", fundCode);
          return null;
        }
        return fundId;
      })
      .exceptionally(t -> {
        logger.error("Error when looking up fund id with fundCode: {}", fundCode, t);
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
    logger.debug("lookupProductIdType: Try to look up productTypeId with productType: {}", productType);
    String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, productType));
    String endpoint = String.format(IDENTIFIERS_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture().thenCompose(productTypes -> {
        String productTypeId = HelperUtils.extractProductTypeId(productTypes);
        if (StringUtils.isEmpty(productTypeId)) {
          logger.warn("lookupProductIdType:: ProductType '{}' not found", productType);
          // the productType is already a default value in the mappings, so fallback to first one
          return DEFAULT_LOOKUP_CODE.equals(productType) ? completedFuture(null) : lookupProductIdType(DEFAULT_LOOKUP_CODE);
        }
        return completedFuture(productTypeId);
      })
      .exceptionally(t -> {
        logger.error("Error when looking up productId type UUID with productType: {}", productType, t);
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
    logger.debug("lookupContributorNameType:: Trying to look up ContributorTypeId {}", name);
    String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, name));
    String endpoint = String.format(CONTRIBUTOR_NAME_TYPES_ENDPOINT + QUERY + LIMIT_1, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture().thenApply(HelperUtils::extractContributorNameTypeId)
      .thenCompose(typeId -> {
        if (StringUtils.isEmpty(typeId)) {
          if (DEFAULT_LOOKUP_CODE.equals(name)) {
            logger.warn("lookupContributorNameType:: No contributorNameTypes are available");
            return completedFuture(null);
          }
          // the type is already a default value in the mappings, so fallback to first one
          return lookupContributorNameTypeId(DEFAULT_LOOKUP_CODE);
        }
        return completedFuture(typeId);
      })
      .exceptionally(t -> {
        logger.error("Exception looking up contributorNameType type UUID with its name: {}", name, t);
        return null;
      });
  }

  public CompletableFuture<String> lookupExpenseClassId(String expenseClassCode) {
    logger.debug("lookupExpenseClassId:: Trying to look up expenseClassId by expenseClassCode: {}", expenseClassCode);
    String query = HelperUtils.encodeValue(String.format("code==%s", expenseClassCode));
    String endpoint = String.format(EXPENSE_CLASS_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(funds -> {
        String expenseClassId = HelperUtils.extractIdOfFirst(funds, "expenseClasses");
        if (StringUtils.isEmpty(expenseClassId)) {
          logger.warn("lookupExpenseClassId:: ExpenseClassCode '{}' not found", expenseClassCode);
          return null;
        }
        return expenseClassId;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up expense class id with expenseClassCode: {}", expenseClassCode, t);
        return null;
      });
  }

  public CompletableFuture<String> lookupAcquisitionUnitIdsByName(String data) {
    logger.debug("lookupAcquisitionUnitIdsByName:: Trying to look up acquisitionUnitIds by acquisitionUnitName: {}", data);
    String query = HelperUtils.encodeValue(String.format(CQL_NAME_STRING_FMT + CHECK_ACQ_UNIT_IS_NOT_DELETED, data));
    String endpoint = String.format(ACQUISITION_UNIT_ENDPOINT + QUERY, query);

    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(resp ->
        Optional.ofNullable(resp.getJsonArray("acquisitionsUnits"))
          .flatMap(acquisitionUnits -> acquisitionUnits.stream().findFirst())
          .map(organization -> ((JsonObject) organization).mapTo(AcquisitionsUnit.class).getId())
          .orElse(null))
      .exceptionally(t -> {
        logger.error("Exception looking up Acquisition unit for a vendor with name: {}", data, t);
        return null;
      });

  }

  /**
   *
   * Because of the impossibility at the moment to get two values from the xml file,it was decided to bring
   * the logic of searching for acquisition units by account into the {@link org.folio.gobi.Mapper}.
   * Because of the uniqueness of the account number only within the organization, we will also need the name of the
   * organization in addition to the account number. This method will help us get the account number from mapping
   * using the Mapping.Field.ACQUISITION_UNIT constant.
   *
   * @param data organization account number
   * @return organization account number wrapped in completable future
   */
  public CompletableFuture<Object> lookupAcquisitionUnitIdsByAccount(String data) {
    logger.debug("lookupAcquisitionUnitIdsByAccount:: Trying to look up acquisitionUnitIds by accountNumber: {}", data);
    return CompletableFuture.completedFuture(data);
  }

  public CompletableFuture<String> lookupConfigAddress(String shipToName) {
    logger.debug("lookupConfigAddress:: Trying to look up config address by name: {}", shipToName);
    return restClient.handleGetRequest(TENANT_ADDRESSES_ENDPOINT).toCompletionStage().toCompletableFuture()
      .thenApply(addressesResponse ->  {
        JsonArray addressJsonArray = addressesResponse.getJsonArray(ADDRESSES);
        if(addressJsonArray != null && !addressJsonArray.isEmpty()) {
          for (int i = 0; i < addressJsonArray.size(); i++) {
            JsonObject address = addressJsonArray.getJsonObject(i);
            String addressName = address.getString(NAME);
            if (addressName != null && addressName.equals(shipToName)) {
              logger.info("lookupConfigAddress:: Found address with name '{}' matching '{}'", addressName, shipToName);
              return address.getString(ID);
            }
          }
        }
        logger.warn("lookupConfigAddress:: Config address with name '{}' not found", shipToName);
        return null;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up address ID from tenant-addresses with shipToName: {}", shipToName, t);
        return null;
      });
  }

  public CompletableFuture<String> lookupPrefix(String prefixName) {
    logger.debug("lookupPrefix:: Trying to lookup prefix by name: {}", prefixName);
    final String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, prefixName));
    String endpoint = String.format(PREFIXES_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(prefixes ->  {
        JsonArray prefixesJsonArray = prefixes.getJsonArray(PREFIXES);
        if(!prefixesJsonArray.isEmpty()) {
          JsonObject address = prefixesJsonArray.getJsonObject(FIRST_ELEM);
          return address.getString(NAME);
        }
        logger.warn("lookupPrefix:: Prefix '{}' not found", prefixName);
        return null;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up prefix ID with prefixName: {}", prefixName, t);
        return null;
      });
  }

  public CompletableFuture<String> lookupSuffix(String suffixName) {
    logger.debug("lookupSuffix:: Trying to lookup suffix by name: {}", suffixName);
    final String query = HelperUtils.encodeValue(String.format(CQL_NAME_CRITERIA, suffixName));
    String endpoint = String.format(SUFFIXES_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(suffixes ->  {
        JsonArray suffixesJsonArray = suffixes.getJsonArray(SUFFIXES);
        if(!suffixesJsonArray.isEmpty()) {
          JsonObject address = suffixesJsonArray.getJsonObject(FIRST_ELEM);
          return address.getString(NAME);
        }
        logger.warn("lookupSuffix:: Suffix '{}' not found", suffixName);
        return null;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up suffix ID with suffixName: {}", suffixName, t);
        return null;
      });
  }

  public CompletableFuture<String> lookupLinkedPackage(String linkedPackageLineNumber) {
    logger.debug("lookupLinkedPackage:: Trying to lookup linked package by line number: {}", linkedPackageLineNumber);
    final String query = HelperUtils.encodeValue(String.format(PO_LINE_NUMBER_QUERY, linkedPackageLineNumber));
    String endpoint = String.format(ORDER_LINES_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(addressConfig ->  {
        JsonArray addressJsonArray = addressConfig.getJsonArray(PO_LINES);
        if(!addressJsonArray.isEmpty()) {
          JsonObject address = addressJsonArray.getJsonObject(FIRST_ELEM);
          return address.getString(ID);
        }
        logger.warn("lookupLinkedPackage:: Linked package with line number '{}' not found", linkedPackageLineNumber);
        return null;
      })
      .exceptionally(t -> {
        logger.error("Exception looking up address ID from configuration with its number: {}", linkedPackageLineNumber, t);
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
    logger.debug("lookupAcquisitionMethodId:: Trying to look up acquisition method id by name: {}", acquisitionMethod);
    String query = HelperUtils.encodeValue(String.format(ACQ_METHODS_QUERY, acquisitionMethod));
    String endpoint = String.format(ACQUISITION_METHOD_ENDPOINT + QUERY, query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(acquisitionMethods -> {
        Map<String, AcquisitionMethod> acqMethods = acquisitionMethods.getJsonArray(ACQ_METHODS_NAME).stream()
          .map(obj -> ((JsonObject) obj).mapTo(AcquisitionMethod.class))
          .collect(toMap(AcquisitionMethod::getValue, identity()));

        return Optional.ofNullable(acqMethods.get(acquisitionMethod))
          .map(AcquisitionMethod::getId)
          .orElse(acqMethods.get(DEFAULT_ACQ_METHOD_VALUE).getId());
      })
      .exceptionally(t -> {
        logger.error("Exception looking up acquisition method id with its name: {}", acquisitionMethod, t);
        return null;
      });
  }
}

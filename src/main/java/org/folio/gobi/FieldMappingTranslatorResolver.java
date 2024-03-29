package org.folio.gobi;

import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_ACQUISITION_METHOD_IDS;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_ACQUISITION_UNIT_IDS_BY_ACCOUNT;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_ACQUISITION_UNIT_IDS_BY_NAME;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_CONFIG_ADDRESS;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_CONTRIBUTOR_NAME_TYPE_ID;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_EXPENSE_CLASS_ID;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_FUND_ID;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_LINKED_PACKAGE;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_LOCATION_ID;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_MATERIAL_TYPE_ID;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_ORGANIZATION;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_PREFIX;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_PRODUCT_ID_TYPE;
import static org.folio.rest.jaxrs.model.DataSource.Translation.LOOKUP_SUFFIX;
import static org.folio.rest.jaxrs.model.DataSource.Translation.SEPARATE_ISBN_QUALIFIER;
import static org.folio.rest.jaxrs.model.DataSource.Translation.TO_BOOLEAN;
import static org.folio.rest.jaxrs.model.DataSource.Translation.TO_DATE;
import static org.folio.rest.jaxrs.model.DataSource.Translation.TO_DOUBLE;
import static org.folio.rest.jaxrs.model.DataSource.Translation.TO_INTEGER;
import static org.folio.rest.jaxrs.model.DataSource.Translation.TRUNCATE_ISBN_QUALIFIER;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.DataSource;

public class FieldMappingTranslatorResolver {
  private static final Logger logger = LogManager.getLogger(FieldMappingTranslatorResolver.class);
  private final Map<DataSource.Translation, Function<String, CompletableFuture<?>>> methodTranslatorsMap =
          new EnumMap<>(DataSource.Translation.class);

  public FieldMappingTranslatorResolver(LookupService lookupService) {
    methodTranslatorsMap.put(LOOKUP_CONTRIBUTOR_NAME_TYPE_ID, lookupService::lookupContributorNameTypeId);
    methodTranslatorsMap.put(LOOKUP_EXPENSE_CLASS_ID, lookupService::lookupExpenseClassId);
    methodTranslatorsMap.put(LOOKUP_ACQUISITION_METHOD_IDS, lookupService::lookupAcquisitionMethodId);
    methodTranslatorsMap.put(LOOKUP_ACQUISITION_UNIT_IDS_BY_NAME, lookupService::lookupAcquisitionUnitIdsByName);
    methodTranslatorsMap.put(LOOKUP_ACQUISITION_UNIT_IDS_BY_ACCOUNT, lookupService::lookupAcquisitionUnitIdsByAccount);
    methodTranslatorsMap.put(LOOKUP_LOCATION_ID, lookupService::lookupLocationId);
    methodTranslatorsMap.put(LOOKUP_MATERIAL_TYPE_ID, lookupService::lookupMaterialTypeId);
    methodTranslatorsMap.put(LOOKUP_FUND_ID, lookupService::lookupFundId);

    methodTranslatorsMap.put(LOOKUP_ORGANIZATION, lookupService::lookupOrganization);
    methodTranslatorsMap.put(LOOKUP_PRODUCT_ID_TYPE, lookupService::lookupProductIdType);
    methodTranslatorsMap.put(LOOKUP_CONFIG_ADDRESS, lookupService::lookupConfigAddress);

    methodTranslatorsMap.put(LOOKUP_PREFIX, lookupService::lookupPrefix);
    methodTranslatorsMap.put(LOOKUP_SUFFIX, lookupService::lookupSuffix);

    methodTranslatorsMap.put(LOOKUP_LINKED_PACKAGE, lookupService::lookupLinkedPackage);
    methodTranslatorsMap.put(SEPARATE_ISBN_QUALIFIER, lookupService::separateISBNQualifier);

    methodTranslatorsMap.put(TRUNCATE_ISBN_QUALIFIER, lookupService::truncateISBNQualifier);
    methodTranslatorsMap.put(TO_DATE, Mapper::toDate);

    methodTranslatorsMap.put(TO_DOUBLE, Mapper::toDouble);
    methodTranslatorsMap.put(TO_INTEGER, Mapper::toInteger);
    methodTranslatorsMap.put(TO_BOOLEAN, Mapper::toBoolean);
  }

  public Mapper.Translation<?> resolve(DataSource.Translation translation) {
    Mapper.Translation<?> t = null;
    if (translation != null) {
      t = buildTranslator(translation);
    }
    return t;
  }

  private Mapper.Translation<?> buildTranslator(DataSource.Translation translation) {
    return data -> {
      CompletableFuture translatedValue;
      try {
        translatedValue = Optional.ofNullable(methodTranslatorsMap.get(translation))
                                  .map(translator -> translator.apply(data))
                                  .orElseThrow(() -> new IllegalArgumentException("No such Translation available: " + translation));
        return translatedValue;
      } catch (Exception e) {
        logger.error("Exception in translation time", e);
      }
      return CompletableFuture.completedFuture(null);
    };
  }
}

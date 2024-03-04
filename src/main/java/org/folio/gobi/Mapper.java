package org.folio.gobi;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.folio.gobi.HelperUtils.FUND_CODE_EXPENSE_CLASS_SEPARATOR;
import static org.folio.gobi.HelperUtils.INVALID_ISBN_PRODUCT_ID_TYPE;
import static org.folio.gobi.HelperUtils.extractExpenseClassFromFundCode;
import static org.folio.gobi.HelperUtils.extractFundCode;
import static org.folio.rest.jaxrs.model.Mapping.Field.BILL_TO;
import static org.folio.rest.jaxrs.model.Mapping.Field.LINKED_PACKAGE;
import static org.folio.rest.jaxrs.model.Mapping.Field.PREFIX;
import static org.folio.rest.jaxrs.model.Mapping.Field.SHIP_TO;
import static org.folio.rest.jaxrs.model.Mapping.Field.SUFFIX;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.domain.BindingResult;
import org.folio.isbn.IsbnUtil;
import org.folio.rest.acq.model.Account;
import org.folio.rest.acq.model.AcquisitionMethod;
import org.folio.rest.acq.model.CompositePoLine;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.acq.model.Contributor;
import org.folio.rest.acq.model.Cost;
import org.folio.rest.acq.model.Cost.DiscountType;
import org.folio.rest.acq.model.Details;
import org.folio.rest.acq.model.Eresource;
import org.folio.rest.acq.model.FundDistribution;
import org.folio.rest.acq.model.Location;
import org.folio.rest.acq.model.Ongoing;
import org.folio.rest.acq.model.Organization;
import org.folio.rest.acq.model.Physical;
import org.folio.rest.acq.model.ProductIdentifier;
import org.folio.rest.acq.model.ReferenceNumberItem;
import org.folio.rest.acq.model.Tags;
import org.folio.rest.acq.model.VendorDetail;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.util.UuidUtil;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import io.vertx.core.json.JsonObject;

public class Mapper {

  public static final String VALUE_FOR_FIELD_NOT_FOUND = "Value for field not found";
  private static final Logger logger = LogManager.getLogger(Mapper.class);
  private static final Map<String, Boolean> gobiBooleanType = Map.of("YES", Boolean.TRUE, "NO", Boolean.FALSE);
  private static final Map<String, Boolean> gobiReceivingFlowType = Map.of("SYNCHRONIZED", Boolean.FALSE, "INDEPENDENT", Boolean.TRUE);
  public static final String LOOKUP_ERROR = "LOOKUP_ERROR";

  private final LookupService lookupService;

  public Mapper(LookupService lookupService) {
    this.lookupService = lookupService;
  }

  public CompletableFuture<BindingResult<CompositePurchaseOrder>> map(Map<Mapping.Field, DataSourceResolver> mappings, Document doc) {
    CompletableFuture<BindingResult<CompositePurchaseOrder>> future = new CompletableFuture<>();

    CompositePoLine pol = new CompositePoLine();
    CompositePurchaseOrder compPO = new CompositePurchaseOrder();
    compPO.getCompositePoLines().add(pol);
    BindingResult<CompositePurchaseOrder> bindingResult = new BindingResult<>(compPO);

    List<CompletableFuture<?>> purchaseOrderFutures = new ArrayList<>();
    mapPurchaseOrder(purchaseOrderFutures, mappings, bindingResult, doc);
    mapPurchaseOrderLine(purchaseOrderFutures, mappings, bindingResult, doc);
    mapPurchaseOrderLineStrings(purchaseOrderFutures, mappings, bindingResult, doc);

    CompletableFuture.allOf(purchaseOrderFutures.toArray(new CompletableFuture<?>[0]))
      .thenCompose(v -> mapCompositePOLine(mappings, doc, bindingResult))
      .thenAccept(mappedCompPO -> mapPurchaseOrderWorkflow(bindingResult))
      .thenAccept(mappedCompPO -> future.complete(bindingResult))
      .exceptionally(t -> {
        logger.error("Exception Mapping Composite PO Line fields", t);
        future.completeExceptionally(t);
        return null;
      });

    return future;
  }

  private void mapPurchaseOrderWorkflow(BindingResult<CompositePurchaseOrder> bindingResult) {
    EnumSet<Mapping.Field> pendingSet = EnumSet.of(BILL_TO, SHIP_TO, LINKED_PACKAGE, SUFFIX, PREFIX);
    List<Error> errors = bindingResult.getAllErrors();
    if (CollectionUtils.isNotEmpty(bindingResult.getAllErrors())) {
      boolean isPendingStatus = pendingSet.stream().anyMatch(errors::contains);
      if (isPendingStatus) {
        bindingResult.getResult().setWorkflowStatus(CompositePurchaseOrder.WorkflowStatus.PENDING);
      }
    }
  }

  private CompletableFuture<CompositePurchaseOrder> mapCompositePOLine(Map<Mapping.Field, DataSourceResolver> mappings, Document doc,
    BindingResult<CompositePurchaseOrder> bindingResult) {
    CompletableFuture<CompositePurchaseOrder> future = new CompletableFuture<>();

    Details detail = new Details();
    Cost cost = new Cost();
    Location location = new Location();
    Eresource eresource = new Eresource();
    FundDistribution fundDistribution = new FundDistribution();
    VendorDetail vendorDetail = new VendorDetail();
    Physical physical = new Physical();
    Contributor contributor = new Contributor();
    Tags tags = new Tags();
    AcquisitionMethod acquisitionMethod = new AcquisitionMethod();
    CompositePurchaseOrder compPO = bindingResult.getResult();
    CompositePoLine pol = compPO.getCompositePoLines().get(0);

    List<CompletableFuture<?>> futures = new ArrayList<>();

    mapCost(futures, mappings, cost, doc);
    mapDetail(futures, mappings, detail, doc);
    mapFundDistribution(futures, mappings, fundDistribution, doc);
    mapLocation(futures, mappings, location, doc);
    mapVendorDetail(futures, mappings, vendorDetail, doc);
    mapContributor(futures, mappings, contributor, doc);
    mapVendorDependentFields(futures, mappings, eresource, physical, compPO, doc);
    mapTags(futures, mappings, tags, doc);
    mapAcquisitionMethod(futures, mappings, acquisitionMethod, doc);

    if (pol.getOrderFormat().equals(CompositePoLine.OrderFormat.ELECTRONIC_RESOURCE)) {
      mapEresource(futures, mappings, eresource, doc);
    } else {
      mapPhysical(futures, mappings, physical, doc);
    }
    CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
      .thenAccept(v -> {
        compPO.setTotalItems(location.getQuantity());

        setObjectIfPresent(detail, o -> pol.setDetails((Details) o));
        setObjectIfPresent(cost, o -> pol.setCost((Cost) o));
        if (pol.getOrderFormat().equals(CompositePoLine.OrderFormat.ELECTRONIC_RESOURCE)) {
          setObjectIfPresent(eresource, o -> pol.setEresource((Eresource) o));
        } else {
          setObjectIfPresent(physical, o -> pol.setPhysical((Physical) o));
        }
        setObjectIfPresent(vendorDetail, o -> pol.setVendorDetail((VendorDetail) o));

        setObjectIfPresent(location, o -> {
          List<Location> locations = new ArrayList<>();
          locations.add(location);
          pol.setLocations(locations);
        });
        setObjectIfPresent(contributor, o -> {
          List<Contributor> contributors = new ArrayList<>();
          contributors.add(contributor);
          pol.setContributors(contributors);
        });
        setObjectIfPresent(fundDistribution, o -> {
          if (StringUtils.isNotEmpty(fundDistribution.getFundId())) {
            pol.setFundDistribution(Collections.singletonList(fundDistribution));
          }
        });
        setObjectIfPresent(tags, o -> pol.setTags(tags));
        setObjectIfPresent(acquisitionMethod, o -> pol.setAcquisitionMethod(acquisitionMethod.getId()));
        future.complete(compPO);
      })
      .exceptionally(t -> {
        logger.error("Exception creating Composite PO", t);
        future.completeExceptionally(t);
        return null;
      });

    return future;

  }

  private void mapAcquisitionMethod(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings,
    AcquisitionMethod acquisitionMethodToUpdate, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.ACQUISITION_METHOD))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenApply(String.class::cast)
        .thenCompose(lookupService::lookupAcquisitionMethodId)
        .thenAccept(acquisitionMethodToUpdate::setId)
        .exceptionally(Mapper::logException)));
  }

  /**
   * This method handles all the different fields that are dependent on
   * Organization that is a vendor record to be fetched from the
   * organizations-storage
   *  @param futures
   * @param mappings
   * @param eresource
   * @param physical
   * @param doc
   */
  private void mapVendorDependentFields(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings,
    Eresource eresource, Physical physical, CompositePurchaseOrder compPo, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          if (null != o) {
            Organization organization = (Organization) o;
            compPo.setVendor(organization.getId());
            // Map Receipt dates based on the order format type
            LocalDateTime dt = LocalDateTime.from(new Date().toInstant()
              .atOffset(ZoneOffset.UTC));
            Optional.ofNullable(mappings.get(Mapping.Field.PO_LINE_ORDER_FORMAT))
              .ifPresent(orderformat -> orderformat.resolve(doc)
                .thenAccept(format -> {
                  if ((format.toString())
                    .equalsIgnoreCase(CompositePoLine.OrderFormat.ELECTRONIC_RESOURCE.toString())) {
                    Optional.ofNullable(organization.getExpectedActivationInterval())
                      .ifPresent(activationDue -> {
                        eresource.setActivationDue(activationDue);
                        eresource.setExpectedActivation(Date.from(dt.plusDays(activationDue)
                          .toInstant(ZoneOffset.UTC)));
                      });
                  } else {
                    Optional.ofNullable(organization.getExpectedReceiptInterval())
                      .ifPresent(expectedReceiptInterval -> physical
                        .setExpectedReceiptDate(Date.from(dt.plusDays(expectedReceiptInterval)
                          .toInstant(ZoneOffset.UTC))));
                  }
                })
                .exceptionally(Mapper::logException));

            if (compPo.getCompositePoLines()
              .get(0)
              .getOrderFormat()
              .equals(CompositePoLine.OrderFormat.ELECTRONIC_RESOURCE)) {
              Optional.ofNullable(organization.getExpectedActivationInterval())
                .ifPresent(activationDue -> {
                  eresource.setActivationDue(activationDue);
                  eresource.setExpectedActivation(Date.from(dt.plusDays(activationDue)
                    .toInstant(ZoneOffset.UTC)));
                });
            } else {
              Optional.ofNullable(organization.getExpectedReceiptInterval())
                .ifPresent(expectedReceiptInterval -> physical
                  .setExpectedReceiptDate(Date.from(dt.plusDays(expectedReceiptInterval)
                    .toInstant(ZoneOffset.UTC))));
            }

          }
        })
        .exceptionally(Mapper::logException)));

  }

  private void setObjectIfPresent(Object obj, Consumer<Object> setter) {
    if (!isObjectEmpty(obj)) {
      setter.accept(obj);
    }
  }

  private void mapContributor(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Contributor contributor,
    Document doc) {
    // If contributor name is available, resolve contributor name type (required field) and only then populate contributor details
    Optional.ofNullable(mappings.get(Mapping.Field.CONTRIBUTOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenApply(String.class::cast)
        .thenCompose(name -> {
          // Map contributor name type only if name is available
          if (StringUtils.isNotBlank(name)) {
            return Optional.ofNullable(mappings.get(Mapping.Field.CONTRIBUTOR_NAME_TYPE))
              .map(typeField -> typeField.resolve(doc)
                .thenAccept(o -> Optional.ofNullable(o)
                  .map(Object::toString)
                  .ifPresent(type -> {
                    contributor.setContributor(name);
                    contributor.setContributorNameTypeId(type);
                  }))
                .exceptionally(Mapper::logException))
              .orElse(completedFuture(null));
          }
          return completedFuture(null);
        })
        .exceptionally(Mapper::logException)));
  }

  private void mapPhysical(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Physical physical,
    Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.MATERIAL_SUPPLIER))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          if (o != null) {
            Organization organization = (Organization) o;
            physical.setMaterialSupplier(organization.getId());
          }
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.RECEIPT_DUE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> physical.setReceiptDue((Date) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.VOLUMES))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          List<String> volumes = new ArrayList<>();
          volumes.add((String) o);
          physical.setVolumes(volumes);
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.CREATE_INVENTORY))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> physical.setCreateInventory(Physical.CreateInventory.fromValue((String) o)))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.MATERIAL_TYPE))
    .ifPresent(field -> futures.add(field.resolve(doc)
      .thenAccept(o -> physical.setMaterialType((String) o))
      .exceptionally(Mapper::logException)));
  }

  private void mapOngoing(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Ongoing ongoing,
    Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_IS_SUBSCRIPTION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setIsSubscription((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_INTERVAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setInterval((Integer) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_MANUAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setManualRenewal((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_DATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setRenewalDate((Date) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_REVIEW_PERIOD))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setReviewPeriod((Integer) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_REVIEW_DATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setReviewDate((Date) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_NOTES))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setNotes((String) o))
        .exceptionally(Mapper::logException)));
  }



  private void mapTags(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Tags tags, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.TAGS))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenApply(tagsObject -> splitStringIntoList((String) tagsObject, ","))
        .thenAccept(tags::setTagList)
        .exceptionally(Mapper::logException)));
  }

  private void mapPurchaseOrder(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, BindingResult<CompositePurchaseOrder> bindingResult, Document doc) {
    CompositePurchaseOrder compPo = bindingResult.getResult();
    mapAcquisitionUnits(futures, mappings, compPo, doc);
    Optional.ofNullable(mappings.get(Mapping.Field.ORDER_TYPE))
      .ifPresent(orderTypeField -> futures.add(orderTypeField.resolve(doc)
        .thenApply(o -> {
          compPo.setOrderType(CompositePurchaseOrder.OrderType.fromValue((String) o));
          return CompositePurchaseOrder.OrderType.fromValue((String) o);
        })
        .thenAccept(orderType -> {
          if (orderType == CompositePurchaseOrder.OrderType.ONGOING) {
            Ongoing ongoing = new Ongoing();
            mapOngoing(futures, mappings, ongoing, doc);
            compPo.setOngoing(ongoing);
          }
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.APPROVED))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> compPo.setApproved((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ASSIGNED_TO))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> compPo.setAssignedTo((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.MANUAL_PO))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> compPo.setManualPo((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.NOTES))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          List<String> notes = new ArrayList<>();
          notes.add((String) o);
          compPo.setNotes(notes);
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.RE_ENCUMBER))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> compPo.setReEncumber((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.TOTAL_ESTIMATED_PRICE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> compPo.setTotalEstimatedPrice((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.WORKFLOW_STATUS))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> compPo.setWorkflowStatus(CompositePurchaseOrder.WorkflowStatus.fromValue((String) o)))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(SHIP_TO))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(shipToId -> Optional.ofNullable(shipToId)
                                      .ifPresentOrElse(value -> bindingResult.getResult().setShipTo((String) value),
                                                      () -> addLookupError(SHIP_TO, bindingResult)))
        .exceptionally(ex -> {
          Mapper.logException(ex);
          addLookupError(SHIP_TO, bindingResult);
          return null;
        })));

    Optional.ofNullable(mappings.get(BILL_TO))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(billToId -> Optional.ofNullable(billToId)
                                      .ifPresentOrElse(value -> bindingResult.getResult().setBillTo((String) value),
                                                      () -> addLookupError(BILL_TO, bindingResult)))
        .exceptionally(ex -> {
          Mapper.logException(ex);
          addLookupError(BILL_TO, bindingResult);
          return null;
        })));

    Optional.ofNullable(mappings.get(PREFIX))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(prefixId -> Optional.ofNullable(prefixId)
                                        .ifPresentOrElse(value -> bindingResult.getResult().setPoNumberPrefix((String) value),
                                                        () -> addLookupError(PREFIX, bindingResult)))
        .exceptionally(ex -> {
          Mapper.logException(ex);
          addLookupError(PREFIX, bindingResult);
          return null;
        })));

    Optional.ofNullable(mappings.get(Mapping.Field.SUFFIX))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(suffixId -> Optional.ofNullable(suffixId)
                                        .ifPresentOrElse(value -> bindingResult.getResult().setPoNumberSuffix((String) value),
                                                        () -> addLookupError(Mapping.Field.SUFFIX, bindingResult)))
        .exceptionally(ex -> {
          Mapper.logException(ex);
          addLookupError(Mapping.Field.SUFFIX, bindingResult);
          return null;
        })));
  }

  private void addLookupError(Mapping.Field field, BindingResult<CompositePurchaseOrder> bindingResult) {
    bindingResult.addError(field, new Error().withType(LOOKUP_ERROR).withCode(field.value())
                                             .withMessage(VALUE_FOR_FIELD_NOT_FOUND));
  }

  private void mapAcquisitionUnits(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings,
    CompositePurchaseOrder compPo, Document doc) {

    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          if (o != null) {
            Organization organization = (Organization) o;
            Optional.ofNullable(mappings.get(Mapping.Field.ACQUISITION_UNIT))
              .ifPresent(a ->
                futures.add(a.resolve(doc)
                  .thenAccept(lookupResult -> mapTranslatedAcquisitionsUnit(compPo, organization, lookupResult))
                  .exceptionally(Mapper::logException)));
          }
        }).exceptionally(Mapper::logException)));
  }

  private void mapTranslatedAcquisitionsUnit(CompositePurchaseOrder compPo, Organization organization, Object lookupResult) {
    // map value from translator lookupAcquisitionUnitIdsByName
    if (lookupResult instanceof String && UuidUtil.isUuid((String) lookupResult)) {
      compPo.setAcqUnitIds(Collections.singletonList((String) lookupResult));
    }
    // map value from translator lookupAcquisitionUnitIdsByAccount
    else if (lookupResult instanceof String) {
      List<String> acqCollection = organization.getAccounts().stream()
        .filter(acc -> HelperUtils.normalizeSubAccout(acc.getAccountNo()).equals(HelperUtils.normalizeSubAccout((String) lookupResult)))
        .findFirst()
        .map(Account::getAcqUnitIds)
        .orElse(null);
      compPo.setAcqUnitIds(acqCollection);
    }
  }

  private void mapPurchaseOrderLineStrings(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings,
    BindingResult<CompositePurchaseOrder> bindingResult, Document doc) {
    Optional.ofNullable(bindingResult.getResult().getCompositePoLines().get(0)).ifPresent(pol -> {
      Optional.ofNullable(mappings.get(Mapping.Field.REQUESTER))
        .ifPresent(field -> futures.add(field.resolve(doc)
          .thenAccept(o -> pol.setRequester((String) o))
          .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.TITLE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setTitleOrPackage((String) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.CANCELLATION_RESTRICTION_NOTE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setCancellationRestrictionNote((String) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.DESCRIPTION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setDescription((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.DONOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          if (o != null) {
            Organization organization = (Organization) o;
            pol.setDonorOrganizationIds(Collections.singletonList(organization.getId()));
          }
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.CLAIM_ACTIVE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setClaimingActive((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.CLAIM_INTERVAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setClaimingInterval((Integer) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.SELECTOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setSelector((String) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.SOURCE))
        .ifPresent(field -> futures.add(field.resolve(doc)
          .thenAccept(o -> pol.setSource(CompositePoLine.Source.fromValue((String) o)))
          .exceptionally(Mapper::logException)));
    });
  }


  private void mapPurchaseOrderLine(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings,
    BindingResult<CompositePurchaseOrder> bindingResult, Document doc) {
    Optional.ofNullable(bindingResult.getResult().getCompositePoLines().get(0)).ifPresent(pol -> {
      Optional.ofNullable(mappings.get(Mapping.Field.CANCELLATION_RESTRICTION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setCancellationRestriction((Boolean) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.PO_LINE_ORDER_FORMAT))
        .ifPresent(field -> futures.add(field.resolve(doc)
          .thenAccept(o -> pol.setOrderFormat(CompositePoLine.OrderFormat.fromValue((String) o)))
          .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.COLLECTION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setCollection((Boolean) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.PO_LINE_DESCRIPTION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setPoLineDescription((String) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.PO_LINE_PAYMENT_STATUS))
        .ifPresent(field -> futures.add(field.resolve(doc)
          .thenAccept(o -> pol.setPaymentStatus(CompositePoLine.PaymentStatus.fromValue((String) o)))
          .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.PUBLICATION_DATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setPublicationDate((String) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.PUBLISHER))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setPublisher((String) o))
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.RECEIPT_DATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setReceiptDate((Date) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.RUSH))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setRush((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.TAGS))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
        Tags tags = new Tags();
        List<String> tagList = new ArrayList<>();
        tagList.add((String) o);
        tags.setTagList(tagList);
        pol.setTags(tags);
        })
        .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.PO_LINE_RECEIPT_STATUS))
        .ifPresent(field -> futures.add(field.resolve(doc)
          .thenAccept(o -> pol.setReceiptStatus(CompositePoLine.ReceiptStatus.fromValue((String) o)))
          .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.RECEIVING_WORKFLOW))
        .ifPresent(field -> futures.add(field.resolve(doc)
          .thenAccept(checkinItemsFlag -> mapCheckinItemsFlag(checkinItemsFlag).ifPresent(pol::setCheckinItems))
          .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.PACKAGE_DESIGNATION))
        .ifPresent(field -> futures.add(field.resolve(doc)
          .thenAccept(isPackage -> mapIsPackageToBoolean(isPackage).ifPresent(pol::setIsPackage))
          .exceptionally(Mapper::logException)));

      Optional.ofNullable(mappings.get(Mapping.Field.LINKED_PACKAGE))
        .ifPresent(field -> futures.add(field.resolve(doc)
          .thenAccept(packagePoLineId -> Optional.ofNullable(packagePoLineId)
                                                .ifPresentOrElse(value -> pol.setPackagePoLineId((String) value),
                                                                () -> addLookupError(Mapping.Field.LINKED_PACKAGE, bindingResult)))
          .exceptionally(ex -> {
            Mapper.logException(ex);
            addLookupError(Mapping.Field.LINKED_PACKAGE, bindingResult);
            return null;
          })));
    });
  }

  private Optional<Boolean> mapCheckinItemsFlag(Object checkinItemsFlag) {
    return Optional.ofNullable(checkinItemsFlag)
                    .map(String.class::cast)
                    .map(String::toUpperCase)
                    .map(gobiReceivingFlowType::get);
  }

  private Optional<Boolean> mapIsPackageToBoolean(Object isPackage) {
    return Optional.ofNullable(isPackage).map(String.class::cast)
                             .map(String::toUpperCase)
                             .map(gobiBooleanType::get);
  }

  private void mapCost(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Cost cost, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.CURRENCY))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setCurrency((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.LIST_UNIT_PRICE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setListUnitPrice((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.LIST_UNIT_PRICE_ELECTRONIC))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setListUnitPriceElectronic((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setPoLineEstimatedPrice((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.QUANTITY_ELECTRONIC))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setQuantityElectronic((Integer) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.QUANTITY_PHYSICAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setQuantityPhysical((Integer) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.DISCOUNT))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setDiscount((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.DISCOUNT_TYPE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setDiscountType(DiscountType.fromValue((String) o)))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ADDITIONAL_COST))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setAdditionalCost((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.EXCHANGE_RATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(exchangeRateStr -> cost.setExchangeRate(Double.parseDouble(String.valueOf(exchangeRateStr))))
        .exceptionally(Mapper::logException)));
  }

  private void mapDetail(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Details detail,
    Document doc) {
    // Adding a new entry to product id only if the product ID and product id
    // type are present
    // as both of them are together are mandatory to create an inventory
    // instance
    Optional.ofNullable(mappings.get(Mapping.Field.PRODUCT_ID))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenCompose(o -> {
          ProductIdentifier productId = new ProductIdentifier();
          productId.setProductId(o.toString());
          return Optional.ofNullable(mappings.get(Mapping.Field.PRODUCT_ID_TYPE))
            .map(prodIdType -> resolveProductIdType(prodIdType, doc, productId)
              .thenAccept(typeId -> {
                productId.setProductIdType((String) typeId);
                Optional.ofNullable(mappings.get(Mapping.Field.PRODUCT_QUALIFIER))
                  .ifPresent(qualifierField -> qualifierField.resolve(doc)
                    .thenAccept(qualifier -> productId.setQualifier((String) qualifier)));

                detail.setProductIds(Collections.singletonList(productId));
              })
              .exceptionally(Mapper::logException))
            .orElse(completedFuture(null));
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.RECEIVING_NOTE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> detail.setReceivingNote((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.SUBSCRIPTION_FROM))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> detail.setSubscriptionFrom((Date) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.SUBSCRIPTION_INTERVAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> detail.setSubscriptionInterval((Integer) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.SUBSCRIPTION_TO))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> detail.setSubscriptionTo((Date) o))
        .exceptionally(Mapper::logException)));

  }

  private CompletableFuture<?> resolveProductIdType(DataSourceResolver prodIdType, Document doc, ProductIdentifier productId) {
    if (IsbnUtil.isValid13DigitNumber(productId.getProductId()) || IsbnUtil.isValid10DigitNumber(productId.getProductId())) {
      return prodIdType.resolve(doc);
    } else {
      return lookupService.lookupProductIdType(INVALID_ISBN_PRODUCT_ID_TYPE);
    }
  }

  private void mapEresource(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Eresource eresource,
    Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.ACCESS_PROVIDER))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> Optional.ofNullable(o)
          .ifPresent(organization -> eresource.setAccessProvider(((Organization) o).getId())))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.USER_LIMIT))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> eresource.setUserLimit((Integer) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ACTIVATED))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> eresource.setActivated((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.CREATE_INVENTORY))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> eresource.setCreateInventory(Eresource.CreateInventory.fromValue((String) o)))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.TRIAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> eresource.setTrial((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.MATERIAL_TYPE))
    .ifPresent(field -> futures.add(field.resolve(doc)
      .thenAccept(o -> eresource.setMaterialType((String) o))
      .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.URL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> eresource.setResourceUrl((String) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapFundDistribution(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings,
    FundDistribution fundDistribution, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.FUND_ID))
      .ifPresent(fundIdField -> futures.add(fundIdField.resolve(doc)
        .thenAccept(fundIdObject -> {

          if (StringUtils.isNotEmpty((String) fundIdObject)) {
            fundDistribution.setFundId((String) fundIdObject);

            Optional.ofNullable(mappings.get(Mapping.Field.FUND_CODE))
              .ifPresent(fundCodeField -> futures.add(fundCodeField.resolve(doc)
                .thenCompose(fundCodeObject -> fundCodeResolver(mappings, fundDistribution, doc, (String) fundCodeObject))
                .exceptionally(Mapper::logException)));

            Optional.ofNullable(mappings.get(Mapping.Field.FUND_PERCENTAGE))
              .ifPresent(fundPercentageField -> futures.add(fundPercentageField.resolve(doc)
                .thenAccept(percentage -> fundDistribution.withDistributionType(FundDistribution.DistributionType.PERCENTAGE)
                  .setValue((Double) percentage))
                .exceptionally(Mapper::logException)));

          }
        })
        .exceptionally(Mapper::logException)));
  }

  private CompletableFuture<Void> fundCodeResolver(Map<Mapping.Field, DataSourceResolver> mappings, FundDistribution fundDistribution, Document doc, String fundCode) {

    if (StringUtils.isNotEmpty(fundCode) && fundCode.contains(FUND_CODE_EXPENSE_CLASS_SEPARATOR)) {
      fundDistribution.setCode(extractFundCode(fundCode));
      return lookupService.lookupExpenseClassId(extractExpenseClassFromFundCode(fundCode))
        .thenAccept(fundDistribution::setExpenseClassId)
        .exceptionally(ex -> {
          Mapper.logException(ex);
          return null;
        });
    } else {
      fundDistribution.setCode(fundCode);
      DataSourceResolver expenseClassCode = mappings.get(Mapping.Field.EXPENSE_CLASS);
      if (expenseClassCode != null) {
        return expenseClassCode.resolve(doc)
          .thenAccept(expenseClassIdObj -> fundDistribution.setExpenseClassId((String) expenseClassIdObj))
          .exceptionally(ex -> {
            Mapper.logException(ex);
            return null;
          });
      } else {
        return completedFuture(null);
      }
    }
  }

  private void mapLocation(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Location location,
    Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.LOCATION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> location.setLocationId((String) o))
        .exceptionally(Mapper::logException)));

    // A GOBI order can only be one of the below type per order, hence the total
    // quantity will be the same

    // Also, GOBI doesn't support ordering by location so the quantity in
    // location and cost will always be the same
    Optional.ofNullable(mappings.get(Mapping.Field.QUANTITY_ELECTRONIC))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          location.setQuantityElectronic((Integer) o);
          location.setQuantity((Integer) o);
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.QUANTITY_PHYSICAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          location.setQuantityPhysical((Integer) o);
          location.setQuantity((Integer) o);
        })
        .exceptionally(Mapper::logException)));
  }

  private void mapVendorDetail(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, VendorDetail vendorDetail, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR_INSTRUCTIONS))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> vendorDetail.setInstructions((String) o))
        .exceptionally(Mapper::logException)));

    ReferenceNumberItem referenceNumber = new ReferenceNumberItem()
      .withVendorDetailsSource(ReferenceNumberItem.VendorDetailsSource.ORDER_LINE);

    mapRefTypeNumberPair(futures, mappings, referenceNumber, doc);
    setObjectIfPresent(referenceNumber, o -> {
      List<ReferenceNumberItem> referenceNumbers = new ArrayList<>();
      referenceNumbers.add(referenceNumber);
      vendorDetail.setReferenceNumbers(referenceNumbers);
    });

    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          if (o != null) {
          Organization organization = (Organization) o;
          mapVendorAccount(futures, mappings, organization, vendorDetail, doc);
          }
        })
        .exceptionally(Mapper::logException)));
  }

  private void mapVendorAccount(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings, Organization organization, VendorDetail vendorDetail,
    Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR_ACCOUNT))
      .ifPresent(vendorAccountField -> vendorAccountField.resolve(doc)
        .thenAccept(vendorAccountNo -> Optional.of(organization.getAccounts().stream()
         .map(Account::getAccountNo).collect(Collectors.toList()))
         .ifPresentOrElse(organizationAccountNo -> {
         String normalizeOrgAccountNo = HelperUtils.getVendAccountFromOrgAccountsList((String) vendorAccountNo,organizationAccountNo);
          if (!normalizeOrgAccountNo.isEmpty()) {
              logger.info("AccountNo matched with subAccount received by GOBI");
              futures.add(CompletableFuture.supplyAsync(() -> normalizeOrgAccountNo)
                .thenAccept(vendorDetail::setVendorAccount));
          }
          else {
              futures.add(vendorAccountField.resolve(doc)
              .thenAccept(vendorAccNo -> vendorDetail.setVendorAccount((String) vendorAccNo))
              .exceptionally(Mapper::logException));
          }
         }, () -> futures.add(vendorAccountField.resolve(doc)
            .thenAccept(accountFieldObject -> vendorDetail.setVendorAccount((String) accountFieldObject))
            .exceptionally(Mapper::logException))))
        .exceptionally(Mapper::logException));
}

  private void mapRefTypeNumberPair(List<CompletableFuture<?>> futures, Map<Mapping.Field, DataSourceResolver> mappings,
    ReferenceNumberItem referenceNumber, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR_REF_NO))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          referenceNumber.setRefNumber((String) o);
          Optional.ofNullable(mappings.get(Mapping.Field.VENDOR_REF_NO_TYPE))
            .ifPresent(refType -> futures.add(refType.resolve(doc)
              .thenAccept(numberType ->
                referenceNumber.setRefNumberType(ReferenceNumberItem.RefNumberType.fromValue((String) numberType)))
              .exceptionally(Mapper::logException)));
        })
        .exceptionally(Mapper::logException)));
  }

    public boolean isObjectEmpty(Object instance) {
    return JsonObject.mapFrom(instance).isEmpty();
  }

  public static Void logException(Throwable t) {
    logger.error("Mapper Exception", t);
    return null;
  }

  public static CompletableFuture<Integer> toInteger(String s) {
    Integer val = s != null ? Integer.parseInt(s) : null;
    return CompletableFuture.completedFuture(val);
  }

  public static CompletableFuture<Double> toDouble(String s) {
    Double val = s != null ? Double.parseDouble(s) : null;
    return CompletableFuture.completedFuture(val);
  }

  public static CompletableFuture<Date> toDate(String s) {
    DateTime val = s != null ? DateTime.parse(s) : DateTime.now();
    return CompletableFuture.completedFuture(val.toDate());
  }

  public static CompletableFuture<Boolean> toBoolean(String s) {
    Boolean val = s != null ? Boolean.parseBoolean(s) : null;
    return CompletableFuture.completedFuture(val);
  }

  public static String concat(NodeList nodes) {
    if (nodes != null) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < nodes.getLength(); i++) {
        sb.append(nodes.item(i).getTextContent());
      }
      return sb.length() > 0 ? sb.toString() : null;
    }
    return null;
  }

  public static String multiply(NodeList nodes) {
    if (nodes == null || nodes.getLength() < 1) {
      return null;
    }
    BigDecimal product = new BigDecimal(nodes.item(0).getTextContent());
    for (var i = 1; i < nodes.getLength(); i++) {
      product = product.multiply(new BigDecimal(nodes.item(i).getTextContent()));
    }
    return String.valueOf(product);
  }

  public static List<String> splitStringIntoList(String tags, String delimiter) {
    return Stream.of(tags.split(delimiter))
      .collect(Collectors.toList());
  }

  public interface Translation<T> {
    CompletableFuture<T> apply(String s);
  }

  public interface NodeCombinator {
    String apply(NodeList n);
  }

}

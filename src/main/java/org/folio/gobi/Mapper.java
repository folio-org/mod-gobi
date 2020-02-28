package org.folio.gobi;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.folio.rest.acq.model.Alert;
import org.folio.rest.acq.model.Claim;
import org.folio.rest.acq.model.CompositePoLine;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.acq.model.Contributor;
import org.folio.rest.acq.model.Cost;
import org.folio.rest.acq.model.Cost.DiscountType;
import org.folio.rest.acq.model.Details;
import org.folio.rest.acq.model.Eresource;
import org.folio.rest.acq.model.FundDistribution;
import org.folio.rest.acq.model.License;
import org.folio.rest.acq.model.Location;
import org.folio.rest.acq.model.Ongoing;
import org.folio.rest.acq.model.Organization;
import org.folio.rest.acq.model.Physical;
import org.folio.rest.acq.model.ProductId;
import org.folio.rest.acq.model.ReportingCode;
import org.folio.rest.acq.model.Tags;
import org.folio.rest.acq.model.VendorDetail;
import org.folio.rest.mappings.model.Mapping;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import io.vertx.core.json.JsonObject;
import scala.math.BigDecimal;

public class Mapper {

  private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

  private final Map<Mapping.Field, DataSourceResolver> mappings;

  public Mapper(Map<Mapping.Field, DataSourceResolver> mappings) {
    this.mappings = mappings;
  }

  public CompletableFuture<CompositePurchaseOrder> map(Document doc) {
    CompletableFuture<CompositePurchaseOrder> future = new CompletableFuture<>();

    CompositePoLine pol = new CompositePoLine();
    CompositePurchaseOrder compPO = new CompositePurchaseOrder();

    List<CompletableFuture<?>> purchaseOrderFutures = new ArrayList<>();
    mapPurchaseOrder(purchaseOrderFutures, compPO, doc);
    mapPurchaseOrderLine(purchaseOrderFutures, pol, doc);
    mapPurchaseOrderLineStrings(purchaseOrderFutures, pol, doc);

    CompletableFuture.allOf(purchaseOrderFutures.toArray(new CompletableFuture<?>[0]))
      .thenApply(v -> compPO.getCompositePoLines().add(pol))
      .thenCompose(v -> mapCompositePOLine(doc, compPO))
      .thenAccept(future::complete)
      .exceptionally(t -> {
        logger.error("Exception Mapping Composite PO Line fields", t);
        future.completeExceptionally(t);
        return null;
      });

    return future;
  }

  private CompletableFuture<CompositePurchaseOrder> mapCompositePOLine(Document doc, CompositePurchaseOrder compPO) {
    CompletableFuture<CompositePurchaseOrder> future = new CompletableFuture<>();

    Details detail = new Details();
    Cost cost = new Cost();
    Location location = new Location();
    Eresource eresource = new Eresource();
    FundDistribution fundDistribution = new FundDistribution();
    VendorDetail vendorDetail = new VendorDetail();
    Claim claim = new Claim();
    ProductId productId = new ProductId();
    Physical physical = new Physical();
    Contributor contributor = new Contributor();
    ReportingCode reportingCode = new ReportingCode();
    License license = new License();

    List<CompletableFuture<?>> futures = new ArrayList<>();

    mapCost(futures, cost, doc);
    mapDetail(futures, detail, productId, doc);
    mapFundDistibution(futures, fundDistribution, doc);
    mapLocation(futures, location, doc);
    mapVendorDetail(futures, vendorDetail, doc);
    mapClaims(futures, claim, doc);
    mapContributor(futures, contributor, doc);
    mapReportingCodes(futures, reportingCode, doc);
    mapVendorDependentFields(futures, eresource, physical, compPO, claim, doc);
    mapLicense(futures, license, doc);

    CompositePoLine pol = compPO.getCompositePoLines().get(0);

    if (pol.getOrderFormat().equals(CompositePoLine.OrderFormat.ELECTRONIC_RESOURCE)) {
      mapEresource(futures, eresource, doc);
    } else {
      mapPhysical(futures, physical, doc);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
      .thenAccept(v -> {
        compPO.setTotalItems(location.getQuantity());

        setObjectIfPresent(detail, o -> pol.setDetails((Details) o));
        setObjectIfPresent(cost, o -> pol.setCost((Cost) o));
        setObjectIfPresent(license, o -> eresource.setLicense((License) o));
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
        setObjectIfPresent(reportingCode, o -> {
          List<ReportingCode> reportingCodes = new ArrayList<>();
          reportingCodes.add(reportingCode);
          pol.setReportingCodes(reportingCodes);
        });
        setObjectIfPresent(claim, o -> {
          List<Claim> claims = new ArrayList<>();
          claims.add(claim);
          pol.setClaims(claims);
        });
        setObjectIfPresent(fundDistribution, o -> {
          List<FundDistribution> fundDistributions = new ArrayList<>();
          fundDistributions.add(fundDistribution);
          pol.setFundDistribution(fundDistributions);
        });

        future.complete(compPO);
      })
      .exceptionally(t -> {
        logger.error("Exception creating Composite PO", t);
        future.completeExceptionally(t);
        return null;
      });

    return future;

  }

  /**
   * This method handles all the different fields that are dependent on
   * Organization that is a vendor record to be fetched from the
   * organizations-storage
   *
   * @param futures
   * @param eresource
   * @param physical
   * @param doc
   */
  private void mapVendorDependentFields(List<CompletableFuture<?>> futures, Eresource eresource, Physical physical,
      CompositePurchaseOrder compPo, Claim claim, Document doc) {
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

            claim.setGrace(organization.getClaimingInterval());
            Optional.ofNullable(mappings.get(Mapping.Field.CLAIM_GRACE))
              .ifPresent(grace -> futures.add(grace.resolve(doc)
                .thenAccept(claimgrace -> claim.setGrace((Integer) claimgrace))
                .exceptionally(Mapper::logException)));
          }
        })
        .exceptionally(Mapper::logException)));

  }

  private void setObjectIfPresent(Object obj, Consumer<Object> setter) {
    if (!isObjectEmpty(obj)) {
      setter.accept(obj);
    }
  }

  private void mapReportingCodes(List<CompletableFuture<?>> futures, ReportingCode reportingCode, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.REPORTING_DESCRIPTION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> reportingCode.setDescription((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.REPORTING_CODE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> reportingCode.setCode((String) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapContributor(List<CompletableFuture<?>> futures, Contributor contributor, Document doc) {
    // If contributor name is available, resolve contributor name type (required field) and only then populate contributor details
    Optional.ofNullable(mappings.get(Mapping.Field.CONTRIBUTOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenApply(o -> (String) o)
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

  private void mapPhysical(List<CompletableFuture<?>> futures, Physical physical, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.MATERIAL_SUPPLIER))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> physical.setMaterialSupplier((String) o))
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

  private void mapOngoing(List<CompletableFuture<?>> futures, Ongoing ongoing, Document doc) {
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

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_NOTES))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setNotes((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_DATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setRenewalDate((Date) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_REVIEW_DATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setReviewDate((Date) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ONGOING_REVIEW_PERIOD))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> ongoing.setReviewPeriod((Integer) o))
        .exceptionally(Mapper::logException)));
  }


    private void mapClaims(List<CompletableFuture<?>> futures, Claim claim, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.CLAIMED))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> claim.setClaimed((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.CLAIM_SENT))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> claim.setSent((Date) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapPurchaseOrder(List<CompletableFuture<?>> futures, CompositePurchaseOrder compPo, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.ORDER_TYPE))
      .ifPresent(orderTypeField -> futures.add(orderTypeField.resolve(doc)
        .thenApply(o -> {
          compPo.setOrderType(CompositePurchaseOrder.OrderType.fromValue((String) o));
          return CompositePurchaseOrder.OrderType.fromValue((String) o);
        })
        .thenAccept(orderType -> {
          if (orderType == CompositePurchaseOrder.OrderType.ONGOING) {
            Ongoing ongoing = new Ongoing();
            mapOngoing(futures, ongoing, doc);
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

    Optional.ofNullable(mappings.get(Mapping.Field.DATE_ORDERED))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> compPo.setDateOrdered((Date) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapPurchaseOrderLineStrings(List<CompletableFuture<?>> futures, CompositePoLine pol, Document doc) {
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
        .thenAccept(o -> pol.setDonor((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.SELECTOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setSelector((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.SOURCE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setSource(CompositePoLine.Source.fromValue((String) o)))
        .exceptionally(Mapper::logException)));
  }

  private void mapPurchaseOrderLine(List<CompletableFuture<?>> futures, CompositePoLine pol, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.ACQUISITION_METHOD))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setAcquisitionMethod(CompositePoLine.AcquisitionMethod.fromValue(o.toString())))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ALERTS))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          Alert alert = new Alert();
          alert.setAlert((String) o);
          List<Alert> alerts = new ArrayList<>();
          alerts.add(alert);
          pol.setAlerts(alerts);
        })
        .exceptionally(Mapper::logException)));

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

    Optional.ofNullable(mappings.get(Mapping.Field.PURCHASE_ORDER_ID))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setPurchaseOrderId((String) o))
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
  }

  private void mapCost(List<CompletableFuture<?>> futures, Cost cost, Document doc) {
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
  }

  private void mapDetail(List<CompletableFuture<?>> futures, Details detail, ProductId productId, Document doc) {
    // Adding a new entry to product id only if the product ID and product id
    // type are present
    // as both of them are together are mandatory to create an inventory
    // instance
    Optional.ofNullable(mappings.get(Mapping.Field.PRODUCT_ID))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenCompose(o -> {
          productId.setProductId(o.toString());
          return Optional.ofNullable(mappings.get(Mapping.Field.PRODUCT_ID_TYPE))
            .map(prodIdType -> prodIdType.resolve(doc)
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

  private void mapEresource(List<CompletableFuture<?>> futures, Eresource eresource, Document doc) {
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
  }

  private void mapLicense(List<CompletableFuture<?>> futures, License license, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.LICENSE_CODE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> license.setCode((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.LICENSE_DESCRIPTION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> license.setDescription((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.LICENSE_REFERENCE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> license.setReference((String) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapFundDistibution(List<CompletableFuture<?>> futures, FundDistribution fundDistribution, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.FUND_ID))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> fundDistribution.setFundId((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.FUND_CODE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> fundDistribution.setCode((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.FUND_PERCENTAGE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> fundDistribution.withDistributionType(FundDistribution.DistributionType.PERCENTAGE).setValue((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ENCUMBERANCE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> fundDistribution.setEncumbrance((String) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapLocation(List<CompletableFuture<?>> futures, Location location, Document doc) {
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

  private void mapVendorDetail(List<CompletableFuture<?>> futures, VendorDetail vendorDetail, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR_INSTRUCTIONS))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> vendorDetail.setInstructions((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.NOTE_FROM_VENDOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> vendorDetail.setNoteFromVendor((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR_REF_NO))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          vendorDetail.setRefNumber((String) o);
          Optional.ofNullable(mappings.get(Mapping.Field.VENDOR_REF_NO_TYPE))
            .ifPresent(refType -> futures.add(refType.resolve(doc)
              .thenAccept(numberType -> vendorDetail
                .setRefNumberType(VendorDetail.RefNumberType.fromValue((String) numberType)))
              .exceptionally(Mapper::logException)));
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.VENDOR_ACCOUNT))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> vendorDetail.setVendorAccount((String) o))
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

  public static Object toBoolean(String s) {
    Boolean val = s != null ? Boolean.parseBoolean(s) : null;
    return CompletableFuture.completedFuture(val);
  }

  public static String concat(NodeList nodes) {
    if (nodes != null) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < nodes.getLength(); i++) {
        sb.append(nodes.item(i)
          .getTextContent());
      }
      return sb.length() > 0 ? sb.toString() : null;
    }
    return null;
  }

  public static String multiply(NodeList nodes) {
    if (nodes != null && nodes.getLength() > 1) {
      BigDecimal product = null;
      for (int i = 0; i < nodes.getLength(); i++) {
        if (product == null) {
          product = BigDecimal.exact(nodes.item(i)
            .getTextContent());
        } else {
          product = product.$times(BigDecimal.exact(nodes.item(i)
            .getTextContent()));
        }
      }
      return String.valueOf(product);
    }
    return null;
  }

  public interface Translation<T> {
    CompletableFuture<T> apply(String s);
  }

  public interface NodeCombinator {
    String apply(NodeList n);
  }

}

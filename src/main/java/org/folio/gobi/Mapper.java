package org.folio.gobi;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import org.folio.rest.acq.model.*;
import org.folio.rest.mappings.model.Mapping;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import scala.math.BigDecimal;

public class Mapper {

  private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

  private final Map<Mapping.Field, DataSourceResolver> mappings;

  public Mapper(Map<Mapping.Field, DataSourceResolver> mappings) {
    this.mappings = mappings;
  }

  public CompletableFuture<CompositePurchaseOrder> map(Document doc) {
    CompletableFuture<CompositePurchaseOrder> future = new CompletableFuture<>();

    try {
      CompositePoLine pol = new CompositePoLine();
      List<CompositePoLine> poLines = new ArrayList<>();
      CompositePurchaseOrder compPO = new CompositePurchaseOrder();

      Details detail = new Details();
      Cost cost = new Cost();
      Location location = new Location();
      Eresource eresource = new Eresource();
      FundDistribution fundDistribution = new FundDistribution();
      VendorDetail vendorDetail = new VendorDetail();
      Claim claim = new Claim();
      Renewal renewal = new Renewal();
      ProductId productId = new ProductId();
      Physical physical = new Physical();
      Adjustment adjustment = new Adjustment();
      Source source = new Source();
      Contributor contributor = new Contributor();
      ReportingCode reportingCode = new ReportingCode();

      List<CompletableFuture<?>> futures = new ArrayList<>();
      mapPurchaseOrder(futures, compPO, doc);
      mapPurchaseOrderLine(futures, pol, doc);
      mapPurchaseOrderLineStrings(futures, pol, doc);
      mapCost(futures, cost, doc);
      mapDetail(futures, detail, productId, doc);
      mapEresource(futures, eresource, doc);
      mapFundDistibution(futures, fundDistribution, doc);
      mapLocation(futures, location, doc);
      mapVendorDetail(futures, vendorDetail, doc);
      mapClaims(futures, claim, doc);
      mapRenewal(futures, renewal, doc);
      mapPhysical(futures, physical, doc);
      mapAdjustment(futures, adjustment, doc);
      mapSource(futures, source, doc);
      mapContributor(futures, contributor, doc);
      mapReportingCodes(futures, reportingCode, doc);
      mapVendorDependentFields(futures, eresource, physical, compPO, claim, doc);

      CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[futures.size()]))
        .thenAccept(v -> {
          compPO.setTotalItems(location.getQuantity());

          setObjectIfPresent(adjustment, o -> compPO.setAdjustment((Adjustment) o));
          setObjectIfPresent(detail, o -> pol.setDetails((Details) o));
          setObjectIfPresent(cost, o -> pol.setCost((Cost) o));
          setObjectIfPresent(eresource, o -> pol.setEresource((Eresource) o));
          setObjectIfPresent(vendorDetail, o -> pol.setVendorDetail((VendorDetail) o));
          setObjectIfPresent(renewal, o -> compPO.setRenewal((Renewal) o));
          setObjectIfPresent(physical, o -> pol.setPhysical((Physical) o));
          setObjectIfPresent(source, o -> pol.setSource((Source) o));

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
          poLines.add(pol);

          compPO.setCompositePoLines(poLines);
          future.complete(compPO);
        });
    } catch (Exception e) {
      throw new CompletionException(e);
    }

    return future;
  }

  /**
   * This method handles all the different fields that are dependent on Vendor
   * record to be fetched from the vendors-storage
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
            Vendor vendor = (Vendor) o;
            compPo.setVendor(vendor.getId());
            // Map Receipt dates based on the order format type
            LocalDateTime dt = LocalDateTime.from(new Date().toInstant()
              .atOffset(ZoneOffset.UTC));
            Optional.ofNullable(mappings.get(Mapping.Field.PO_LINE_ORDER_FORMAT))
              .ifPresent(orderformat -> orderformat.resolve(doc)
                .thenAccept(format -> {
                  if ((format.toString())
                    .equalsIgnoreCase(CompositePoLine.OrderFormat.ELECTRONIC_RESOURCE.toString())) {
                    Optional.ofNullable(vendor.getExpectedActivationInterval())
                      .ifPresent(activationDue -> {
                        eresource.setActivationDue(activationDue);
                        eresource.setExpectedActivation(Date.from(dt.plusDays(activationDue)
                          .toInstant(ZoneOffset.UTC)));
                      });
                  } else {
                    Optional.ofNullable(vendor.getExpectedReceiptInterval())
                      .ifPresent(expectedReceiptInterval -> physical
                        .setExpectedReceiptDate(Date.from(dt.plusDays(expectedReceiptInterval)
                          .toInstant(ZoneOffset.UTC))));
                  }
                })
                .exceptionally(Mapper::logException));

            claim.setGrace(vendor.getClaimingInterval());
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
    Optional.ofNullable(mappings.get(Mapping.Field.CONTRIBUTOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> contributor.setContributor((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.CONTRIBUTOR_TYPE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> contributor.setContributorType((String) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapSource(List<CompletableFuture<?>> futures, Source source, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.SOURCE_CODE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> source.setCode((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.SOURCE_DESCRIPTION))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> source.setDescription((String) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapAdjustment(List<CompletableFuture<?>> futures, Adjustment adjustment, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.CREDIT))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> adjustment.setCredit((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.DISCOUNT))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> adjustment.setDiscount((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.INSURANCE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> adjustment.setInsurance((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.OVERHEAD))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> adjustment.setOverhead((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.SHIPMENT))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> adjustment.setShipment((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.TAX_1))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> adjustment.setTax1((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.TAX_2))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> adjustment.setTax2((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.USE_PRORATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> adjustment.setUseProRate((Boolean) o))
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
  }

  private void mapRenewal(List<CompletableFuture<?>> futures, Renewal renewal, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.RENEWAL_CYCLE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> renewal.setCycle(Renewal.Cycle.fromValue((String) o)))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.RENEWAL_INTERVAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> renewal.setInterval((Integer) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.RENEWAL_MANUAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> renewal.setManualRenewal((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.RENEWAL_DATE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> renewal.setRenewalDate((Date) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.RENEWAL_REVIEW_PERIOD))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> renewal.setReviewPeriod((Integer) o))
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
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> compPo.setOrderType(CompositePurchaseOrder.OrderType.fromValue((String) o)))
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
  }

  private void mapPurchaseOrderLineStrings(List<CompletableFuture<?>> futures, CompositePoLine pol, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.REQUESTER))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setRequester((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.TITLE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setTitle((String) o))
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

    Optional.ofNullable(mappings.get(Mapping.Field.OWNER))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setOwner((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.SELECTOR))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setSelector((String) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapPurchaseOrderLine(List<CompletableFuture<?>> futures, CompositePoLine pol, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.ACQUISITION_METHOD))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> pol.setAcquisitionMethod(CompositePoLine.AcquisitionMethod.fromValue(o.toString())))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.ALERT))
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
          List<String> tags = new ArrayList<>();
          tags.add((String) o);
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

    Optional.ofNullable(mappings.get(Mapping.Field.LIST_PRICE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setListPrice((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setPoLineEstimatedPrice((Double) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.QUANTITY_ORDERED_ELECTRONIC))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setQuantityElectronic((Integer) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.QUANTITY_ORDERED_PHYSICAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> cost.setQuantityPhysical((Integer) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapDetail(List<CompletableFuture<?>> futures, Details detail, ProductId productId, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.MATERIAL_TYPE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> detail.setMaterialTypes((List<String>) o))
        .exceptionally(Mapper::logException)));

    // Adding a new entry to product id only if the product ID and product id
    // type are present
    // as both of them are together are mandatory to create an inventory
    // instance
    Optional.ofNullable(mappings.get(Mapping.Field.PRODUCT_ID))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          productId.setProductId(o.toString());
          Optional.ofNullable(mappings.get(Mapping.Field.PRODUCT_ID_TYPE))
            .ifPresent(prodidtype -> prodidtype.resolve(doc)
              .thenAccept(
                  idType -> {
                    productId.setProductIdType(ProductId.ProductIdType.fromValue((String) idType));
                    List<ProductId> ids = new ArrayList<>();
                    ids.add(productId);
                    detail.setProductIds(ids);
                  }));
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
          .ifPresent(vendor -> eresource.setAccessProvider(((Vendor) o).getId())))
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
        .thenAccept(o -> eresource.setCreateInventory((Boolean) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.LICENSE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> eresource.setLicense((String) o))
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.TRIAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> eresource.setTrial((Boolean) o))
        .exceptionally(Mapper::logException)));
  }

  private void mapFundDistibution(List<CompletableFuture<?>> futures, FundDistribution fundDistribution, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.FUND_CODE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> fundDistribution.setCode((String) o))
        .exceptionally(Mapper::logException)));
    Optional.ofNullable(mappings.get(Mapping.Field.FUND_PERCENTAGE))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> fundDistribution.setPercentage((Double) o))
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
    Optional.ofNullable(mappings.get(Mapping.Field.QUANTITY_ORDERED_ELECTRONIC))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          location.setQuantityElectronic((Integer) o);
          location.setQuantity((Integer) o);
        })
        .exceptionally(Mapper::logException)));

    Optional.ofNullable(mappings.get(Mapping.Field.QUANTITY_ORDERED_PHYSICAL))
      .ifPresent(field -> futures.add(field.resolve(doc)
        .thenAccept(o -> {
          location.setQuantityPhysical((Integer) o);
          location.setQuantity((Integer) o);
        })
        .exceptionally(Mapper::logException)));
  }

  private void mapVendorDetail(List<CompletableFuture<?>> futures, VendorDetail vendorDetail, Document doc) {
    Optional.ofNullable(mappings.get(Mapping.Field.INSTRUCTIONS))
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
    for (Field f : instance.getClass()
      .getDeclaredFields()) {
      f.setAccessible(true);
      try {
        if (f.get(instance) != null)
          return false;
      } catch (IllegalArgumentException e) {
        logger.error("Unable to determine Object", e);
      } catch (IllegalAccessException e) {
        logger.error("Unable to access Object", e);
      }
    }
    return true;
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

  public static interface Translation<T> {
    public CompletableFuture<T> apply(String s);
  }

  public static interface NodeCombinator {
    public String apply(NodeList n);
  }

}

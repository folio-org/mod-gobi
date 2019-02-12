package org.folio.gobi;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
      mapVendorDependentFields(futures,eresource,physical,doc, compPO);

      CompletableFuture
          .allOf(futures.toArray(new CompletableFuture<?>[futures.size()]))
          .thenAccept(v -> {
            List<ProductId> ids = new ArrayList<>();
            ids.add(productId);
            detail.setProductIds(ids);
            compPO.setTotalItems(location.getQuantity());

            setObjectIfPresent(adjustment, o -> compPO.setAdjustment((Adjustment) o));
            setObjectIfPresent(detail, o -> pol.setDetails((Details) o));
            setObjectIfPresent(detail, o -> pol.setDetails((Details) o));
            setObjectIfPresent(cost, o -> pol.setCost((Cost) o));
            setObjectIfPresent(location, o -> pol.setLocation((Location) o));
            setObjectIfPresent(eresource, o -> pol.setEresource((Eresource) o));
            setObjectIfPresent(vendorDetail, o -> pol.setVendorDetail((VendorDetail) o));
            setObjectIfPresent(renewal, o -> compPO.setRenewal((Renewal) o));
            setObjectIfPresent(physical, o -> pol.setPhysical((Physical) o));
            setObjectIfPresent(source, o -> pol.setSource((Source) o));

            setObjectIfPresent(contributor,o-> {
               List<Contributor> contributors = new ArrayList<>();
               contributors.add(contributor);
               pol.setContributors(contributors);
            });
            setObjectIfPresent(reportingCode,o-> {
               List<ReportingCode> reportingCodes = new ArrayList<>();
               reportingCodes.add(reportingCode);
               pol.setReportingCodes(reportingCodes);
            });
            setObjectIfPresent(claim,o-> {
               List<Claim> claims = new ArrayList<>();
               claims.add(claim);
               pol.setClaims(claims);
            });
            setObjectIfPresent(fundDistribution,o-> {
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
   * This method handles all the different fields that are dependent on Vendor record to be fetched from the vendors-storage
   *
   * @param futures
   * @param eresource
   * @param physical
   * @param doc
   */
  private void mapVendorDependentFields(List<CompletableFuture<?>> futures, Eresource eresource, Physical physical,
      Document doc, CompositePurchaseOrder compPo) {
    if (mappings.containsKey(Mapping.Field.VENDOR)) {
      futures.add(mappings.get(Mapping.Field.VENDOR)
          .resolve(doc)
          .thenAccept(o -> {
            Vendor vend = (Vendor) o;
            compPo.setVendor(vend.getId());
            LocalDateTime dt = LocalDateTime.from(new Date().toInstant().atOffset(ZoneOffset.UTC));
            mappings.get(Mapping.Field.PO_LINE_ORDER_FORMAT)
            .resolve(doc)
            .thenAccept(format->{
              if(((String) format).equalsIgnoreCase("Electronic Resource")){
                Integer activationDue = vend.getExpectedActivationInterval();
                eresource.setActivationDue(activationDue);
                eresource.setExpectedActivation(Date.from(dt.plusDays(activationDue).toInstant(ZoneOffset.UTC)));
              }else {
                Integer expectedReceiptInterval = vend.getExpectedReceiptInterval();
                physical.setExpectedReceiptDate(Date.from(dt.plusDays(expectedReceiptInterval).toInstant(ZoneOffset.UTC)));
              }
            })
            .exceptionally(Mapper::logException);
          })
          .exceptionally(Mapper::logException));
    }

  }

  private void setObjectIfPresent(Object obj, Consumer<Object> setter) {
    if(!isObjectEmpty(obj)) {
      setter.accept(obj);
    }
  }

  private void mapReportingCodes(List<CompletableFuture<?>> futures,ReportingCode reportingCode, Document doc) {
    if (mappings.containsKey(Mapping.Field.REPORTING_DESCRIPTION)) {
      futures.add(mappings.get(Mapping.Field.REPORTING_DESCRIPTION)
          .resolve(doc)
          .thenAccept(o -> reportingCode.setDescription((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.REPORTING_CODE)) {
      futures.add(mappings.get(Mapping.Field.REPORTING_CODE)
          .resolve(doc)
          .thenAccept(o -> reportingCode.setCode((String) o))
          .exceptionally(Mapper::logException));
    }
  }

  private void mapContributor(List<CompletableFuture<?>> futures,Contributor contributor, Document doc) {
    if (mappings.containsKey(Mapping.Field.CONTRIBUTOR)) {
      futures.add(mappings.get(Mapping.Field.CONTRIBUTOR)
          .resolve(doc)
          .thenAccept(o -> contributor.setContributor((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.CONTRIBUTOR_TYPE)) {
      futures.add(mappings.get(Mapping.Field.CONTRIBUTOR_TYPE)
          .resolve(doc)
          .thenAccept(o -> contributor.setContributorType((String) o))
          .exceptionally(Mapper::logException));
    }
  }

  private void mapSource(List<CompletableFuture<?>> futures, Source source,Document doc) {
    if (mappings.containsKey(Mapping.Field.SOURCE_CODE)) {
      futures.add(mappings.get(Mapping.Field.SOURCE_CODE)
          .resolve(doc)
          .thenAccept(o -> source.setCode((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.SOURCE_DESCRIPTION)) {
      futures.add(mappings.get(Mapping.Field.SOURCE_DESCRIPTION)
          .resolve(doc)
          .thenAccept(o -> source.setDescription((String) o))
          .exceptionally(Mapper::logException));
    }
  }

  private void mapAdjustment(List<CompletableFuture<?>> futures,Adjustment adjustment, Document doc) {
    if (mappings.containsKey(Mapping.Field.CREDIT)) {
      futures.add(mappings.get(Mapping.Field.CREDIT)
          .resolve(doc)
          .thenAccept(o -> adjustment.setCredit((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.DISCOUNT)) {
      futures.add(mappings.get(Mapping.Field.DISCOUNT)
          .resolve(doc)
          .thenAccept(o -> adjustment.setDiscount((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.INSURANCE)) {
      futures.add(mappings.get(Mapping.Field.INSURANCE)
          .resolve(doc)
          .thenAccept(o -> adjustment.setInsurance((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.OVERHEAD)) {
      futures.add(mappings.get(Mapping.Field.OVERHEAD)
          .resolve(doc)
          .thenAccept(o -> adjustment.setOverhead((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.SHIPMENT)) {
      futures.add(mappings.get(Mapping.Field.SHIPMENT)
          .resolve(doc)
          .thenAccept(o -> adjustment.setShipment((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.TAX_1)) {
      futures.add(mappings.get(Mapping.Field.TAX_1)
          .resolve(doc)
          .thenAccept(o -> adjustment.setTax1((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.TAX_2)) {
      futures.add(mappings.get(Mapping.Field.TAX_2)
          .resolve(doc)
          .thenAccept(o -> adjustment.setTax2((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.USE_PRORATE)) {
      futures.add(mappings.get(Mapping.Field.USE_PRORATE)
          .resolve(doc)
          .thenAccept(o -> adjustment.setUseProRate((Boolean) o))
          .exceptionally(Mapper::logException));
    }
  }

  @SuppressWarnings("unchecked")
  private void mapPhysical(List<CompletableFuture<?>> futures,Physical physical, Document doc) {

    if (mappings.containsKey(Mapping.Field.MATERIAL_SUPPLIER)) {
      futures.add(mappings.get(Mapping.Field.MATERIAL_SUPPLIER)
          .resolve(doc)
          .thenAccept(o -> physical.setMaterialSupplier((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.RECEIPT_DUE)) {
      futures.add(mappings.get(Mapping.Field.RECEIPT_DUE)
          .resolve(doc)
          .thenAccept(o -> physical.setReceiptDue((Date) o))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.VOLUMES)) {
      futures.add(mappings.get(Mapping.Field.VOLUMES)
          .resolve(doc)
          .thenAccept(o -> physical.setVolumes((List<String>) o))
          .exceptionally(Mapper::logException));
    }
  }

  private void mapRenewal(List<CompletableFuture<?>> futures, Renewal renewal, Document doc) {
    if (mappings.containsKey(Mapping.Field.RENEWAL_CYCLE)) {
      futures.add(mappings.get(Mapping.Field.RENEWAL_CYCLE)
          .resolve(doc)
          .thenAccept(o -> renewal.setCycle(Renewal.Cycle.fromValue((String) o)))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.RENEWAL_INTERVAL)) {
      futures.add(mappings.get(Mapping.Field.RENEWAL_INTERVAL)
          .resolve(doc)
          .thenAccept(o -> renewal.setInterval((Integer) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.RENEWAL_MANUAL)) {
      futures.add(mappings.get(Mapping.Field.RENEWAL_MANUAL)
          .resolve(doc)
          .thenAccept(o -> renewal.setManualRenewal((Boolean) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.RENEWAL_DATE)) {
      futures.add(mappings.get(Mapping.Field.RENEWAL_DATE)
          .resolve(doc)
          .thenAccept(o -> renewal.setRenewalDate((Date) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.RENEWAL_REVIEW_PERIOD)) {
      futures.add(mappings.get(Mapping.Field.RENEWAL_REVIEW_PERIOD)
          .resolve(doc)
          .thenAccept(o -> renewal.setReviewPeriod((Integer) o))
          .exceptionally(Mapper::logException));
    }
  }

  private void mapClaims(List<CompletableFuture<?>> futures, Claim claim, Document doc) {
    if (mappings.containsKey(Mapping.Field.CLAIMED)) {
      futures.add(mappings.get(Mapping.Field.CLAIMED)
          .resolve(doc)
          .thenAccept(o -> claim.setClaimed((Boolean) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.CLAIM_GRACE)) {
      futures.add(mappings.get(Mapping.Field.CLAIM_GRACE)
          .resolve(doc)
          .thenAccept(o -> claim.setGrace((Integer) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.CLAIM_SENT)) {
      futures.add(mappings.get(Mapping.Field.CLAIM_SENT)
          .resolve(doc)
          .thenAccept(o -> claim.setSent((Date) o))
          .exceptionally(Mapper::logException));
    }
  }
  

  private void mapPurchaseOrder(List<CompletableFuture<?>> futures, CompositePurchaseOrder compPo, Document doc) {
    if (mappings.containsKey(Mapping.Field.ORDER_TYPE)) {
      futures.add(mappings.get(Mapping.Field.ORDER_TYPE)
          .resolve(doc)
          .thenAccept(o -> compPo.setOrderType(
              CompositePurchaseOrder.OrderType.fromValue((String) o)))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.APPROVED)) {
      futures.add(mappings.get(Mapping.Field.APPROVED)
          .resolve(doc)
          .thenAccept(o -> compPo.setApproved((Boolean) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.ASSIGNED_TO)) {
      futures.add(mappings.get(Mapping.Field.ASSIGNED_TO)
          .resolve(doc)
          .thenAccept(o -> compPo.setAssignedTo((String) o))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.MANUAL_PO)) {
      futures.add(mappings.get(Mapping.Field.MANUAL_PO)
          .resolve(doc)
          .thenAccept(o -> compPo.setManualPo((Boolean) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.NOTES)) {
      futures
          .add(mappings.get(Mapping.Field.NOTES)
         .resolve(doc)
         .thenAccept(o -> {
            List<String> notes = new ArrayList<>();
            notes.add((String) o);
            compPo.setNotes(notes);})
         .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.RE_ENCUMBER)) {
      futures.add(mappings.get(Mapping.Field.RE_ENCUMBER)
          .resolve(doc)
          .thenAccept(o -> compPo.setReEncumber((Boolean) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.TOTAL_ESTIMATED_PRICE)) {
      futures.add(mappings.get(Mapping.Field.TOTAL_ESTIMATED_PRICE)
          .resolve(doc)
          .thenAccept(o -> compPo.setTotalEstimatedPrice((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.WORKFLOW_STATUS)) {
      futures.add(mappings.get(Mapping.Field.WORKFLOW_STATUS)
          .resolve(doc)
          .thenAccept(o -> compPo.setWorkflowStatus(
              CompositePurchaseOrder.WorkflowStatus.fromValue((String) o)))
          .exceptionally(Mapper::logException));
    }
  }

  private void mapPurchaseOrderLineStrings(List<CompletableFuture<?>> futures,CompositePoLine pol, Document doc) {
    if (mappings.containsKey(Mapping.Field.REQUESTER)) {
      futures.add(mappings.get(Mapping.Field.REQUESTER)
          .resolve(doc)
          .thenAccept(o -> pol.setRequester((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.TITLE)) {
      futures.add(mappings.get(Mapping.Field.TITLE)
          .resolve(doc)
          .thenAccept(o -> pol.setTitle((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.CANCELLATION_RESTRICTION_NOTE)) {
      futures.add(
          mappings.get(Mapping.Field.CANCELLATION_RESTRICTION_NOTE)
          .resolve(doc)
          .thenAccept(o -> pol.setCancellationRestrictionNote((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.DESCRIPTION)) {
      futures.add(mappings.get(Mapping.Field.DESCRIPTION)
          .resolve(doc)
          .thenAccept(o -> pol.setDescription((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.DONOR)) {
      futures.add(mappings.get(Mapping.Field.DONOR)
          .resolve(doc)
          .thenAccept(o -> pol.setDonor((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.OWNER)) {
      futures.add(mappings.get(Mapping.Field.OWNER)
          .resolve(doc)
          .thenAccept(o -> pol.setOwner((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.SELECTOR)) {
      futures.add(mappings.get(Mapping.Field.SELECTOR)
          .resolve(doc)
          .thenAccept(o -> pol.setSelector((String) o))
          .exceptionally(Mapper::logException));
    }

  }

  private void mapPurchaseOrderLine(List<CompletableFuture<?>> futures,CompositePoLine pol, Document doc) {
    if (mappings.containsKey(Mapping.Field.ACQUISITION_METHOD)) {
      futures.add(mappings.get(Mapping.Field.ACQUISITION_METHOD)
          .resolve(doc)
          .thenAccept(o -> pol.setAcquisitionMethod(
              CompositePoLine.AcquisitionMethod.fromValue(o.toString())))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.ALERT)) {
      futures
          .add(mappings.get(Mapping.Field.ALERT).resolve(doc)
          .thenAccept(o -> {
            Alert alert = new Alert();
            alert.setAlert((String) o);
            List<Alert> alerts = new ArrayList<>();
            alerts.add(alert);
            pol.setAlerts(alerts);
          })
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.CANCELLATION_RESTRICTION)) {
      futures
          .add(mappings.get(Mapping.Field.CANCELLATION_RESTRICTION).resolve(doc)
              .thenAccept(o -> pol.setCancellationRestriction((Boolean) o))
              .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.PO_LINE_ORDER_FORMAT)) {
      futures.add(mappings.get(Mapping.Field.PO_LINE_ORDER_FORMAT)
          .resolve(doc)
          .thenAccept(o -> pol.setOrderFormat(
              CompositePoLine.OrderFormat.fromValue((String) o)))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.COLLECTION)) {
      futures.add(mappings.get(Mapping.Field.COLLECTION).resolve(doc)
          .thenAccept(o -> pol.setCollection((Boolean) o))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.PO_LINE_DESCRIPTION)) {
      futures.add(mappings.get(Mapping.Field.PO_LINE_DESCRIPTION)
          .resolve(doc)
          .thenAccept(o -> pol.setPoLineDescription((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.PO_LINE_PAYMENT_STATUS)) {
      futures
          .add(mappings.get(Mapping.Field.PO_LINE_PAYMENT_STATUS)
          .resolve(doc)
          .thenAccept(o -> pol.setPaymentStatus(
                  CompositePoLine.PaymentStatus.fromValue((String) o)))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.PUBLICATION_DATE)) {
      futures.add(mappings.get(Mapping.Field.PUBLICATION_DATE)
          .resolve(doc)
          .thenAccept(o -> pol.setPublicationDate((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.PUBLISHER)) {
      futures.add(mappings.get(Mapping.Field.PUBLISHER)
          .resolve(doc)
          .thenAccept(o -> pol.setPublisher((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.PURCHASE_ORDER_ID)) {
      futures.add(mappings.get(Mapping.Field.PURCHASE_ORDER_ID)
          .resolve(doc)
          .thenAccept(o -> pol.setPurchaseOrderId((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.RECEIPT_DATE)) {
      futures.add(mappings.get(Mapping.Field.RECEIPT_DATE)
          .resolve(doc)
          .thenAccept(o -> pol.setReceiptDate((Date) o))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.RUSH)) {
      futures.add(mappings.get(Mapping.Field.RUSH)
          .resolve(doc)
          .thenAccept(o -> pol.setRush((Boolean) o))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.TAGS)) {
      futures
          .add(mappings.get(Mapping.Field.TAGS)
          .resolve(doc)
          .thenAccept(o -> {
            List<String> tags = new ArrayList<>();
            tags.add((String) o);
            pol.setTags(tags);
          }).exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.PO_LINE_RECEIPT_STATUS)) {
      futures
          .add(mappings.get(Mapping.Field.PO_LINE_RECEIPT_STATUS)
              .resolve(doc)
              .thenAccept(o -> pol.setReceiptStatus(
                  CompositePoLine.ReceiptStatus.fromValue((String) o)))
              .exceptionally(Mapper::logException));
    }
  }

  private void mapCost(List<CompletableFuture<?>> futures, Cost cost,Document doc) {
    if (mappings.containsKey(Mapping.Field.CURRENCY)) {
      futures.add(mappings.get(Mapping.Field.CURRENCY)
          .resolve(doc)
          .thenAccept(o -> cost.setCurrency((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.LIST_PRICE)) {
      futures.add(mappings.get(Mapping.Field.LIST_PRICE)
          .resolve(doc)
          .thenAccept(o -> cost.setListPrice((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.PO_LINE_ESTIMATED_PRICE)) {
      futures
          .add(mappings.get(Mapping.Field.PO_LINE_ESTIMATED_PRICE)
              .resolve(doc)
              .thenAccept(o -> cost.setPoLineEstimatedPrice((Double) o))
              .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.QUANTITY_ORDERED_ELECTRONIC)) {
      futures.add(mappings.get(Mapping.Field.QUANTITY_ORDERED_ELECTRONIC)
          .resolve(doc)
          .thenAccept(o -> cost.setQuantityElectronic((Integer) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.QUANTITY_ORDERED_PHYSICAL)) {
      futures.add(mappings.get(Mapping.Field.QUANTITY_ORDERED_PHYSICAL)
          .resolve(doc)
          .thenAccept(o -> cost.setQuantityPhysical((Integer) o))
          .exceptionally(Mapper::logException));
    }
  }

  @SuppressWarnings("unchecked")
  private void mapDetail(List<CompletableFuture<?>> futures, Details detail,ProductId productId, Document doc) {

    if (mappings.containsKey(Mapping.Field.MATERIAL_TYPE)) {
      futures.add(mappings.get(Mapping.Field.MATERIAL_TYPE)
          .resolve(doc)
          .thenAccept(o -> detail.setMaterialTypes((List<String>) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.PRODUCT_ID_TYPE)) {
      futures.add(mappings.get(Mapping.Field.PRODUCT_ID_TYPE)
          .resolve(doc)
          .thenAccept(o -> productId
              .setProductIdType(ProductId.ProductIdType.fromValue((String) o)))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.PRODUCT_ID)) {
      futures.add(
          mappings.get(Mapping.Field.PRODUCT_ID)
          .resolve(doc)
          .thenAccept(o -> productId.setProductId(o.toString()))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.RECEIVING_NOTE)) {
      futures.add(mappings.get(Mapping.Field.RECEIVING_NOTE)
          .resolve(doc)
          .thenAccept(o -> detail.setReceivingNote((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.SUBSCRIPTION_FROM)) {
      futures.add(mappings.get(Mapping.Field.SUBSCRIPTION_FROM)
          .resolve(doc)
          .thenAccept(o -> detail.setSubscriptionFrom((Date) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.SUBSCRIPTION_INTERVAL)) {
      futures.add(mappings.get(Mapping.Field.SUBSCRIPTION_INTERVAL)
          .resolve(doc)
          .thenAccept(o -> detail.setSubscriptionInterval((Integer) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.SUBSCRIPTION_TO)) {
      futures.add(mappings.get(Mapping.Field.SUBSCRIPTION_TO)
          .resolve(doc)
          .thenAccept(o -> detail.setSubscriptionTo((Date) o))
          .exceptionally(Mapper::logException));
    }

  }

  private void mapEresource(List<CompletableFuture<?>> futures,Eresource eresource, Document doc) {
    if (mappings.containsKey(Mapping.Field.ACCESS_PROVIDER)) {
      futures.add(mappings.get(Mapping.Field.ACCESS_PROVIDER)
          .resolve(doc)
          .thenAccept(o -> eresource.setAccessProvider(((Vendor) o).getId()))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.USER_LIMIT)) {
      futures.add(mappings.get(Mapping.Field.USER_LIMIT)
          .resolve(doc)
          .thenAccept(o -> eresource.setUserLimit((Integer) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.ACTIVATED)) {
      futures.add(mappings.get(Mapping.Field.ACTIVATED)
          .resolve(doc)
          .thenAccept(o -> eresource.setActivated((Boolean) o))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.CREATE_INVENTORY)) {
      futures.add(mappings.get(Mapping.Field.CREATE_INVENTORY)
          .resolve(doc)
          .thenAccept(o -> eresource.setCreateInventory((Boolean) o))
          .exceptionally(Mapper::logException));
    }

    if (mappings.containsKey(Mapping.Field.LICENSE)) {
      futures.add(mappings.get(Mapping.Field.LICENSE)
          .resolve(doc)
          .thenAccept(o -> eresource.setLicense((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.TRIAL)) {
      futures.add(mappings.get(Mapping.Field.TRIAL)
          .resolve(doc)
          .thenAccept(o -> eresource.setTrial((Boolean) o))
          .exceptionally(Mapper::logException));
    }
  }

  private void mapFundDistibution(List<CompletableFuture<?>> futures, FundDistribution fundDistribution, Document doc) {
    if (mappings.containsKey(Mapping.Field.FUND_CODE)) {
      futures.add(mappings.get(Mapping.Field.FUND_CODE)
          .resolve(doc)
          .thenAccept(o -> fundDistribution.setCode((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.FUND_PERCENTAGE)) {
      futures.add(mappings.get(Mapping.Field.FUND_PERCENTAGE)
          .resolve(doc)
          .thenAccept(o -> fundDistribution.setPercentage((Double) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.ENCUMBERANCE)) {
      futures.add(mappings.get(Mapping.Field.ENCUMBERANCE)
          .resolve(doc)
          .thenAccept(o -> fundDistribution.setEncumbrance((String) o))
          .exceptionally(Mapper::logException));
    }
  }

  private void mapLocation(List<CompletableFuture<?>> futures, Location location, Document doc) {

    if (mappings.containsKey(Mapping.Field.LOCATION)) {
      futures.add(mappings.get(Mapping.Field.LOCATION)
          .resolve(doc)
          .thenAccept(o -> location.setLocationId((String) o))
          .exceptionally(Mapper::logException));
    }
    // A GOBI order can only be one of the below type per order
    if (mappings.containsKey(Mapping.Field.QUANTITY_ORDERED_ELECTRONIC)) {
      futures.add(
          mappings.get(Mapping.Field.QUANTITY_ORDERED_ELECTRONIC)
          .resolve(doc)
          .thenAccept(o -> {
            location.setQuantityElectronic((Integer) o);
            location.setQuantity((Integer) o);
          })
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.QUANTITY_ORDERED_PHYSICAL)) {
      futures.add(
          mappings.get(Mapping.Field.QUANTITY_ORDERED_PHYSICAL)
          .resolve(doc)
              .thenAccept(o -> {
                location.setQuantityPhysical((Integer) o);
                location.setQuantity((Integer) o);
              })
              .exceptionally(Mapper::logException));
    }
  }

  private void mapVendorDetail(List<CompletableFuture<?>> futures, VendorDetail vendorDetail, Document doc) {
    if (mappings.containsKey(Mapping.Field.INSTRUCTIONS)) {
      futures.add(mappings.get(Mapping.Field.INSTRUCTIONS)
          .resolve(doc)
          .thenAccept(o -> vendorDetail.setInstructions((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.NOTE_FROM_VENDOR)) {
      futures.add(mappings.get(Mapping.Field.NOTE_FROM_VENDOR)
          .resolve(doc)
          .thenAccept(o -> vendorDetail.setNoteFromVendor((String) o))
          .exceptionally(Mapper::logException));
    }
    if (mappings.containsKey(Mapping.Field.VENDOR_REF_NO)) {
      futures.add(mappings.get(Mapping.Field.VENDOR_REF_NO)
          .resolve(doc)
          .thenAccept(o -> vendorDetail.setRefNumber((String) o))
          .exceptionally(Mapper::logException));
      if (mappings.containsKey(Mapping.Field.VENDOR_REF_NO_TYPE)) {
        futures.add(mappings.get(Mapping.Field.VENDOR_REF_NO_TYPE)
            .resolve(doc)
            .thenAccept(o -> vendorDetail.setRefNumberType(
                VendorDetail.RefNumberType.fromValue((String) o)))
            .exceptionally(Mapper::logException));
      }
      }

    if (mappings.containsKey(Mapping.Field.VENDOR_ACCOUNT)) {
      futures.add(mappings.get(Mapping.Field.VENDOR_ACCOUNT)
          .resolve(doc)
          .thenAccept(o -> vendorDetail.setVendorAccount((String) o))
          .exceptionally(Mapper::logException));
    }
  }

  public boolean isObjectEmpty(Object instance) {
    for (Field f : instance.getClass().getDeclaredFields())
    {
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
        sb.append(nodes.item(i).getTextContent());
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
          product = BigDecimal.exact(nodes.item(i).getTextContent());
        } else {
          product = product
              .$times(BigDecimal.exact(nodes.item(i).getTextContent()));
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

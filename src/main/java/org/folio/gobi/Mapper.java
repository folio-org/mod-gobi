package org.folio.gobi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.folio.rest.acq.model.CompositePoLine;
import org.folio.rest.acq.model.CompositePurchaseOrder;
import org.folio.rest.acq.model.Cost;
import org.folio.rest.acq.model.Details;
import org.folio.rest.acq.model.Eresource;
import org.folio.rest.acq.model.Location;
import org.folio.rest.acq.model.PurchaseOrder;
import org.folio.rest.acq.model.Vendor;
import org.folio.rest.impl.PostGobiOrdersHelper;
import org.folio.rest.mappings.model.Mapping;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import scala.math.BigDecimal;

public class Mapper {

  private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

  private final Map<Mapping.Field, DataSource> mappings;
  private final PostGobiOrdersHelper gobiHelper;

  public Mapper(Map<Mapping.Field, DataSource> mappings, PostGobiOrdersHelper gobiHelper) {
    this.mappings = mappings;
    this.gobiHelper = gobiHelper;
  }

  public CompletableFuture<CompositePurchaseOrder> map(Document doc) {
    CompletableFuture<CompositePurchaseOrder> future = new CompletableFuture<>();

    try {
      PurchaseOrder po = new PurchaseOrder();
      CompositePoLine pol = new CompositePoLine();
      List<CompositePoLine> poLines = new ArrayList<>();

      Details detail = new Details();
      Cost cost = new Cost();
      Location location = new Location();
      Eresource eresource = new Eresource();
      Vendor vendor = new Vendor();

      List<CompletableFuture<?>> futures = new ArrayList<>();

      futures.add(mappings.get(Mapping.Field.CREATED_BY)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> po.setCreatedBy((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.ACCOUNT_NUMBER)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> pol.setAccountNumber((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.ACQUISITION_METHOD)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> pol.setAcquisitionMethod((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.REQUESTER)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> pol.setRequester((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.TITLE)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> detail.setTitle((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.MATERIAL_TYPE)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> detail.setMaterialType((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.RECEIVING_NOTE)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> detail.setReceivingNote((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.PRODUCT_ID)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> detail.setProductId((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.LOCATION)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> location.setLocationId((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.QUANTITY)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> {
          cost.setQuantity((Integer) o);
          location.setQuantity((Integer) o);
        })
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.LIST_PRICE)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> cost.setListPrice((Double) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.ESTIMATED_PRICE)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> cost.setEstimatedPrice((Double) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.CURRENCY)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> cost.setCurrency((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.ACCESS_PROVIDER)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> eresource.setAccessProvider((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.USER_LIMIT)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> eresource.setUserLimit((Integer) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.VENDOR_ID)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> vendor.setRefNumber((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Mapping.Field.INSTRUCTIONS)
        .resolve(doc, gobiHelper)
        .thenAccept(o -> vendor.setInstructions((String) o))
        .exceptionally(Mapper::logException));
      CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[futures.size()])).thenAccept(v -> {
        pol.setDetails(detail);
        pol.setCost(cost);
        pol.setLocation(location);
        pol.setEresource(eresource);
        pol.setVendor(vendor);

        poLines.add(pol);
        CompositePurchaseOrder compPO = new CompositePurchaseOrder();
        compPO.setPurchaseOrder(po);
        compPO.setPoLines(poLines);
        future.complete(compPO);
      });
    } catch (Exception e) {
      throw new CompletionException(e);
    }

    return future;
  }

  public static Void logException(Throwable t) {
    logger.error("Mapper Exception", t);
    return null;
  }

  public static CompletableFuture<Integer> toInteger(String s, PostGobiOrdersHelper gobiHelper) {
    Integer val = s != null ? Integer.parseInt(s) : null;
    return CompletableFuture.completedFuture(val);
  }

  public static CompletableFuture<Double> toDouble(String s, PostGobiOrdersHelper gobiHelper) {
    Double val = s != null ? Double.parseDouble(s) : null;
    return CompletableFuture.completedFuture(val);
  }

  public static CompletableFuture<Date> toDate(String s, PostGobiOrdersHelper gobiHelper) {
    DateTime val = s != null ? DateTime.parse(s) : DateTime.now();
    return CompletableFuture.completedFuture(val.toDate());
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
          product = product.$times(BigDecimal.exact(nodes.item(i).getTextContent()));
        }
      }
      return String.valueOf(product);
    }
    return null;
  }

  public static interface Translation<T> {
    public CompletableFuture<T> apply(String s, PostGobiOrdersHelper gobiHelper);
  }

  public static interface NodeCombinator {
    public String apply(NodeList n);
  }
}
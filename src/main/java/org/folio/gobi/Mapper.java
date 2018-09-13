package org.folio.gobi;

import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import scala.math.BigDecimal;

public class Mapper {

  private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

  public enum Field {
    ACCOUNT_NUMBER("account_number"),
    ACQUISITION_METHOD("acquisition_method"),
    QUANTITY("quantity"),
    LIST_PRICE("list_price"),
    ESTIMATED_PRICE("estimated_price"),
    CURRENCY("currency"),
    FUND_CODE("fund_code"),
    CREATED_BY("created_by"),
    TITLE("title"),
    MATERIAL_TYPE("material_type"),
    RECEIVING_NOTE("receiving_note"),
    REQUESTER("requester"),
    VENDOR_ID("vendor_id"),
    INSTRUCTIONS("instruction_to_vendor"),
    NOTE_FROM_VENDOR("note_from_vendor"),
    USER_LIMIT("user_limit"),
    LOCATION("location"),
    PRODUCT_ID("product_id"),
    ACCESS_PROVIDER("access_provider");

    public final String fieldName;

    Field(String fieldName) {
      this.fieldName = fieldName;
    }
  }

  private final Map<Field, DataSource> mappings;

  public Mapper(Map<Field, DataSource> mappings) {
    this.mappings = mappings;
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

      futures.add(mappings.get(Field.CREATED_BY)
        .resolve(doc)
        .thenAccept(o -> po.setCreatedBy((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.ACCOUNT_NUMBER)
        .resolve(doc)
        .thenAccept(o -> pol.setAccountNumber((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.ACQUISITION_METHOD)
        .resolve(doc)
        .thenAccept(o -> pol.setAcquisitionMethod((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.REQUESTER)
        .resolve(doc)
        .thenAccept(o -> pol.setRequester((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.TITLE)
        .resolve(doc)
        .thenAccept(o -> detail.setTitle((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.MATERIAL_TYPE)
        .resolve(doc)
        .thenAccept(o -> detail.setMaterialType((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.RECEIVING_NOTE)
        .resolve(doc)
        .thenAccept(o -> detail.setReceivingNote((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.PRODUCT_ID)
        .resolve(doc)
        .thenAccept(o -> detail.setProductId((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.LOCATION)
        .resolve(doc)
        .thenAccept(o -> location.setLocationId((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.QUANTITY)
        .resolve(doc)
        .thenAccept(o -> {
          cost.setQuantity((Integer) o);
          location.setQuantity((Integer) o);
        })
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.LIST_PRICE)
        .resolve(doc)
        .thenAccept(o -> cost.setListPrice((Double) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.ESTIMATED_PRICE)
        .resolve(doc)
        .thenAccept(o -> cost.setEstimatedPrice((Double) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.CURRENCY)
        .resolve(doc)
        .thenAccept(o -> cost.setCurrency((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.ACCESS_PROVIDER)
        .resolve(doc)
        .thenAccept(o -> eresource.setAccessProvider((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.USER_LIMIT)
        .resolve(doc)
        .thenAccept(o -> eresource.setUserLimit((Integer) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.VENDOR_ID)
        .resolve(doc)
        .thenAccept(o -> vendor.setRefNumber((String) o))
        .exceptionally(Mapper::logException));
      futures.add(mappings.get(Field.INSTRUCTIONS)
        .resolve(doc)
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

  public static CompletableFuture<Integer> toInteger(String s) {
    Integer val = s != null ? Integer.parseInt(s) : null;
    return CompletableFuture.completedFuture(val);
  }

  public static CompletableFuture<Double> toDouble(String s) {
    Double val = s != null ? Double.parseDouble(s) : null;
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
          product = product.$times(BigDecimal.exact(nodes.item(i).getTextContent()));
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
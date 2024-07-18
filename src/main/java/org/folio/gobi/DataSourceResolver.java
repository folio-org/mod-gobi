package org.folio.gobi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.Mapper.NodeCombinator;
import org.folio.rest.jaxrs.model.DataSource.Translation;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DataSourceResolver {
  private static final Logger logger = LogManager.getLogger(DataSourceResolver.class);

  private final LookupService lookupService;

  public final String from;
  public final Object defValue;
  public final Translation translation;
  public final NodeCombinator combinator;
  public final boolean translateDefValue;
  private final XPath xpath;

  public static Builder builder() {
    return new Builder();
  }

  public DataSourceResolver(LookupService lookupService,
                            String from,
                            NodeCombinator combinator,
                            Object defValue,
                            Translation translation,
                            boolean translateDefValue) {
    this.lookupService = lookupService;
    this.from = from;
    this.combinator = combinator == null ? Mapper::concat : combinator;
    this.defValue = defValue;
    this.translation = translation;
    this.translateDefValue = translateDefValue;
    XPathFactory xpathFactory = XPathFactory.newInstance();
    xpath = xpathFactory.newXPath();
  }

  public CompletableFuture<Object> resolve(Document doc) {
    CompletableFuture<Object> future = new CompletableFuture<>();

    if (from != null) {
      applyXPath(doc)
        .thenCompose(s -> applyTranslation(s)
          .thenCompose(o -> applyDefault(o, doc)
            .thenAccept(future::complete)
            .exceptionally(t -> {
              future.completeExceptionally(t);
              return null;
            }))
          .exceptionally(t -> {
            future.completeExceptionally(t);
            return null;
          }))
        .exceptionally(t -> {
          future.completeExceptionally(t);
          return null;
        });
    } else {
      applyDefault(null, doc)
        .thenAccept(future::complete)
        .exceptionally(t -> {
          future.completeExceptionally(t);
          return null;
        });
    }
    return future;
  }

  private CompletableFuture<String> applyXPath(Document doc) {
      try {
        NodeList nodeList = (NodeList) xpath.evaluate(from, doc, XPathConstants.NODESET);
        return CompletableFuture.completedFuture(combinator.apply(nodeList));
      } catch (XPathExpressionException e) {
        throw new CompletionException(e);
      }
  }

  public CompletableFuture<?> applyTranslation(Object o) {
    if (translation == null) {
      return CompletableFuture.completedFuture(o);
    }

    String s = o == null ? null : o.toString();
    return switch (translation) {
      case LOOKUP_CONTRIBUTOR_NAME_TYPE_ID -> lookupService.lookupContributorNameTypeId(s);
      case LOOKUP_EXPENSE_CLASS_ID -> lookupService.lookupExpenseClassId(s);
      case LOOKUP_ACQUISITION_METHOD_IDS -> lookupService.lookupAcquisitionMethodId(s);
      case LOOKUP_ACQUISITION_UNIT_IDS_BY_NAME -> lookupService.lookupAcquisitionUnitIdsByName(s);
      case LOOKUP_ACQUISITION_UNIT_IDS_BY_ACCOUNT -> lookupService.lookupAcquisitionUnitIdsByAccount(s);
      case LOOKUP_LOCATION_ID -> lookupService.lookupLocationId(s);
      case LOOKUP_MATERIAL_TYPE_ID -> lookupService.lookupMaterialTypeId(s);
      case LOOKUP_FUND_ID -> lookupService.lookupFundId(s);

      case LOOKUP_ORGANIZATION -> lookupService.lookupOrganization(s);
      case LOOKUP_PRODUCT_ID_TYPE -> lookupService.lookupProductIdType(s);
      case LOOKUP_CONFIG_ADDRESS -> lookupService.lookupConfigAddress(s);
      case LOOKUP_PREFIX -> lookupService.lookupPrefix(s);
      case LOOKUP_SUFFIX -> lookupService.lookupSuffix(s);
      case LOOKUP_LINKED_PACKAGE -> lookupService.lookupLinkedPackage(s);
      case SEPARATE_ISBN_QUALIFIER -> lookupService.separateISBNQualifier(s);
      case TRUNCATE_ISBN_QUALIFIER -> lookupService.truncateISBNQualifier(s);

      case TO_BOOLEAN -> Mapper.toBoolean(s);
      case TO_INTEGER -> Mapper.toInteger(s);
      case TO_DOUBLE -> Mapper.toDouble(s);
      case TO_DATE -> Mapper.toDate(s);

      default -> {
        logger.error("Exception in translation time, no such Translation available: " + translation);
        yield CompletableFuture.completedFuture(null);
      }
    };
  }

  private CompletableFuture<?> applyDefault(Object o, Document doc) {
    if (o == null) {
      if (defValue instanceof DataSourceResolver) {
        if (translateDefValue) {
          return ((DataSourceResolver) defValue).resolve(doc).thenApply(v -> applyTranslation(v.toString()));
        } else {
          return ((DataSourceResolver) defValue).resolve(doc);
        }
      } else {
        if (translateDefValue) {
          return applyTranslation(defValue.toString());
        } else {
          return CompletableFuture.completedFuture(defValue);
        }
      }
    }
    return CompletableFuture.completedFuture(o);
  }

  public static class Builder {
    private LookupService lookupService = null;
    private String from = null;
    private Object defValue = null;
    private Translation translation = null;
    private NodeCombinator combinator = null;
    private boolean translateDefValue = false;

    public Builder withLookupService(LookupService lookupService) {
      this.lookupService = lookupService;
      return this;
    }

    public Builder withFrom(String from) {
      this.from = from;
      return this;
    }

    public Builder withDefault(Object defValue) {
      this.defValue = defValue;
      return this;
    }

    public Builder withTranslation(Translation translation) {
      this.translation = translation;
      return this;
    }

    public Builder withCombinator(NodeCombinator combinator) {
      this.combinator = combinator;
      return this;
    }

    public Builder withTranslateDefault(boolean translateDefValue) {
      this.translateDefValue = translateDefValue;
      return this;
    }

    public DataSourceResolver build() {
      return new DataSourceResolver(lookupService, from, combinator, defValue, translation, translateDefValue);
    }
  }
}

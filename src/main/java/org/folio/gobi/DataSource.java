package org.folio.gobi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.folio.gobi.Mapper.NodeCombinator;
import org.folio.gobi.Mapper.Translation;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DataSource {

  public static final ObjectMapper MAPPER = new ObjectMapper();

  public final String from;
  public final Object defValue;
  public final Translation<?> translation;
  public final NodeCombinator combinator;
  public final boolean translateDefValue;
  private final XPath xpath;

  public static Builder builder() {
    return new Builder();
  }

  private <T> DataSource(String from, NodeCombinator combinator, Object defValue, Translation<T> translation,
      boolean translateDefValue) {
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
        .thenAccept(s -> applyTranslation(s)
          .thenAccept(o -> applyDefault(o, doc)
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
    return CompletableFuture.supplyAsync(() -> {
      try {
        return (NodeList) xpath.evaluate(from, doc, XPathConstants.NODESET);
      } catch (XPathExpressionException e) {
        throw new CompletionException(e);
      }
    }).thenApply(combinator::apply);
  }

  private CompletableFuture<?> applyTranslation(Object o) {
    if (translation != null) {
      return translation.apply(o == null ? null : o.toString());
    } else {
      return CompletableFuture.completedFuture(o);
    }
  }

  private CompletableFuture<?> applyDefault(Object o, Document doc) {
    if (o == null) {
      if (defValue instanceof DataSource) {
        if (translateDefValue) {
          return ((DataSource) defValue).resolve(doc).thenApply(v -> applyTranslation(v.toString()));
        } else {
          return ((DataSource) defValue).resolve(doc);
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
    private String from = null;
    private Object defValue = null;
    private Translation<?> translation = null;
    private NodeCombinator combinator = null;
    private boolean translateDefValue = false;

    public Builder withFrom(String from) {
      this.from = from;
      return this;
    }

    public Builder withDefault(Object defValue) {
      this.defValue = defValue;
      return this;
    }

    public <T> Builder withTranslation(Translation<T> translation) {
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

    public DataSource build() {
      return new DataSource(from, combinator, defValue, translation, translateDefValue);
    }
  }
}

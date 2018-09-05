package org.folio.gobi;

import java.util.concurrent.CompletableFuture;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.folio.gobi.Mapper.Translation;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Mapping {

  public static final ObjectMapper MAPPER = new ObjectMapper();

  public final String from;
  public final Object defValue;
  public final Translation<?> translation;
  public final boolean translateDefValue;
  private final XPath xpath;

  public Mapping(String from, Object defValue, Translation translation) {
    this(from, defValue, translation, false);
  }

  public Mapping(String from, Object defValue, Translation translation, boolean translateDefValue) {
    this.from = from;
    this.defValue = defValue;
    this.translation = translation;
    this.translateDefValue = translateDefValue;
    XPathFactory xpathFactory = XPathFactory.newInstance();
    xpath = xpathFactory.newXPath();
  }

  public CompletableFuture<Object> map(Document doc) {
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
    CompletableFuture<String> future = new CompletableFuture<>();

    CompletableFuture.runAsync(() -> {
      NodeList nodes = null;
      try {
        nodes = (NodeList) xpath.evaluate(from, doc, XPathConstants.NODESET);

        StringBuilder sb = new StringBuilder();
        if (nodes != null) {
          for (int i = 0; i < nodes.getLength(); i++) {
            sb.append(nodes.item(i).getTextContent());
          }
        }
        future.complete(sb.length() > 0 ? sb.toString() : null);
      } catch (XPathExpressionException e) {
        future.completeExceptionally(e);
      }
    });

    return future;
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
      if (defValue instanceof Mapping) {
        if (translateDefValue) {
          return ((Mapping) defValue).map(doc).thenApply(this::applyTranslation);
        } else {
          return ((Mapping) defValue).map(doc);
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
}
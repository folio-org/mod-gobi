package org.folio.gobi;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.folio.rest.jaxrs.model.PurchaseOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderParser {
  private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderParser.class);
  private static final String PURCHASE_ORDER_SCHEMA = "PurchaseOrder.xsd";

  private Unmarshaller jaxbUnmarshaller;

  private static final PurchaseOrderParser INSTANCE = new PurchaseOrderParser();

  public static PurchaseOrderParser getParser() {
    return INSTANCE;
  }

  private PurchaseOrderParser() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(PurchaseOrder.class);
      jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      jaxbUnmarshaller.setSchema(SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(this.getClass().getClassLoader().getResourceAsStream(PURCHASE_ORDER_SCHEMA))));
    } catch (Exception e) {
      LOG.error("Unable to create PurchaseOrderParser", e);
      jaxbUnmarshaller = null;
    }
  }

  public PurchaseOrder parse(Reader data) throws PurchaseOrderParserException {
    if (jaxbUnmarshaller == null) {
      throw new IllegalStateException("Unmarshaller is not available");
    }

    PurchaseOrder purchaseOrder;

    try {
      purchaseOrder = (PurchaseOrder) jaxbUnmarshaller.unmarshal(new StreamSource(data));
    } catch (JAXBException e) {
      LOG.error("Parsing failed", e);

      final Throwable cause = e.getCause();
      final String message;
      if (cause != null) {
        message = cause.getMessage();
      } else {
        message = e.getMessage();
      }

      throw new PurchaseOrderParserException(message, e);
    }

    return purchaseOrder;
  }
}

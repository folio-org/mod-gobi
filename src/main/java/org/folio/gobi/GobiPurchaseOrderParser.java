package org.folio.gobi;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.folio.rest.gobi.model.GobiPurchaseOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class GobiPurchaseOrderParser {
  private static final Logger logger = LoggerFactory.getLogger(GobiPurchaseOrderParser.class);
  private static final String PURCHASE_ORDER_SCHEMA = "GobiPurchaseOrder.xsd";
  private static final GobiPurchaseOrderParser INSTANCE = new GobiPurchaseOrderParser();

  private Unmarshaller jaxbUnmarshaller;

  public static GobiPurchaseOrderParser getParser() {
    return INSTANCE;
  }

  private GobiPurchaseOrderParser() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(GobiPurchaseOrder.class);
      SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new ResourceResolver());
      Schema schema = schemaFactory
        .newSchema(new StreamSource(this.getClass().getClassLoader().getResourceAsStream(PURCHASE_ORDER_SCHEMA)));
      jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      jaxbUnmarshaller.setSchema(schema);
    } catch (Exception e) {
      logger.error("Unable to create GobiPurchaseOrderParser", e);
      jaxbUnmarshaller = null;
    }
  }

  public GobiPurchaseOrder parse(Reader data) throws GobiPurchaseOrderParserException {
    if (jaxbUnmarshaller == null) {
      throw new IllegalStateException("Unmarshaller is not available");
    }

    GobiPurchaseOrder purchaseOrder;

    try {
      purchaseOrder = (GobiPurchaseOrder) jaxbUnmarshaller.unmarshal(new StreamSource(data));
    } catch (JAXBException e) {
      logger.error("Parsing failed", e);

      final Throwable cause = e.getCause();
      final String message;
      if (cause != null) {
        message = cause.getMessage();
      } else {
        message = e.getMessage();
      }

      throw new GobiPurchaseOrderParserException(message, e);
    }

    return purchaseOrder;
  }

  public static class ResourceResolver implements LSResourceResolver {

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
      InputStream resourceAsStream = this.getClass()
        .getClassLoader()
        .getResourceAsStream(systemId);
      return new Input(publicId, systemId, resourceAsStream);
    }
  }

  public static class Input implements LSInput {

    private static final Logger logger = LoggerFactory.getLogger(Input.class);

    private String publicId;
    private String systemId;

    private InputStream inputStream;

    public Input(String publicId, String sysId, InputStream input) {
      this.publicId = publicId;
      this.systemId = sysId;
      this.inputStream = input;
    }

    public String getPublicId() {
      return publicId;
    }

    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    public String getBaseURI() {
      return null;
    }

    public InputStream getByteStream() {
      return null;
    }

    public boolean getCertifiedText() {
      return false;
    }

    public Reader getCharacterStream() {
      return null;
    }

    public String getEncoding() {
      return null;
    }

    public String getStringData() {
      synchronized (inputStream) {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
          return buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
          logger.error("Exception", e);
          return null;
        }
      }
    }

    public void setBaseURI(String baseURI) {
      throw new UnsupportedOperationException();
    }

    public void setByteStream(InputStream byteStream) {
      throw new UnsupportedOperationException();
    }

    public void setCertifiedText(boolean certifiedText) {
      throw new UnsupportedOperationException();
    }

    public void setCharacterStream(Reader characterStream) {
      throw new UnsupportedOperationException();
    }

    public void setEncoding(String encoding) {
      throw new UnsupportedOperationException();
    }

    public void setStringData(String stringData) {
      throw new UnsupportedOperationException();
    }

    public String getSystemId() {
      return systemId;
    }

    public void setSystemId(String systemId) {
      this.systemId = systemId;
    }

    public InputStream getInputStream() {
      return inputStream;
    }

    public void setInputStream(BufferedInputStream inputStream) {
      this.inputStream = inputStream;
    }
  }
}

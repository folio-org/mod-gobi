package org.folio.gobi;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class GobiPurchaseOrderParser {
  private static final Logger logger = LoggerFactory.getLogger(GobiPurchaseOrderParser.class);
  private static final String PURCHASE_ORDER_SCHEMA = "GobiPurchaseOrder.xsd";
  private static final GobiPurchaseOrderParser INSTANCE = new GobiPurchaseOrderParser();
  private static final String EXTERNAL_DTD_FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  private static final String DTD_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

  private Validator validator;

  public static GobiPurchaseOrderParser getParser() {
    return INSTANCE;
  }

  private GobiPurchaseOrderParser() {
    try {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new ResourceResolver());
      Schema schema = schemaFactory
        .newSchema(new StreamSource(this.getClass().getClassLoader().getResourceAsStream(PURCHASE_ORDER_SCHEMA)));
      validator = schema.newValidator();
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    } catch (Exception e) {
      logger.error("Unable to create GobiPurchaseOrderParser", e);
    }
  }

  public Document parse(String data) throws GobiPurchaseOrderParserException {
    Document doc = null;    
    try {
      final InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      
      //https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
      // Protect against XML entity attacks by disallowing DTD
      factory.setFeature(DTD_FEATURE, true);   
      // Protect against external DTDs
      factory.setFeature(EXTERNAL_DTD_FEATURE, false); 
      // Protect from XML Schema
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);

      doc = factory.newDocumentBuilder().parse(stream);
      validator.validate(new DOMSource(doc));
    } catch (Exception e) {
      final Throwable cause = e.getCause();
      final String message;
      if (cause != null) {
        message = cause.getMessage();
      } else {
        message = e.getMessage();
      }

      throw new GobiPurchaseOrderParserException(message, e);
    }
    return doc;
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

    @Override
    public String getPublicId() {
      return publicId;
    }

    @Override
    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    @Override
    public String getBaseURI() {
      return null;
    }

    @Override
    public InputStream getByteStream() {
      return null;
    }

    @Override
    public boolean getCertifiedText() {
      return false;
    }

    @Override
    public Reader getCharacterStream() {
      return null;
    }

    @Override
    public String getEncoding() {
      return null;
    }

    @Override
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

    @Override
    public void setBaseURI(String baseURI) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setByteStream(InputStream byteStream) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setEncoding(String encoding) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setStringData(String stringData) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getSystemId() {
      return systemId;
    }

    @Override
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

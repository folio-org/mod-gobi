package org.folio.gobi;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

public class GobiPurchaseOrderParser {
  private static final Logger logger = LogManager.getLogger(GobiPurchaseOrderParser.class);
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
      // Disable XML External Entity (XXE) vulnerabilities
      // https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
      // https://semgrep.dev/docs/cheat-sheets/java-xxe
      schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      schemaFactory.setResourceResolver(new ResourceResolver());
      Schema schema = schemaFactory.newSchema(new StreamSource(this.getClass().getClassLoader().getResourceAsStream(PURCHASE_ORDER_SCHEMA)));
      validator = schema.newValidator();
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    } catch (SAXException e) {
      logger.error("Unable to create GobiPurchaseOrderParser", e);
    }
  }

  public Document parse(String data) throws GobiPurchaseOrderParserException {
    logger.debug("parse:: Trying to parse data: {}", data);
    Document doc;
    try {
      final InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
      DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();

      //https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
      // By disabling DTD, almost all XXE attacks will be prevented.
      factory.setFeature(DTD_FEATURE, true);
      //Protect against Denial of Service attack and remote file access.
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      // Protect against external DTDs
      factory.setFeature(EXTERNAL_DTD_FEATURE, false);
      // Protect from XML Schema
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);

      doc = factory.newDocumentBuilder().parse(stream);
      validator.validate(new DOMSource(doc));
    } catch (Exception e) {
      final Throwable cause = e.getCause();
      final String message = (cause != null) ? cause.getMessage() : e.getMessage();
      logger.error("Unable to parse Gobi Purchase Order with data '{}'  ", data, e);
      throw new GobiPurchaseOrderParserException(message, e);
    }
    return doc;
  }

  public static class ResourceResolver implements LSResourceResolver {

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
      InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(systemId);
      return new Input(publicId, systemId, resourceAsStream);
    }
  }

  public static class Input implements LSInput {

    private static final Logger logger = LogManager.getLogger(Input.class);

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
          logger.error("Error when buffering input stream", e);
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

  }
}

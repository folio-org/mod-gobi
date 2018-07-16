package org.folio.gobi;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.folio.rest.jaxrs.model.GOBIResponse;
import org.folio.rest.tools.utils.BinaryOutStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GOBIResponseWriter {
  private static final Logger LOG = LoggerFactory.getLogger(GOBIResponseWriter.class);
  private static final String RESPONSE_SCHEMA = "Response.xsd";

  private Marshaller jaxbMarshaller;

  private static final GOBIResponseWriter INSTANCE = new GOBIResponseWriter();

  public static GOBIResponseWriter getWriter() {
    return INSTANCE;
  }

  public GOBIResponseWriter() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(GOBIResponse.class);
      jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setSchema(SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(this.getClass().getClassLoader().getResourceAsStream(RESPONSE_SCHEMA))));
    } catch (Exception e) {
      LOG.error("Unable to create GOBIResponseWriter", e);
      jaxbMarshaller = null;
    }
  }

  public BinaryOutStream write(GOBIResponse response) {
    if (jaxbMarshaller == null) {
      throw new IllegalStateException("Marshaller is not available");
    }

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      jaxbMarshaller.marshal(response, baos);
    } catch (JAXBException e) {
      LOG.error("Marshalling failed", e);
    }

    final BinaryOutStream outStream = new BinaryOutStream();
    outStream.setData(baos.toByteArray());

    return outStream;
  }
}

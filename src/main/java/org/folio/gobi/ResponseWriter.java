package org.folio.gobi;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.folio.rest.gobi.model.Response;
import org.folio.rest.tools.utils.BinaryOutStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseWriter {
  private static final Logger logger = LoggerFactory.getLogger(ResponseWriter.class);
  private static final String RESPONSE_SCHEMA = "Response.xsd";
  private static final ResponseWriter INSTANCE = new ResponseWriter();

  private Marshaller jaxbMarshaller;

  public static ResponseWriter getWriter() {
    return INSTANCE;
  }

  public ResponseWriter() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);
      jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setSchema(SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI)
        .newSchema(new StreamSource(this.getClass().getClassLoader().getResourceAsStream(RESPONSE_SCHEMA))));
    } catch (Exception e) {
      logger.error("Unable to create ResponseWriter", e);
      jaxbMarshaller = null;
    }
  }

  public BinaryOutStream write(Response response) {
    if (jaxbMarshaller == null) {
      throw new IllegalStateException("Marshaller is not available");
    }

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      jaxbMarshaller.marshal(response, baos);
    } catch (JAXBException e) {
      logger.error("Marshalling failed", e);
    }

    final BinaryOutStream outStream = new BinaryOutStream();
    outStream.setData(baos.toByteArray());

    return outStream;
  }
}

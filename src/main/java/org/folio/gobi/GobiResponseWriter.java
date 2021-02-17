package org.folio.gobi;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.gobi.model.GobiResponse;
import org.folio.rest.tools.utils.BinaryOutStream;

public class GobiResponseWriter {
  private static final Logger logger = LogManager.getLogger(GobiResponseWriter.class);
  private static final String RESPONSE_SCHEMA = "Response.xsd";
  private static final GobiResponseWriter INSTANCE = new GobiResponseWriter();

  private Marshaller jaxbMarshaller;

  public static GobiResponseWriter getWriter() {
    return INSTANCE;
  }

  public GobiResponseWriter() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(GobiResponse.class);
      jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, RESPONSE_SCHEMA);
    } catch (Exception e) {
      logger.error("Unable to create ResponseWriter", e);
      jaxbMarshaller = null;
    }
  }

  public BinaryOutStream write(GobiResponse response) {
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

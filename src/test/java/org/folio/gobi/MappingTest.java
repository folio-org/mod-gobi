package org.folio.gobi;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class MappingTest {

  private static final Logger logger = LoggerFactory.getLogger(MappingTest.class);

  public static final String testdataPath = "Mapping/testdata.xml";
  private Document doc;

  @Before
  public void setUp() throws Exception {
    InputStream data = this.getClass().getClassLoader().getResourceAsStream(testdataPath);
    doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testBasicXPath() throws Exception {
    logger.info("begin: Test Mapping - xpath evalutation");

    assertEquals("Hello World", new Mapping("//Doo/Dah", null, null).map(doc).get());
    assertEquals("DIT", new Mapping("//Bar[@attr='dit']", null, null).map(doc).get());
    assertEquals("DITDATDOT", new Mapping("//Bar", null, null).map(doc).get());
  }

  @Test
  public void testDefaults() throws Exception {
    logger.info("begin: Test Mapping - defaults");

    // default to a string literal
    assertEquals("PKD", new Mapping("//Doo/Dud", "PKD", null).map(doc).get());

    // default to an integer literal
    assertEquals(1, new Mapping("//Bar[@attr='one']", 1, null).map(doc).get());

    // default to another mapping
    Mapping defMapping = new Mapping("//Bar[@attr='dat']", null, null);
    assertEquals("DAT", new Mapping("//DAT", defMapping, null).map(doc).get());

    // default to another mapping (multiple levels)
    Mapping defMapping1 = new Mapping("//Bar[@attr='dot']", null, null);
    Mapping defMapping2 = new Mapping("//DAT", defMapping1, null);
    assertEquals("DOT", new Mapping("//DIT", defMapping2, null).map(doc).get());
  }

  @Test
  public void testTranslations() throws Exception {
    logger.info("begin: Test Mapping - translations");

    assertEquals("HELLO WORLD", new Mapping("//Doo/Dah", null, this::toUpper).map(doc).get());
    assertEquals(1.5d, new Mapping("//Zap", null, Mapper::toDouble).map(doc).get());
    assertEquals(90210, new Mapping("//Zip", null, Mapper::toInteger).map(doc).get());    
  }
  
  @Test(expected = ExecutionException.class)
  public void testExceptionInTranslator() throws Exception {
    logger.info("begin: Test Exception in applyTranslator()");

    new Mapping("//Zip", null, this::throwException).map(doc).get();    
  }

  @Test(expected = ExecutionException.class)
  public void testExceptionInApplyDefault() throws Exception {
    logger.info("begin: Test Exception in applyDefault()");

    Mapping defMapping = new Mapping("//Bar[@attr='dat']", null, null);
    new Mapping(null, defMapping, null).map(null).get();    
  }
  
  private CompletableFuture<String> toUpper(String s) {
    String ret = s != null ? s.toUpperCase() : null;
    return CompletableFuture.completedFuture(ret);
  }
  
  private CompletableFuture<String> throwException(String s) {
    CompletableFuture<String> future = new CompletableFuture<>();
    CompletableFuture.runAsync(() -> {
      future.completeExceptionally(new Throwable("Whoops!"));
    });
    return future;
  }
}

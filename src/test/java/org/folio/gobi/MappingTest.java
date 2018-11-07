package org.folio.gobi;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.folio.rest.tools.utils.NetworkUtils;
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
  private int port = NetworkUtils.nextFreePort();
  private Map<String, String> okapiHeaders = new HashMap<>();

  @Before
  public void setUp() throws Exception {
    InputStream data = this.getClass().getClassLoader().getResourceAsStream(testdataPath);
    doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
    okapiHeaders.put("x-okapi-url", "http://localhost:" + port);
    okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testBasicXPath() throws Exception {
    logger.info("begin: Test Mapping - xpath evalutation");
    assertEquals("Hello World", DataSourceResolver.builder().withFrom("//Doo/Dah").build().resolve(doc).get());
    assertEquals("DIT", DataSourceResolver.builder().withFrom("//Bar[@attr='dit']").build().resolve(doc).get());
  }

  @Test
  public void testDefaults() throws Exception {
    logger.info("begin: Test Mapping - defaults");
    // default to a string literal
    assertEquals("PKD", DataSourceResolver.builder().withFrom("//Doo/Dud").withDefault("PKD").build().resolve(doc).get());

    // default to an integer literal
    assertEquals(1, DataSourceResolver.builder().withFrom("//Bar[@attr='one']").withDefault(1).build().resolve(doc).get());

    // default to another mapping
    DataSourceResolver defMapping = DataSourceResolver.builder().withFrom("//Bar[@attr='dat']").build();
    assertEquals("DAT", DataSourceResolver.builder().withFrom("//DAT").withDefault(defMapping).build().resolve(doc).get());

    // default to another mapping (multiple levels)
    DataSourceResolver defMapping1 = DataSourceResolver.builder().withFrom("//Bar[@attr='dot']").build();
    DataSourceResolver defMapping2 = DataSourceResolver.builder().withFrom("//DAT").withDefault(defMapping1).build();
    assertEquals("DOT", DataSourceResolver.builder().withFrom("//DIT").withDefault(defMapping2).build().resolve(doc).get());
  }

  @Test
  public void testCombinators() throws Exception {
    logger.info("begin: Test Mapping - combinators");

    assertEquals("DITDATDOT", DataSourceResolver.builder().withFrom("//Bar").build().resolve(doc).get());
    assertEquals(4.5d, DataSourceResolver.builder()
      .withFrom("//Zap | //Zop")
      .withCombinator(Mapper::multiply)
      .withTranslation(Mapper::toDouble)
      .build()
      .resolve(doc)
      .get());
  }

  @Test
  public void testTranslations() throws Exception {
    logger.info("begin: Test Mapping - translations");

    assertEquals("HELLO WORLD",
        DataSourceResolver.builder().withFrom("//Doo/Dah").withTranslation(this::toUpper).build().resolve(doc).get());
    assertEquals(1.5d,
        DataSourceResolver.builder().withFrom("//Zap").withTranslation(Mapper::toDouble).build().resolve(doc).get());
    assertEquals(90210,
        DataSourceResolver.builder().withFrom("//Zip").withTranslation(Mapper::toInteger).build().resolve(doc).get());
  }

  @Test(expected = ExecutionException.class)
  public void testExceptionInTranslator() throws Exception {
    DataSourceResolver.builder().withFrom("//Zip").withTranslation(this::throwException).build().resolve(doc).get();
  }

  @Test(expected = ExecutionException.class)
  public void testExceptionInApplyDefault() throws Exception {
    logger.info("begin: Test Exception in applyDefault()");

    DataSourceResolver defMapping = DataSourceResolver.builder().withFrom("//Bar[@attr='dat']").build();
    DataSourceResolver.builder().withDefault(defMapping).build().resolve(null).get();
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

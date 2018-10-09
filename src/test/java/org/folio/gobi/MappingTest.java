package org.folio.gobi;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.folio.rest.impl.GOBIIntegrationServiceResourceImpl;
import org.folio.rest.impl.PostGobiOrdersHelper;
import org.folio.rest.mappings.model.DataSource.Translation;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;

public class MappingTest {

  private static final Logger logger = LoggerFactory.getLogger(MappingTest.class);

  public static final String testdataPath = "Mapping/testdata.xml";
  private Document doc;
  Vertx vertx = Vertx.vertx();
  int port = NetworkUtils.nextFreePort();
  Map<String, String> okapiHeaders = new HashMap<>();

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
    okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
    okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");
    PostGobiOrdersHelper postGobiOrdersHelper = new PostGobiOrdersHelper(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), null, okapiHeaders, vertx.getOrCreateContext());
    
    
    assertEquals("Hello World", DataSource.builder().withFrom("//Doo/Dah").build().resolve(doc, postGobiOrdersHelper).get());
    assertEquals("DIT", DataSource.builder().withFrom("//Bar[@attr='dit']").build().resolve(doc, postGobiOrdersHelper).get());
  }

  @Test
  public void testDefaults() throws Exception {
    logger.info("begin: Test Mapping - defaults");
    
    okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
    okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");
    PostGobiOrdersHelper postGobiOrdersHelper = new PostGobiOrdersHelper(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), null, okapiHeaders, vertx.getOrCreateContext());

    // default to a string literal
    assertEquals("PKD", DataSource.builder().withFrom("//Doo/Dud").withDefault("PKD").build().resolve(doc, postGobiOrdersHelper).get());

    // default to an integer literal
    assertEquals(1, DataSource.builder().withFrom("//Bar[@attr='one']").withDefault(1).build().resolve(doc, postGobiOrdersHelper).get());

    // default to another mapping
    DataSource defMapping = DataSource.builder().withFrom("//Bar[@attr='dat']").build();
    assertEquals("DAT", DataSource.builder().withFrom("//DAT").withDefault(defMapping).build().resolve(doc, postGobiOrdersHelper).get());

    // default to another mapping (multiple levels)
    DataSource defMapping1 = DataSource.builder().withFrom("//Bar[@attr='dot']").build();
    DataSource defMapping2 = DataSource.builder().withFrom("//DAT").withDefault(defMapping1).build();
    assertEquals("DOT", DataSource.builder().withFrom("//DIT").withDefault(defMapping2).build().resolve(doc, postGobiOrdersHelper).get());
  }

  @Test
  public void testCombinators() throws Exception {
    logger.info("begin: Test Mapping - combinators");

    okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
    okapiHeaders.put("x-okapi-tenant", "testLookupOrderMappings");
    PostGobiOrdersHelper postGobiOrdersHelper = new PostGobiOrdersHelper(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), null, okapiHeaders, vertx.getOrCreateContext());
    assertEquals("DITDATDOT", DataSource.builder().withFrom("//Bar").build().resolve(doc, postGobiOrdersHelper).get());
    assertEquals(4.5d, DataSource.builder()
      .withFrom("//Zap | //Zop")
      .withCombinator(Mapper::multiply)
      .withTranslation(Translation.TO_DOUBLE)
      .build()
      .resolve(doc, postGobiOrdersHelper)
      .get());
  }

  @Test
  public void testTranslations() throws Exception {
    logger.info("begin: Test Mapping - translations");

    assertEquals("HELLO WORLD",
        DataSource.builder().withFrom("//Doo/Dah").withTranslation(this::toUpper).build().resolve(doc).get());
    assertEquals(1.5d,
        DataSource.builder().withFrom("//Zap").withTranslation(Mapper::toDouble).build().resolve(doc).get());
    assertEquals(90210,
        DataSource.builder().withFrom("//Zip").withTranslation(Mapper::toInteger).build().resolve(doc).get());
  }

  @Test(expected = ExecutionException.class)
  public void testExceptionInTranslator() throws Exception {
    logger.info("begin: Test Exception in applyTranslator()");

    DataSource.builder().withFrom("//Zip").withTranslation(this::throwException).build().resolve(doc).get();
  }

  @Test(expected = ExecutionException.class)
  public void testExceptionInApplyDefault() throws Exception {
    logger.info("begin: Test Exception in applyDefault()");

    DataSource defMapping = DataSource.builder().withFrom("//Bar[@attr='dat']").build();
    DataSource.builder().withDefault(defMapping).build().resolve(null).get();
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

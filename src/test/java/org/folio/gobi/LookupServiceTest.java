package org.folio.gobi;

import java.util.HashMap;
import java.util.Map;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.folio.rest.ResourcePaths;
import org.folio.rest.core.RestClient;
import org.folio.rest.impl.GOBIIntegrationServiceResourceImpl;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class LookupServiceTest {


  @Test
  public final void testSuccessLookupFundIdByFundCodeWithExpenseClass(TestContext context) {
    testLookupFundId(context, "AFRICAHIST:Elec", false);
  }

  @Test
  public final void testShouldReturnNullFundIfFundIdNotFound(TestContext context) {
    testLookupFundId(context, "NONEXISTENT", true);
  }

  @Test
  public final void testLookupFirstMaterialTypes(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.MATERIAL_TYPES_ENDPOINT) && req.query().contains("*")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_materialType.json");
      } else {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/empty_materialType.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testDefaultMaterialTypes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupMaterialTypeId("unspecified")
        .thenAccept(list -> {
          context.assertNotNull(list);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }


  @Test
  public final void testSuccessLookupFundId(TestContext context) {
    testLookupFundId(context, "AFRICAHIST", false);
  }

  @Test
  public final void testSuccessMapLookupExpenseClassId(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.EXPENSE_CLASS_ENDPOINT) && req.query().contains("Elec")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_expenseClasses.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_expenseClasses.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testDefaultExpenseClasses");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupExpenseClassId("Elec")
        .thenAccept(id -> {
          context.assertNotNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testShouldReturnNullExpenseClassIdIfExpenseClassNotFound(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.EXPENSE_CLASS_ENDPOINT) && req.query().contains("Elec")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_expenseClasses.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_expenseClasses.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testDefaultExpenseClasses");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupExpenseClassId("Prn")
        .thenAccept(id -> {
          context.assertNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testSuccessMapLookupSuffixId(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.SUFFIXES_ENDPOINT) && req.query().contains("Suf")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_suffix_collection.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_suffixes.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testSuffixes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupSuffix("Suf")
        .thenAccept(id -> {
          context.assertNotNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testShouldReturnNullIdIfSuffixNotFound(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.SUFFIXES_ENDPOINT) && req.query().contains("Sdf")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_suffix_collection.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_suffixes.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testPrefixes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupSuffix("NonValidAddd")
        .thenAccept(id -> {
          context.assertNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testSuccessMapLookupPrefixId(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.PREFIXES_ENDPOINT) && req.query().contains("Pref")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_prefix_collection.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_prefixes.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testPrefixes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupPrefix("Pref")
        .thenAccept(id -> {
          context.assertNotNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testShouldReturnNullIdIfPrefixNotFound(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.PREFIXES_ENDPOINT) && req.query().contains("Pref")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_prefix_collection.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_prefixes.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testSuffixes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupPrefix("NonValidAddd")
        .thenAccept(id -> {
          context.assertNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testSuccessMapLookupAddressId(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.CONFIGURATION_ENDPOINT) && req.query().contains("Address")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_address_collection.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_address.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testPrefixes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupConfigAddress("Address")
        .thenAccept(id -> {
          context.assertNotNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testShouldReturnNullIdIfAddressNotFound(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.CONFIGURATION_ENDPOINT) && req.query().contains("Address")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_address_collection.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_address.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testSuffixes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupConfigAddress("NonValid")
        .thenAccept(id -> {
          context.assertNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testSuccessMapLookupPoLineId(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.ORDER_LINES_ENDPOINT) && req.query().contains("343434343")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_po_line_collection.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_po_lines.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testPrefixes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupLinkedPackage("343434343")
        .thenAccept(id -> {
          context.assertNotNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  @Test
  public final void testShouldReturnNullIdIfPoLineNotFound(TestContext context) {
    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.ORDER_LINES_ENDPOINT) && req.query().contains("22222")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_po_line_collection.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_po_lines.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testSuffixes");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupLinkedPackage("NonValid")
        .thenAccept(id -> {
          context.assertNull(id);
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }

  private void testLookupFundId(TestContext context, String fundCode, boolean shouldReturnNull) {

    final Async async = context.async();
    final Vertx vertx = Vertx.vertx();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(ResourcePaths.FUNDS_ENDPOINT) && req.query().contains("AFRICAHIST")) {
        req.response().setStatusCode(200).sendFile("PostGobiOrdersHelper/valid_funds.json");
      } else {
        req.response().setStatusCode(200).sendFile( "PostGobiOrdersHelper/empty_funds.json");
      }
    });

    int port = NetworkUtils.nextFreePort();
    server.listen(port, "localhost", ar -> {
      context.assertTrue(ar.succeeded());

      Map<String, String> okapiHeaders = new HashMap<>();
      okapiHeaders.put("X-Okapi-Url", "http://localhost:" + port);
      okapiHeaders.put("x-okapi-tenant", "testDefaultFunds");
      RestClient restClient = new RestClient(GOBIIntegrationServiceResourceImpl.getHttpClient(okapiHeaders), okapiHeaders, vertx.getOrCreateContext());
      LookupService lookupService = new LookupService(restClient);
      lookupService.lookupFundId(fundCode)
        .thenAccept(id -> {
          if (shouldReturnNull) {
            context.assertNull(id);
          } else {
            context.assertNotNull(id);
          }
          vertx.close(context.asyncAssertSuccess());
          async.complete();
        });
    });
  }
}

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

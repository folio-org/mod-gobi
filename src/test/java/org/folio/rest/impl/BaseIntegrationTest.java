package org.folio.rest.impl;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.utils.Envs;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;

@ExtendWith(VertxExtension.class)
@Testcontainers
public abstract class BaseIntegrationTest {

  protected static final String TENANT_ID = "test_tenant";
  private static final String OKAPI_HEADER_TENANT = "X-Okapi-Tenant";

  protected static Vertx vertx;
  protected static int okapiPort;

  @Container
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
    .withStartupAttempts(3);

  @BeforeAll
  public static void beforeAll(Vertx vertx, VertxTestContext context) {
    BaseIntegrationTest.vertx = vertx;
    okapiPort = NetworkUtils.nextFreePort();

    // Use Envs.setEnv to configure the database for PostgresClient
    Envs.setEnv(
      postgres.getHost(),
      postgres.getFirstMappedPort(),
      postgres.getUsername(),
      postgres.getPassword(),
      postgres.getDatabaseName()
    );

    RestAssured.baseURI = "http://localhost";
    RestAssured.port = okapiPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    final JsonObject conf = new JsonObject()
      .put("http.port", okapiPort);

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);

    vertx.deployVerticle(RestVerticle.class.getName(), opt).onComplete(id -> {
      String tenantJobId = RestAssured
        .given()
          .header(OKAPI_HEADER_TENANT, TENANT_ID)
          .contentType(ContentType.JSON)
          .body(new JsonObject().put("module_to", "mod-gobi-1.0.0").encode())
        .when()
          .post("/_/tenant")
        .then()
          .statusCode(201)
          .extract().path("id");

      // Wait for the tenant job to complete
      RestAssured
        .given()
          .header(OKAPI_HEADER_TENANT, TENANT_ID)
        .when()
          .get("/_/tenant/" + tenantJobId + "?wait=60000")
        .then()
          .statusCode(200)
          .body(not(hasKey("error")));

      context.completeNow();
    });
  }

  @AfterAll
  public static void afterAll(VertxTestContext context) {
    // The Vertx instance is managed by the VertxExtension, so we don't need to close it.
    context.completeNow();
  }
}

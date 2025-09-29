package org.folio.gobi;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.rest.utils.TestUtils.getMockData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import org.folio.gobi.domain.OrdersSettingKey;
import org.folio.rest.core.RestClient;
import org.folio.rest.utils.CopilotGenerated;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.json.JsonObject;

@CopilotGenerated(model = "Claude Sonnet 4")
public class OrdersSettingsRetrieverTest {

  @Mock
  RestClient restClient;

  private AutoCloseable mocks;

  @BeforeEach
  public void initMocks() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void closeMocks() throws Exception {
    if (mocks != null) {
      mocks.close();
    }
  }

  @Test
  void testGetSettingByKeyWhenCentralOrderingEnabled() throws IOException {
    var settings = getMockData("PostGobiOrdersHelper/valid_settings_central_enabled.json");
    OrdersSettingsRetriever settingsRetriever = new OrdersSettingsRetriever(restClient);

    doReturn(succeededFuture(new JsonObject(settings))).when(restClient).handleGetRequest(anyString());
    var setting = settingsRetriever.getSettingByKey(OrdersSettingKey.CENTRAL_ORDERING_ENABLED).join();

    assertNotNull(setting);
    assertEquals("ALLOW_ORDERING_WITH_AFFILIATED_LOCATIONS", setting.getKey());
    assertEquals("true", setting.getValue());
  }

  @Test
  void testGetSettingByKeyWhenCentralOrderingDisabled() throws IOException {
    var settings = getMockData("PostGobiOrdersHelper/valid_settings_central_disabled.json");
    OrdersSettingsRetriever settingsRetriever = new OrdersSettingsRetriever(restClient);

    doReturn(succeededFuture(new JsonObject(settings))).when(restClient).handleGetRequest(anyString());
    var setting = settingsRetriever.getSettingByKey(OrdersSettingKey.CENTRAL_ORDERING_ENABLED).join();

    assertNotNull(setting);
    assertEquals("ALLOW_ORDERING_WITH_AFFILIATED_LOCATIONS", setting.getKey());
    assertEquals("false", setting.getValue());
  }

  @Test
  void testGetSettingByKeyWhenSettingNotFound() throws IOException {
    var settings = getMockData("PostGobiOrdersHelper/empty_settings.json");
    OrdersSettingsRetriever settingsRetriever = new OrdersSettingsRetriever(restClient);

    doReturn(succeededFuture(new JsonObject(settings))).when(restClient).handleGetRequest(anyString());
    var setting = settingsRetriever.getSettingByKey(OrdersSettingKey.CENTRAL_ORDERING_ENABLED).join();

    assertNull(setting);
  }

  @Test
  void testGetSettingByKeyWhenExceptionOccurs() {
    OrdersSettingsRetriever settingsRetriever = new OrdersSettingsRetriever(restClient);

    doReturn(succeededFuture(null)).when(restClient).handleGetRequest(anyString());
    var setting = settingsRetriever.getSettingByKey(OrdersSettingKey.CENTRAL_ORDERING_ENABLED).join();

    assertNull(setting);
  }
}

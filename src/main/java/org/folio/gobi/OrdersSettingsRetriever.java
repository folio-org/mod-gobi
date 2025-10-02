package org.folio.gobi;

import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.folio.gobi.domain.OrdersSettingKey;
import org.folio.rest.acq.model.Setting;
import org.folio.rest.acq.model.SettingCollection;
import org.folio.rest.core.RestClient;

@Log4j2
public class OrdersSettingsRetriever {

  private static final String SETTINGS_ENDPOINT = "/orders-storage/settings";
  private static final String SETTINGS_BY_KEY_QUERY = "key==%s";

  private final RestClient restClient;

  public OrdersSettingsRetriever(RestClient restClient) {
    this.restClient = restClient;
  }

  public CompletableFuture<Setting> getSettingByKey(OrdersSettingKey settingKey) {
    String query = HelperUtils.encodeValue(String.format(SETTINGS_BY_KEY_QUERY, settingKey.getName()));
    String endpoint = String.format(SETTINGS_ENDPOINT + "?query=%s", query);
    return restClient.handleGetRequest(endpoint).toCompletionStage().toCompletableFuture()
      .thenApply(settings -> {
        SettingCollection settingCollection = settings.mapTo(SettingCollection.class);
        if (CollectionUtils.isEmpty(settingCollection.getSettings())) {
          return null;
        }
        return settingCollection.getSettings().getFirst();
      })
      .exceptionally(t -> {
        log.error("Exception occurred when retrieving setting for key: {}", settingKey.getName(), t);
        return null;
      });
  }
}

package org.folio.gobi;

import static org.junit.Assert.assertFalse;

import org.folio.rest.mappings.model.OrderMappings;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.List;

@RunWith(VertxUnitRunner.class)
public class RetrievingServiceTest {

  private final RetrievingService retrievingService = new RetrievingService();

  @Test
  public final void givenCollectionMappingFields() {
    JsonArray fields = retrievingService.retrieveFields();
    assertFalse(fields.getList().isEmpty());
  }

  @Test
  public final void givenCollectionMappingTranslators() {
    JsonArray translators = retrievingService.retrieveTranslators();
    assertFalse(translators.getList().isEmpty());
  }

  @Test
  public final void givenCollectionMappingTypes() {
    List<OrderMappings.OrderType> types = retrievingService.retrieveMappingsTypes();
    assertFalse(types.isEmpty());
  }
}

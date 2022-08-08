package org.folio.gobi;

import static org.junit.Assert.assertFalse;

import org.folio.rest.mappings.model.OrderMappings;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.List;

@RunWith(VertxUnitRunner.class)
public class MappingDetailsServiceTest {

  private final MappingDetailsService mappingDetailsService = new MappingDetailsService();

  @Test
  public final void givenCollectionMappingFields() {
    JsonArray fields = mappingDetailsService.retrieveFields();
    assertFalse(fields.getList().isEmpty());
  }

  @Test
  public final void givenCollectionMappingTranslators() {
    JsonArray translators = mappingDetailsService.retrieveTranslators();
    assertFalse(translators.getList().isEmpty());
  }

  @Test
  public final void givenCollectionMappingTypes() {
    List<OrderMappings.OrderType> types = mappingDetailsService.retrieveMappingsTypes();
    assertFalse(types.isEmpty());
  }
}

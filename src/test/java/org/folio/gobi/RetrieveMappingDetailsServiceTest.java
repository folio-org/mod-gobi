package org.folio.gobi;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.folio.rest.jaxrs.model.OrderMappings;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonArray;

class RetrieveMappingDetailsServiceTest {

  private final RetrieveMappingDetailsService retrieveMappingDetailsService = new RetrieveMappingDetailsService();

  @Test
  void givenCollectionMappingFields() {
    JsonArray fields = retrieveMappingDetailsService.retrieveFields();
    assertFalse(fields.getList().isEmpty());
  }

  @Test
  void givenCollectionMappingTranslators() {
    JsonArray translators = retrieveMappingDetailsService.retrieveTranslators();
    assertFalse(translators.getList().isEmpty());
  }

  @Test
  void givenCollectionMappingTypes() {
    List<OrderMappings.OrderType> types = retrieveMappingDetailsService.retrieveMappingsTypes();
    assertFalse(types.isEmpty());
  }
}

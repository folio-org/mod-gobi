package org.folio.gobi;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.folio.rest.acq.model.FolioOrderFields;
import org.folio.rest.acq.model.FolioOrderTranslators;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.junit.jupiter.api.Test;

public class MappingDetailsServiceTest {

  private final MappingDetailsService mappingDetailsService = new MappingDetailsService();

  @Test
  void givenCollectionMappingFields() {
    FolioOrderFields fields = mappingDetailsService.retrieveFields();
    assertFalse(fields.getFields().isEmpty());
  }

  @Test
  void givenCollectionMappingTranslators() {
    FolioOrderTranslators translators = mappingDetailsService.retrieveTranslators();
    assertFalse(translators.getTranslators().isEmpty());
  }

  @Test
  void givenCollectionMappingTypes() {
    List<OrderMappings.OrderType> types = mappingDetailsService.retrieveMappingsTypes();
    assertFalse(types.isEmpty());
    assertFalse(types.contains(OrderMappings.OrderType.LISTED_ELECTRONIC_SERIAL));
    assertFalse(types.contains(OrderMappings.OrderType.LISTED_PRINT_SERIAL));
    assertFalse(types.contains(OrderMappings.OrderType.UNLISTED_PRINT_SERIAL));
  }
}

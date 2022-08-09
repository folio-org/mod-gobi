package org.folio.gobi;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.folio.rest.acq.model.FolioOrderFields;
import org.folio.rest.acq.model.FolioOrderTranslators;
import org.folio.rest.mappings.model.OrderMappings;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MappingDetailsServiceTest {

  private final MappingDetailsService mappingDetailsService = new MappingDetailsService();

  @Test
  public final void givenCollectionMappingFields() {
    FolioOrderFields fields = mappingDetailsService.retrieveFields();
    assertFalse(fields.getFields().isEmpty());
  }

  @Test
  public final void givenCollectionMappingTranslators() {
    FolioOrderTranslators translators = mappingDetailsService.retrieveTranslators();
    assertFalse(translators.getTranslators().isEmpty());
  }

  @Test
  public final void givenCollectionMappingTypes() {
    List<OrderMappings.OrderType> types = mappingDetailsService.retrieveMappingsTypes();
    assertFalse(types.isEmpty());
  }
}

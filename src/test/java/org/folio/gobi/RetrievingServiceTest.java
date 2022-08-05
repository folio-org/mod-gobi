package org.folio.gobi;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.junit.VertxUnitRunner;

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
    JsonArray types = retrievingService.retrieveFields();
    assertFalse(types.getList().isEmpty());
  }


}

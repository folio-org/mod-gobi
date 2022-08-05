package org.folio.gobi;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;

import static org.junit.Assert.assertFalse;

@RunWith(VertxUnitRunner.class)
public class RetrievingServiceTest {

  private RetrievingService retrievingService = new RetrievingService();

  @Test
  public final void givenCollectionMappingFields() {
    List fields = retrievingService.retrieveFields();
    assertFalse(fields.isEmpty());
  }

  @Test
  public final void givenCollectionMappingTranslators() {
    List translators = retrievingService.retrieveTranslators();
    assertFalse(translators.isEmpty());
  }

  @Test
  public final void givenCollectionMappingTypes() {
    List types = retrievingService.retrieveFields();
    assertFalse(types.isEmpty());
  }


}

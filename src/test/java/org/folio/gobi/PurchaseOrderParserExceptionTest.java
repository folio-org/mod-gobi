package org.folio.gobi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PurchaseOrderParserExceptionTest {
  private final String testString = "This is a test";

  @Test
  void testPurchaseOrderParserException() {
    var exception = Assertions.assertThrows(GobiPurchaseOrderParserException.class, () -> {
      throw new GobiPurchaseOrderParserException();
    });
    assertThat(exception.getMessage(),  is(emptyOrNullString()));
    assertNull(exception.getCause());
  }

  @Test
  void testPurchaseOrderParserExceptionString() {
    var exception = Assertions.assertThrows(GobiPurchaseOrderParserException.class, () -> {
      throw new GobiPurchaseOrderParserException(testString);
    });
    assertEquals(testString, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void testPurchaseOrderParserExceptionThrowable() {
    var exception = Assertions.assertThrows(GobiPurchaseOrderParserException.class, () -> {
      throw new GobiPurchaseOrderParserException(new RuntimeException(testString));
    });
    assertEquals(testString, exception.getCause().getMessage());
    assertThat(exception.getCause(), isA(RuntimeException.class));
  }

  @Test
  void testPurchaseOrderParserExceptionStringThrowable() {
    var exception = Assertions.assertThrows(GobiPurchaseOrderParserException.class, () -> {
      throw new GobiPurchaseOrderParserException(testString, new RuntimeException());
    });
    assertEquals(testString, exception.getMessage());
    assertThat(exception.getCause(), isA(RuntimeException.class));
  }

  @Test
  void testPurchaseOrderParserExceptionStringThrowableBooleanBoolean() {
    var exception = Assertions.assertThrows(GobiPurchaseOrderParserException.class, () -> {
      throw new GobiPurchaseOrderParserException(testString, new RuntimeException(), true, true);
    });
    assertEquals(testString, exception.getMessage());
    assertThat(exception.getCause(), isA(RuntimeException.class));
  }
}

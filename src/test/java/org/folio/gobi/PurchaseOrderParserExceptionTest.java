package org.folio.gobi;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PurchaseOrderParserExceptionTest {
  private final String testString = "This is a test";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public final void testPurchaseOrderParserException() throws PurchaseOrderParserException {
    exception.expect(PurchaseOrderParserException.class);
    exception.expectMessage(Matchers.isEmptyOrNullString());
    exception.expectCause(Matchers.nullValue(Throwable.class));

    throw new PurchaseOrderParserException();
  }

  @Test
  public final void testPurchaseOrderParserExceptionString() throws PurchaseOrderParserException {
    exception.expect(PurchaseOrderParserException.class);
    exception.expectMessage(testString);
    exception.expectCause(Matchers.nullValue(Throwable.class));

    throw new PurchaseOrderParserException(testString);
  }

  @Test
  public final void testPurchaseOrderParserExceptionThrowable() throws PurchaseOrderParserException {
    exception.expect(PurchaseOrderParserException.class);
    exception.expectMessage(testString);
    exception.expectCause(Matchers.isA(RuntimeException.class));

    throw new PurchaseOrderParserException(new RuntimeException(testString));
  }

  @Test
  public final void testPurchaseOrderParserExceptionStringThrowable() throws PurchaseOrderParserException {
    exception.expect(PurchaseOrderParserException.class);
    exception.expectMessage(testString);
    exception.expectCause(Matchers.isA(RuntimeException.class));

    throw new PurchaseOrderParserException(testString, new RuntimeException());
  }

  @Test
  public final void testPurchaseOrderParserExceptionStringThrowableBooleanBoolean() throws PurchaseOrderParserException {
    exception.expect(PurchaseOrderParserException.class);
    exception.expectMessage(testString);
    exception.expectCause(Matchers.isA(RuntimeException.class));

    throw new PurchaseOrderParserException(testString, new RuntimeException(), true, true);
  }
}

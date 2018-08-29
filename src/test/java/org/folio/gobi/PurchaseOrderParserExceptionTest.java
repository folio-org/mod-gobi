package org.folio.gobi;

import org.folio.gobi.exceptions.GobiPurchaseOrderParserException;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PurchaseOrderParserExceptionTest {
  private final String testString = "This is a test";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public final void testPurchaseOrderParserException() throws GobiPurchaseOrderParserException {
    exception.expect(GobiPurchaseOrderParserException.class);
    exception.expectMessage(Matchers.isEmptyOrNullString());
    exception.expectCause(Matchers.nullValue(Throwable.class));

    throw new GobiPurchaseOrderParserException();
  }

  @Test
  public final void testPurchaseOrderParserExceptionString() throws GobiPurchaseOrderParserException {
    exception.expect(GobiPurchaseOrderParserException.class);
    exception.expectMessage(testString);
    exception.expectCause(Matchers.nullValue(Throwable.class));

    throw new GobiPurchaseOrderParserException(testString);
  }

  @Test
  public final void testPurchaseOrderParserExceptionThrowable() throws GobiPurchaseOrderParserException {
    exception.expect(GobiPurchaseOrderParserException.class);
    exception.expectMessage(testString);
    exception.expectCause(Matchers.isA(RuntimeException.class));

    throw new GobiPurchaseOrderParserException(new RuntimeException(testString));
  }

  @Test
  public final void testPurchaseOrderParserExceptionStringThrowable() throws GobiPurchaseOrderParserException {
    exception.expect(GobiPurchaseOrderParserException.class);
    exception.expectMessage(testString);
    exception.expectCause(Matchers.isA(RuntimeException.class));

    throw new GobiPurchaseOrderParserException(testString, new RuntimeException());
  }

  @Test
  public final void testPurchaseOrderParserExceptionStringThrowableBooleanBoolean() throws GobiPurchaseOrderParserException {
    exception.expect(GobiPurchaseOrderParserException.class);
    exception.expectMessage(testString);
    exception.expectCause(Matchers.isA(RuntimeException.class));

    throw new GobiPurchaseOrderParserException(testString, new RuntimeException(), true, true);
  }
}

package org.folio.gobi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

class MapperTest {

  @Test
  void multiplyNull() {
    assertThat(Mapper.multiply(null), is(nullValue()));
  }

  @Test
  void multiplyEmpty() {
    assertThat(multiply(), is(nullValue()));
  }

  @Test
  void multiplySingle() {
    assertThat(multiply("2.6"), is("2.6"));
  }

  @Test
  void multiplyPair() {
    assertThat(multiply("2.6", "1.2"), is("3.12"));
  }

  @Test
  void multiplyTriple() {
    assertThat(multiply("10", "0.2", "1.5"), is("3.00"));
  }

  static class NodeListMock implements NodeList {
    private final Node [] items;

    NodeListMock(String ... s) {
      this.items = new Node [s.length];
      for (int i = 0; i < s.length; i++) {
        items[i] = mock(Text.class);
        when(items[i].getTextContent()).thenReturn(s[i]);
      }
    }

    @Override
    public Node item(int index) {
      return items[index];
    }

    @Override
    public int getLength() {
      return items.length;
    }
  }

  private static String multiply(String ... s) {
    return Mapper.multiply(new NodeListMock(s));
  }
}

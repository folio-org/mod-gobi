package org.folio.gobi;

public enum GobiBooleanType {
  YES(true), NO(false);

  private final boolean booleanValue;

  GobiBooleanType(boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public boolean asBoolean(){
    return booleanValue;
  }
}

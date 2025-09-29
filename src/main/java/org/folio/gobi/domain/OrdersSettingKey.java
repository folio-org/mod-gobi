package org.folio.gobi.domain;

import lombok.Getter;

@Getter
public enum OrdersSettingKey {

  CENTRAL_ORDERING_ENABLED("ALLOW_ORDERING_WITH_AFFILIATED_LOCATIONS");

  private final String name;

  OrdersSettingKey(String name) {
    this.name = name;
  }

}

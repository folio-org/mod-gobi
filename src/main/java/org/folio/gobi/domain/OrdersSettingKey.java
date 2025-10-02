package org.folio.gobi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrdersSettingKey {

  CENTRAL_ORDERING_ENABLED("ALLOW_ORDERING_WITH_AFFILIATED_LOCATIONS");

  private final String name;
}

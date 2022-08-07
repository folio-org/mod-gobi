package org.folio.gobi.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Mapping;
;

public class BindingResult<T> {
  private Map<Mapping.Field, Error> errors = new ConcurrentHashMap<>();
  private final T result;

  public BindingResult(T result) {
    this.result = result;
  }

  public Error getError(Mapping.Field key) {
    return errors.get(key);
  }

  public List<Error> getAllErrors() {
    return new ArrayList<>(errors.values());
  }

  public void addError(Mapping.Field key, Error error) {
    this.errors.put(key, error);
  }

  public T getResult() {
    return result;
  }
}

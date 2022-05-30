package org.folio.gobi.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.folio.rest.jaxrs.model.Errors;

public class BindingResult<T> {
  private Map<String, Errors> errors = new ConcurrentHashMap<>();
  private final T result;

  public BindingResult(T result) {
    this.result = result;
  }

  public Errors getErrors(String key) {
    return errors.get(key);
  }

  public List<Errors> getAllErrors() {
    return new ArrayList<>(errors.values());
  }

  public void addErrors(String key, Errors errors) {
    this.errors.put(key, errors);
  }

  public T getResult() {
    return result;
  }
}

package org.folio.dao;

import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.persist.Conn;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.interfaces.Results;

/**
 * Implementation of OrderMappingsDao using Conn class.
 */
@Log4j2
public class OrderMappingsDaoImpl implements OrderMappingsDao {

  @Override
  public Future<List<OrderMappings>> get(Criterion criterion, int offset, int limit, Conn conn) {
    return conn.get(TABLE_NAME, OrderMappings.class, criterion, true)
      .map(Results::getResults)
      .onFailure(t -> log.error("get:: Failed to retrieve order mappings", t));
  }

  @Override
  public Future<OrderMappings> getById(String id, Conn conn) {
    return conn.getById(TABLE_NAME, id, OrderMappings.class)
      .onFailure(t -> log.error("getById:: Failed to retrieve order mapping with id: {}", id, t));
  }

  @Override
  public Future<OrderMappings> getByOrderType(String orderType, Conn conn) {
    return conn.get(TABLE_NAME, OrderMappings.class, getOrderTypeCriterion(orderType), false)
      .map(results -> {
        if (results.getResults().isEmpty()) {
          return null;
        }
        return results.getResults().getFirst();
      })
      .onFailure(t -> log.error("findByOrderType:: Failed to find order mapping by orderType: {}", orderType, t));
  }

  @Override
  public Future<OrderMappings> save(OrderMappings orderMappings, Conn conn) {
    if (StringUtils.isBlank(orderMappings.getId())) {
      orderMappings.setId(UUID.randomUUID().toString());
    }

    String id = orderMappings.getId();
    return conn.saveAndReturnUpdatedEntity(TABLE_NAME, id, orderMappings)
      .onFailure(t -> log.error("save:: Failed to save order mapping", t));
  }

  @Override
  public Future<Void> update(String id, OrderMappings orderMappings, Conn conn) {
    return conn.update(TABLE_NAME, orderMappings, id)
      .onFailure(t -> log.error("update:: Failed to update order mapping with id: {}", id, t))
      .mapEmpty();
  }

  @Override
  public Future<Void> updateByOrderType(String orderType, OrderMappings orderMappings, Conn conn) {
    return conn.update(TABLE_NAME, orderMappings, getOrderTypeCriterion(orderType), true)
      .compose(updateResult -> {
        if (updateResult.size() == 0) {
          log.warn("updateByOrderType:: No mapping found for orderType {} to update", orderType);
          return Future.failedFuture(new HttpException(404, "No matching order mapping found to update for orderType: " + orderType));
        }
        return Future.succeededFuture();
      })
      .onFailure(t -> log.error("updateByOrderType:: Failed to update order mapping for orderType: {}", orderType, t))
      .mapEmpty();
  }

  @Override
  public Future<Void> delete(String id, Conn conn) {
    return conn.delete(TABLE_NAME, id)
      .onFailure(t -> log.error("delete:: Failed to delete order mapping with id: {}", id, t))
      .mapEmpty();
  }

  private Criterion getOrderTypeCriterion(String orderType) {
    return new Criterion(new Criteria().addField("'orderType'").setOperation("=").setVal(orderType));
  }
}

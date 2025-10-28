package org.folio.dao;

import io.vertx.core.Future;
import java.util.List;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.persist.Conn;
import org.folio.rest.persist.Criteria.Criterion;

/**
 * Data Access Object for OrderMappings table.
 * Manages CRUD operations for custom order mappings stored in the database.
 */
public interface OrderMappingsDao {

  String TABLE_NAME = "order_mappings";

  /**
   * Retrieves order mappings with pagination using CQL query.
   *
   * @param criterion the CQL criterion for filtering
   * @param offset the starting position
   * @param limit the maximum number of results
   * @param conn the database connection
   * @return Future containing the order mappings collection
   */
  Future<List<OrderMappings>> get(Criterion criterion, int offset, int limit, Conn conn);

  /**
   * Retrieves a custom order mapping by ID.
   *
   * @param id the order mapping ID
   * @param conn the database connection
   * @return Future containing the order mapping, or null if not found
   */
  Future<OrderMappings> getById(String id, Conn conn);

  /**
   * Retrieves a custom order mapping by order type.
   *
   * @param orderType the order type to search for
   * @param conn the database connection
   * @return Future containing the order mapping, or null if not found
   */
  Future<OrderMappings> getByOrderType(String orderType, Conn conn);

  /**
   * Saves a new custom order mapping.
   *
   * @param orderMappings the order mapping to save
   * @param conn the database connection
   * @return Future containing the saved mapping with all database-managed fields (e.g., id, version)
   */
  Future<OrderMappings> save(OrderMappings orderMappings, Conn conn);

  /**
   * Updates an existing custom order mapping.
   *
   * @param id the order mapping ID
   * @param orderMappings the order mapping to update
   * @param conn the database connection
   * @return Future that completes when the update is done
   */
  Future<Void> update(String id, OrderMappings orderMappings, Conn conn);

  /**
   * Updates an existing custom order mapping by criterion. Returns a failed future if no matching record is found.
   *
   * @param orderType the criterion to match (e.g., orderType)
   * @param orderMappings the order mapping to update
   * @param conn the database connection
   * @return Future that completes when the update is done, or fails if no record found
   */
  Future<Void> updateByOrderType(String orderType, OrderMappings orderMappings, Conn conn);

  /**
   * Deletes a custom order mapping by ID.
   *
   * @param id the order mapping ID
   * @param conn the database connection
   * @return Future that completes when the delete is done
   */
  Future<Void> delete(String id, Conn conn);
}

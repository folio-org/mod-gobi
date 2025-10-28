package org.folio.rest.service;

import static java.util.stream.Collectors.toList;
import static org.folio.gobi.exceptions.ErrorCodes.ERROR_READING_DEFAULT_MAPPING_FILE;
import static org.folio.gobi.exceptions.ErrorCodes.ORDER_MAPPINGS_RECORD_NOT_FOUND;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.dao.OrderMappingsDao;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappingsView;
import org.folio.rest.jaxrs.model.OrderMappingsViewCollection;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import io.vertx.core.Future;
import io.vertx.core.json.Json;

public class GobiCustomMappingsService {
  private final PostgresClient pgClient;
  private final OrderMappingsDao orderMappingsDao;
  private static final Logger logger = LogManager.getLogger(GobiCustomMappingsService.class);

  public GobiCustomMappingsService(PostgresClient pgClient, OrderMappingsDao orderMappingsDao) {
    this.pgClient = pgClient;
    this.orderMappingsDao = orderMappingsDao;
  }

  public Future<OrderMappingsViewCollection> getCustomMappingListByQuery(int offset, int limit) {
    return pgClient.withConn(conn -> {
      Criterion criterion = new Criterion();
      return orderMappingsDao.get(criterion, offset, limit, conn)
        .map(this::buildOrderMappingsViewCollectionResponse);
    });
  }

  public Future<OrderMappingsView> getCustomMappingByOrderType(String orderType) {
    return pgClient.withConn(conn -> orderMappingsDao.getByOrderType(orderType, conn)
      .map(orderMapping -> {
        if (orderMapping == null) {
          return buildOrderMappingsViewResponse(null, orderType);
        } else {
          return buildOrderMappingsViewResponse(orderMapping, orderType);
        }
      }));
  }

  public Future<OrderMappingsView> postCustomMapping(OrderMappings orderMappings) {
    return pgClient.withConn(conn ->
      orderMappingsDao.save(orderMappings, conn)
        .map(savedMapping -> new OrderMappingsView()
          .withMappingType(OrderMappingsView.MappingType.CUSTOM)
          .withOrderMappings(savedMapping))
    );
  }

  public Future<Void> putCustomMapping(String orderType, OrderMappings updatedOrderMappings) {
    return pgClient.withConn(conn ->
      orderMappingsDao.updateByOrderType(orderType, updatedOrderMappings, conn));
  }

  public Future<Void> deleteCustomMapping(String orderType) {
    return pgClient.withConn(conn -> orderMappingsDao.getByOrderType(orderType, conn)
      .compose(orderMappings -> {
        if (orderMappings == null) {
          logger.warn("deleteCustomMapping:: Mapping not found for orderType={}", orderType);
          return Future.failedFuture(new HttpException(404, ORDER_MAPPINGS_RECORD_NOT_FOUND));
        }
        String id = orderMappings.getId();
        return orderMappingsDao.delete(id, conn);
      }));
  }

  private OrderMappingsView buildOrderMappingsViewResponse(OrderMappings customMapping, String orderType) {
    var omvResponse = new OrderMappingsView();
    if (customMapping == null) {
      return omvResponse
        .withOrderMappings(loadDefaultMappingByType(orderType))
        .withMappingType(OrderMappingsView.MappingType.DEFAULT);
    } else {
      return omvResponse
        .withMappingType(OrderMappingsView.MappingType.CUSTOM)
        .withOrderMappings(customMapping);
    }
  }

  /**
   * Receives a collection of custom mappings from DB and merges these
   * with default mappings, returning a list of all mappings, by first checking if a
   * custom mapping exists for an order type, otherwise uses its default mapping
   *
   * @param orderMappings Collection containing custom mappings from DB
   * @return mappings for each order type, custom if present, otherwise default is returned
   */
  private OrderMappingsViewCollection buildOrderMappingsViewCollectionResponse(List<OrderMappings> orderMappings) {
    final var customMappings = getCustomMappingsMap(orderMappings);
    var mappings = loadDefaultMappings().stream()
      .map(defMap -> new OrderMappingsView()
        .withMappingType(getMappingType(defMap, customMappings))
        .withOrderMappings(getMapping(defMap, customMappings)))
      .collect(toList());

    return new OrderMappingsViewCollection()
      .withOrderMappingsViews(mappings)
      .withTotalRecords(mappings.size());
  }

  private List<OrderMappings> loadDefaultMappings() {
    return Arrays.stream(OrderMappings.OrderType.values())
      .map(OrderMappings.OrderType::value)
      .map(this::loadDefaultMappingByType)
      .collect(toList());
  }

  private OrderMappings loadDefaultMappingByType(String orderType) {
    URL mappingJson = ClassLoader.getSystemClassLoader().getResource(orderType + ".json");
    if (mappingJson == null) {
      logger.error("loadDefaultMappingByType:: Mapping file not found for orderType={}", orderType);
      throw new HttpException(500, ERROR_READING_DEFAULT_MAPPING_FILE);
    }

    try (InputStream mappingJsonStream = mappingJson.openStream()) {
      String jsonString = new String(mappingJsonStream.readAllBytes(), StandardCharsets.UTF_8);
      return Json.decodeValue(jsonString, OrderMappings.class);
    } catch (IOException e) {
      logger.error("loadDefaultMappingByType:: Exception when reading a mappingJson file: {}", e.getMessage());
      throw new HttpException(500, ERROR_READING_DEFAULT_MAPPING_FILE);
    }
  }

  private Map<OrderMappings.OrderType, OrderMappings> getCustomMappingsMap(List<OrderMappings> customMappingsList) {
    final var customMappings = new HashMap<OrderMappings.OrderType, OrderMappings>();
    for (OrderMappings mapping : customMappingsList) {
      customMappings.put(mapping.getOrderType(), mapping);
    }
    return customMappings;
  }

  private OrderMappingsView.MappingType getMappingType(OrderMappings mapping, Map<OrderMappings.OrderType, OrderMappings> customMappings) {
    return customMappings.containsKey(mapping.getOrderType()) ? OrderMappingsView.MappingType.CUSTOM : OrderMappingsView.MappingType.DEFAULT;
  }

  private OrderMappings getMapping(OrderMappings mapping, Map<OrderMappings.OrderType, OrderMappings> customMappings) {
    return Optional.ofNullable(customMappings.get(mapping.getOrderType())).orElse(mapping);
  }
}

package org.folio.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.folio.dao.OrderMappingsDao;
import org.folio.rest.jaxrs.model.DataSource;
import org.folio.rest.jaxrs.model.DataSource.Translation;
import org.folio.rest.jaxrs.model.Mapping;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.jaxrs.model.OrderMappings.OrderType;
import org.folio.rest.jaxrs.model.OrderMappingsView;
import org.folio.rest.persist.Conn;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(MockitoExtension.class)
@ExtendWith(VertxExtension.class)
class GobiCustomMappingsServiceTest {

  @Mock
  private PostgresClient pgClient;

  @Mock
  private OrderMappingsDao orderMappingsDao;

  @Mock
  private Conn conn;

  private GobiCustomMappingsService service;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    service = new GobiCustomMappingsService(pgClient, orderMappingsDao);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (closeable != null) {
      closeable.close();
    }
  }

  @Test
  void testGetCustomMappingListByQuery(VertxTestContext vtc) {
    // Given
    OrderMappings mapping = createSampleOrderMapping(OrderType.LISTED_PRINT_MONOGRAPH);
    List<OrderMappings> customMappings = Collections.singletonList(mapping);

    when(pgClient.withConn(any())).thenAnswer(invocation -> {
      Function<Conn, Future<List<OrderMappings>>> function = invocation.getArgument(0);
      when(orderMappingsDao.get(any(Criterion.class), anyInt(), anyInt(), eq(conn)))
        .thenReturn(Future.succeededFuture(customMappings));
      return function.apply(conn);
    });

    // When/Then
    service.getCustomMappingListByQuery(0, 10)
      .onComplete(vtc.succeeding(result -> {
        assertNotNull(result);
        assertNotNull(result.getOrderMappingsViews());
        assertFalse(result.getOrderMappingsViews().isEmpty());
        vtc.completeNow();
      }));
  }

  @Test
  void testGetCustomMappingByOrderType_CustomMapping(VertxTestContext vtc) {
    // Given
    OrderType orderType = OrderType.LISTED_ELECTRONIC_MONOGRAPH;
    OrderMappings customMapping = createSampleOrderMapping(orderType);

    when(pgClient.withConn(any())).thenAnswer(invocation -> {
      Function<Conn, Future<OrderMappingsView>> function = invocation.getArgument(0);
      when(orderMappingsDao.getByOrderType(eq(orderType.value()), eq(conn)))
        .thenReturn(Future.succeededFuture(customMapping));
      return function.apply(conn);
    });

    // When/Then
    service.getCustomMappingByOrderType(orderType.value())
      .onComplete(vtc.succeeding(result -> {
        assertNotNull(result);
        assertEquals(OrderMappingsView.MappingType.CUSTOM, result.getMappingType());
        assertNotNull(result.getOrderMappings());
        assertEquals(orderType, result.getOrderMappings().getOrderType());
        vtc.completeNow();
      }));
  }

  @Test
  void testGetCustomMappingByOrderType_DefaultMapping(VertxTestContext vtc) {
    // Given - no custom mapping found
    OrderType orderType = OrderType.LISTED_PRINT_SERIAL;

    when(pgClient.withConn(any())).thenAnswer(invocation -> {
      Function<Conn, Future<OrderMappingsView>> function = invocation.getArgument(0);
      when(orderMappingsDao.getByOrderType(eq(orderType.value()), eq(conn)))
        .thenReturn(Future.succeededFuture(null));
      return function.apply(conn);
    });

    // When/Then
    service.getCustomMappingByOrderType(orderType.value())
      .onComplete(vtc.succeeding(result -> {
        assertNotNull(result);
        assertEquals(OrderMappingsView.MappingType.DEFAULT, result.getMappingType());
        assertNotNull(result.getOrderMappings());
        assertEquals(orderType, result.getOrderMappings().getOrderType());
        vtc.completeNow();
      }));
  }

  @Test
  void testPostCustomMapping(VertxTestContext vtc) {
    // Given
    OrderMappings newMapping = createSampleOrderMapping(OrderType.UNLISTED_PRINT_MONOGRAPH);
    OrderMappings savedMapping = createSampleOrderMapping(OrderType.UNLISTED_PRINT_MONOGRAPH)
      .withId(UUID.randomUUID().toString());

    when(pgClient.withConn(any())).thenAnswer(invocation -> {
      Function<Conn, Future<OrderMappingsView>> function = invocation.getArgument(0);
      when(orderMappingsDao.save(eq(newMapping), eq(conn)))
        .thenReturn(Future.succeededFuture(savedMapping));
      return function.apply(conn);
    });

    // When/Then
    service.postCustomMapping(newMapping)
      .onComplete(vtc.succeeding(result -> {
        assertNotNull(result);
        assertEquals(OrderMappingsView.MappingType.CUSTOM, result.getMappingType());
        assertNotNull(result.getOrderMappings());
        assertEquals(savedMapping.getId(), result.getOrderMappings().getId());
        vtc.completeNow();
      }));
  }

  @Test
  void testPutCustomMapping(VertxTestContext vtc) {
    // Given
    OrderType orderType = OrderType.LISTED_ELECTRONIC_MONOGRAPH;
    OrderMappings updatedMapping = createSampleOrderMapping(orderType);

    when(pgClient.withConn(any())).thenAnswer(invocation -> {
      Function<Conn, Future<Void>> function = invocation.getArgument(0);
      when(orderMappingsDao.updateByOrderType(anyString(), eq(updatedMapping), eq(conn)))
        .thenReturn(Future.succeededFuture());
      return function.apply(conn);
    });

    // When/Then
    service.putCustomMapping(orderType.value(), updatedMapping)
      .onComplete(vtc.succeeding(result -> {
        assertNull(result);
        vtc.completeNow();
      }));
  }

  @Test
  void testDeleteCustomMapping(VertxTestContext vtc) {
    // Given
    OrderType orderType = OrderType.LISTED_ELECTRONIC_MONOGRAPH;
    String existingId = UUID.randomUUID().toString();
    OrderMappings existingMapping = createSampleOrderMapping(orderType);
    existingMapping.setId(existingId);

    when(pgClient.withConn(any())).thenAnswer(invocation -> {
      Function<Conn, Future<Void>> function = invocation.getArgument(0);
      when(orderMappingsDao.getByOrderType(eq(orderType.value()), eq(conn)))
        .thenReturn(Future.succeededFuture(existingMapping));
      when(orderMappingsDao.delete(eq(existingId), eq(conn)))
        .thenReturn(Future.succeededFuture());
      return function.apply(conn);
    });

    // When/Then
    service.deleteCustomMapping(orderType.value())
      .onComplete(vtc.succeeding(result -> {
        assertNull(result);
        vtc.completeNow();
      }));
  }

  /**
   * Helper method to create a sample OrderMappings object for testing
   */
  private OrderMappings createSampleOrderMapping(OrderType orderType) {
    OrderMappings orderMappings = new OrderMappings();
    orderMappings.setOrderType(orderType);

    Mapping mapping = new Mapping();
    mapping.setField(Mapping.Field.VENDOR);

    DataSource dataSource = new DataSource();
    dataSource.setFrom("//vendor");
    dataSource.setTranslation(Translation.LOOKUP_ORGANIZATION);
    mapping.setDataSource(dataSource);

    orderMappings.setMappings(Collections.singletonList(mapping));

    return orderMappings;
  }
}

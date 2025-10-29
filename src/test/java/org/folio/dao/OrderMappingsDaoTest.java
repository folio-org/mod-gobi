package org.folio.dao;

import static org.folio.dao.OrderMappingsDao.TABLE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.folio.rest.jaxrs.model.OrderMappings;
import org.folio.rest.persist.Conn;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.interfaces.Results;
import org.folio.rest.utils.CopilotGenerated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@CopilotGenerated
class OrderMappingsDaoTest {

  @Mock
  private Conn conn;
  @Mock
  private Results<OrderMappings> results;
  @Mock
  private RowSet<Row> rowSet;

  private OrderMappingsDaoImpl dao;

  @BeforeEach
  void setUp() {
    dao = new OrderMappingsDaoImpl();
  }

  @Test
  void testGetWithCriterion_Success() {
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();

    OrderMappings mapping1 = new OrderMappings()
      .withId(id1)
      .withOrderType(OrderMappings.OrderType.LISTED_PRINT_MONOGRAPH);

    OrderMappings mapping2 = new OrderMappings()
      .withId(id2)
      .withOrderType(OrderMappings.OrderType.LISTED_PRINT_SERIAL);

    List<OrderMappings> mappingsList = List.of(mapping1, mapping2);

    when(results.getResults()).thenReturn(mappingsList);
    when(conn.get(eq(TABLE_NAME), eq(OrderMappings.class), any(Criterion.class), eq(true)))
      .thenReturn(Future.succeededFuture(results));
    Criterion criterion = new Criterion();

    Future<List<OrderMappings>> future = dao.get(criterion, 0, 10, conn);

    assertTrue(future.succeeded());
    List<OrderMappings> resultList = future.result();
    assertNotNull(resultList);
    assertThat(resultList, hasSize(2));
    assertThat(resultList.get(0).getId(), is(id1));
    assertThat(resultList.get(1).getId(), is(id2));

    verify(conn).get(eq(TABLE_NAME), eq(OrderMappings.class), eq(criterion), eq(true));
  }

  @Test
  void testGetWithCriterion_EmptyResults() {
    when(results.getResults()).thenReturn(new ArrayList<>());
    when(conn.get(eq(TABLE_NAME), eq(OrderMappings.class), any(Criterion.class), eq(true)))
      .thenReturn(Future.succeededFuture(results));

    Criterion criterion = new Criterion();

    Future<List<OrderMappings>> future = dao.get(criterion, 0, 10, conn);

    assertTrue(future.succeeded());
    List<OrderMappings> resultList = future.result();
    assertNotNull(resultList);
    assertThat(resultList, hasSize(0));
  }

  @Test
  void testGetWithCriterion_Failure() {
    String errorMessage = "Database error";
    when(conn.get(eq(TABLE_NAME), eq(OrderMappings.class), any(Criterion.class), eq(true)))
      .thenReturn(Future.failedFuture(new RuntimeException(errorMessage)));
    Criterion criterion = new Criterion();

    Future<List<OrderMappings>> future = dao.get(criterion, 0, 10, conn);

    assertTrue(future.failed());
    assertThat(future.cause().getMessage(), containsString(errorMessage));
  }

  @Test
  void testGetById_Success() {
    String id = UUID.randomUUID().toString();
    OrderMappings mapping = new OrderMappings()
      .withId(id)
      .withOrderType(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH);

    when(conn.getById(TABLE_NAME, id, OrderMappings.class))
      .thenReturn(Future.succeededFuture(mapping));

    Future<OrderMappings> future = dao.getById(id, conn);

    assertTrue(future.succeeded());
    OrderMappings result = future.result();
    assertNotNull(result);
    assertThat(result.getId(), is(id));
    assertThat(result.getOrderType(), is(OrderMappings.OrderType.LISTED_ELECTRONIC_MONOGRAPH));
    verify(conn).getById(TABLE_NAME, id, OrderMappings.class);
  }

  @Test
  void testGetById_NotFound() {
    String id = UUID.randomUUID().toString();
    when(conn.getById(TABLE_NAME, id, OrderMappings.class))
      .thenReturn(Future.succeededFuture(null));

    Future<OrderMappings> future = dao.getById(id, conn);

    assertTrue(future.succeeded());
    assertNull(future.result());
  }

  @Test
  void testGetById_Failure() {
    String id = UUID.randomUUID().toString();
    String errorMessage = "Database connection failed";
    when(conn.getById(TABLE_NAME, id, OrderMappings.class))
      .thenReturn(Future.failedFuture(new RuntimeException(errorMessage)));

    Future<OrderMappings> future = dao.getById(id, conn);

    assertTrue(future.failed());
    assertThat(future.cause().getMessage(), containsString(errorMessage));
  }

  @Test
  void testSave_WithProvidedId() {
    String id = UUID.randomUUID().toString();
    OrderMappings mapping = new OrderMappings()
      .withId(id)
      .withOrderType(OrderMappings.OrderType.UNLISTED_PRINT_MONOGRAPH);
    when(conn.saveAndReturnUpdatedEntity(eq(TABLE_NAME), eq(id), eq(mapping)))
      .thenReturn(Future.succeededFuture(mapping));

    Future<OrderMappings> future = dao.save(mapping, conn);

    assertTrue(future.succeeded());
    assertThat(future.result().getId(), is(id));
    verify(conn).saveAndReturnUpdatedEntity(eq(TABLE_NAME), eq(id), eq(mapping));
  }

  @Test
  void testSave_WithoutProvidedId() {
    OrderMappings mapping = new OrderMappings()
      .withOrderType(OrderMappings.OrderType.LISTED_PRINT_SERIAL);
    when(conn.saveAndReturnUpdatedEntity(eq(TABLE_NAME), anyString(), eq(mapping)))
      .thenReturn(Future.succeededFuture(mapping.withId(UUID.randomUUID().toString())));

    Future<OrderMappings> future = dao.save(mapping, conn);

    assertTrue(future.succeeded());
    OrderMappings result = future.result();
    assertNotNull(result);
    assertNotNull(result.getId()); // ID should be generated
    verify(conn).saveAndReturnUpdatedEntity(eq(TABLE_NAME), eq(result.getId()), eq(mapping));
  }

  @Test
  void testSave_Failure() {
    String id = UUID.randomUUID().toString();
    OrderMappings mapping = new OrderMappings()
      .withId(id)
      .withOrderType(OrderMappings.OrderType.LISTED_ELECTRONIC_SERIAL);
    String errorMessage = "Unique constraint violation";
    when(conn.saveAndReturnUpdatedEntity(eq(TABLE_NAME), eq(id), eq(mapping)))
      .thenReturn(Future.failedFuture(new RuntimeException(errorMessage)));

    Future<OrderMappings> future = dao.save(mapping, conn);

    assertTrue(future.failed());
    assertThat(future.cause().getMessage(), containsString(errorMessage));
  }

  @Test
  void testUpdate_Success() {
    String id = UUID.randomUUID().toString();
    OrderMappings mapping = new OrderMappings()
      .withId(id)
      .withOrderType(OrderMappings.OrderType.UNLISTED_PRINT_SERIAL);

    when(conn.update(TABLE_NAME, mapping, id))
      .thenReturn(Future.succeededFuture(rowSet));

    Future<Void> future = dao.update(id, mapping, conn);

    assertTrue(future.succeeded());
    verify(conn).update(TABLE_NAME, mapping, id);
  }

  @Test
  void testUpdate_Failure() {
    String id = UUID.randomUUID().toString();
    OrderMappings mapping = new OrderMappings()
      .withId(id)
      .withOrderType(OrderMappings.OrderType.LISTED_PRINT_MONOGRAPH);

    String errorMessage = "Record not found";
    when(conn.update(TABLE_NAME, mapping, id))
      .thenReturn(Future.failedFuture(new RuntimeException(errorMessage)));

    Future<Void> future = dao.update(id, mapping, conn);

    assertTrue(future.failed());
    assertThat(future.cause().getMessage(), containsString(errorMessage));
  }

  @Test
  void testDelete_Success() {
    String id = UUID.randomUUID().toString();
    when(conn.delete(TABLE_NAME, id))
      .thenReturn(Future.succeededFuture(rowSet));

    Future<Void> future = dao.delete(id, conn);

    assertTrue(future.succeeded());
    verify(conn).delete(TABLE_NAME, id);
  }

  @Test
  void testDelete_Failure() {
    String id = UUID.randomUUID().toString();
    String errorMessage = "Deletion failed";

    when(conn.delete(TABLE_NAME, id))
      .thenReturn(Future.failedFuture(new RuntimeException(errorMessage)));

    Future<Void> future = dao.delete(id, conn);

    assertTrue(future.failed());
    assertThat(future.cause().getMessage(), containsString(errorMessage));
  }

  @Test
  void testGet_WithPagination() {
    List<OrderMappings> mappingsList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      mappingsList.add(new OrderMappings()
        .withId(UUID.randomUUID().toString())
        .withOrderType(OrderMappings.OrderType.LISTED_PRINT_MONOGRAPH));
    }
    when(results.getResults()).thenReturn(mappingsList);
    when(conn.get(eq(TABLE_NAME), eq(OrderMappings.class), any(Criterion.class), eq(true)))
      .thenReturn(Future.succeededFuture(results));
    Criterion criterion = new Criterion();

    Future<List<OrderMappings>> future = dao.get(criterion, 20, 5, conn);

    assertTrue(future.succeeded());
    List<OrderMappings> resultList = future.result();
    assertNotNull(resultList);
    assertThat(resultList, hasSize(5));
  }
}

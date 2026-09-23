package com.add2numweb.workorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.add2numweb.exception.WorkOrderIdGenerationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    private WorkOrderService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-23T08:00:00Z"), ZoneOffset.UTC);
        service = new WorkOrderService(workOrderRepository, clock, new SequenceRandom(10432));
    }

    @Test
    void createsWorkOrderWithServerManagedValues() {
        when(workOrderRepository.save(any(WorkOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = service.create(
                new CreateWorkOrderRequest(" EQ-10001 ", "HIGH"));

        assertThat(response.id()).isEqualTo("WO-10432");
        assertThat(response.equipmentId()).isEqualTo("EQ-10001");
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
        assertThat(response.status()).isEqualTo(WorkOrderStatus.Open);
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-09-23T08:00:00Z"));
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    void retriesUpToThreeTimesWhenIdCollides() {
        when(workOrderRepository.save(any(WorkOrder.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"work_orders_pkey\""))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = service.create(
                new CreateWorkOrderRequest("EQ-10001", "LOW"));

        assertThat(response.id()).isEqualTo("WO-10432");
        verify(workOrderRepository, times(2)).save(any(WorkOrder.class));
    }

    @Test
    void throwsAfterThreeCollisionAttempts() {
        when(workOrderRepository.save(any(WorkOrder.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"work_orders_pkey\""));

        RuntimeException exception = captureException(
                new CreateWorkOrderRequest("EQ-10001", "MEDIUM"));

        assertThat(exception)
                .isInstanceOf(WorkOrderIdGenerationException.class)
                .hasMessageContaining("3 attempts");
        verify(workOrderRepository, times(3)).save(any(WorkOrder.class));
    }

    @Test
    void doesNotRetryNonIdDataIntegrityFailures() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("foreign key constraint violation");
        when(workOrderRepository.save(any(WorkOrder.class))).thenThrow(exception);

        RuntimeException thrown = captureException(
                new CreateWorkOrderRequest("EQ-10001", "LOW"));

        assertThat(thrown)
                .isSameAs(exception);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    void retriesWhenDuplicateIdIsReportedByNestedCause() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "could not execute statement",
                        new IllegalStateException(
                                "duplicate key violates work_orders.id"));
        when(workOrderRepository.save(any(WorkOrder.class)))
                .thenThrow(exception)
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = service.create(
                new CreateWorkOrderRequest("EQ-10001", "LOW"));

        assertThat(response.id()).isEqualTo("WO-10432");
        verify(workOrderRepository, times(2)).save(any(WorkOrder.class));
    }

    @Test
    void retriesForUniqueConstraintOnWorkOrderId() {
        when(workOrderRepository.save(any(WorkOrder.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "unique constraint violation on work_orders.id"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = service.create(
                new CreateWorkOrderRequest("EQ-10001", "LOW"));

        assertThat(response.id()).isEqualTo("WO-10432");
        verify(workOrderRepository, times(2)).save(any(WorkOrder.class));
    }

    @Test
    void retriesForPrimaryKeyViolation() {
        when(workOrderRepository.save(any(WorkOrder.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "primary key violation"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = service.create(
                new CreateWorkOrderRequest("EQ-10001", "LOW"));

        assertThat(response.id()).isEqualTo("WO-10432");
        verify(workOrderRepository, times(2)).save(any(WorkOrder.class));
    }

    @Test
    void doesNotRetryUnrelatedDuplicateKey() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("duplicate key on equipment");
        when(workOrderRepository.save(any(WorkOrder.class))).thenThrow(exception);

        RuntimeException thrown = captureException(
                new CreateWorkOrderRequest("EQ-10001", "LOW"));

        assertThat(thrown)
                .isSameAs(exception);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    void retriesWhenMessageNamesWorkOrderIdExplicitly() {
        when(workOrderRepository.save(any(WorkOrder.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key on work order id"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = service.create(
                new CreateWorkOrderRequest("EQ-10001", "LOW"));

        assertThat(response.id()).isEqualTo("WO-10432");
        verify(workOrderRepository, times(2)).save(any(WorkOrder.class));
    }

    @Test
    void doesNotRetryWhenConstraintMessageIsMissing() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(null);
        when(workOrderRepository.save(any(WorkOrder.class))).thenThrow(exception);

        RuntimeException thrown = captureException(
                new CreateWorkOrderRequest("EQ-10001", "LOW"));

        assertThat(thrown)
                .isSameAs(exception);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    void sequenceRandomReturnsConfiguredValue() {
        SequenceRandom random = new SequenceRandom(7);

        assertThat(random.nextInt(100)).isEqualTo(7);
    }

    private RuntimeException captureException(CreateWorkOrderRequest request) {
        try {
            service.create(request);
            return null;
        } catch (RuntimeException exception) {
            return exception;
        }
    }

    private static final class SequenceRandom extends Random {

        private final int value;

        private SequenceRandom(int value) {
            this.value = value;
        }

        @Override
        public int nextInt(int bound) {
            return value;
        }
    }
}

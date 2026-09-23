package com.add2numweb.workorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class WorkOrderTest {

    @Test
    void storesApprovedDomainValues() {
        Instant createdAt = Instant.parse("2026-09-23T08:00:00Z");
        WorkOrder workOrder = new WorkOrder(
                "WO-10432",
                "EQ-10001",
                Priority.HIGH,
                WorkOrderStatus.Open,
                createdAt);

        assertThat(workOrder.getId()).matches("^WO-[0-9]{5}$");
        assertThat(workOrder.getEquipmentId()).isEqualTo("EQ-10001");
        assertThat(workOrder.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.Open);
        assertThat(workOrder.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void exposesOnlyApprovedPriorityValues() {
        assertThat(Priority.values()).containsExactly(
                Priority.LOW,
                Priority.MEDIUM,
                Priority.HIGH);
    }

    @Test
    void exposesOnlyOpenInitialStatus() {
        assertThat(WorkOrderStatus.values()).containsExactly(WorkOrderStatus.Open);
    }
}

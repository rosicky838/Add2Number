package com.add2numweb.workorder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @Column(nullable = false, unique = true, length = 8, updatable = false)
    private String id;

    @Column(nullable = false, length = 50, updatable = false)
    private String equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6, updatable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4, updatable = false)
    private WorkOrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected WorkOrder() {
    }

    public WorkOrder(
            String id,
            String equipmentId,
            Priority priority,
            WorkOrderStatus status,
            Instant createdAt) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public Priority getPriority() {
        return priority;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

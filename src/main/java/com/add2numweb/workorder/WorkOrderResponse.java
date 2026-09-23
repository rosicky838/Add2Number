package com.add2numweb.workorder;

import java.time.Instant;

public record WorkOrderResponse(
        String id,
        String equipmentId,
        Priority priority,
        WorkOrderStatus status,
        Instant createdAt) {
}

package com.add2numweb.workorder;

import com.add2numweb.exception.WorkOrderIdGenerationException;
import java.time.Clock;
import java.time.Instant;
import java.util.Random;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderService {

    private static final int MAX_ID_ATTEMPTS = 3;
    private static final int ID_BOUND = 100000;

    private final WorkOrderRepository workOrderRepository;
    private final Clock clock;
    private final Random random;

    @Autowired
    public WorkOrderService(WorkOrderRepository workOrderRepository) {
        this(workOrderRepository, Clock.systemUTC(), new Random());
    }

    WorkOrderService(WorkOrderRepository workOrderRepository, Clock clock, Random random) {
        this.workOrderRepository = workOrderRepository;
        this.clock = clock;
        this.random = random;
    }

    public WorkOrderResponse create(CreateWorkOrderRequest request) {
        String equipmentId = request.equipmentId().trim();
        Priority priority = Priority.valueOf(request.priority());
        Instant createdAt = Instant.now(clock);

        for (int attempt = 0; ; attempt++) {
            WorkOrder workOrder = new WorkOrder(
                    generateId(),
                    equipmentId,
                    priority,
                    WorkOrderStatus.Open,
                    createdAt);
            try {
                WorkOrder saved = workOrderRepository.save(workOrder);
                return toResponse(saved);
            } catch (DataIntegrityViolationException exception) {
                if (!isIdCollision(exception)) {
                    throw exception;
                }
                if (attempt == MAX_ID_ATTEMPTS - 1) {
                    throw new WorkOrderIdGenerationException(
                            "Unable to generate a unique work order ID after "
                                    + MAX_ID_ATTEMPTS + " attempts.",
                            exception);
                }
            }
        }

    }

    private boolean isIdCollision(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                boolean duplicateViolation = normalized.contains("duplicate key")
                        || normalized.contains("unique constraint")
                        || normalized.contains("primary key");
                boolean workOrderId = normalized.contains("work_orders_pkey")
                        || normalized.contains("primary key")
                        || normalized.contains("work_orders.id")
                        || normalized.contains("work order id");
                if (duplicateViolation && workOrderId) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private String generateId() {
        return "WO-%05d".formatted(random.nextInt(ID_BOUND));
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getEquipmentId(),
                workOrder.getPriority(),
                workOrder.getStatus(),
                workOrder.getCreatedAt());
    }
}

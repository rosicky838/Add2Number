package com.add2numweb.workorder;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_TECHNICIAN')")
    public ResponseEntity<WorkOrderResponse> create(
            @Valid @RequestBody CreateWorkOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workOrderService.create(request));
    }
}

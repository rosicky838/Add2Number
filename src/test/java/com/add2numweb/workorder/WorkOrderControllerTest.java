package com.add2numweb.workorder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.add2numweb.config.SecurityConfig;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkOrderController.class)
@Import(SecurityConfig.class)
class WorkOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkOrderService workOrderService;

    @Test
    void createsWorkOrderForTechnician() throws Exception {
        WorkOrderResponse response = new WorkOrderResponse(
                "WO-10432",
                "EQ-10001",
                Priority.HIGH,
                WorkOrderStatus.Open,
                Instant.parse("2026-09-23T08:00:00Z"));
        when(workOrderService.create(any(CreateWorkOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/workorders")
                        .with(user("technician").roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":"EQ-10001","priority":"HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("WO-10432"))
                .andExpect(jsonPath("$.status").value("Open"));
        verify(workOrderService).create(any(CreateWorkOrderRequest.class));
    }

    @Test
    void authenticatesConfiguredInMemoryTechnician() throws Exception {
        WorkOrderResponse response = new WorkOrderResponse(
                "WO-10432",
                "EQ-10001",
                Priority.LOW,
                WorkOrderStatus.Open,
                Instant.parse("2026-09-23T08:00:00Z"));
        when(workOrderService.create(any(CreateWorkOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/workorders")
                        .with(httpBasic("technician", "test-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":"EQ-10001","priority":"LOW"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsInvalidRequestWithProblemDetails() throws Exception {
        mockMvc.perform(post("/api/workorders")
                        .with(user("technician").roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":" ","priority":"URGENT"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/workorders"));
    }

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/workorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":"EQ-10001","priority":"HIGH"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUserWithoutTechnicianRole() throws Exception {
        mockMvc.perform(post("/api/workorders")
                        .with(user("operator").roles("OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":"EQ-10001","priority":"HIGH"}
                                """))
                .andExpect(status().isForbidden());
    }
}

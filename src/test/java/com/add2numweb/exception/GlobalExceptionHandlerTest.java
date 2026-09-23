package com.add2numweb.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.add2numweb.config.SecurityConfig;
import com.add2numweb.config.JacksonConfig;
import com.add2numweb.workorder.WorkOrderController;
import com.add2numweb.workorder.WorkOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

@WebMvcTest(WorkOrderController.class)
@Import({SecurityConfig.class, JacksonConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkOrderService workOrderService;

    @Test
    void mapsValidationFailureToProblemDetails() throws Exception {
        mockMvc.perform(post("/api/workorders")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("technician")
                                .roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":"","priority":"INVALID"}
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
    void mapsMalformedJsonToProblemDetails() throws Exception {
        mockMvc.perform(post("/api/workorders")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("technician")
                                .roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipmentId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Malformed request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/api/workorders"));
    }

    @Test
    void rejectsUnknownRequestFields() throws Exception {
        mockMvc.perform(post("/api/workorders")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("technician")
                                .roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":"EQ-10001","priority":"HIGH","status":"Open"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Malformed request"));
    }

    @Test
    void mapsIdGenerationFailureToProblemDetails() throws Exception {
        when(workOrderService.create(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new WorkOrderIdGenerationException(
                        "Unable to generate a unique work order ID."));

        mockMvc.perform(post("/api/workorders")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("technician")
                                .roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":"EQ-10001","priority":"HIGH"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Work order ID generation failed"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail")
                        .value("Unable to generate a unique work order ID."));
    }

    @Test
    void mapsPersistenceFailureToProblemDetails() throws Exception {
        when(workOrderService.create(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataIntegrityViolationException("database unavailable"));

        mockMvc.perform(post("/api/workorders")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("technician")
                                .roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":"EQ-10001","priority":"HIGH"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Persistence failure"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail")
                        .value("The work order could not be persisted."));
    }
}

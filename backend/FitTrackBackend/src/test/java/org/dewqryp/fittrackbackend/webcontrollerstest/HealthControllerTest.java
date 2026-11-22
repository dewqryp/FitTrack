package org.dewqryp.fittrackbackend.webcontrollerstest;

import org.dewqryp.fittrackbackend.auth.security.JwtAuthenticationFilter;
import org.dewqryp.fittrackbackend.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(controllers = HealthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class)
)
public class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetHealth() throws Exception {
        mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("fittrack-backend"));

    }

}

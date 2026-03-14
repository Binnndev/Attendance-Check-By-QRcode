package com.attendance.backend.stats.api;

import com.attendance.backend.security.UserPrincipal;
import com.attendance.backend.security.jwt.JwtAuthenticationFilter;
import com.attendance.backend.support.AbstractMySqlIntegrationTest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(
        scripts = {
                "/sql/cleanup/cleanup_all.sql",
                "/sql/stats/attendance_summary_seed.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup/cleanup_all.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class AttendanceStatsControllerIT extends AbstractMySqlIntegrationTest {

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private static final UUID OWNER_A    = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID MEMBER_A1  = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final UUID OWNER_ZERO = UUID.fromString("10000000-0000-0000-0000-000000000006");

    private static final UUID GROUP_A    = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID GROUP_ZERO = UUID.fromString("20000000-0000-0000-0000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void allowJwtFilterToPassThrough() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Student xem được summary của chính mình")
    void getMyAttendanceSummary_studentCanViewOwnSummary() throws Exception {
        mockMvc.perform(get("/api/v1/me/attendance/summary")
                        .with(authUser(MEMBER_A1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSessions").value(4))
                .andExpect(jsonPath("$.presentCount").value(1))
                .andExpect(jsonPath("$.lateCount").value(1))
                .andExpect(jsonPath("$.absentCount").value(1))
                .andExpect(jsonPath("$.excusedCount").value(1))
                .andExpect(jsonPath("$.attendancePercent").value(75.00))
                .andExpect(jsonPath("$.absencePercent").value(25.00))
                .andExpect(jsonPath("$.warningLevel").value("CRITICAL_20"))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"));
    }

    @Test
    @DisplayName("Group có 0 CLOSED session vẫn trả paged summary với count = 0")
    void getGroupAttendanceSummary_groupWithNoClosedSessions_returnsZeroSummary() throws Exception {
        mockMvc.perform(get("/api/v1/groups/{groupId}/attendance/summary", GROUP_ZERO)
                        .param("page", "0")
                        .param("size", "20")
                        .with(authUser(OWNER_ZERO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[*].totalSessions", hasItem(0)))
                .andExpect(jsonPath("$.items[*].warningLevel", hasItem("NONE")))
                .andExpect(jsonPath("$.items[*].riskLevel", hasItem("LOW")));
    }

    @Test
    @DisplayName("Non-host gọi group summary bị 403")
    void getGroupAttendanceSummary_nonHostGetsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/groups/{groupId}/attendance/summary", GROUP_A)
                        .param("page", "0")
                        .param("size", "20")
                        .with(authUser(MEMBER_A1)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Only OWNER/CO_HOST can view group attendance summary"))
                .andExpect(jsonPath("$.path").value("/api/v1/groups/" + GROUP_A + "/attendance/summary"));
    }

    @Test
    @DisplayName("OWNER gọi group summary thành công")
    void getGroupAttendanceSummary_ownerCanViewGroupSummary() throws Exception {
        mockMvc.perform(get("/api/v1/groups/{groupId}/attendance/summary", GROUP_A)
                        .param("page", "0")
                        .param("size", "20")
                        .with(authUser(OWNER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    private RequestPostProcessor authUser(UUID userId) {
        UserPrincipal principal = new UserPrincipal() {
            @Override
            public UUID getUserId() {
                return userId;
            }

            @Override
            public UUID getSessionId() {
                return UUID.fromString("90000000-0000-0000-0000-000000000001");
            }

            @Override
            public String getRole() {
                return "USER";
            }
        };

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return authentication(auth);
    }
}
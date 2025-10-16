package sa.cerebra.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import sa.cerebra.task.dto.request.LoginRequest;
import sa.cerebra.task.dto.request.ValidateOtpRequest;
import sa.cerebra.task.dto.response.TokenResponse;
import sa.cerebra.task.security.AuthService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void login_returnsOk_and_callsService() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setPhone("+1000");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService).login("+1000");
    }

    @Test
    void validateOtp_returnsToken() throws Exception {
        ValidateOtpRequest req = new ValidateOtpRequest();
        req.setPhone("+1000");
        req.setOtp("111111");
        when(authService.validate("+1000", "111111"))
                .thenReturn(TokenResponse.builder().accessToken("tok").build());

        mockMvc.perform(post("/api/v1/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("tok"));
    }
}



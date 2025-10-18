package sa.cerebra.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import sa.cerebra.task.BaseIntegrationTest;
import sa.cerebra.task.dto.request.LoginRequest;
import sa.cerebra.task.dto.request.ValidateOtpRequest;
import sa.cerebra.task.dto.response.TokenResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.repository.UserRepository;
import sa.cerebra.task.security.JwtUtil;
import sa.cerebra.task.sms.SendSms;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


class AuthControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private SendSms sendSms;

    private User testUser;

    @BeforeEach
    void setUp() {
        var phone="+1234567890";
        // Create and save test user
        testUser = userRepository.findByPhone(phone).orElseGet(()-> {
            var user = new User();
            user.setPhone(phone);
            return userRepository.save(user);
        });
    }

    @Test
    void completeLoginFlow_loginAndValidateOtp_shouldReturnValidToken() throws Exception {
        // Arrange - Set up SMS mock to capture OTP
        AtomicReference<String> capturedOtp = new AtomicReference<>();
        AtomicReference<String> capturedPhone = new AtomicReference<>();

        doAnswer(invocation -> {
            capturedPhone.set(invocation.getArgument(0));
            String msg = invocation.getArgument(1).toString();
            capturedOtp.set(msg.substring(msg.length()-6));
            return null;
        }).when(sendSms).send(anyString(), anyString());

        String phoneNumber = "+1234567890";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPhone(phoneNumber);

        // Act - Step 1: Call login endpoint
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // Assert - Step 1: Verify SMS was called and OTP was captured
        verify(sendSms).send(eq(phoneNumber), anyString());
        assertThat(capturedPhone.get()).isEqualTo(phoneNumber);
        assertThat(capturedOtp.get()).isNotNull();

        // Act - Step 2: Call validate OTP endpoint
        ValidateOtpRequest validateRequest = new ValidateOtpRequest();
        validateRequest.setPhone(phoneNumber);
        validateRequest.setOtp(capturedOtp.get());

        String responseContent = mockMvc.perform(post("/api/v1/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert - Step 2: Verify token is returned and valid
        TokenResponse tokenResponse = objectMapper.readValue(responseContent, TokenResponse.class);
        assertThat(tokenResponse.getAccessToken()).isNotNull();
        
        // Verify the token is valid by checking it's not expired and can extract user ID
        assertThat(jwtUtil.isTokenExpired(tokenResponse.getAccessToken())).isFalse();
        String extractedUserId = jwtUtil.extractUsername(tokenResponse.getAccessToken());
        assertThat(extractedUserId).isEqualTo(testUser.getId().toString());
        
        // Verify SMS was called exactly once
        verify(sendSms, times(1)).send(anyString(), anyString());
    }

    @Test
    void login_returnsOk_and_callsService() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setPhone("+1000");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(sendSms).send(eq("+1000"), anyString());
    }

}



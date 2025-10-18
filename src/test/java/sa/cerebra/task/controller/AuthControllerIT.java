package sa.cerebra.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sa.cerebra.task.BaseIntegrationTest;
import sa.cerebra.task.cache.CacheStore;
import sa.cerebra.task.dto.request.LoginRequest;
import sa.cerebra.task.dto.request.ValidateOtpRequest;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.helper.OtpHelper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheStore cacheStore;


    @Test
    @DisplayName("POST /api/v1/auth/login returns 200 and calls service")
    void login_ShouldReturnOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setPhone("+1234567890");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 400 when phone is blank")
    void login_ShouldReturnBadRequest_WhenPhoneBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setPhone("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("bad request")))
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    @DisplayName("POST Login then validate otp (successful flow)")
    void loginValidateSuccessfulFlow() throws Exception {
        var phone = "+1234567890";
        LoginRequest request = new LoginRequest();
        request.setPhone(phone);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        // Get the actual OTP from cache
        String actualOtp = (String) cacheStore.get("login-otp", phone);
        assertNotNull(actualOtp, "OTP should be stored in cache after login");
        
        ValidateOtpRequest validateOtpRequest = new ValidateOtpRequest();
        validateOtpRequest.setPhone(phone);
        validateOtpRequest.setOtp(actualOtp);

        mockMvc.perform(post("/api/v1/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists());
    }
    @Test
    @DisplayName("POST Login then validate otp (wrong otp flow)")
    void loginValidateFailedFlow() throws Exception {
        var phone = "+1234567890";
        LoginRequest request = new LoginRequest();
        request.setPhone(phone);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());


        ValidateOtpRequest validateOtpRequest = new ValidateOtpRequest();
        validateOtpRequest.setPhone(phone);
        validateOtpRequest.setOtp(OtpHelper.generateRandomOtp());

        mockMvc.perform(post("/api/v1/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateOtpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/auth/validate-otp returns 400 on INVALID_OTP")
    void validateOtp_ShouldReturnBadRequest_OnInvalidOtp() throws Exception {
        ValidateOtpRequest request = new ValidateOtpRequest();
        request.setPhone("+1234567890");
        request.setOtp("000000");


        mockMvc.perform(post("/api/v1/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errorCode", equalTo(ErrorCode.INVALID_OTP.getCode())));
    }

    @Test
    @DisplayName("POST /api/v1/auth/validate-otp returns 400 when fields blank")
    void validateOtp_ShouldReturnBadRequest_WhenFieldsBlank() throws Exception {
        ValidateOtpRequest request = new ValidateOtpRequest();
        request.setPhone("");
        request.setOtp("");

        mockMvc.perform(post("/api/v1/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("bad request")))
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.otp").exists());
    }
}



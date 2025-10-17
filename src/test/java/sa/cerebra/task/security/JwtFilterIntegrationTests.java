package sa.cerebra.task.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sa.cerebra.task.config.Configs;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.repository.UserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtFilterIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @BeforeEach
    void setupUser() {
        User u = new User();
        u.setPhone("+3000");
        userRepository.save(u);
        Configs configs = new Configs();
        // Configure a stable signing key and short expiration for tests
        setField(configs, "signingKey", "test-signing-key-256-bit-test-signing-key-256-bit");
        setField(configs, "accessTokenExpiration", 2000L);
        jwtUtil = new JwtUtil(configs);

    }

    @Test
    void rejects_request_with_expired_token() throws Exception {
        String expiredToken = jwtUtil.generateToken(1L);
        Thread.sleep(2100L); // ensure expiration (configured to 1s in test properties)

        mockMvc.perform(get("/api/v1/files")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().string(containsString("Token expired")));
    }
}



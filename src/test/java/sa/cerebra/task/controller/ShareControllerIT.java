package sa.cerebra.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import sa.cerebra.task.BaseIntegrationTest;
import sa.cerebra.task.cache.CacheStore;
import sa.cerebra.task.dto.request.CreateShareLinkRequest;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.helper.JwtHelper;
import sa.cerebra.task.repository.UserRepository;

import java.util.Random;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShareControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserRepository userRepository;



    private User testUser;
    private String authToken;
    private String uploadedFilePath;

    @BeforeEach
    void setUp() throws Exception {
        // Create a test user
        testUser = new User();
        testUser.setPhone("+123" + new Random().nextInt(100000));
        testUser = userRepository.save(testUser);
        
        // Generate JWT token for the test user
        authToken = jwtHelper.generateToken(testUser.getId());
        
        // Upload a test file first
        uploadedFilePath = uploadTestFile();
    }

    private String uploadTestFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", 
                "share-test.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "This is a test file for sharing".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated());

        return "share-test.txt";
    }

    @Test
    @DisplayName("POST /api/v1/share should return 200 with share link when authenticated")
    void createShareLink_ShouldReturnOk_WhenAuthenticated() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath(uploadedFilePath);
        request.setExpirationDays(7);

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.link").exists())
                .andExpect(jsonPath("$.link").value(startsWith("http://localhost:8080/api/v1/share/")));
    }

    @Test
    @DisplayName("POST /api/v1/share should return 200 with share link and SMS notification when phone provided")
    void createShareLink_ShouldReturnOk_WithSmsNotification_WhenPhoneProvided() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath(uploadedFilePath);
        request.setRecipientPhone("+1234567890");
        request.setExpirationDays(3);

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.link").exists())
                .andExpect(jsonPath("$.link").value(startsWith("http://localhost:8080/api/v1/share/")));
    }

    @Test
    @DisplayName("GET /api/v1/share/{token} should return 200 with file content when valid token")
    void downloadShareLink_ShouldReturnOk_WhenValidToken() throws Exception {
        // First create a share link
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath(uploadedFilePath);
        request.setExpirationDays(7);

        String responseContent = mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the share token from the response
        String shareLink = objectMapper.readTree(responseContent).get("link").asText();
        String shareToken = shareLink.substring(shareLink.lastIndexOf("/") + 1);

        // Then download using the share token
        mockMvc.perform(get("/api/v1/share/" + shareToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string("This is a test file for sharing"));
    }

    @Test
    @DisplayName("GET /api/v1/share/{token} should return 400 when token is invalid/expired")
    void downloadShareLink_ShouldReturnBadRequest_WhenInvalidToken() throws Exception {
        String invalidToken = "invalid-token-12345";

        mockMvc.perform(get("/api/v1/share/" + invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Share link has expired")))
                .andExpect(jsonPath("$.errorCode", is(ErrorCode.SHARE_LINK_EXPIRED.getCode())));
    }

    @Test
    @DisplayName("POST /api/v1/share should return 401 when not authenticated")
    void createShareLink_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath(uploadedFilePath);
        request.setExpirationDays(7);

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/share should return 400 when path is blank")
    void createShareLink_ShouldReturnBadRequest_WhenPathBlank() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath("");
        request.setExpirationDays(7);

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.errors.path").exists());
    }

    @Test
    @DisplayName("POST /api/v1/share should return 400 when path contains invalid characters")
    void createShareLink_ShouldReturnBadRequest_WhenInvalidPath() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath("../../etc/passwd");
        request.setExpirationDays(7);

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/share should return 400 when phone number format is invalid")
    void createShareLink_ShouldReturnBadRequest_WhenInvalidPhone() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath(uploadedFilePath);
        request.setRecipientPhone("invalid-phone");
        request.setExpirationDays(7);

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.errors.recipientPhone").exists());
    }

    @Test
    @DisplayName("POST /api/v1/share should work with valid phone number formats")
    void createShareLink_ShouldWork_WithValidPhoneFormats() throws Exception {
        String[] validPhones = {"+1234567890", "1234567890", "+12345678901234"};

        for (String phone : validPhones) {
            CreateShareLinkRequest request = new CreateShareLinkRequest();
            request.setPath(uploadedFilePath);
            request.setRecipientPhone(phone);
            request.setExpirationDays(1);

            mockMvc.perform(post("/api/v1/share")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.link").exists());
        }
    }

    @Test
    @DisplayName("POST /api/v1/share should use default expiration days when not provided")
    void createShareLink_ShouldUseDefaultExpiration_WhenNotProvided() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath(uploadedFilePath);
        // expirationDays not set, should default to 7

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.link").exists());
    }

    @Test
    @DisplayName("GET /api/v1/share/{token} should work without authentication (public access)")
    void downloadShareLink_ShouldWork_WithoutAuthentication() throws Exception {
        // First create a share link
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath(uploadedFilePath);
        request.setExpirationDays(7);

        String responseContent = mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the share token from the response
        String shareLink = objectMapper.readTree(responseContent).get("link").asText();
        String shareToken = shareLink.substring(shareLink.lastIndexOf("/") + 1);

        // Download without authentication should work
        mockMvc.perform(get("/api/v1/share/" + shareToken))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a test file for sharing"));
    }
}

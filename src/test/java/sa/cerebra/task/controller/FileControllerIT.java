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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sa.cerebra.task.BaseIntegrationTest;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.helper.JwtHelper;
import sa.cerebra.task.repository.UserRepository;

import java.util.Random;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Create a test user

        testUser = new User();
        testUser.setPhone("+123"+ new Random().nextInt(100000));
        testUser = userRepository.save(testUser);
        
        // Generate JWT token for the test user
        authToken = jwtHelper.generateToken(testUser.getId());
    }

    @Test
    @DisplayName("GET /api/v1/files should return 200 with file list when authenticated")
    void listFiles_ShouldReturnOk_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/files")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0))); // Empty list initially
    }

    @Test
    @DisplayName("GET /api/v1/files should return 401 with file list when expired authentication")
    void listFiles_expired_token() throws Exception {
        String expiredToken = jwtHelper.generateToken(testUser.getId(), -1l);
        mockMvc.perform(get("/api/v1/files")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/files should return 200 with file list for specific path when authenticated")
    void listFiles_ShouldReturnOk_WithPath_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/files")
                        .param("path", "uploads")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0))); // Empty list initially
    }

    @Test
    @DisplayName("POST /api/v1/files should return 201 with uploaded files when authenticated")
    void uploadFiles_ShouldReturnCreated_WhenAuthenticated() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", 
                "test1.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "Hello World 1".getBytes()
        );
        
        MockMultipartFile file2 = new MockMultipartFile(
                "files", 
                "test2.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "Hello World 2".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file1)
                        .file(file2)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("test1.txt")))
                .andExpect(jsonPath("$[1].name", is("test2.txt")));
    }

    @Test
    @DisplayName("POST /api/v1/files should return 201 with uploaded files to specific path when authenticated")
    void uploadFiles_ShouldReturnCreated_WithPath_WhenAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", 
                "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "Hello World".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("path", "documents")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("test.txt")));
    }

    @Test
    @DisplayName("GET /api/v1/files/download should return 200 with file content when authenticated")
    void downloadFile_ShouldReturnOk_WhenAuthenticated() throws Exception {
        // First upload a file
        MockMultipartFile file = new MockMultipartFile(
                "files", 
                "download-test.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "Download test content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated());

        // Then download it
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "download-test.txt")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"download-test.txt\""))
                .andExpect(content().string("Download test content"));
    }

    @Test
    @DisplayName("GET /api/v1/files/download should return 200 with preview when preview=true")
    void downloadFile_ShouldReturnOk_WithPreview_WhenAuthenticated() throws Exception {
        // First upload a file
        MockMultipartFile file = new MockMultipartFile(
                "files", 
                "preview-test.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "Preview test content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated());

        // Then download it with preview
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "preview-test.txt")
                        .param("preview", "true")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"preview-test.txt\""))
                .andExpect(content().string("Preview test content"));
    }

    @Test
    @DisplayName("GET /api/v1/files should return 401 when not authenticated")
    void listFiles_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/files should return 401 when not authenticated")
    void uploadFiles_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", 
                "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "Hello World".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/files/download should return 401 when not authenticated")
    void downloadFile_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "test.txt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/files should return 400 when path contains invalid characters")
    void listFiles_ShouldReturnBadRequest_WhenInvalidPath() throws Exception {
        mockMvc.perform(get("/api/v1/files")
                        .param("path", "../../etc/passwd")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/files should return 400 when path contains invalid characters")
    void uploadFiles_ShouldReturnBadRequest_WhenInvalidPath() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", 
                "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "Hello World".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("path", "../../etc")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/files/download should return 400 when path contains invalid characters")
    void downloadFile_ShouldReturnBadRequest_WhenInvalidPath() throws Exception {
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "../../etc/passwd")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }
}

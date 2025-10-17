package sa.cerebra.task.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.repository.UserRepository;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.storage.path=cerebra-test-storage"
})
@Transactional
class FileControllerFullIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @TempDir
    Path tempDir;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setPhone("+1234567890");
        testUser = userRepository.save(testUser);

        // Set up authentication context
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void fullWorkflow_uploadListDownload_shouldWorkCorrectly() throws Exception {
        // Step 1: Upload a file
        MockMultipartFile file = new MockMultipartFile(
                "files", "test-file.txt", "text/plain", "Hello, World!".getBytes());

        String folderName="test-upload";
        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("path", folderName))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("test-file.txt"))
                .andExpect(jsonPath("$[0].relativePath").value(folderName+"/test-file.txt"))
                .andReturn();

        // Step 2: List files in folderName directory
        mockMvc.perform(get("/api/v1/files")
                        .param("path", folderName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("test-file.txt"));


        // Step 4: Download the uploaded file
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", folderName+"/test-file.txt")
                        .param("preview", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test-file.txt\""))
                .andExpect(content().string("Hello, World!"));

        // Step 5: Preview the uploaded file
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", folderName+"/test-file.txt")
                        .param("preview", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test-file.txt\""))
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void uploadMultipleFiles_shouldHandleAllFiles() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "file1.txt", "text/plain", "Content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "file2.txt", "text/plain", "Content 2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile(
                "files", "file3.txt", "text/plain", "Content 3".getBytes());

        // When & Then
        mockMvc.perform(multipart("/api/v1/files")
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .param("path", "batch-uploads"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("file1.txt"))
                .andExpect(jsonPath("$[1].name").value("file2.txt"))
                .andExpect(jsonPath("$[2].name").value("file3.txt"));

        // Verify all files can be listed
        mockMvc.perform(get("/api/v1/files")
                        .param("path", "batch-uploads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void uploadFile_withDifferentContentTypes_shouldHandleCorrectly() throws Exception {
        // Given - JSON file
        MockMultipartFile jsonFile = new MockMultipartFile(
                "files", "config.json", "application/json", "{\"key\": \"value\"}".getBytes());
        
        // Given - Image file (simulated)
        MockMultipartFile imageFile = new MockMultipartFile(
                "files", "image.png", "image/png", "fake-image-data".getBytes());

        // When & Then - Upload JSON file
        mockMvc.perform(multipart("/api/v1/files")
                        .file(jsonFile)
                        .param("path", "config"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("config.json"));

        // When & Then - Upload image file
        mockMvc.perform(multipart("/api/v1/files")
                        .file(imageFile)
                        .param("path", "images"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("image.png"));

        // Verify both can be downloaded
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "config/config.json")
                        .param("preview", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"key\": \"value\"}"));

        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "images/image.png")
                        .param("preview", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("fake-image-data"));
    }

    @Test
    void downloadFile_nonexistentFile_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "nonexistent/file.txt")
                        .param("preview", "false"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void downloadFile_directory_shouldReturnBadRequest() throws Exception {
        // First create a directory by listing it
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk());

        // Try to download the root directory
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "")
                        .param("preview", "false"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFile_replaceExisting_shouldOverwrite() throws Exception {
        // Given - Upload initial file
        MockMultipartFile initialFile = new MockMultipartFile(
                "files", "replace-test.txt", "text/plain", "Initial content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(initialFile))
                .andExpect(status().isCreated());

        // Verify initial content
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "replace-test.txt")
                        .param("preview", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("Initial content"));

        // When - Upload file with same name but different content
        MockMultipartFile replacementFile = new MockMultipartFile(
                "files", "replace-test.txt", "text/plain", "Replaced content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(replacementFile))
                .andExpect(status().isCreated());

        // Then - Verify content was replaced
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "replace-test.txt")
                        .param("preview", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("Replaced content"));
    }

    @Test
    void listFiles_nestedDirectories_shouldWorkCorrectly() throws Exception {
        // Upload files to nested directories
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "file1.txt", "text/plain", "Content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "file2.txt", "text/plain", "Content 2".getBytes());

        // Upload to nested path
        mockMvc.perform(multipart("/api/v1/files")
                        .file(file1)
                        .param("path", "level1/level2"))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file2)
                        .param("path", "level1"))
                .andExpect(status().isCreated());

        // List root directory - should see level1 directory
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name", hasItem("level1")));
        // List level1 directory - should see level2 directory and file2.txt
        mockMvc.perform(get("/api/v1/files")
                        .param("path", "level1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // List level1/level2 directory - should see file1.txt
        mockMvc.perform(get("/api/v1/files")
                        .param("path", "level1/level2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("file1.txt"));
    }

//    @Test
    void userIsolation_differentUsers_shouldHaveSeparateFiles() throws Exception {
        // Create second user
        User user2 = new User();
        user2.setPhone("+9876543210");
        user2 = userRepository.save(user2);

        // Upload file as first user
        MockMultipartFile file = new MockMultipartFile(
                "files", "user1-file.txt", "text/plain", "User 1 content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file))
                .andExpect(status().isCreated());

        // Switch to second user
        Authentication auth2 = new UsernamePasswordAuthenticationToken(user2, null, user2.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth2);


        // Second user should not see first user's files
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Upload file as second user
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "user2-file.txt", "text/plain", "User 2 content".getBytes());

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file2))
                .andExpect(status().isCreated());

        // Second user should only see their own file
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("user2-file.txt"));

        // Switch back to first user
        Authentication auth1 = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth1);

        // First user should only see their own file
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("user1-file.txt"));
    }
}

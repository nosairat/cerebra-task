package sa.cerebra.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.model.FileModel;
import sa.cerebra.task.service.FileService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileService fileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("+1234567890");

        // Set up authentication context
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void listFiles_shouldReturnFileList() throws Exception {
        // Given
        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder()
                        .name("file1.txt")
                        .relativePath("file1.txt")
                        .uploadDate(LocalDateTime.now())
                        .build(),
                FileModel.builder()
                        .name("file2.txt")
                        .relativePath("file2.txt")
                        .uploadDate(LocalDateTime.now())
                        .build()
        );
        when(fileService.listFiles(eq(testUser), any(String.class))).thenReturn(expectedFiles);

        // When & Then
        mockMvc.perform(get("/api/v1/files")
                        .param("path", "/test/path"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("file1.txt"))
                .andExpect(jsonPath("$[1].name").value("file2.txt"));
    }

    @Test
    void listFiles_withoutPath_shouldReturnFileList() throws Exception {
        // Given
        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder()
                        .name("file1.txt")
                        .relativePath("file1.txt")
                        .uploadDate(LocalDateTime.now())
                        .build()
        );
        when(fileService.listFiles(eq(testUser), eq(null))).thenReturn(expectedFiles);

        // When & Then
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("file1.txt"));
    }

    @Test
    void uploadFiles_shouldReturnCreatedFiles() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "test1.txt", "text/plain", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "test2.txt", "text/plain", "content2".getBytes());

        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder()
                        .name("test1.txt")
                        .relativePath("uploads/test1.txt")
                        .uploadDate(LocalDateTime.now())
                        .build(),
                FileModel.builder()
                        .name("test2.txt")
                        .relativePath("uploads/test2.txt")
                        .uploadDate(LocalDateTime.now())
                        .build()
        );
        when(fileService.uploadMultipleFiles(eq(testUser), any(MultipartFile[].class), eq("uploads")))
                .thenReturn(expectedFiles);

        // When & Then
        mockMvc.perform(multipart("/api/v1/files")
                        .file(file1)
                        .file(file2)
                        .param("path", "uploads"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("test1.txt"))
                .andExpect(jsonPath("$[1].name").value("test2.txt"));
    }

    @Test
    void uploadFiles_withoutPath_shouldReturnCreatedFiles() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "files", "test.txt", "text/plain", "content".getBytes());

        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder()
                        .name("test.txt")
                        .relativePath("test.txt")
                        .uploadDate(LocalDateTime.now())
                        .build()
        );
        when(fileService.uploadMultipleFiles(eq(testUser), any(MultipartFile[].class), eq(null)))
                .thenReturn(expectedFiles);

        // When & Then
        mockMvc.perform(multipart("/api/v1/files")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("test.txt"));
    }

    @Test
    void downloadFile_withPreview_shouldReturnInlineContent() throws Exception {
        // Given
        String filePath = "test.txt";
        ByteArrayResource resource = new ByteArrayResource("file content".getBytes());
        resource.getFilename(); // Initialize filename
        
        when(fileService.downloadFile(eq(testUser), eq(filePath))).thenReturn(resource);

        // When & Then
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", filePath)
                        .param("preview", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"" + resource.getFilename() + "\""))
                .andExpect(content().string("file content"));
    }

    @Test
    void downloadFile_withoutPreview_shouldReturnAttachment() throws Exception {
        // Given
        String filePath = "test.txt";
        ByteArrayResource resource = new ByteArrayResource("file content".getBytes());
        resource.getFilename(); // Initialize filename
        
        when(fileService.downloadFile(eq(testUser), eq(filePath))).thenReturn(resource);

        // When & Then
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", filePath)
                        .param("preview", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\""))
                .andExpect(content().string("file content"));
    }

    @Test
    void downloadFile_missingPath_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/files/download")
                        .param("preview", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void downloadFile_missingPreview_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", "test.txt"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFiles_missingFiles_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/files")
                        .param("path", "uploads"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFiles_emptyFiles_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/files")
                        .param("path", "uploads"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listFiles_withEmptyPath_shouldReturnFileList() throws Exception {
        // Given
        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder()
                        .name("file1.txt")
                        .relativePath("file1.txt")
                        .uploadDate(LocalDateTime.now())
                        .build()
        );
        when(fileService.listFiles(eq(testUser), eq(""))).thenReturn(expectedFiles);

        // When & Then
        mockMvc.perform(get("/api/v1/files")
                        .param("path", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("file1.txt"));
    }

    @Test
    void uploadFiles_withSpecialCharacters_shouldHandleCorrectly() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "files", "test-file_123.txt", "text/plain", "content".getBytes());

        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder()
                        .name("test-file_123.txt")
                        .relativePath("uploads/test-file_123.txt")
                        .uploadDate(LocalDateTime.now())
                        .build()
        );
        when(fileService.uploadMultipleFiles(eq(testUser), any(MultipartFile[].class), eq("uploads")))
                .thenReturn(expectedFiles);

        // When & Then
        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("path", "uploads"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("test-file_123.txt"));
    }

    @Test
    void downloadFile_withSpecialCharacters_shouldHandleCorrectly() throws Exception {
        // Given
        String filePath = "test-file_123.txt";
        ByteArrayResource resource = new ByteArrayResource("file content".getBytes());
        resource.getFilename(); // Initialize filename
        
        when(fileService.downloadFile(eq(testUser), eq(filePath))).thenReturn(resource);

        // When & Then
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", filePath)
                        .param("preview", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string("file content"));
    }
}

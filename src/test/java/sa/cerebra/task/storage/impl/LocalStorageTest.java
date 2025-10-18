package sa.cerebra.task.storage.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.model.FileModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class LocalStorageTest {

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private LocalStorage localStorage;

    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Inject storageBasePath via reflection since it's @Value injected in prod
        var field = LocalStorage.class.getDeclaredField("storageBasePath");
        field.setAccessible(true);
        field.set(localStorage, "tmp/cerebra-storage");

        tempDir = Files.createTempDirectory("local-storage-test");
        // Override getFullPath to use tempDir for tests
        var getFullPathMethod = LocalStorage.class.getDeclaredMethod("getFullPath", String.class);
        getFullPathMethod.setAccessible(true);
        // We'll use reflection to mock the behavior or create a test-specific instance
    }

    @Test
    void list_ShouldReturnEmptyList_WhenPathDoesNotExist() {
        // Given
        String nonExistentPath = "nonexistent";

        // When
        List<FileModel> result = localStorage.list(nonExistentPath);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void list_ShouldReturnSingleFile_WhenPathIsFile() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "content".getBytes());
        String filePath = testFile.getFileName().toString();

        // When
        List<FileModel> result = localStorage.list(filePath);

        // Then
        assertEquals(1, result.size());
        assertEquals("test.txt", result.get(0).getName());
        assertNotNull(result.get(0).getUploadDate());
    }

    @Test
    void list_ShouldReturnAllFiles_WhenPathIsDirectory() throws IOException {
        // Given
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectories(subDir);
        Files.write(subDir.resolve("file1.txt"), "content1".getBytes());
        Files.write(subDir.resolve("file2.txt"), "content2".getBytes());

        // When
        List<FileModel> result = localStorage.list("subdir");

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(f -> "file1.txt".equals(f.getName())));
        assertTrue(result.stream().anyMatch(f -> "file2.txt".equals(f.getName())));
    }

    @Test
    void upload_ShouldCreateDirectoryAndUploadFiles() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("uploaded.txt");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        // When
        List<FileModel> result = localStorage.upload(new MultipartFile[]{multipartFile}, "uploads");

        // Then
        assertEquals(1, result.size());
        assertEquals("uploaded.txt", result.get(0).getName());
        assertNotNull(result.get(0).getUploadDate());
    }

    @Test
    void upload_ShouldHandleMultipleFiles() throws IOException {
        // Given
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        when(file1.getOriginalFilename()).thenReturn("file1.txt");
        when(file1.getInputStream()).thenReturn(new ByteArrayInputStream("content1".getBytes()));
        when(file2.getOriginalFilename()).thenReturn("file2.txt");
        when(file2.getInputStream()).thenReturn(new ByteArrayInputStream("content2".getBytes()));

        // When
        List<FileModel> result = localStorage.upload(new MultipartFile[]{file1, file2}, "batch");

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(f -> "file1.txt".equals(f.getName())));
        assertTrue(result.stream().anyMatch(f -> "file2.txt".equals(f.getName())));
    }

    @Test
    void getResource_ShouldReturnFileSystemResource_WhenFileExists() throws IOException {
        // Given
        Path testFile = tempDir.resolve("resource.txt");
        Files.write(testFile, "resource content".getBytes());

        // When
        Resource result = localStorage.getResource("resource.txt");

        // Then
        assertInstanceOf(FileSystemResource.class, result);
        assertTrue(result.exists());
    }

    @Test
    void getResource_ShouldThrowException_WhenFileDoesNotExist() {
        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> localStorage.getResource("nonexistent.txt"));
        assertEquals(ErrorCode.FILE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getResource_ShouldThrowException_WhenPathIsDirectory() throws IOException {
        // Given
        Path testDir = tempDir.resolve("testdir");
        Files.createDirectories(testDir);

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> localStorage.getResource("testdir"));
        assertEquals(ErrorCode.PATH_NOT_FILE, ex.getErrorCode());
    }
}

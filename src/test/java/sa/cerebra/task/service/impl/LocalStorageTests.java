package sa.cerebra.task.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.model.FileModel;
import sa.cerebra.task.storage.impl.LocalStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class LocalStorageTests {

    @TempDir
    Path tempDir;

    private LocalStorage localStorage;
    private User testUser;

    @BeforeEach
    void setUp() {
        localStorage = new LocalStorage();
        // Use reflection to set the storageBasePath field
        try {
            java.lang.reflect.Field field = LocalStorage.class.getDeclaredField("storageBasePath");
            field.setAccessible(true);
            field.set(localStorage, tempDir.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set storageBasePath", e);
        }

        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("+1234567890");
    }

    @Test
    void list_shouldReturnEmptyList_whenPathDoesNotExist() {
        // Given
        String path = "1/nonexistent/path";

        // When
        List<FileModel> result = localStorage.list(path);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void list_shouldReturnFile_whenPathIsFile() throws IOException {
        // Given
        Path userDir = tempDir.resolve("1");
        Files.createDirectories(userDir);
        Path filePath = userDir.resolve("test.txt");
        Files.write(filePath, "test content".getBytes());
        String path = "1";

        // When
        List<FileModel> result = localStorage.list( path);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test.txt");
    }

    @Test
    void list_shouldReturnAllFiles_whenPathIsDirectory() throws IOException {
        // Given
        Path userDir = tempDir.resolve("1");
        Files.createDirectories(userDir);
        Files.write(userDir.resolve("file1.txt"), "content1".getBytes());
        Files.write(userDir.resolve("file2.txt"), "content2".getBytes());
        Files.createDirectory(userDir.resolve("subdir"));

        // When
        List<FileModel> result = localStorage.list("1");

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(FileModel::getName).containsExactlyInAnyOrder("file1.txt", "file2.txt", "subdir");
    }

    @Test
    void list_withSubdirectory_shouldReturnFilesInSubdirectory() throws IOException {
        // Given
        Path userDir = tempDir.resolve("1");
        Path subdir = userDir.resolve("subdir");
        Files.createDirectories(subdir);
        Files.write(subdir.resolve("file.txt"), "content".getBytes());
        String path = "1/subdir";

        // When
        List<FileModel> result = localStorage.list( path);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("file.txt");
    }

    @Test
    void uploadFile_shouldCreateFileAndReturnFileModel() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        String path = "1/uploads";
        Path fullPath = localStorage.getFullPath(path);
        // When
        FileModel result = localStorage.uploadFile( file, fullPath);

        // Then
        assertThat(result.getName()).isEqualTo("test.txt");

        // Verify file was created
        Path expectedPath = tempDir.resolve("1").resolve("uploads").resolve("test.txt");
        assertThat(Files.exists(expectedPath)).isTrue();
        assertThat(Files.readString(expectedPath)).isEqualTo("test content");
    }

    @Test
    void uploadFile_withNullPath_shouldUploadToRoot() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        Path fullPath = localStorage.getFullPath("1");
        // When
        FileModel result = localStorage.uploadFile(file, fullPath);

        // Then
        assertThat(result.getName()).isEqualTo("test.txt");

        // Verify file was created in root
        Path expectedPath = tempDir.resolve("1").resolve("test.txt");
        assertThat(Files.exists(expectedPath)).isTrue();
    }

    @Test
    void uploadFile_withEmptyPath_shouldUploadToRoot() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        Path fullPath = localStorage.getFullPath("1");

        // When
        FileModel result = localStorage.uploadFile( file, fullPath);

        // Then
        assertThat(result.getName()).isEqualTo("test.txt");

        // Verify file was created in root
        Path expectedPath = tempDir.resolve("1").resolve("test.txt");
        assertThat(Files.exists(expectedPath)).isTrue();
    }

    @Test
    void uploadFile_shouldReplaceExistingFile() throws IOException {
        // Given
        Path userDir = tempDir.resolve("1");
        Files.createDirectories(userDir);
        Path existingFile = userDir.resolve("test.txt");
        Files.write(existingFile, "old content".getBytes());
        
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "new content".getBytes());
        Path fullPath = localStorage.getFullPath("1");

        // When
        localStorage.uploadFile(file, fullPath);

        // Then
        assertThat(Files.readString(existingFile)).isEqualTo("new content");
    }

    @Test
    void uploadMultipleFiles_shouldUploadAllFiles() throws IOException {
        // Given
        MultipartFile file1 = new MockMultipartFile("file1", "file1.txt", "text/plain", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file2", "file2.txt", "text/plain", "content2".getBytes());
        MultipartFile[] files = {file1, file2};
        String path = "1/uploads";

        // When
        List<FileModel> result = localStorage.upload( files, path);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FileModel::getName).containsExactlyInAnyOrder("file1.txt", "file2.txt");
        
        // Verify files were created
        Path userDir = tempDir.resolve("1");
        assertThat(Files.exists(userDir.resolve("uploads").resolve("file1.txt"))).isTrue();
        assertThat(Files.exists(userDir.resolve("uploads").resolve("file2.txt"))).isTrue();
    }

    @Test
    void getResourceExists() throws IOException {
        // Given
        Path userDir = tempDir.resolve("1");
        Files.createDirectories(userDir);
        Path filePath = userDir.resolve("test.txt");
        Files.write(filePath, "test content".getBytes());
        String path = "1/test.txt";

        // When
        Resource result = localStorage.getResource( path);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo("test.txt");
    }

    @Test
    void getResourceDoesNotExist() {
        // Given
        String path = "1/nonexistent.txt";

        // When & Then
        assertThatThrownBy(() -> localStorage.getResource( path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.FILE_NOT_FOUND.getMessage());
    }

    @Test
    void getResource_shouldThrowException_whenPathIsDirectory() throws IOException {
        // Given
        Path userDir = tempDir.resolve("1");
        Files.createDirectories(userDir);
        Files.createDirectory(userDir.resolve("subdir"));
        String path = "1/subdir";

        // When & Then
        assertThatThrownBy(() -> localStorage.getResource( path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.PATH_NOT_FILE.getMessage());
    }




    @Test
    void createFileModel_shouldSetCorrectProperties() throws IOException {
        // Given
        Path userDir = tempDir.resolve("1");
        Files.createDirectories(userDir);
        Path filePath = userDir.resolve("subdir").resolve("test.txt");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "test content".getBytes());
        String path = "1/subdir/test.txt";

        // When
        List<FileModel> result = localStorage.list(path);

        // Then
        assertThat(result).hasSize(1);
        FileModel fileModel = result.get(0);
        assertThat(fileModel.getName()).isEqualTo("test.txt");
        assertThat(fileModel.getUploadDate()).isNotNull();
    }
}

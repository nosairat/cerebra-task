package sa.cerebra.task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.model.FileModel;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTests {

    @Mock
    private StorageService storageService;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Resource resource;

    private FileService fileService;
    private User testUser;

    @BeforeEach
    void setUp() {
        fileService = new FileService(storageService);
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("+1234567890");
    }

    @Test
    void listFiles_shouldCallStorageService() {
        // Given
        String path = "test/path";
        String userPath = "1/test/path";
        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder().name("file1.txt").build(),
                FileModel.builder().name("file2.txt").build()
        );
        when(storageService.list(userPath)).thenReturn(expectedFiles);

        // When
        List<FileModel> result = fileService.listFiles(testUser, path);

        // Then
        assertThat(result).isEqualTo(expectedFiles);
        verify(storageService).list( userPath);
    }

    @Test
    void listFiles_withNullPath_shouldCallStorageServiceWithNull() {
        // Given
        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder().name("file1.txt").build()
        );
        when(storageService.list( "1")).thenReturn(expectedFiles);

        // When
        List<FileModel> result = fileService.listFiles(testUser, null);

        // Then
        assertThat(result).isEqualTo(expectedFiles);
        verify(storageService).list( "1");
    }

    @Test
    void uploadMultipleFiles_withValidFiles_shouldCallStorageService() {
        // Given
        MultipartFile[] files = {multipartFile, multipartFile};
        String path = "test/path";
        String userFilePath = "1/test/path";
        List<FileModel> expectedFiles = Arrays.asList(
                FileModel.builder().name("file1.txt").build(),
                FileModel.builder().name("file2.txt").build()
        );

        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(storageService.upload( files, userFilePath)).thenReturn(expectedFiles);

        // When
        List<FileModel> result = fileService.uploadMultipleFiles(testUser, files, path);

        // Then
        assertThat(result).isEqualTo(expectedFiles);
        verify(storageService).upload( files, userFilePath);
    }

    @Test
    void uploadMultipleFiles_withEmptyFilename_shouldThrowException() {
        // Given
        MultipartFile[] files = {multipartFile};
        String path = "/test/path";

        when(multipartFile.getOriginalFilename()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> fileService.uploadMultipleFiles(testUser, files, path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.FILE_NAME_REQUIRED.getMessage());
    }

    @Test
    void uploadMultipleFiles_withEmptyStringFilename_shouldThrowException() {
        // Given
        MultipartFile[] files = {multipartFile};
        String path = "/test/path";

        when(multipartFile.getOriginalFilename()).thenReturn("");

        // When & Then
        assertThatThrownBy(() -> fileService.uploadMultipleFiles(testUser, files, path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.FILE_NAME_REQUIRED.getMessage());
    }

    @Test
    void uploadMultipleFiles_withWhitespaceOnlyFilename_shouldThrowException() {
        // Given
        MultipartFile[] files = {multipartFile};
        String path = "/test/path";

        when(multipartFile.getOriginalFilename()).thenReturn("   ");

        // When & Then
        assertThatThrownBy(() -> fileService.uploadMultipleFiles(testUser, files, path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.FILE_NAME_REQUIRED.getMessage());
    }

    @Test
    void uploadMultipleFiles_withPathTraversalInFilename_shouldThrowException() {
        // Given
        MultipartFile[] files = {multipartFile};
        String path = "/test/path";

        when(multipartFile.getOriginalFilename()).thenReturn("../../../etc/passwd");

        // When & Then
        assertThatThrownBy(() -> fileService.uploadMultipleFiles(testUser, files, path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_NAME.getMessage());
    }

    @Test
    void uploadMultipleFiles_withForwardSlashInFilename_shouldThrowException() {
        // Given
        MultipartFile[] files = {multipartFile};
        String path = "/test/path";

        when(multipartFile.getOriginalFilename()).thenReturn("folder/file.txt");

        // When & Then
        assertThatThrownBy(() -> fileService.uploadMultipleFiles(testUser, files, path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_NAME.getMessage());
    }

    @Test
    void uploadMultipleFiles_withBackslashInFilename_shouldThrowException() {
        // Given
        MultipartFile[] files = {multipartFile};
        String path = "/test/path";

        when(multipartFile.getOriginalFilename()).thenReturn("folder\\file.txt");

        // When & Then
        assertThatThrownBy(() -> fileService.uploadMultipleFiles(testUser, files, path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_NAME.getMessage());
    }

    @Test
    void uploadMultipleFiles_withFileSizeExceeded_shouldThrowException() {
        // Given
        MultipartFile[] files = {multipartFile};
        String path = "/test/path";

        when(multipartFile.getOriginalFilename()).thenReturn("large-file.txt");
        when(multipartFile.getSize()).thenReturn(101L * 1024 * 1024); // 101MB

        // When & Then
        assertThatThrownBy(() -> fileService.uploadMultipleFiles(testUser, files, path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.FILE_SIZE_EXCEEDED.getMessage());
    }

    @Test
    void uploadMultipleFiles_withValidFileSize_shouldPass() {
        // Given
        MultipartFile[] files = {multipartFile};
        String path = "test/path";
        String userFilePath = "1/test/path";
        List<FileModel> expectedFiles = Arrays.asList(FileModel.builder().name("file.txt").build());

        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getSize()).thenReturn(100L * 1024 * 1024); // 100MB (exactly at limit)
        when(storageService.upload( files, userFilePath)).thenReturn(expectedFiles);

        // When
        List<FileModel> result = fileService.uploadMultipleFiles(testUser, files, path);

        // Then
        assertThat(result).isEqualTo(expectedFiles);
        verify(storageService).upload( files, userFilePath);
    }

    @Test
    void uploadMultipleFiles_validatesAllFiles() {
        // Given
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        MultipartFile[] files = {file1, file2};
        String path = "/test/path";

        when(file1.getOriginalFilename()).thenReturn("file1.txt");
        when(file1.getSize()).thenReturn(1024L);
        when(file2.getOriginalFilename()).thenReturn("../../../etc/passwd");
//        when(file2.getSize()).thenReturn(1024L);

        // When & Then
        assertThatThrownBy(() -> fileService.uploadMultipleFiles(testUser, files, path))
                .isInstanceOf(CerebraException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_NAME.getMessage());
    }

    @Test
    void downloadFile_shouldCallStorageService() {
        // Given
        String filePath = "test/file.txt";
        String userFilePath = "1/test/file.txt";
        when(storageService.getResource(userFilePath)).thenReturn(resource);

        // When
        Resource result = fileService.downloadFile(testUser, filePath);

        // Then
        assertThat(result).isEqualTo(resource);
        verify(storageService).getResource(userFilePath);
    }
}

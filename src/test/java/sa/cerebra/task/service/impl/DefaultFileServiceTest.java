package sa.cerebra.task.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.model.FileModel;
import sa.cerebra.task.storage.StorageService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultFileServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Resource resource;

    @InjectMocks
    private DefaultFileService fileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("+1234567890");
    }

    @Test
    void listFiles_ShouldReturnFilesWithCorrectPaths() {
        // Given
        String path = "documents";
        FileModel file1 = FileModel.builder()
                .name("file1.txt")
                .uploadDate(LocalDateTime.now())
                .build();
        FileModel file2 = FileModel.builder()
                .name("file2.pdf")
                .uploadDate(LocalDateTime.now())
                .build();
        List<FileModel> storageFiles = Arrays.asList(file1, file2);
        when(storageService.list("1/documents")).thenReturn(storageFiles);

        // When
        List<FileModel> result = fileService.listFiles(testUser, path);

        // Then
        assertEquals(2, result.size());
        assertEquals("documents/file1.txt", result.get(0).getPath());
        assertEquals("documents/file2.pdf", result.get(1).getPath());
        verify(storageService).list("1/documents");
    }

    @Test
    void listFiles_ShouldReturnFilesWithEmptyPathWhenPathIsNull() {
        // Given
        FileModel file = FileModel.builder()
                .name("root.txt")
                .uploadDate(LocalDateTime.now())
                .build();
        when(storageService.list("1")).thenReturn(Arrays.asList(file));

        // When
        List<FileModel> result = fileService.listFiles(testUser, null);

        // Then
        assertEquals(1, result.size());
        assertEquals("root.txt", result.get(0).getPath());
        verify(storageService).list("1");
    }

    @Test
    void uploadMultipleFiles_ShouldValidateAndUploadFiles() {
        // Given
        String path = "uploads";
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.isEmpty()).thenReturn(false);

        FileModel uploadedFile = FileModel.builder()
                .name("test.txt")
                .uploadDate(LocalDateTime.now())
                .build();
        when(storageService.upload(any(MultipartFile[].class), eq("1/uploads")))
                .thenReturn(Arrays.asList(uploadedFile));

        // When
        List<FileModel> result = fileService.uploadMultipleFiles(testUser, new MultipartFile[]{multipartFile}, path);

        // Then
        assertEquals(1, result.size());
        assertEquals("uploads/test.txt", result.get(0).getPath());
        verify(storageService).upload(any(MultipartFile[].class), eq("1/uploads"));
    }

    @Test
    void uploadMultipleFiles_ShouldThrowExceptionWhenFileNameIsNull() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> fileService.uploadMultipleFiles(testUser, new MultipartFile[]{multipartFile}, "uploads"));
        assertEquals(ErrorCode.FILE_NAME_REQUIRED, ex.getErrorCode());
        verifyNoInteractions(storageService);
    }

    @Test
    void uploadMultipleFiles_ShouldThrowExceptionWhenFileNameIsEmpty() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("   ");

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> fileService.uploadMultipleFiles(testUser, new MultipartFile[]{multipartFile}, "uploads"));
        assertEquals(ErrorCode.FILE_NAME_REQUIRED, ex.getErrorCode());
        verifyNoInteractions(storageService);
    }

    @Test
    void uploadMultipleFiles_ShouldThrowExceptionWhenFileNameContainsPathTraversal() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("../malicious.txt");

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> fileService.uploadMultipleFiles(testUser, new MultipartFile[]{multipartFile}, "uploads"));
        assertEquals(ErrorCode.INVALID_FILE_NAME, ex.getErrorCode());
        verifyNoInteractions(storageService);
    }

    @Test
    void uploadMultipleFiles_ShouldThrowExceptionWhenFileNameContainsBackslash() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("file\\name.txt");

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> fileService.uploadMultipleFiles(testUser, new MultipartFile[]{multipartFile}, "uploads"));
        assertEquals(ErrorCode.INVALID_FILE_NAME, ex.getErrorCode());
        verifyNoInteractions(storageService);
    }

    @Test
    void uploadMultipleFiles_ShouldThrowExceptionWhenFileSizeExceedsLimit() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("large.txt");
        when(multipartFile.getSize()).thenReturn(101L * 1024 * 1024); // 101MB

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> fileService.uploadMultipleFiles(testUser, new MultipartFile[]{multipartFile}, "uploads"));
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDED, ex.getErrorCode());
        verifyNoInteractions(storageService);
    }

    @Test
    void downloadFile_ShouldReturnResourceWhenFileExists() {
        // Given
        String path = "documents/file.txt";
        when(resource.exists()).thenReturn(true);
        when(storageService.getResource("1/documents/file.txt")).thenReturn(resource);

        // When
        Resource result = fileService.downloadFile(testUser, path);

        // Then
        assertSame(resource, result);
        verify(storageService).getResource("1/documents/file.txt");
    }

    @Test
    void downloadFile_ShouldThrowExceptionWhenFileDoesNotExist() {
        // Given
        String path = "documents/nonexistent.txt";
        when(resource.exists()).thenReturn(false);
        when(storageService.getResource("1/documents/nonexistent.txt")).thenReturn(resource);

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> fileService.downloadFile(testUser, path));
        assertEquals(ErrorCode.FILE_NOT_FOUND, ex.getErrorCode());
        verify(storageService).getResource("1/documents/nonexistent.txt");
    }

    @Test
    void uploadMultipleFiles_ShouldHandleMultipleFiles() {
        // Given
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        when(file1.getOriginalFilename()).thenReturn("file1.txt");
        when(file1.getSize()).thenReturn(1024L);
        when(file2.getOriginalFilename()).thenReturn("file2.txt");
        when(file2.getSize()).thenReturn(2048L);

        FileModel uploadedFile1 = FileModel.builder().name("file1.txt").build();
        FileModel uploadedFile2 = FileModel.builder().name("file2.txt").build();
        when(storageService.upload(any(MultipartFile[].class), eq("1/uploads")))
                .thenReturn(Arrays.asList(uploadedFile1, uploadedFile2));

        // When
        List<FileModel> result = fileService.uploadMultipleFiles(testUser, new MultipartFile[]{file1, file2}, "uploads");

        // Then
        assertEquals(2, result.size());
        assertEquals("uploads/file1.txt", result.get(0).getPath());
        assertEquals("uploads/file2.txt", result.get(1).getPath());
    }

    @Test
    void uploadMultipleFiles_ShouldValidateAllFilesBeforeUpload() {
        // Given
        MultipartFile validFile = mock(MultipartFile.class);
        MultipartFile invalidFile = mock(MultipartFile.class);
        when(validFile.getOriginalFilename()).thenReturn("valid.txt");
        when(validFile.getSize()).thenReturn(1024L);
        when(invalidFile.getOriginalFilename()).thenReturn("../invalid.txt");

        // When / Then
        CerebraException ex = assertThrows(CerebraException.class,
                () -> fileService.uploadMultipleFiles(testUser, new MultipartFile[]{validFile, invalidFile}, "uploads"));
        assertEquals(ErrorCode.INVALID_FILE_NAME, ex.getErrorCode());
        verifyNoInteractions(storageService);
    }
}

package sa.cerebra.task.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private ConstraintViolation<?> constraintViolation;

    @Mock
    private Path path;

    @Mock
    private ServletRequestBindingException servletRequestBindingException;

    @Mock
    private MissingServletRequestPartException missingServletRequestPartException;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleCerebraException_ShouldReturnBadRequest_WhenCerebraExceptionThrown() {
        // Given
        ErrorCode errorCode = ErrorCode.FILE_NOT_FOUND;
        String message = "File not found";
        CerebraException cerebraException = new CerebraException(errorCode, message);

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleCerebraException(cerebraException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(message, body.get("message"));
        assertEquals(errorCode.getCode(), body.get("errorCode"));
    }

    @Test
    void handleCerebraException_ShouldReturnBadRequest_WhenCerebraExceptionWithDefaultMessage() {
        // Given
        ErrorCode errorCode = ErrorCode.INVALID_OTP;
        CerebraException cerebraException = new CerebraException(errorCode);

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleCerebraException(cerebraException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(errorCode.getMessage(), body.get("message"));
        assertEquals(errorCode.getCode(), body.get("errorCode"));
    }

    @Test
    void handleCerebraException_ShouldReturnBadRequest_WhenCerebraExceptionWithLegacyConstructor() {
        // Given
        String message = "Legacy error message";
        CerebraException cerebraException = new CerebraException(message);

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleCerebraException(cerebraException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(message, body.get("message"));
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), body.get("errorCode"));
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequest_WhenMethodArgumentNotValidExceptionThrown() {
        // Given
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("testObject", "field1", "Field 1 is required"),
                new FieldError("testObject", "field2", "Field 2 is invalid")
        );
        
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("bad request", body.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals("Field 1 is required", errors.get("field1"));
        assertEquals("Field 2 is invalid", errors.get("field2"));
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequest_WhenNoFieldErrors() {
        // Given
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("bad request", body.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertTrue(errors.isEmpty());
    }

    @Test
    void handleConstraintViolationException_ShouldReturnBadRequest_WhenConstraintViolationExceptionThrown() {
        // Given
        String propertyPath = "testField";
        String violationMessage = "Constraint violation message";
        
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn(propertyPath);
        when(constraintViolation.getMessage()).thenReturn(violationMessage);
        
        Set<ConstraintViolation<?>> violations = Set.of(constraintViolation);
        when(constraintViolationException.getConstraintViolations()).thenReturn(violations);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleConstraintViolationException(constraintViolationException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("bad request", body.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals(violationMessage, errors.get(propertyPath));
    }

    @Test
    void handleConstraintViolationException_ShouldReturnBadRequest_WhenNoViolations() {
        // Given
        when(constraintViolationException.getConstraintViolations()).thenReturn(Collections.emptySet());

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleConstraintViolationException(constraintViolationException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("bad request", body.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertTrue(errors.isEmpty());
    }

    @Test
    void handleMissingServletRequestPartException_ShouldReturnBadRequest_WhenMissingServletRequestPartExceptionThrown() {
        // Given
        String errorMessage = "Required request part 'file' is not present";
        when(missingServletRequestPartException.getMessage()).thenReturn(errorMessage);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleMissingServletRequestPartException(missingServletRequestPartException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("bad request", body.get("message"));
        assertEquals(errorMessage, body.get("errors"));
    }

    @Test
    void handleMissingServletRequestPartException_ShouldReturnBadRequest_WhenServletRequestBindingExceptionThrown() {
        // Given
        String errorMessage = "Required request parameter 'id' is not present";
        when(servletRequestBindingException.getMessage()).thenReturn(errorMessage);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleMissingServletRequestPartException(servletRequestBindingException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("bad request", body.get("message"));
        assertEquals(errorMessage, body.get("errors"));
    }



    @Test
    void handleGenericException_ShouldReturnInternalServerError_WhenGenericExceptionThrown() {
        // Given
        String errorMessage = "Unexpected error occurred";
        RuntimeException genericException = new RuntimeException(errorMessage);

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleGenericException(genericException);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("internal server error", body.get("message"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError_WhenExceptionMessageIsNull() {
        // Given
        RuntimeException genericException = new RuntimeException();

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleGenericException(genericException);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("internal server error", body.get("message"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError_WhenCheckedExceptionThrown() {
        // Given
        Exception checkedException = new Exception("Checked exception message");

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleGenericException(checkedException);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("internal server error", body.get("message"));
    }

    @Test
    void handleValidationExceptions_ShouldHandleMultipleFieldErrors_WhenMultipleFieldsHaveErrors() {
        // Given
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("user", "email", "Email is required"),
                new FieldError("user", "password", "Password must be at least 8 characters"),
                new FieldError("user", "phone", "Phone number is invalid"),
                new FieldError("user", "name", "Name cannot be empty")
        );
        
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("bad request", body.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals(4, errors.size());
        assertEquals("Email is required", errors.get("email"));
        assertEquals("Password must be at least 8 characters", errors.get("password"));
        assertEquals("Phone number is invalid", errors.get("phone"));
        assertEquals("Name cannot be empty", errors.get("name"));
    }

    @Test
    void handleConstraintViolationException_ShouldHandleMultipleViolations_WhenMultipleConstraintsViolated() {
        // Given
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);
        
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(path1.toString()).thenReturn("field1");
        when(path2.toString()).thenReturn("field2");
        when(violation1.getMessage()).thenReturn("Field 1 violation");
        when(violation2.getMessage()).thenReturn("Field 2 violation");
        
        Set<ConstraintViolation<?>> violations = Set.of(violation1, violation2);
        when(constraintViolationException.getConstraintViolations()).thenReturn(violations);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleConstraintViolationException(constraintViolationException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("bad request", body.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals(2, errors.size());
        assertEquals("Field 1 violation", errors.get("field1"));
        assertEquals("Field 2 violation", errors.get("field2"));
    }

    @Test
    void handleCerebraException_ShouldHandleAllErrorCodes_WhenDifferentErrorCodesUsed() {
        // Test all error codes from the enum
        ErrorCode[] errorCodes = ErrorCode.values();
        
        for (ErrorCode errorCode : errorCodes) {
            // Given
            CerebraException cerebraException = new CerebraException(errorCode);
            
            // When
            ResponseEntity<Object> response = globalExceptionHandler.handleCerebraException(cerebraException);
            
            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(errorCode.getMessage(), body.get("message"));
            assertEquals(errorCode.getCode(), body.get("errorCode"));
        }
    }
}

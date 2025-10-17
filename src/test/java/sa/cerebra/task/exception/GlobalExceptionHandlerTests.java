package sa.cerebra.task.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsCerebraException_toBadRequest() {
        ResponseEntity<Object> response = handler.handleCerebraException(new CerebraException(ErrorCode.FILE_NOT_FOUND));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("message", "File not found");
        assertThat(body).containsEntry("errorCode", "FILE_NOT_FOUND");
    }

    @Test
    void mapsCerebraException_withStringMessage_toBadRequest() {
        ResponseEntity<Object> response = handler.handleCerebraException(new CerebraException("Custom error message"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("message", "Custom error message");
        assertThat(body).containsEntry("errorCode", "INTERNAL_ERROR");
    }

    @Test
    void mapsGenericException_toInternalServerError() {
        ResponseEntity<Object> response = handler.handleGenericException(new RuntimeException("boom"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat((Map<String, Object>) response.getBody()).containsEntry("message", "internal server error");
    }

    @Test
    void mapsValidationException_toBadRequest_withErrors() throws NoSuchMethodException {
        record Dummy(String phone) {}
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Dummy(""), "dummy");
        binding.rejectValue("phone", "NotBlank", "must not be blank");
        java.lang.reflect.Constructor<?> constructor = Dummy.class.getDeclaredConstructors()[0];
        MethodParameter methodParameter = new MethodParameter(constructor, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                methodParameter,
                binding
        );

        ResponseEntity<?> response = handler.handleValidationExceptions(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("message", "bad request");
        assertThat((Map<String, Object>) body.get("errors")).containsEntry("phone", "must not be blank");
    }
}



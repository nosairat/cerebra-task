package sa.cerebra.task.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CerebraException.class)
    public ResponseEntity<Object> handleCerebraException(CerebraException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        body.put("errorCode", e.getErrorCode().getCode());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getAllErrors()
                .stream()
                .collect(Collectors.toMap(error -> ((FieldError) error).getField(), DefaultMessageSourceResolvable::getDefaultMessage));
        Map<String, Object> body = Map.of(
                "message", "bad request",
                "errors", errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        Map<String, String> body = new HashMap<>();
        body.put("message", "internal server error");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}

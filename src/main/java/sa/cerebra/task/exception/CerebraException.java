package sa.cerebra.task.exception;


import lombok.Getter;

@Getter
public class CerebraException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public CerebraException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public CerebraException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    // Keep the old constructor for backward compatibility
    public CerebraException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

}

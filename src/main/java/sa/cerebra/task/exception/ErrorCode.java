package sa.cerebra.task.exception;

public enum ErrorCode {
    // File related errors
    FILE_NOT_FOUND("FILE_NOT_FOUND", "File not found"),
    FILE_NAME_REQUIRED("FILE_NAME_REQUIRED", "File must have a name"),
    INVALID_FILE_NAME("INVALID_FILE_NAME", "Invalid file name"),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "File size exceeds maximum limit of 100MB"),
    PATH_NOT_FILE("PATH_NOT_FILE", "Path is not a file"),
    ACCESS_DENIED("ACCESS_DENIED", "Access denied"),
    
    // Authentication related errors
    INVALID_OTP("INVALID_OTP", "Invalid or expired OTP"),
    
    // Generic errors
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized access"),
    BAD_REQUEST("BAD_REQUEST", "Bad request");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

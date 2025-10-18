package sa.cerebra.task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cerebra Task API")
                        .description("""
                                # Cerebra Task API Documentation
                                
                                ## Overview
                                The Cerebra Task API is a comprehensive file management and sharing system designed for secure document handling and collaboration. This RESTful API provides robust authentication, file operations, and sharing capabilities with enterprise-grade security.
                                
                                ## Key Features
                                - **Phone-based Authentication**: Secure OTP-based login system
                                - **File Management**: Upload, download, and organize files with path-based storage
                                - **Secure File Sharing**: Generate time-limited shareable links with recipient targeting
                                - **JWT Security**: Bearer token authentication for all protected endpoints
                                - **Path Validation**: Secure file path handling to prevent directory traversal attacks
                                
                                ## Authentication Flow
                                1. **Login**: Send phone number to `/api/v1/auth/login` to receive OTP
                                2. **Validate OTP**: Submit OTP to `/api/v1/auth/validate-otp` to get JWT token
                                3. **Use Token**: Include JWT token in Authorization header for protected endpoints
                                
                                ## File Operations
                                - **Upload**: Multi-file upload with optional directory path
                                - **Download**: Secure file download with preview mode support
                                - **List**: Browse files and directories with path-based navigation
                                
                                ## Sharing System
                                - **Create Share Links**: Generate secure, time-limited sharing URLs
                                - **Recipient Targeting**: Share files with specific phone numbers
                                - **Expiration Control**: Configurable link expiration (default: 7 days)
                                
                                ## Error Handling
                                The API uses standardized error responses with specific error codes for different scenarios. All errors include descriptive messages and appropriate HTTP status codes.
                                
                                ### Error Types
                                
                                #### Authentication Errors
                                - **INVALID_OTP** (400): Invalid or expired OTP code
                                - **UNAUTHORIZED** (401): Missing or invalid JWT token
                                
                                #### File Operation Errors  
                                - **FILE_NOT_FOUND** (400): Requested file does not exist
                                - **FILE_SIZE_EXCEEDED** (400): File exceeds 100MB limit
                                - **INVALID_FILE_NAME** (400): File name contains invalid characters
                                - **PATH_NOT_FILE** (400): Specified path is not a file
                                - **ACCESS_DENIED** (403): Insufficient permissions for file access
                                
                                #### Sharing Errors
                                - **SHARE_LINK_EXPIRED** (404): Share link has expired or is invalid
                                
                                #### Validation Errors
                                - **VALIDATION_ERROR** (400): Request validation failed
                                - **BAD_REQUEST** (400): Malformed request data
                                
                                #### System Errors
                                - **INTERNAL_ERROR** (500): Unexpected server error
                                
                                ## Rate Limiting & Security
                                - File size limits: 100MB per file
                                - Request size limits: 100MB total
                                - Path validation to prevent security vulnerabilities
                                - JWT token expiration for session management
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cerebra Development Team")
                                .email("support@cerebra.com")
                                .url("https://cerebra.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from validate-otp API after calling login API endpoint"))
                        .addSchemas("ErrorResponse", createErrorResponseSchema())
                        .addSchemas("ValidationErrorResponse", createValidationErrorResponseSchema())
                        .addSchemas("InternalErrorResponse", createInternalErrorResponseSchema())
                        .addSchemas("UnauthorizedErrorResponse", createUnauthorizedErrorResponseSchema())
                        .addSchemas("FileNotFoundErrorResponse", createFileNotFoundErrorResponseSchema())
                        .addSchemas("AccessDeniedErrorResponse", createAccessDeniedErrorResponseSchema())
                        .addSchemas("FileSizeExceededErrorResponse", createFileSizeExceededErrorResponseSchema())
                        .addSchemas("InvalidOtpErrorResponse", createInvalidOtpErrorResponseSchema())
                        .addSchemas("ShareLinkExpiredErrorResponse", createShareLinkExpiredErrorResponseSchema())
                        .addSchemas("BadRequestErrorResponse", createBadRequestErrorResponseSchema()));
    }

    private Schema<?> createErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .description("Human-readable error message")
                        .example("File not found"))
                .addProperty("errorCode", new StringSchema()
                        .description("Machine-readable error code")
                        .example("FILE_NOT_FOUND"));
    }

    private Schema<?> createValidationErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("bad request")
                        .description("General error message"))
                .addProperty("errors", new ObjectSchema()
                        .description("Field-specific validation errors")
                        .addProperty("phone", new StringSchema().example("Phone number is required"))
                        .addProperty("otp", new StringSchema().example("OTP is required")));
    }

    private Schema<?> createInternalErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("internal server error")
                        .description("Generic server error message"));
    }

    private Schema<?> createUnauthorizedErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("Unauthorized")
                        .description("Authentication required"))
                .addProperty("errorCode", new StringSchema()
                        .example("UNAUTHORIZED")
                        .description("Authentication error code"));
    }

    private Schema<?> createFileNotFoundErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("File not found")
                        .description("Requested file does not exist"))
                .addProperty("errorCode", new StringSchema()
                        .example("FILE_NOT_FOUND")
                        .description("File not found error code"));
    }

    private Schema<?> createAccessDeniedErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("Access denied")
                        .description("Insufficient permissions for the requested operation"))
                .addProperty("errorCode", new StringSchema()
                        .example("ACCESS_DENIED")
                        .description("Access denied error code"));
    }

    private Schema<?> createFileSizeExceededErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("File size exceeds maximum limit of 100MB")
                        .description("File size limit exceeded"))
                .addProperty("errorCode", new StringSchema()
                        .example("FILE_SIZE_EXCEEDED")
                        .description("File size limit error code"));
    }

    private Schema<?> createInvalidOtpErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("Invalid or expired OTP")
                        .description("OTP validation failed"))
                .addProperty("errorCode", new StringSchema()
                        .example("INVALID_OTP")
                        .description("OTP validation error code"));
    }

    private Schema<?> createShareLinkExpiredErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("Share link has expired")
                        .description("Share link is no longer valid"))
                .addProperty("errorCode", new StringSchema()
                        .example("SHARE_LINK_EXPIRED")
                        .description("Share link expiration error code"));
    }

    private Schema<?> createBadRequestErrorResponseSchema() {
        return new ObjectSchema()
                .addProperty("message", new StringSchema()
                        .example("Bad request")
                        .description("Request format is invalid"))
                .addProperty("errorCode", new StringSchema()
                        .example("BAD_REQUEST")
                        .description("Bad request error code"));
    }
}

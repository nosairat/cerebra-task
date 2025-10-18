package sa.cerebra.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sa.cerebra.task.dto.request.LoginRequest;
import sa.cerebra.task.dto.request.ValidateOtpRequest;
import sa.cerebra.task.dto.response.TokenResponse;
import sa.cerebra.task.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    final AuthService authService;

    @Operation(
            summary = "Login with phone number",
            description = "Send OTP to the provided phone number for authentication"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid phone number format", 
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/InternalErrorResponse")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        authService.login(request.getPhone());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Validate OTP and get access token",
            description = "Validate the OTP received via SMS and return JWT access token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "OTP validated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid OTP or phone number",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/InvalidOtpErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "OTP expired or invalid",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/UnauthorizedErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/InternalErrorResponse")))
    })
    @PostMapping("/validate-otp")
    public ResponseEntity<?> validateOtp(@Valid @RequestBody ValidateOtpRequest request){
        TokenResponse response =  authService.validate(request.getPhone(), request.getOtp());
        return ResponseEntity.ok(response);
    }


}

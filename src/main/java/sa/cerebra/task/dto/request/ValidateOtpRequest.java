package sa.cerebra.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "OTP validation request")
public class ValidateOtpRequest {
    @NotBlank
    @Schema(description = "Phone number", example = "+1234567890", required = true)
    String phone;
    @NotBlank
    @Schema(description = "OTP code received via SMS", example = "123456", required = true)
    String otp;
}

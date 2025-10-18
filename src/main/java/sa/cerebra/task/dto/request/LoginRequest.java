package sa.cerebra.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request with phone number")
public class LoginRequest {
    @NotBlank
    @Schema(description = "Phone number for authentication", example = "+1234567890", required = true)
    String phone;
}

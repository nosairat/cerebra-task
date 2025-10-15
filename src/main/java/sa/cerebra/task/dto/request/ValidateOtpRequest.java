package sa.cerebra.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateOtpRequest {
    @NotBlank
    String phone;
    @NotBlank
    String otp;
}

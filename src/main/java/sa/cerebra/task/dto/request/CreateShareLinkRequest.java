package sa.cerebra.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import sa.cerebra.task.validation.SafePath;

@Data
public class CreateShareLinkRequest {
    
    @NotBlank
    @SafePath
    private String path;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String recipientPhone;
    
    private Integer expirationDays = 7; // Default to 7 days
}

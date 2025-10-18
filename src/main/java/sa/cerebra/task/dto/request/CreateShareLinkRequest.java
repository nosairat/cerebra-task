package sa.cerebra.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import sa.cerebra.task.validation.SafePath;

@Data
@Schema(description = "Request to create a shareable link for a file")
public class CreateShareLinkRequest {
    
    @NotBlank
    @SafePath
    @Schema(description = "Path to the file to share", example = "/documents/file.pdf", required = true)
    private String path;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Schema(description = "Phone number of the recipient", example = "+1234567890", required = true)
    private String recipientPhone;
    
    @Schema(description = "Number of days until the share link expires", example = "7", defaultValue = "7")
    private Integer expirationDays = 7; // Default to 7 days
}

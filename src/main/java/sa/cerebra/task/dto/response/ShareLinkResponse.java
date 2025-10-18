package sa.cerebra.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response containing the generated share link")
public class ShareLinkResponse {
    @Schema(description = "Shareable link URL", example = "https://api.example.com/api/v1/share/abc123def456")
    private String link;
}

package sa.cerebra.task.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class TokenResponse {
    private String accessToken;
}

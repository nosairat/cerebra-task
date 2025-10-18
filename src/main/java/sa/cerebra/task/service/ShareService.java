package sa.cerebra.task.service;

import org.springframework.core.io.Resource;
import sa.cerebra.task.dto.request.CreateShareLinkRequest;
import sa.cerebra.task.dto.response.ShareLinkResponse;
import sa.cerebra.task.entity.User;

public interface ShareService {
    
    ShareLinkResponse shareLink(User user, CreateShareLinkRequest request);

    Resource download(String shareToken);
}

package sa.cerebra.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import sa.cerebra.task.BaseIntegrationTest;
import sa.cerebra.task.dto.request.CreateShareLinkRequest;
import sa.cerebra.task.dto.response.ShareLinkResponse;
import sa.cerebra.task.entity.User;
import sa.cerebra.task.exception.CerebraException;
import sa.cerebra.task.exception.ErrorCode;
import sa.cerebra.task.service.ShareService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShareControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShareService shareService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("+1234567890");

        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createShareLink_shouldReturnShareLink() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath("test/file.txt");
        request.setRecipientPhone("+11234567890");
        request.setExpirationDays(5);

        ShareLinkResponse response = new ShareLinkResponse().setLink("http://localhost:8080/api/v1/share/abc123");
        when(shareService.shareLink(eq(testUser), any(CreateShareLinkRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.link").value("http://localhost:8080/api/v1/share/abc123"));
    }

    @Test
    void createShareLink_withInvalidPath_shouldReturnBadRequest() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath("/bad/path");
        request.setRecipientPhone("+11234567890");

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShareLink_withInvalidPhone_shouldReturnBadRequest() throws Exception {
        CreateShareLinkRequest request = new CreateShareLinkRequest();
        request.setPath("ok/path.txt");
        request.setRecipientPhone("invalid-phone");

        mockMvc.perform(post("/api/v1/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getShareLink_shouldReturnAttachment() throws Exception {
        byte[] bytes = "shared content".getBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return "shared.txt";
            }
        };

        when(shareService.download(eq("token123"))).thenReturn(resource);

        mockMvc.perform(get("/api/v1/share/{token}", "token123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"shared.txt\""))
                .andExpect(content().string("shared content"));
    }

    @Test
    void getShareLink_whenExpired_shouldReturnBadRequest() throws Exception {
        when(shareService.download(eq("expired"))).thenThrow(new CerebraException(ErrorCode.SHARE_LINK_EXPIRED));

        mockMvc.perform(get("/api/v1/share/{token}", "expired"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("SHARE_LINK_EXPIRED"));
    }
}



package sa.cerebra.task.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@ConfigurationProperties(prefix = "cerebra")
@Configuration
public class Configs {
    private String signingKey = "your-256-bit-secret-your-256-bit-secret";
    private long accessTokenExpiration=360000;

}

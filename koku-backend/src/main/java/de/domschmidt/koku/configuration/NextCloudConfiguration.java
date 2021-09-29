package de.domschmidt.koku.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class NextCloudConfiguration {

    @Value("${nextcloud.password}")
    private String nextcloudPassword;
    @Value("${nextcloud.user}")
    private String nextcloudUser;
    @Value("${nextcloud.endpoint}")
    private String nextcloudEndpoint;

}

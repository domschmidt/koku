package de.domschmidt.koku.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class UploadConfiguration {

    @Value("${uploads.dir}")
    private String uploadsDir;

}

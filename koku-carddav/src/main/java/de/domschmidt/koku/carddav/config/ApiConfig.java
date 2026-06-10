package de.domschmidt.koku.carddav.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ApiConfig {

    private final String basePath;

    @Autowired
    public ApiConfig(@Value("${carddav.basePath:''}") String basePath) {
        this.basePath = basePath;
    }
}

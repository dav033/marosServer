package io.dav033.maroconstruction.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "clickup.routes")
public class ClickUpRoutingProperties {
    @Data
    public static class Route {
        private String listId;
        private String leadNumberId;
        private String locationTextId;
        private String contactNameId;
        private String companyNameId;
        private String emailId;
        private String phoneId;
    }
    private Map<String, Route> map;
}

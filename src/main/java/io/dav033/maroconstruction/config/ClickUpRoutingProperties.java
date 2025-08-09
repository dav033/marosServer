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
    public static class Fields {
        private String contactNameId;
        private String customerNameId;
        private String emailId;
        private String phoneId;
        private String phoneTextId;
        private String leadNumberId;
        private String locationTextId;
    private String locationId;
    }

    @Data
    public static class Route {
        private String listId;
        private Fields fields;
    }
    private Map<String, Route> map;
}

package io.dav033.maroconstruction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "clickup")
public class ClickUpConfig {
    private String apiUrl = "https://api.clickup.com/api/v2";
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String teamId;
    private String spaceId;
    private String listId;
    private Integer defaultPriority = 3;

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }
    public Integer getDefaultPriority() { return defaultPriority; }
    public void setDefaultPriority(Integer defaultPriority) { this.defaultPriority = defaultPriority; }
}

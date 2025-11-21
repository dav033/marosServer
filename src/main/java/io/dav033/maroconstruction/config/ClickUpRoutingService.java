package io.dav033.maroconstruction.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.services.ClickUpHeadersProvider;
import io.dav033.maroconstruction.services.ClickUpUrlBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class ClickUpRoutingService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClickUpRoutingService.class);
        public String getLocationId(LeadType type) {
        try {
            ClickUpRoutingProperties.Route r = route(type);
            ClickUpRoutingProperties.Fields f = r.getFields();
            if (f == null) return null;
            try {
                var field = f.getClass().getDeclaredField("locationId");
                field.setAccessible(true);
                return (String) field.get(f);
            } catch (Exception ignored) {}
            return null;
        } catch (Exception e) {
            return null;
        }
    }

        public String getLocationTextId(LeadType type) {
        try {
            ClickUpRoutingProperties.Route r = route(type);
            ClickUpRoutingProperties.Fields f = r.getFields();
            if (f == null) return null;
            return f.getLocationTextId();
        } catch (Exception e) {
            return null;
        }
    }
    private final ClickUpRoutingProperties props;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ClickUpUrlBuilder urls;
    private final ClickUpHeadersProvider headers;

    public ClickUpRoutingService(ClickUpRoutingProperties props,
                                 RestTemplate restTemplate,
                                 ObjectMapper objectMapper,
                                 ClickUpUrlBuilder urls,
                                 ClickUpHeadersProvider headers) {
        this.props = props;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.urls = urls;
        this.headers = headers;
    }

    public String resolveLeadNumberFieldId(LeadType type) {
        log.info("🔍 Resolving leadNumberId for type: {}", type);
        String fid = null;
        try {
            ClickUpRoutingProperties.Route r = route(type);
            log.info("🔍 Route resolved: {}", r);
            ClickUpRoutingProperties.Fields f = r.getFields();
            log.info("🔍 Fields: {}", f);
            if (f != null) {
                fid = f.getLeadNumberId();
                log.info("🔍 LeadNumberId from config: {}", fid);
            }
        } catch (Exception e) {
            log.warn("🔍 Exception getting leadNumberId from config: {}", e.getMessage());
        }
        if (StringUtils.hasText(fid)) {
            log.info("🔍 Returning configured leadNumberId: {}", fid);
            return fid;
        }
        String listId = getListId(type);
        log.info("🔍 Falling back to auto-discovery for listId: {}", listId);
        String discovered = discoverByName(listId, List.of("Lead #", "# Leads"));
        if (StringUtils.hasText(discovered)) {
            log.warn("Auto-descubierto leadNumberId={} para listId={}. Fije este valor en configuración.", discovered, listId);
            return discovered;
        }
        throw new IllegalStateException("No se pudo resolver leadNumberId para " + type + ". Configure el ID o habilite auto-descubrimiento.");
    }

    private String discoverByName(String listId, List<String> names) {
        try {
            ResponseEntity<String> res = restTemplate.exchange(urls.listFields(listId), HttpMethod.GET, new HttpEntity<>(headers.get()), String.class);
            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) return null;
            JsonNode fields = objectMapper.readTree(res.getBody()).path("fields");
            for (JsonNode f : fields) {
                String name = f.path("name").asText("");
                if (names.stream().anyMatch(n -> n.equalsIgnoreCase(name))) {
                    return f.path("id").asText(null);
                }
            }
        } catch (Exception e) {
            log.error("Error autodiscovering leadNumberId: {}", e.getMessage());
        }
        return null;
    }

    public ClickUpRoutingProperties.Route route(LeadType type) {
        log.info("🗺️ Getting route for type: {}", type);
        Map<String, ClickUpRoutingProperties.Route> map = props.getMap();
        log.info("🗺️ Available routes in map: {}", map != null ? map.keySet() : "null");
        if (map == null || map.isEmpty()) {
            throw new IllegalStateException(
                    "ClickUpRoutingProperties.map vacío: faltan propiedades clickup.routes.map.* en application.yml/properties");
        }
        ClickUpRoutingProperties.Route r = map.get(type.name());
        log.info("🗺️ Route for {}: {}", type.name(), r);
        if (r == null || r.getListId() == null || r.getListId().isBlank()) {
            throw new IllegalStateException("ClickUp no configurado para leadType=" + type);
        }
        return r;
    }

    public String getListId(LeadType type) {
        return route(type).getListId();
    }
        public boolean isConfigured(LeadType type) {
        return props.getMap() != null
            && props.getMap().containsKey(type.name());
    }

        public java.util.Set<LeadType> configuredTypes() {
        if (props.getMap() == null) return java.util.Collections.emptySet();
        return props.getMap().keySet().stream()
                .map(LeadType::valueOf)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}

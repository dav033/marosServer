package io.dav033.maroconstruction.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.services.ClickUpHeadersProvider;
import io.dav033.maroconstruction.services.ClickUpUrlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickUpRoutingService {
    /**
     * Devuelve el ID del campo tipo location (objeto) para el tipo de lead dado, o null si no está configurado.
     */
    public String getLocationId(LeadType type) {
        try {
            ClickUpRoutingProperties.Route r = route(type);
            ClickUpRoutingProperties.Fields f = r.getFields();
            if (f == null) return null;
            // Reflection para soportar locationId aunque no esté en Fields por defecto
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

    /**
     * Devuelve el ID del campo tipo short_text (texto plano) para el tipo de lead dado, o null si no está configurado.
     */
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

    public String resolveLeadNumberFieldId(LeadType type) {
        String fid = null;
        try {
            ClickUpRoutingProperties.Route r = route(type);
            ClickUpRoutingProperties.Fields f = r.getFields();
            if (f != null) fid = f.getLeadNumberId();
        } catch (Exception ignored) {}
        if (StringUtils.hasText(fid)) return fid;

        // Auto-descubrimiento por nombre
        String listId = getListId(type);
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
        Map<String, ClickUpRoutingProperties.Route> map = props.getMap();
        if (map == null || map.isEmpty()) {
            throw new IllegalStateException(
                    "ClickUpRoutingProperties.map vacío: faltan propiedades clickup.routes.map.* en application.yml/properties");
        }
        ClickUpRoutingProperties.Route r = map.get(type.name());
        if (r == null || r.getListId() == null || r.getListId().isBlank()) {
            throw new IllegalStateException("ClickUp no configurado para leadType=" + type);
        }
        return r;
    }

    public String getListId(LeadType type) {
        return route(type).getListId();
    }
    /** Retorna true si el LeadType tiene ruta configurada (listId y fields). */
    public boolean isConfigured(LeadType type) {
        return props.getMap() != null
            && props.getMap().containsKey(type.name());
    }

    /** Conjunto de LeadType efectivamente configurados en properties. */
    public java.util.Set<LeadType> configuredTypes() {
        if (props.getMap() == null) return java.util.Collections.emptySet();
        return props.getMap().keySet().stream()
                .map(LeadType::valueOf)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}

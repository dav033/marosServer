package io.dav033.maroconstruction.config;

import io.dav033.maroconstruction.enums.LeadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickUpRoutingService {
    private final ClickUpRoutingProperties props;

    public ClickUpRoutingProperties.Route route(LeadType type) {
        var map = props.getMap();
        if (map == null || map.isEmpty()) {
            throw new IllegalStateException(
                "ClickUpRoutingProperties.map vacío: faltan propiedades clickup.routes.map.* en application.yml/properties");
        }
        var r = map.get(type.name());
        if (r == null || r.getListId() == null || r.getListId().isBlank()) {
            throw new IllegalStateException("ClickUp no configurado para leadType=" + type);
        }
        return r;
    }
}

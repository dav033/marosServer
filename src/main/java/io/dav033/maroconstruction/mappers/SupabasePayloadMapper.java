package io.dav033.maroconstruction.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface SupabasePayloadMapper {

    // Método principal que decide de dónde extraer los datos
    default LeadPayloadDto toDto(SupabaseWebhookPayload payload) {
        // Para DELETE, usar oldRecord; para INSERT/UPDATE, usar record
        Map<String, Object> sourceRecord = "DELETE".equals(payload.getType()) 
            ? payload.getOldRecord() 
            : payload.getRecord();
            
        if (sourceRecord == null) {
            return new LeadPayloadDto();
        }
        
        LeadPayloadDto dto = new LeadPayloadDto();
        dto.setId(toInteger(sourceRecord.get("id")));
        dto.setLeadNumber(toStringObject(sourceRecord.get("lead_number")));
        dto.setName(toStringObject(sourceRecord.get("name")));
        dto.setLeadType(toStringObject(sourceRecord.get("lead_type")));
        dto.setLocation(toStringObject(sourceRecord.get("location")));
        dto.setStartDate(toStringObject(sourceRecord.get("start_date")));
        dto.setStatus(toStringObject(sourceRecord.get("status")));
        dto.setContactId(toLong(sourceRecord.get("contact_id")));
        
        return dto;
    }

    /** Convierte cualquier Object numérico o String a Integer. */
    @Named("toInteger")
    default Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** Convierte cualquier Object numérico o String a Long. */
    @Named("toLong")
    default Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** Convierte cualquier Object a String (o devuelve null si es null). */
    @Named("toStringObject")
    default String toStringObject(Object value) {
        return (value == null) ? null : value.toString();
    }

    // Explicit mapping method for Object to String for MapStruct
    @Named("mapObjectToString")
    default String mapObjectToString(Object value) {
        return toStringObject(value);
    }
}

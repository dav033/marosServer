package io.dav033.maroconstruction.mappers;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;

import java.util.Map;

@Slf4j
@Component
public class SupabasePayloadMapper {

    // Método principal que decide de dónde extraer los datos
    public LeadPayloadDto toDto(SupabaseWebhookPayload payload) {
        log.info("🔍 Converting SupabaseWebhookPayload to LeadPayloadDto");
        log.info("📋 Payload type: {}, table: {}", payload.getType(), payload.getTable());
        
        // Para DELETE, usar oldRecord; para INSERT/UPDATE, usar record
        Map<String, Object> sourceRecord = "DELETE".equals(payload.getType()) 
            ? payload.getOldRecord() 
            : payload.getRecord();
            
        if (sourceRecord == null) {
            log.warn("⚠️ Source record is null in payload");
            return new LeadPayloadDto();
        }
        
        log.info("📄 Raw record keys: {}", sourceRecord.keySet());
        log.info("📄 Raw record values: {}", sourceRecord);
        
        LeadPayloadDto dto = new LeadPayloadDto();
        
        // Extraer y asignar cada campo con logging detallado
        Integer id = toInteger(sourceRecord.get("id"));
        dto.setId(id);
        log.info("✅ Extracted id: {}", id);
        
        String leadNumber = toStringObject(sourceRecord.get("lead_number"));
        dto.setLeadNumber(leadNumber);
        log.info("✅ Extracted lead_number: '{}'", leadNumber);
        
        String name = toStringObject(sourceRecord.get("name"));
        dto.setName(name);
        log.info("✅ Extracted name: '{}'", name);
        
        String leadType = toStringObject(sourceRecord.get("lead_type"));
        dto.setLeadType(leadType);
        log.info("✅ Extracted lead_type: '{}'", leadType);
        
        String location = toStringObject(sourceRecord.get("location"));
        dto.setLocation(location);
        log.info("✅ Extracted location: '{}'", location);
        
        String startDate = toStringObject(sourceRecord.get("start_date"));
        dto.setStartDate(startDate);
        log.info("✅ Extracted start_date: '{}'", startDate);
        
        String status = toStringObject(sourceRecord.get("status"));
        dto.setStatus(status);
        log.info("✅ Extracted status: '{}'", status);
        
        // CRÍTICO: contact_id
        Long contactId = toLong(sourceRecord.get("contact_id"));
        dto.setContactId(contactId);
        log.info("🎯 CRITICAL - Extracted contact_id: {} (type: {})", 
                contactId, contactId != null ? contactId.getClass().getSimpleName() : "null");
        
        if (contactId == null) {
            log.error("❌ ALERT: contact_id is NULL in webhook payload! This will prevent contact updates in ClickUp");
            log.error("❌ Available keys in record: {}", sourceRecord.keySet());
            log.error("❌ contact_id raw value: {} (type: {})", 
                    sourceRecord.get("contact_id"), 
                    sourceRecord.get("contact_id") != null ? sourceRecord.get("contact_id").getClass().getSimpleName() : "null");
        }
        
        log.info("✅ Final DTO: id={}, leadNumber='{}', name='{}', contactId={}", 
                dto.getId(), dto.getLeadNumber(), dto.getName(), dto.getContactId());
        
        return dto;
    }

    /** Convierte cualquier Object numérico o String a Integer. */
    private Integer toInteger(Object value) {
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
    private Long toLong(Object value) {
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
    private String toStringObject(Object value) {
        return (value == null) ? null : value.toString();
    }

    // Explicit mapping method for Object to String for MapStruct
    public String mapObjectToString(Object value) {
        return toStringObject(value);
    }
}

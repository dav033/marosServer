package io.dav033.maroconstruction.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;

@Mapper(componentModel = "spring")
public interface SupabasePayloadMapper {

    @Mapping(target = "id",
             source = "record.id",
             qualifiedByName = "toInteger")
    @Mapping(target = "leadNumber",
             source = "record.lead_number",
             qualifiedByName = "toStringObject")
    @Mapping(target = "name",
             source = "record.name",
             qualifiedByName = "toStringObject")
    @Mapping(target = "leadType",
             source = "record.lead_type",
             qualifiedByName = "toStringObject")
    @Mapping(target = "location",
             source = "record.location",
             qualifiedByName = "toStringObject")
    @Mapping(target = "startDate",
             source = "record.start_date",
             qualifiedByName = "toStringObject")
    @Mapping(target = "status",
             source = "record.status",
             qualifiedByName = "toStringObject")
    @Mapping(target = "contactId",
             source = "record.contact_id",
             qualifiedByName = "toLong")
    LeadPayloadDto toDto(SupabaseWebhookPayload payload);

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

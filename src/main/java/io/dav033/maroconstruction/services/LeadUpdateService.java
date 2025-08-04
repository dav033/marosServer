package io.dav033.maroconstruction.services;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.mappers.LeadToClickUpTaskMapper;
import io.dav033.maroconstruction.mappers.SupabasePayloadMapper;
import io.dav033.maroconstruction.dto.LeadPayloadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadUpdateService {

    private final ClickUpService clickUpService;
    private final SupabasePayloadMapper payloadMapper;
    private final LeadToClickUpTaskMapper taskMapper;

    @Transactional
    public ClickUpTaskResponse processLeadUpdate(SupabaseWebhookPayload payload) {
        log.info("🚀 INICIANDO PROCESAMIENTO DE UPDATE DE LEAD");
        log.info("📥 Payload recibido: type={}, table={}", payload.getType(), payload.getTable());
        
        if (!clickUpService.isConfigured()) {
            log.warn("❌ ClickUp no configurado. Se omite actualización de tarea.");
            return null;
        }
        
        log.info("✅ ClickUp está configurado correctamente");
        
        try {
            log.info("🔄 Convirtiendo payload a DTO...");
            LeadPayloadDto dto = payloadMapper.toDto(payload);
            log.info("✅ DTO creado exitosamente");
            
            log.info("📊 DATOS DEL LEAD A ACTUALIZAR:");
            log.info("   • Lead Number: '{}'", dto.getLeadNumber());
            log.info("   • Contact ID: {}", dto.getContactId());
            log.info("   • Name: '{}'", dto.getName());
            log.info("   • Location: '{}'", dto.getLocation());
            log.info("   • Lead Type: '{}'", dto.getLeadType());
            log.info("   • Start Date: '{}'", dto.getStartDate());
            
            if (dto.getLeadNumber() == null || dto.getLeadNumber().trim().isEmpty()) {
                log.error("❌ FALLO: lead_number no disponible en el payload");
                log.error("❌ Sin lead number no se puede buscar la tarea en ClickUp");
                return null;
            }
            
            log.info("✅ Lead number válido: '{}'", dto.getLeadNumber());
            
            log.info("✅ Lead number válido: '{}'", dto.getLeadNumber());
            
            // Buscar el ID de la tarea en ClickUp por lead_number
            log.info("🔍 Buscando tarea en ClickUp por lead_number: '{}'", dto.getLeadNumber());
            String taskId = clickUpService.findTaskIdByLeadNumber(dto.getLeadNumber());
            
            if (taskId != null) {
                log.info("🎯 TAREA ENCONTRADA EN CLICKUP:");
                log.info("   • Lead Number: '{}'", dto.getLeadNumber());
                log.info("   • Task ID: '{}'", taskId);
                
                // Crear el request de actualización usando el mapper
                log.info("🔧 Creando request de actualización...");
                ClickUpTaskRequest updateRequest = taskMapper.toClickUpTask(dto);
                
                log.info("📝 REQUEST DE ACTUALIZACIÓN CREADO:");
                log.info("   • Name: '{}'", updateRequest.getName());
                log.info("   • Description length: {} chars", 
                        updateRequest.getDescription() != null ? updateRequest.getDescription().length() : 0);
                log.info("   • Custom Fields: {} fields", 
                        updateRequest.getCustomFields() != null ? updateRequest.getCustomFields().size() : 0);
                
                // Log detallado de custom fields
                if (updateRequest.getCustomFields() != null && !updateRequest.getCustomFields().isEmpty()) {
                    log.info("🏷️ CUSTOM FIELDS A ACTUALIZAR:");
                    for (ClickUpTaskRequest.CustomField field : updateRequest.getCustomFields()) {
                        log.info("   • Field ID: '{}' → Value: '{}'", field.getId(), field.getValue());
                    }
                } else {
                    log.warn("⚠️ NO HAY CUSTOM FIELDS - La información de contacto podría no actualizarse");
                }
                
                // Usar updateTaskWithNewContact para asegurar que se actualice toda la información del contacto
                log.info("🔄 INICIANDO ACTUALIZACIÓN COMPLETA EN CLICKUP");
                log.info("🔄 Usando updateTaskWithNewContact para asegurar actualización completa del contacto");
                
                try {
                    ClickUpTaskResponse response = clickUpService.updateTaskWithNewContact(taskId, updateRequest);
                    
                    if (response != null) {
                        log.info("🎉 ÉXITO: Tarea actualizada correctamente en ClickUp");
                        log.info("   • Lead Number: '{}'", dto.getLeadNumber());
                        log.info("   • Task ID: '{}'", response.getId());
                        log.info("   • Task URL: '{}'", response.getUrl());
                        log.info("✅ PROCESO DE ACTUALIZACIÓN COMPLETADO EXITOSAMENTE");
                    } else {
                        log.error("❌ FALLO: Response de ClickUp fue null");
                        log.error("❌ La tarea no se pudo actualizar para leadNumber={}", dto.getLeadNumber());
                    }
                    
                    return response;
                    
                } catch (Exception clickUpException) {
                    log.error("💥 ERROR DURANTE ACTUALIZACIÓN EN CLICKUP:");
                    log.error("   • Lead Number: '{}'", dto.getLeadNumber());
                    log.error("   • Task ID: '{}'", taskId);
                    log.error("   • Error Type: {}", clickUpException.getClass().getSimpleName());
                    log.error("   • Error Message: {}", clickUpException.getMessage());
                    log.error("❌ FALLO EN CLICKUP - RE-LANZANDO EXCEPCIÓN", clickUpException);
                    throw clickUpException;
                }
                
            } else {
                log.error("🚫 TAREA NO ENCONTRADA EN CLICKUP:");
                log.error("   • Lead Number buscado: '{}'", dto.getLeadNumber());
                log.error("   • Posibles causas:");
                log.error("     - La tarea no existe en ClickUp");
                log.error("     - El custom field lead_number no coincide");
                log.error("     - Error de conectividad con ClickUp");
                log.error("❌ SIN TAREA = SIN ACTUALIZACIÓN");
                return null;
            }
            
        } catch (Exception e) {
            log.error("💥💥💥 ERROR CRÍTICO DURANTE PROCESAMIENTO DE UPDATE:");
            log.error("   • Error Type: {}", e.getClass().getSimpleName());
            log.error("   • Error Message: {}", e.getMessage());
            log.error("   • Payload: {}", payload);
            
            // Log del stack trace completo para debugging
            log.error("📚 STACK TRACE COMPLETO:", e);
            
            log.error("❌❌❌ FALLO TOTAL EN PROCESAMIENTO DE UPDATE - RE-LANZANDO EXCEPCIÓN");
            throw e;
        }
    }
}

package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.LeadPayloadDto;
import io.dav033.maroconstruction.mappers.CustomFieldsBuilder;
import io.dav033.maroconstruction.mappers.LeadToClickUpTaskMapper;
import io.dav033.maroconstruction.services.ClickUpService;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/test-builder")
@RequiredArgsConstructor
public class CustomFieldsTestController {

    private final CustomFieldsBuilder customFieldsBuilder;
    private final LeadToClickUpTaskMapper taskMapper;
    private final ClickUpService clickUpService;

    @PostMapping("/test-contact-change/{leadNumber}")
    public ResponseEntity<?> testCustomFieldsBuilder(@PathVariable String leadNumber) {
        try {
            log.info("🧪 Testing CustomFieldsBuilder with forced contact ID 5 for lead: {}", leadNumber);
            
            // Crear un DTO simulado
            LeadPayloadDto dto = new LeadPayloadDto();
            dto.setLeadNumber(leadNumber);
            dto.setName("PRUEBA CONTACTO ID 5");
            dto.setLocation("1");
            dto.setContactId(99L); // Este será ignorado por el CustomFieldsBuilder forzado
            
            // Probar el CustomFieldsBuilder directamente
            var customFields = customFieldsBuilder.build(dto);
            
            log.info("✅ CustomFieldsBuilder generated {} fields", customFields.size());
            
            // Buscar la tarea en ClickUp
            String taskId = clickUpService.findTaskIdByLeadNumber(leadNumber);
            
            if (taskId != null) {
                log.info("Found task ID: {}", taskId);
                
                // Crear request completo
                ClickUpTaskRequest updateRequest = taskMapper.toClickUpTask(dto);
                
                // Actualizar la tarea
                var response = clickUpService.updateTask(taskId, updateRequest);
                
                if (response != null) {
                    log.info("✅ Task updated successfully with contact ID 5 data!");
                    return ResponseEntity.ok()
                        .body("✅ SUCCESS! Task updated with contact ID 5 data. " +
                              "CustomFields generated: " + customFields.size() + 
                              ", TaskId: " + taskId);
                } else {
                    return ResponseEntity.internalServerError()
                        .body("❌ Failed to update task in ClickUp");
                }
            } else {
                return ResponseEntity.status(404)
                    .body("❌ Task not found for lead number: " + leadNumber);
            }
            
        } catch (Exception e) {
            log.error("❌ Error testing CustomFieldsBuilder", e);
            return ResponseEntity.internalServerError()
                .body("❌ Error: " + e.getMessage());
        }
    }

    @PostMapping("/test-complete-update/{leadNumber}")
    public ResponseEntity<?> testCompleteTaskUpdate(@PathVariable String leadNumber) {
        try {
            log.info("🧪 Testing COMPLETE task update with contact ID 5 for lead: {}", leadNumber);
            
            // Crear un DTO simulado con información del contacto ID 5
            LeadPayloadDto dto = new LeadPayloadDto();
            dto.setLeadNumber(leadNumber);
            dto.setName("PRUEBA CONTACTO ID 5 - ACTUALIZACIÓN COMPLETA");
            dto.setLocation("Miami, FL");
            dto.setContactId(99L); // Este será ignorado por el CustomFieldsBuilder forzado
            dto.setLeadType("construction");
            dto.setStartDate("2025-08-04");
            
            // Buscar la tarea en ClickUp
            String taskId = clickUpService.findTaskIdByLeadNumber(leadNumber);
            
            if (taskId != null) {
                log.info("Found task ID: {}", taskId);
                
                // Crear request completo con TODA la información nueva
                ClickUpTaskRequest completeUpdateRequest = taskMapper.toClickUpTask(dto);
                
                log.info("🔄 Using new updateTaskWithNewContact method for COMPLETE update");
                
                // Usar el nuevo método para actualización completa
                var response = clickUpService.updateTaskWithNewContact(taskId, completeUpdateRequest);
                
                if (response != null) {
                    log.info("🎉 COMPLETE task update successful!");
                    return ResponseEntity.ok()
                        .body("🎉 SUCCESS! COMPLETE task update with contact ID 5 data. " +
                              "Updated: name, description, custom fields. TaskId: " + taskId);
                } else {
                    return ResponseEntity.internalServerError()
                        .body("❌ Failed to perform complete task update in ClickUp");
                }
            } else {
                return ResponseEntity.status(404)
                    .body("❌ Task not found for lead number: " + leadNumber);
            }
            
        } catch (Exception e) {
            log.error("❌ Error testing complete task update", e);
            return ResponseEntity.internalServerError()
                .body("❌ Error: " + e.getMessage());
        }
    }
}

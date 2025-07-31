package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.services.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {
    
    private final WebhookService webhookService;
    
    @Value("${supabase.webhook.secret:#{null}}")
    private String webhookSecret;
    
    @PostMapping("/supabase")
    public ResponseEntity<Map<String, Object>> receiveSupabaseWebhook(
            @RequestBody SupabaseWebhookPayload payload,
            HttpServletRequest request) {
        
        log.info("Webhook recibido de Supabase: tabla={}, tipo={}", 
            payload.getTable(), payload.getType());
        
        Map<String, Object> response = new HashMap<>();
        
        Object result = webhookService.processSupabaseWebhook(payload);
        
        if (result instanceof ClickUpTaskResponse) {
            ClickUpTaskResponse taskResponse = (ClickUpTaskResponse) result;
            response.put("success", true);
            response.put("message", "Tarea creada en ClickUp");
            response.put("operation", "CREATE");
            response.put("clickup_task_id", taskResponse.getId());
            response.put("clickup_task_url", taskResponse.getUrl());
            log.info("Webhook procesado exitosamente. Tarea ClickUp creada: {}", taskResponse.getId());
        } else if (result instanceof Boolean && (Boolean) result) {
            response.put("success", true);
            response.put("message", "Tarea eliminada de ClickUp");
            response.put("operation", "DELETE");
            log.info("Webhook procesado exitosamente. Tarea ClickUp eliminada");
        } else {
            response.put("success", true);
            response.put("message", "Webhook procesado pero no se realizó operación en ClickUp");
            response.put("operation", "NONE");
            log.info("Webhook procesado pero no se realizó operación en ClickUp");
        }
        
        return ResponseEntity.ok(response);
    }
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "webhook");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test-contact/{contactId}")
    public ResponseEntity<Map<String, Object>> testContactWebhook(@PathVariable Long contactId) {
        log.info("🧪 Test webhook iniciado para contactId: {}", contactId);
        
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> simulatedLeadData = new HashMap<>();
        simulatedLeadData.put("lead_number", "LEAD-TEST-" + System.currentTimeMillis());
        simulatedLeadData.put("lead_type", "construction");
        simulatedLeadData.put("location", "Miami Test Project Location");
        simulatedLeadData.put("status", "new");
        
        SupabaseWebhookPayload simulatedPayload = new SupabaseWebhookPayload();
        simulatedPayload.setTable("leads");
        simulatedPayload.setType("INSERT");
        simulatedPayload.setRecord(simulatedLeadData);
        
        simulatedLeadData.put("contact_id", contactId);
        
        log.info("📋 Datos simulados de lead: {}", simulatedLeadData);
        
        Object result = webhookService.processSupabaseWebhook(simulatedPayload);
        
        if (result instanceof ClickUpTaskResponse) {
            ClickUpTaskResponse taskResponse = (ClickUpTaskResponse) result;
            response.put("success", true);
            response.put("message", "Tarea de prueba creada en ClickUp con contacto real");
            response.put("contact_id", contactId);
            response.put("clickup_task_id", taskResponse.getId());
            response.put("clickup_task_url", taskResponse.getUrl());
            response.put("lead_data", simulatedLeadData);
            log.info("✅ Test exitoso. Tarea ClickUp creada: {} para contacto: {}", taskResponse.getId(), contactId);
        } else {
            response.put("success", false);
            response.put("message", "No se pudo crear la tarea de prueba");
            response.put("contact_id", contactId);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-delete/{leadId}")
    public ResponseEntity<Map<String, Object>> testDeleteWebhook(@PathVariable Long leadId) {
        log.info("🧪 Test DELETE webhook iniciado para leadId: {}", leadId);
        
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> simulatedLeadData = new HashMap<>();
        simulatedLeadData.put("id", leadId);
        simulatedLeadData.put("lead_number", "LEAD-DELETE-TEST-" + leadId);
        
        SupabaseWebhookPayload simulatedPayload = new SupabaseWebhookPayload();
        simulatedPayload.setTable("leads");
        simulatedPayload.setType("DELETE");
        simulatedPayload.setRecord(simulatedLeadData);
        
        log.info("📋 Datos simulados de lead para DELETE: {}", simulatedLeadData);
        
        Object result = webhookService.processSupabaseWebhook(simulatedPayload);
        
        if (result instanceof Boolean && (Boolean) result) {
            response.put("success", true);
            response.put("message", "Tarea de prueba eliminada de ClickUp");
            response.put("lead_id", leadId);
            response.put("operation", "DELETE");
            log.info("✅ Test DELETE exitoso. Tarea ClickUp eliminada para leadId: {}", leadId);
        } else {
            response.put("success", false);
            response.put("message", "No se pudo eliminar la tarea de prueba o no se encontró mapping");
            response.put("lead_id", leadId);
            response.put("operation", "DELETE_FAILED");
        }
        
        return ResponseEntity.ok(response);
    }
    
}

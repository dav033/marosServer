package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.services.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para recibir webhooks de Supabase
 */
@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {
    
    private final WebhookService webhookService;
    
    @Value("${supabase.webhook.secret:#{null}}")
    private String webhookSecret;
    
    /**
     * Endpoint para recibir webhooks de Supabase
     * 
     * @param payload Datos del webhook
     * @param request Request HTTP para acceder a headers
     * @return Respuesta HTTP
     */
    @PostMapping("/supabase")
    public ResponseEntity<Map<String, Object>> receiveSupabaseWebhook(
            @RequestBody SupabaseWebhookPayload payload,
            HttpServletRequest request) {
        
        log.info("Webhook recibido de Supabase: tabla={}, tipo={}", 
            payload.getTable(), payload.getType());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar secret si está configurado - TEMPORALMENTE DESHABILITADO PARA PRUEBAS
            // if (!validateWebhookSecret(request)) {
            //     log.warn("Webhook rechazado: secret inválido");
            //     response.put("error", "Unauthorized");
            //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            // }
            
            // Procesar el webhook
            ClickUpTaskResponse taskResponse = webhookService.processSupabaseWebhook(payload);
            
            if (taskResponse != null) {
                response.put("success", true);
                response.put("message", "Tarea creada en ClickUp");
                response.put("clickup_task_id", taskResponse.getId());
                response.put("clickup_task_url", taskResponse.getUrl());
                log.info("Webhook procesado exitosamente. Tarea ClickUp creada: {}", taskResponse.getId());
            } else {
                response.put("success", true);
                response.put("message", "Webhook procesado pero no se creó tarea");
                log.info("Webhook procesado pero no se creó tarea en ClickUp");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error procesando webhook de Supabase", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint de health check para verificar que el webhook está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "webhook");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de prueba para simular webhook real con contacto específico
     * 
     * @param contactId ID del contacto a procesar
     * @return Respuesta con información de la tarea creada
     */
    @PostMapping("/test-contact/{contactId}")
    public ResponseEntity<Map<String, Object>> testContactWebhook(@PathVariable Long contactId) {
        log.info("🧪 Test webhook iniciado para contactId: {}", contactId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Simular datos de webhook real
            Map<String, Object> simulatedLeadData = new HashMap<>();
            simulatedLeadData.put("lead_number", "LEAD-TEST-" + System.currentTimeMillis());
            simulatedLeadData.put("lead_type", "construction");
            simulatedLeadData.put("location", "Miami Test Project Location");
            simulatedLeadData.put("status", "new");
            
            // Simular payload de webhook
            SupabaseWebhookPayload simulatedPayload = new SupabaseWebhookPayload();
            simulatedPayload.setTable("leads");
            simulatedPayload.setType("INSERT");
            simulatedPayload.setRecord(simulatedLeadData);
            
            // Agregar contactId al record
            simulatedLeadData.put("contact_id", contactId);
            
            log.info("📋 Datos simulados de lead: {}", simulatedLeadData);
            
            // Procesar usando el servicio existente
            ClickUpTaskResponse taskResponse = webhookService.processSupabaseWebhook(simulatedPayload);
            
            if (taskResponse != null) {
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
            
        } catch (Exception e) {
            log.error("❌ Error en test webhook para contactId {}: {}", contactId, e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("contact_id", contactId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Valida el secret del webhook si está configurado
     * 
     * @param request Request HTTP
     * @return true si es válido o no está configurado, false si es inválido
     */
    private boolean validateWebhookSecret(HttpServletRequest request) {
        // Si no hay secret configurado, no validamos
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            return true;
        }
        
        // Verificar header específico de Supabase webhook
        String supabaseSecret = request.getHeader("X-Supabase-Webhook-Secret");
        if (supabaseSecret != null) {
            return supabaseSecret.equals(webhookSecret);
        }
        
        // Verificar header de autorización como fallback
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            return authHeader.equals("Bearer " + webhookSecret) || 
                   authHeader.equals(webhookSecret);
        }
        
        return false;
    }
}

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
@RequestMapping("/api/webhook/leads")
@RequiredArgsConstructor
public class LeadUpdateController {
    
    private final WebhookService webhookService;
    
    @Value("${supabase.webhook.secret:#{null}}")
    private String webhookSecret;
    
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> receiveLeadUpdate(
            @RequestBody SupabaseWebhookPayload payload,
            HttpServletRequest request) {
        
        log.info("Webhook UPDATE recibido de Supabase: tabla={}, tipo={}", 
                payload.getTable(), payload.getType());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!"leads".equals(payload.getTable())) {
                log.debug("Ignorado: no es tabla leads");
                response.put("status", "ignored");
                response.put("message", "Not leads table");
                return ResponseEntity.ok(response);
            }
            
            if (!"UPDATE".equals(payload.getType())) {
                log.debug("Ignorado: no es operación UPDATE");
                response.put("status", "ignored");
                response.put("message", "Not UPDATE operation");
                return ResponseEntity.ok(response);
            }
            
            ClickUpTaskResponse taskResponse = webhookService.processLeadUpdate(payload);
            
            if (taskResponse != null) {
                log.info("Lead UPDATE procesado exitosamente y tarea actualizada en ClickUp");
                response.put("status", "success");
                response.put("message", "Task updated in ClickUp");
                response.put("clickup_task", taskResponse);
            } else {
                log.info("Lead UPDATE procesado pero no se actualizó en ClickUp");
                response.put("status", "processed");
                response.put("message", "Lead processed but ClickUp task not updated");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al procesar webhook UPDATE de lead", e);
            response.put("status", "error");
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

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
public class LeadInsertController {
    
    private final WebhookService webhookService;
    
    @Value("${supabase.webhook.secret:#{null}}")
    private String webhookSecret;
    
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> receiveLeadInsert(
            @RequestBody SupabaseWebhookPayload payload,
            HttpServletRequest request) {
        
        log.info("Webhook INSERT recibido de Supabase: tabla={}, tipo={}", 
                payload.getTable(), payload.getType());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!"leads".equals(payload.getTable())) {
                log.debug("Ignorado: no es tabla leads");
                response.put("status", "ignored");
                response.put("message", "Not leads table");
                return ResponseEntity.ok(response);
            }
            
            if (!"INSERT".equals(payload.getType())) {
                log.debug("Ignorado: no es operación INSERT");
                response.put("status", "ignored");
                response.put("message", "Not INSERT operation");
                return ResponseEntity.ok(response);
            }
            
            ClickUpTaskResponse taskResponse = webhookService.processLeadInsert(payload);
            
            if (taskResponse != null) {
                log.info("Lead procesado exitosamente y tarea creada en ClickUp");
                response.put("status", "success");
                response.put("message", "Task created in ClickUp");
                response.put("clickup_task", taskResponse);
            } else {
                log.info("Lead procesado pero no se creó tarea en ClickUp");
                response.put("status", "processed");
                response.put("message", "Lead processed but ClickUp task not created");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al procesar webhook INSERT de lead", e);
            response.put("status", "error");
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

package io.dav033.maroconstruction.controllers;

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
public class LeadDeleteController {
    
    private final WebhookService webhookService;
    
    @Value("${supabase.webhook.secret:#{null}}")
    private String webhookSecret;
    
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> receiveLeadDelete(
            @RequestBody SupabaseWebhookPayload payload,
            HttpServletRequest request) {
        
        log.info("Webhook DELETE recibido de Supabase: tabla={}, tipo={}", 
                payload.getTable(), payload.getType());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!"leads".equals(payload.getTable())) {
                log.debug("Ignorado: no es tabla leads");
                response.put("status", "ignored");
                response.put("message", "Not leads table");
                return ResponseEntity.ok(response);
            }
            
            if (!"DELETE".equals(payload.getType())) {
                log.debug("Ignorado: no es operación DELETE");
                response.put("status", "ignored");
                response.put("message", "Not DELETE operation");
                return ResponseEntity.ok(response);
            }
            
            Boolean deleted = webhookService.processLeadDelete(payload);
            
            if (deleted) {
                log.info("Lead procesado exitosamente y tarea eliminada de ClickUp");
                response.put("status", "success");
                response.put("message", "Task deleted from ClickUp");
                response.put("deleted", true);
            } else {
                log.info("Lead procesado pero no se eliminó tarea de ClickUp");
                response.put("status", "processed");
                response.put("message", "Lead processed but ClickUp task not deleted");
                response.put("deleted", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al procesar webhook DELETE de lead", e);
            response.put("status", "error");
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

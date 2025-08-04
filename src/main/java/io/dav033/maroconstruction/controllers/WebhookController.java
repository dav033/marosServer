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
        
        try {
            if (!"leads".equals(payload.getTable())) {
                log.debug("Ignorado: no es tabla leads");
                response.put("status", "ignored");
                response.put("message", "Not leads table");
                return ResponseEntity.ok(response);
            }

            Object result = webhookService.processSupabaseWebhook(payload);
            
            switch (payload.getType()) {
                case "INSERT":
                    if (result instanceof ClickUpTaskResponse) {
                        log.info("Webhook procesado exitosamente: tarea creada en ClickUp");
                        response.put("status", "success");
                        response.put("message", "Task created in ClickUp");
                        response.put("clickup_task", result);
                    } else {
                        log.info("Webhook procesado pero no se realizó operación en ClickUp");
                        response.put("status", "processed");
                        response.put("message", "Processed but ClickUp task not created");
                    }
                    break;
                    
                case "DELETE":
                    Boolean deleteResult = (Boolean) result;
                    if (Boolean.TRUE.equals(deleteResult)) {
                        log.info("Webhook procesado exitosamente: tarea eliminada de ClickUp");
                        response.put("status", "success");
                        response.put("message", "Task deleted from ClickUp");
                        response.put("deleted", true);
                    } else {
                        log.info("Webhook procesado pero no se realizó operación en ClickUp");
                        response.put("status", "processed");
                        response.put("message", "Processed but ClickUp task not deleted");
                        response.put("deleted", false);
                    }
                    break;
                    
                case "UPDATE":
                    if (result instanceof ClickUpTaskResponse) {
                        log.info("Webhook procesado exitosamente: tarea actualizada en ClickUp");
                        response.put("status", "success");
                        response.put("message", "Task updated in ClickUp");
                        response.put("updated", true);
                        response.put("clickup_task", result);
                    } else {
                        log.info("Webhook procesado pero no se realizó operación en ClickUp");
                        response.put("status", "processed");
                        response.put("message", "Processed but ClickUp task not updated");
                        response.put("updated", false);
                    }
                    break;
                    
                default:
                    log.debug("Tipo de operación no soportado: {}", payload.getType());
                    response.put("status", "ignored");
                    response.put("message", "Operation type not supported: " + payload.getType());
                    break;
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al procesar webhook Supabase", e);
            response.put("status", "error");
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "webhook");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}

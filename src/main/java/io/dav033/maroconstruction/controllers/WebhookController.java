package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.services.LeadInsertService;
import io.dav033.maroconstruction.services.LeadDeleteService;
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
    
    private final LeadInsertService leadInsertService;
    private final LeadDeleteService leadDeleteService;
    
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

            switch (payload.getType()) {
                case "INSERT":
                    var insertResult = leadInsertService.processLeadInsert(payload);
                    if (insertResult != null) {
                        log.info("Webhook procesado exitosamente: tarea creada en ClickUp");
                        response.put("status", "success");
                        response.put("message", "Task created in ClickUp");
                        response.put("clickup_task", insertResult);
                    } else {
                        log.info("Webhook procesado pero no se realizó operación en ClickUp");
                        response.put("status", "processed");
                        response.put("message", "Processed but ClickUp task not created");
                    }
                    break;
                    
                case "DELETE":
                    var deleteResult = leadDeleteService.processLeadDelete(payload);
                    if (deleteResult) {
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

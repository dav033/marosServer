package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.services.WebhookService;
import io.dav033.maroconstruction.dto.webhook.SupabaseWebhookPayload;
import io.dav033.maroconstruction.dto.webhook.ClickUpTaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class ContactTestController {

    private final WebhookService webhookService;

    @PostMapping("/change-contact/{leadNumber}/{newContactId}")
    public ResponseEntity<?> testContactChange(
            @PathVariable String leadNumber,
            @PathVariable Long newContactId) {
        
        try {
            log.info("🧪 Testing contact change: leadNumber={}, newContactId={}", leadNumber, newContactId);
            
            // Crear un payload simulado como si viniera de Supabase
            SupabaseWebhookPayload payload = new SupabaseWebhookPayload();
            payload.setType("UPDATE");
            payload.setTable("leads");
            
            // Simular los datos del lead con el nuevo contacto
            Map<String, Object> record = new HashMap<>();
            record.put("id", 103);
            record.put("lead_number", leadNumber);
            record.put("name", "PRUEBA CAMBIO CONTACTO");
            record.put("location", "1");
            record.put("contact_id", newContactId); // NUEVO CONTACTO
            record.put("start_date", "2025-08-04");
            record.put("status", "TO_DO");
            
            payload.setRecord(record);
            
            log.info("🔄 Simulating webhook payload with new contact_id: {}", newContactId);
            
            // Procesar el update
            ClickUpTaskResponse result = webhookService.processLeadUpdate(payload);
            
            if (result != null) {
                log.info("✅ Test completed successfully! Task updated with new contact information");
                return ResponseEntity.ok()
                    .body("✅ Contact change test completed successfully! " +
                          "Lead " + leadNumber + " now has contact_id=" + newContactId + 
                          ". Check ClickUp task: " + result.getId());
            } else {
                log.warn("⚠️ Test completed but no ClickUp update occurred");
                return ResponseEntity.ok()
                    .body("⚠️ Test completed but no ClickUp update occurred");
            }
            
        } catch (Exception e) {
            log.error("❌ Error during contact change test", e);
            return ResponseEntity.internalServerError()
                .body("❌ Error: " + e.getMessage());
        }
    }

    @PostMapping("/simulate-contact-update")
    public ResponseEntity<?> simulateRealContactUpdate() {
        try {
            log.info("🧪 Simulating REAL contact update scenario");
            
            // Simular exactamente lo que pasaría si cambias el contacto en el frontend
            SupabaseWebhookPayload payload = new SupabaseWebhookPayload();
            payload.setType("UPDATE");
            payload.setTable("leads");
            
            Map<String, Object> record = new HashMap<>();
            record.put("id", 103);
            record.put("lead_number", "042-0825");
            record.put("name", "prueba");
            record.put("location", "1");
            record.put("contact_id", 1L); // Cambiar a contacto ID 1 (probablemente existe)
            record.put("start_date", "2025-08-04");
            record.put("status", "TO_DO");
            
            payload.setRecord(record);
            
            log.info("🔄 Processing simulated webhook with contact_id=1");
            
            ClickUpTaskResponse result = webhookService.processLeadUpdate(payload);
            
            if (result != null) {
                return ResponseEntity.ok()
                    .body("✅ Simulation completed! Lead 042-0825 now should show contact_id=1 information in ClickUp");
            } else {
                return ResponseEntity.ok()
                    .body("⚠️ Simulation completed but no ClickUp update");
            }
            
        } catch (Exception e) {
            log.error("❌ Error during simulation", e);
            return ResponseEntity.internalServerError()
                .body("❌ Error: " + e.getMessage());
        }
    }
}

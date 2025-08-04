package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.services.ClickUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import io.dav033.maroconstruction.services.ClickUpHeadersProvider;
import io.dav033.maroconstruction.config.ClickUpConfig;

@Slf4j
@RestController
@RequestMapping("/api/debug/clickup")
@RequiredArgsConstructor
public class ClickUpDebugController {

    private final ClickUpService clickUpService;
    private final ClickUpHeadersProvider headersProvider;
    private final ClickUpConfig config;
    private final RestTemplate restTemplate;

    @GetMapping("/task/{taskId}/fields")
    public ResponseEntity<?> getTaskCustomFields(@PathVariable String taskId) {
        try {
            log.info("🔍 Getting custom fields for task: {}", taskId);
            
            String url = String.format("%s/task/%s", config.getApiUrl(), taskId);
            HttpEntity<Void> entity = new HttpEntity<>(headersProvider.get());
            
            log.info("🌐 Making request to: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );
            
            log.info("✅ Response received: {}", response.getBody());
            
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("❌ Error getting task custom fields", e);
            return ResponseEntity.internalServerError()
                .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}/test-update")
    public ResponseEntity<?> testCustomFieldUpdate(@PathVariable String taskId) {
        try {
            log.info("🧪 Testing custom field update for task: {}", taskId);
            
            // Probar actualizar un custom field específico
            String fieldId = "524a8b7c-cfb7-4361-886e-59a019f8c5b5"; // Contact Name
            String testValue = "TEST UPDATE - " + System.currentTimeMillis();
            
            String url = String.format("%s/task/%s/field/%s", 
                config.getApiUrl(), taskId, fieldId);
            
            var fieldUpdateRequest = new Object() {
                @SuppressWarnings("unused")
                public final Object value = testValue;
            };
            
            HttpEntity<Object> entity = new HttpEntity<>(fieldUpdateRequest, headersProvider.get());
            
            log.info("🌐 Testing update to URL: {}", url);
            log.info("📋 Test value: {}", testValue);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );
            
            log.info("✅ Test update response: Status={}, Body={}", 
                response.getStatusCode(), response.getBody());
            
            return ResponseEntity.ok()
                .body("Test update completed. Status: " + response.getStatusCode() + 
                      ", Response: " + response.getBody());
            
        } catch (Exception e) {
            log.error("❌ Error testing custom field update", e);
            return ResponseEntity.internalServerError()
                .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/find-task/{leadNumber}")
    public ResponseEntity<?> findTaskByLeadNumber(@PathVariable String leadNumber) {
        try {
            log.info("🔍 Finding task with lead number: {}", leadNumber);
            
            String taskId = clickUpService.findTaskIdByLeadNumber(leadNumber);
            
            if (taskId != null) {
                log.info("✅ Found task: {}", taskId);
                return ResponseEntity.ok()
                    .body("Task found: " + taskId);
            } else {
                log.warn("❌ Task not found for lead number: {}", leadNumber);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("❌ Error finding task", e);
            return ResponseEntity.internalServerError()
                .body("Error: " + e.getMessage());
        }
    }
}

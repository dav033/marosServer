package io.dav033.maroconstruction.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @PostMapping("/payload")
    public ResponseEntity<String> testPayload(@RequestBody String payload) {
        log.info("Received payload: {}", payload);
        return ResponseEntity.ok("Payload received and logged. Check server logs.");
    }
}

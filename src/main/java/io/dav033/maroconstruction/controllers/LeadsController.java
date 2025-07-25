package io.dav033.maroconstruction.controllers;


import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.dto.requests.GetLeadsByTypeRequest;
import io.dav033.maroconstruction.services.LeadsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leads")
@AllArgsConstructor
@CrossOrigin
public class LeadsController {

    private LeadsService leadsService;


    @GetMapping("/type")
    public ResponseEntity<List<Leads>> getLeadsBytype(@RequestBody GetLeadsByTypeRequest request) {
        try {
            List<Leads> leads = leadsService.getLeadsByType(request.getType());
            return ResponseEntity.ok(leads);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

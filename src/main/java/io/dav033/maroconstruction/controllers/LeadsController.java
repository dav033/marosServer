package io.dav033.maroconstruction.controllers;


import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.dto.requests.CreateLeadByNewContactRequest;
import io.dav033.maroconstruction.dto.requests.CreateLeadByExistingContactRequest;
import io.dav033.maroconstruction.dto.requests.GetLeadsByTypeRequest;
import io.dav033.maroconstruction.dto.requests.UpdateLeadRequest;
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


    @PostMapping("/type")
    public ResponseEntity<List<Leads>> getLeadsBytype(@RequestBody GetLeadsByTypeRequest request) {
        List<Leads> leads = leadsService.getLeadsByType(request.getType());
        return ResponseEntity.ok(leads);
    }

    @PostMapping("/new-contact")
    public ResponseEntity<Leads> createLeadByNewContact(@RequestBody CreateLeadByNewContactRequest request) {
        Leads lead = leadsService.CreateLeadByNewContact(request.getLead(), request.getContact());
        return ResponseEntity.ok(lead);
    }

    @PostMapping("/existing-contact")
    public ResponseEntity<Leads> createLeadByExistingContact(@RequestBody CreateLeadByExistingContactRequest request) {
        Leads lead = leadsService.CreateLeadByExistingContact(request.getLead(), request.getContactId());
        return ResponseEntity.ok(lead);
    }

    @PutMapping("/{leadId}")
    public ResponseEntity<Leads> updateLead(@PathVariable Long leadId, @RequestBody UpdateLeadRequest request) {
        Leads updatedLead = leadsService.updateLead(leadId, request.getLead());
        return ResponseEntity.ok(updatedLead);
    }

    @DeleteMapping("/{leadId}")
    public ResponseEntity<String> deleteLead(@PathVariable Long leadId) {
        boolean deleted = leadsService.deleteLead(leadId);
        if (deleted) {
            return ResponseEntity.ok("Lead eliminado correctamente");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

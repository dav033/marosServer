package io.dav033.maroconstruction.controllers;


import io.dav033.maroconstruction.dto.Leads;
import io.dav033.maroconstruction.dto.requests.CreateLeadByNewContactRequest;
import io.dav033.maroconstruction.dto.requests.CreateLeadByExistingContactRequest;
import io.dav033.maroconstruction.dto.requests.GetLeadsByTypeRequest;
import io.dav033.maroconstruction.dto.requests.UpdateLeadRequest;
import io.dav033.maroconstruction.enums.LeadType;
import io.dav033.maroconstruction.dto.responses.LeadNumberValidationResponse;
import io.dav033.maroconstruction.services.LeadsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leads")
@CrossOrigin
public class LeadsController {

    private final LeadsService leadsService;

    public LeadsController(LeadsService leadsService) {
        this.leadsService = leadsService;
    }

    @GetMapping
    public ResponseEntity<List<Leads>> getAllLeads() {
        List<Leads> leads = leadsService.getAllLeads();
        return ResponseEntity.ok(leads);
    }

    @PostMapping("/type")
    public ResponseEntity<List<Leads>> getLeadsBytype(@RequestBody GetLeadsByTypeRequest request) {
        List<Leads> leads = leadsService.getLeadsByType(request.getType());
        return ResponseEntity.ok(leads);
    }
    @GetMapping("/type")
    public ResponseEntity<List<Leads>> getLeadsBytypeGet(@RequestParam LeadType type) {
        List<Leads> leads = leadsService.getLeadsByType(type);
        return ResponseEntity.ok(leads);
    }

    @PostMapping("/new-contact")
    public ResponseEntity<Leads> createLeadByNewContact(
            @RequestBody CreateLeadByNewContactRequest request,
            @RequestParam(name = "skipClickUpSync", required = false, defaultValue = "false") boolean skipClickUpSync) {
        Leads lead = leadsService.createLeadWithNewContact(request.getLead(), request.getContact(), skipClickUpSync);
        return ResponseEntity.ok(lead);
    }

    @PostMapping("/existing-contact")
    public ResponseEntity<Leads> createLeadByExistingContact(
            @RequestBody CreateLeadByExistingContactRequest request,
            @RequestParam(name = "skipClickUpSync", required = false, defaultValue = "false") boolean skipClickUpSync) {
        Leads lead = leadsService.createLeadWithExistingContact(request.getLead(), request.getContactId(), skipClickUpSync);
        return ResponseEntity.ok(lead);
    }

    @GetMapping("/validate/lead-number")
    public ResponseEntity<LeadNumberValidationResponse> validateLeadNumber(
            @RequestParam String leadNumber) {
        LeadNumberValidationResponse res = leadsService.validateLeadNumber(leadNumber);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<Leads> getLeadById(@PathVariable Long leadId) {
        Leads lead = leadsService.getLeadById(leadId);
        return ResponseEntity.ok(lead);
    }

    @PutMapping("/{leadId}")
    public ResponseEntity<Leads> updateLead(@PathVariable Long leadId, @RequestBody UpdateLeadRequest request) {
        System.out.println("[LOG] Notas recibidas en controlador: " + request.getLead().getNotes());
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

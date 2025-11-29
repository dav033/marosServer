package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.ContactsCompaniesResponse;
import io.dav033.maroconstruction.services.CompanyService;
import io.dav033.maroconstruction.services.ContactsService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crm")
@CrossOrigin
public class CrmSummaryController {

    private final ContactsService contactsService;
    private final CompanyService companyService;

    public CrmSummaryController(ContactsService contactsService,
                                CompanyService companyService) {
        this.contactsService = contactsService;
        this.companyService = companyService;
    }

    /**
     * GET /crm/customers
     * Respuesta:
     * {
     *   "contacts": [ ... contactos con isCustomer = true ... ],
     *   "companies": [ ... companies con isCustomer = true ... ]
     * }
     */
    @GetMapping("/customers")
    public ResponseEntity<ContactsCompaniesResponse> getCustomers() {
        ContactsCompaniesResponse response = new ContactsCompaniesResponse(
                contactsService.findCustomers(),
                companyService.findCustomers()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * GET /crm/clients
     * Respuesta:
     * {
     *   "contacts": [ ... contactos con isClient = true ... ],
     *   "companies": [ ... companies con isClient = true ... ]
     * }
     */
    @GetMapping("/clients")
    public ResponseEntity<ContactsCompaniesResponse> getClients() {
        ContactsCompaniesResponse response = new ContactsCompaniesResponse(
                contactsService.findClients(),
                companyService.findClients()
        );
        return ResponseEntity.ok(response);
    }
}

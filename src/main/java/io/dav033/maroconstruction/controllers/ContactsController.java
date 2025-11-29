package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.requests.GetContactByNameRequest;
import io.dav033.maroconstruction.dto.responses.ContactValidationResponse;
import io.dav033.maroconstruction.services.ContactsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@CrossOrigin
public class ContactsController {

    private final ContactsService contactsService;

    public ContactsController(ContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Contacts>> getContacts() {
        List<Contacts> contacts = contactsService.findAll();
        return ResponseEntity.ok(contacts);
    }

    @GetMapping
    public ResponseEntity<Contacts> getContactByName(@RequestBody GetContactByNameRequest request) {
        System.out.println("Fetching contact by name: " + request.getName());
        Contacts contact = contactsService.getContactByName(request.getName());
        return ResponseEntity.ok(contact);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Contacts> getContactById(@PathVariable Long id) {
        Contacts contact = contactsService.getContactById(id);
        return ResponseEntity.ok(contact);
    }
    @PostMapping
    public ResponseEntity<Contacts> createContact(@RequestBody Contacts contact) {
        System.out.println("[CREATE] Received contact: name=" + contact.getName() + ", isCustomer=" + contact.isCustomer());
        Contacts created = contactsService.create(contact);
        System.out.println("[CREATE] Saved contact: id=" + created.getId() + ", isCustomer=" + created.isCustomer());
        return ResponseEntity.ok(created);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Contacts> updateContact(@PathVariable Long id, @RequestBody Contacts contact) {
        System.out.println("[UPDATE] Received contact for id " + id + ": name=" + contact.getName() + ", isCustomer=" + contact.isCustomer());
        if (contact.getId() != null && !contact.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        contact.setId(null);
        Contacts updated = contactsService.update(id, contact);
        System.out.println("[UPDATE] Saved contact: id=" + updated.getId() + ", isCustomer=" + updated.isCustomer());
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactsService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/validate")
    public ResponseEntity<ContactValidationResponse> validateContact(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long excludeId) {
        ContactValidationResponse res = contactsService.validateAvailability(name, email, phone, excludeId);
        return ResponseEntity.ok(res);
    }
}

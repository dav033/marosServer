package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.requests.GetContactByNameRequest;
import io.dav033.maroconstruction.dto.responses.ContactValidationResponse;
import io.dav033.maroconstruction.services.ContactsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@AllArgsConstructor
@CrossOrigin
public class ContactsController {

    private ContactsService contactsService;

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

    // GET /contacts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Contacts> getContactById(@PathVariable Long id) {
        Contacts contact = contactsService.getContactById(id);
        return ResponseEntity.ok(contact);
    }

    // POST /contacts
    @PostMapping
    public ResponseEntity<Contacts> createContact(@RequestBody Contacts contact) {
        Contacts created = contactsService.create(contact);
        return ResponseEntity.ok(created);
    }

    // PUT /contacts/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Contacts> updateContact(@PathVariable Long id, @RequestBody Contacts contact) {
        // Validación defensiva: si el body trae id y no coincide con el de la ruta, responder 400
        if (contact.getId() != null && !contact.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        // Normalizar el DTO para evitar que el mapper toque el PK
        contact.setId(null);
        Contacts updated = contactsService.update(id, contact);
        return ResponseEntity.ok(updated);
    }

    // DELETE /contacts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /contacts/validate?name=...&email=...&phone=...&excludeId=...
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

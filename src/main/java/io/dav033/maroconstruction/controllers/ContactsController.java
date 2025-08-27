package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.requests.GetContactByNameRequest;
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
        Contacts updated = contactsService.update(id, contact);
        return ResponseEntity.ok(updated);
    }

    // DELETE /contacts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

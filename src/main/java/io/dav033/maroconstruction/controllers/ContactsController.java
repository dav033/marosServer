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
        try {
            List<Contacts> contacts = contactsService.findAll();
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<Contacts> getContactByName(@RequestBody GetContactByNameRequest request) {

        System.out.println("Fetching contact by name: " + request.getName());
        Contacts contact = contactsService.getContactByName(request.getName());
        return ResponseEntity.ok(contact);

    }
}

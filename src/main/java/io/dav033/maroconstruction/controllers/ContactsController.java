package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.services.ContactsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@AllArgsConstructor
@CrossOrigin
public class ContactsController {

    private ContactsService contactsService;

    @GetMapping
    public ResponseEntity<List<Contacts>> getContacts() {
        try {
            List<Contacts> contacts = contactsService.findAll();
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

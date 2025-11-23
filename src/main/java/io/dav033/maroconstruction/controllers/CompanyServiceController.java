package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.CompanyService;
import io.dav033.maroconstruction.services.CompanyServiceCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company-services")
@CrossOrigin
public class CompanyServiceController {

    private final CompanyServiceCatalogService service;

    public CompanyServiceController(CompanyServiceCatalogService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public ResponseEntity<List<CompanyService>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyService> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CompanyService> create(@RequestBody CompanyService dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyService> update(@PathVariable Long id, @RequestBody CompanyService dto) {
        if (dto.getId() != null && !dto.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        dto.setId(null);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

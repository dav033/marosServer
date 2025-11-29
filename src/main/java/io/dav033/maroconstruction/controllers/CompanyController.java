package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.Company;
import io.dav033.maroconstruction.services.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
@CrossOrigin
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Company>> getCompanies() {
        return ResponseEntity.ok(companyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        return ResponseEntity.ok(companyService.create(company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        if (company.getId() != null && !company.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        company.setId(null);
        return ResponseEntity.ok(companyService.update(id, company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/contacts")
    public ResponseEntity<Void> assignContactsToCompany(
            @PathVariable Long id,
            @RequestBody List<Long> contactIds) {
        companyService.assignContactsToCompany(id, contactIds);
        return ResponseEntity.ok().build();
    }
}

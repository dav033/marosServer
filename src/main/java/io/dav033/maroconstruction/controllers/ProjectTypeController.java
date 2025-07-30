package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.ProjectType;
import io.dav033.maroconstruction.services.ProjectTypeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/project-type")
@AllArgsConstructor
@CrossOrigin
public class ProjectTypeController {

    private ProjectTypeService projectTypeService;

    @GetMapping("/all")
    public ResponseEntity<List<ProjectType>> getAllProjectTypes() {
        try {
            List<ProjectType> projectTypes = projectTypeService.findAll();
            return ResponseEntity.ok(projectTypes);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

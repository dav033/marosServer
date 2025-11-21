package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.ProjectType;
import io.dav033.maroconstruction.services.ProjectTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/project-type")
@CrossOrigin
public class ProjectTypeController {

    private final ProjectTypeService projectTypeService;

    public ProjectTypeController(ProjectTypeService projectTypeService) {
        this.projectTypeService = projectTypeService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProjectType>> getAllProjectTypes() {
        List<ProjectType> projectTypes = projectTypeService.findAll();
        return ResponseEntity.ok(projectTypes);
    }
}

@RestController
@RequestMapping("/project-types")
@CrossOrigin
class ProjectTypesController {

    private final ProjectTypeService projectTypeService;

    public ProjectTypesController(ProjectTypeService projectTypeService) {
        this.projectTypeService = projectTypeService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProjectType>> getAllProjectTypesPlural() {
        List<ProjectType> projectTypes = projectTypeService.findAll();
        return ResponseEntity.ok(projectTypes);
    }
}

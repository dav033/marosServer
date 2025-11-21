package io.dav033.maroconstruction.controllers;

import io.dav033.maroconstruction.dto.Projects;
import io.dav033.maroconstruction.services.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/projects", produces = "application/json")
public class ProjectsController {

    private final ProjectService projectService;

    public ProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }

        @GetMapping("/with-leads")
    public List<Projects> getAllWithLeads() {
        return projectService.getProjectsWithLead();
    }
}

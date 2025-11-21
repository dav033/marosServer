package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.enums.ProjectStatus;
public class GetProjectsByStatusRequest {
    private ProjectStatus status;

    public GetProjectsByStatusRequest() {}
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
}
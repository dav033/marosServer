package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.enums.LeadType;
public class GetLeadsByTypeRequest {
    private LeadType type;

    public GetLeadsByTypeRequest() {}
    public LeadType getType() { return type; }
    public void setType(LeadType type) { this.type = type; }
}

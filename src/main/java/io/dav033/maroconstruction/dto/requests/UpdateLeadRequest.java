package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.dto.Leads;
public class UpdateLeadRequest {
    private Leads lead;

    public UpdateLeadRequest() {}
    public Leads getLead() { return lead; }
    public void setLead(Leads lead) { this.lead = lead; }
}

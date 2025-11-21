package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.dto.Leads;
public class CreateLeadByExistingContactRequest {
    private Leads lead;
    private Long contactId;

    public CreateLeadByExistingContactRequest() {}
    public Leads getLead() { return lead; }
    public void setLead(Leads lead) { this.lead = lead; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
}

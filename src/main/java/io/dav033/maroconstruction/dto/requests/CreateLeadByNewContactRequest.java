package io.dav033.maroconstruction.dto.requests;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.dto.Leads;
public class CreateLeadByNewContactRequest {
    private Leads lead;
    private Contacts contact;

    public CreateLeadByNewContactRequest() {}
    public Leads getLead() { return lead; }
    public void setLead(Leads lead) { this.lead = lead; }
    public Contacts getContact() { return contact; }
    public void setContact(Contacts contact) { this.contact = contact; }
}

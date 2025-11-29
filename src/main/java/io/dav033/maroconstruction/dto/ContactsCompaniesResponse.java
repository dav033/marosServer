package io.dav033.maroconstruction.dto;

import java.util.List;

public class ContactsCompaniesResponse {

    private List<Contacts> contacts;
    private List<Company> companies;

    public ContactsCompaniesResponse() {
    }

    public ContactsCompaniesResponse(List<Contacts> contacts, List<Company> companies) {
        this.contacts = contacts;
        this.companies = companies;
    }

    public List<Contacts> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contacts> contacts) {
        this.contacts = contacts;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }
}

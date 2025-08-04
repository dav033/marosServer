package io.dav033.maroconstruction.mappers;

import org.springframework.stereotype.Component;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.services.ContactsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContactInfoFormatter {

    private final ContactsService contactsService;

    public String formatFor(Long contactId) {
        // 🧪 TEMPORAL: Forzar contacto ID 5 para pruebas
        Long forcedContactId = 5L;
        System.out.println("🧪 ContactInfoFormatter: Original contactId=" + contactId + ", FORCED to contactId=" + forcedContactId);
        
        if (forcedContactId == null) {
            return "";
        }
        try {
            Contacts c = contactsService.getContactById(forcedContactId);
            if (c == null) {
                return "\n**Contact ID:** " + forcedContactId + "\n";
            }
            StringBuilder sb = new StringBuilder("\n**Contact Information:**\n");
            appendIf(sb, "Company",  c.getCompanyName());
            appendIf(sb, "Contact", c.getName());
            appendIf(sb, "Email",    c.getEmail());
            appendIf(sb, "Phone", c.getPhone());
            String result = sb.toString();
            return result.equals("\n**Contact Information:**\n")
                ? ""
                : result;
        } catch (Exception e) {
            return "\n**Contact ID:** " + forcedContactId + "\n";
        }
    }

    private void appendIf(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(String.format("- **%s:** %s\n", label, value));
        }
    }
}

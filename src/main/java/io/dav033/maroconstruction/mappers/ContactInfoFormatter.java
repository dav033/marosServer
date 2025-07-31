package io.dav033.maroconstruction.mappers;

import org.springframework.stereotype.Component;

import io.dav033.maroconstruction.dto.Contacts;
import io.dav033.maroconstruction.services.ContactsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContactInfoFormatter {

    private final ContactsService contactsService;

    /**
     * Devuelve una sección de texto con los datos del contacto,
     * o una línea con el ID si no existe o falla la consulta.
     */
    public String formatFor(Long contactId) {
        if (contactId == null) {
            return "";
        }
        try {
            Contacts c = contactsService.getContactById(contactId);
            if (c == null) {
                return "\n**Contacto ID:** " + contactId + "\n";
            }
            StringBuilder sb = new StringBuilder("\n**Información del Contacto:**\n");
            appendIf(sb, "Empresa",  c.getCompanyName());
            appendIf(sb, "Contacto", c.getName());
            appendIf(sb, "Email",    c.getEmail());
            appendIf(sb, "Teléfono", c.getPhone());
            String result = sb.toString();
            // Si no se añadió ninguna línea, no devolvemos la cabecera
            return result.equals("\n**Información del Contacto:**\n")
                ? ""
                : result;
        } catch (Exception e) {
            // En caso de error al obtener el contacto
            return "\n**Contacto ID:** " + contactId + "\n";
        }
    }

    private void appendIf(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(String.format("- **%s:** %s\n", label, value));
        }
    }
}

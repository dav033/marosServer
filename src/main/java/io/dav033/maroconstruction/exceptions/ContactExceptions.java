package io.dav033.maroconstruction.exceptions;

public class ContactExceptions {

    public static class ContactNotFoundException extends ResourceNotFoundException {
        public ContactNotFoundException(Long contactId) {
            super("Contacto no encontrado con ID: " + contactId, contactId);
        }

        public ContactNotFoundException(String contactName) {
            super("Contacto no encontrado con nombre: " + contactName, contactName);
        }
    }

    public static class DuplicateContactException extends BusinessException {
        public DuplicateContactException(String contactName) {
            super("Ya existe un contacto con el nombre: " + contactName, contactName);
        }
    }

    public static class InvalidContactDataException extends ValidationException {
        public InvalidContactDataException(String field, String value) {
            super("Datos de contacto inválidos - " + field + ": " + value, field, value);
        }
    }
}

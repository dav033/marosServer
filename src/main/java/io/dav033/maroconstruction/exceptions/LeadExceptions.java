package io.dav033.maroconstruction.exceptions;

/**
 * Excepciones específicas para la gestión de Leads
 */
public class LeadExceptions {

    public static class LeadNotFoundException extends ResourceNotFoundException {
        public LeadNotFoundException(Long leadId) {
            super("Lead no encontrado con ID: " + leadId, leadId);
        }
    }

    public static class DuplicateLeadNumberException extends BusinessException {
        public DuplicateLeadNumberException(String leadNumber) {
            super("Ya existe un lead con el número: " + leadNumber, leadNumber);
        }
    }

    public static class InvalidLeadStatusException extends ValidationException {
        public InvalidLeadStatusException(String status) {
            super("Estado de lead inválido: " + status, status);
        }
    }

    public static class LeadCreationException extends DatabaseException {
        public LeadCreationException(String message, Throwable cause) {
            super("Error al crear el lead: " + message, cause);
        }
    }
}

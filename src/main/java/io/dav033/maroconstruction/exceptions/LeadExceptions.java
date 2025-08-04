package io.dav033.maroconstruction.exceptions;

public class LeadExceptions {

    public static class LeadNotFoundException extends ResourceNotFoundException {
        public LeadNotFoundException(Long leadId) {
            super("Lead not found with ID: " + leadId, leadId);
        }
    }

    public static class DuplicateLeadNumberException extends BusinessException {
        public DuplicateLeadNumberException(String leadNumber) {
            super("Lead already exists with number: " + leadNumber, leadNumber);
        }
    }

    public static class InvalidLeadStatusException extends ValidationException {
        public InvalidLeadStatusException(String status) {
            super("Invalid lead status: " + status, status);
        }
    }

    public static class LeadCreationException extends DatabaseException {
        public LeadCreationException(String message, Throwable cause) {
            super("Error creating lead: " + message, cause);
        }
    }

    public static class LeadUpdateException extends DatabaseException {
        public LeadUpdateException(String message, Throwable cause) {
            super("Error updating lead: " + message, cause);
        }
    }
}

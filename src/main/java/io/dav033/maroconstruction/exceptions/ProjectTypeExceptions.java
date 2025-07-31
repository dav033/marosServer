package io.dav033.maroconstruction.exceptions;

/**
 * Excepciones específicas para ProjectType
 */
public class ProjectTypeExceptions {

    public static class ProjectTypeNotFoundException extends ResourceNotFoundException {
        public ProjectTypeNotFoundException(Long projectTypeId) {
            super("Tipo de proyecto no encontrado con ID: " + projectTypeId, projectTypeId);
        }
    }

    public static class InvalidProjectTypeException extends ValidationException {
        public InvalidProjectTypeException(String projectType) {
            super("Tipo de proyecto inválido: " + projectType, projectType);
        }
    }
}

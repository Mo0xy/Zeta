package it.aruba.pec.zeta.common.exception;

public class ResourceNotFoundException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s con id %d non trovato", resourceName, id), DEFAULT_ERROR_CODE);
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s '%s' non trovato", resourceName, identifier), DEFAULT_ERROR_CODE);
    }
}
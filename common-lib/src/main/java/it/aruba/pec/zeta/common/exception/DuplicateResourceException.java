package it.aruba.pec.zeta.common.exception;

public class DuplicateResourceException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "DUPLICATE_RESOURCE";

    public DuplicateResourceException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public DuplicateResourceException(String resourceName, String field, String value) {
        super(String.format("%s con %s '%s' già esistente", resourceName, field, value), DEFAULT_ERROR_CODE);
    }
}
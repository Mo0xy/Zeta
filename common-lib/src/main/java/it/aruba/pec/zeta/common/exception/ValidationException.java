package it.aruba.pec.zeta.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ValidationException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "VALIDATION_ERROR";

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, DEFAULT_ERROR_CODE);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, DEFAULT_ERROR_CODE);
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }

    public ValidationException addFieldError(String field, String error) {
        this.fieldErrors.put(field, error);
        return this;
    }
}
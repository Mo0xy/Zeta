package it.aruba.pec.zeta.common.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "EXTERNAL_SERVICE_ERROR";

    private final String serviceName;
    private final Integer httpStatus;

    public ExternalServiceException(String serviceName, String message) {
        super(message, DEFAULT_ERROR_CODE);
        this.serviceName = serviceName;
        this.httpStatus = null;
    }

    public ExternalServiceException(String serviceName, String message, Integer httpStatus) {
        super(message, DEFAULT_ERROR_CODE);
        this.serviceName = serviceName;
        this.httpStatus = httpStatus;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
        this.serviceName = serviceName;
        this.httpStatus = null;
    }
}
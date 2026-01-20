package it.aruba.pec.zeta.common.exception;

public class ServiceException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "SERVICE_ERROR";

    public ServiceException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public ServiceException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }

    public ServiceException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
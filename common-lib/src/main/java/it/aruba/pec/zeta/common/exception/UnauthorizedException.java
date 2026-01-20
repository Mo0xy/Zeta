package it.aruba.pec.zeta.common.exception;

public class UnauthorizedException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "UNAUTHORIZED";

    public UnauthorizedException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public UnauthorizedException() {
        super("Accesso non autorizzato", DEFAULT_ERROR_CODE);
    }
}
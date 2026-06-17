package callaou.userregistration.exceptions;

import callaou.userregistration.exceptions.enumerations.ErrorCode;

/**
 * Exception thrown when a request is wrongly formatted.
 */
public class BadRequestException extends BusinessException {
    /**
     * The message format for the exception.
     */
    private static final String MSG_BAD_REQUEST = "Request is wrongly formatted: %s";

    /**
     * Constructs an instance of the exception with the specified reason.
     *
     * @param reason the reason why the request is considered bad
     */
    public BadRequestException(String reason) {
        super(ErrorCode.BAD_REQUEST, String.format(MSG_BAD_REQUEST, reason));
    }

}

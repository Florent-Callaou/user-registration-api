package callaou.userregistration.exceptions;

import callaou.userregistration.exceptions.enumerations.ErrorCode;

/**
 * Abstract base class for business exceptions.
 */
public abstract class BusinessException extends RuntimeException {
    /**
     * The error code associated with the exception.
     */
    private final ErrorCode errorCode;

    /**
     * Constructs a new business exception with the specified error code and
     * message.
     *
     * @param errorCode the error code associated with the exception
     * @param message   the detail message
     */
    protected BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new business exception with the specified error code, message,
     * and cause.
     *
     * @param errorCode the error code associated with the exception
     * @param message   the detail message
     * @param cause     the cause of the exception
     */
    protected BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code associated with the exception.
     *
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

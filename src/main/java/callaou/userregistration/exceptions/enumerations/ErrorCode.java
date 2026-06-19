package callaou.userregistration.exceptions.enumerations;

import org.springframework.http.HttpStatus;

/**
 * Enum representing different error codes and their corresponding HTTP status.
 */
public enum ErrorCode {
    BAD_REQUEST("400", HttpStatus.BAD_REQUEST),
    ALREADY_EXISTS("409", HttpStatus.CONFLICT),
    OBJECT_NOT_FOUND("404", HttpStatus.NOT_FOUND),
    USER_NOT_ELIGIBLE("422", HttpStatus.valueOf(422)),
    INTERNAL_SERVER_ERROR("500", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * The error code as a string.
     */
    private final String code;

    /**
     * The corresponding HTTP status for the error code.
     */
    private final HttpStatus httpStatus;

    /**
     * Constructs an error code with the specified code and HTTP status.
     *
     * @param code       the error code as a string
     * @param httpStatus the corresponding HTTP status
     */
    ErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    /**
     * Returns the error code as a string.
     *
     * @return the error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the corresponding HTTP status for the error code.
     *
     * @return the HTTP status
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

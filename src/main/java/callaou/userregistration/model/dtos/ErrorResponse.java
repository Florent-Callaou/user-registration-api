package callaou.userregistration.model.dtos;

import java.time.Instant;
import java.util.List;

import callaou.userregistration.exceptions.enumerations.ErrorCode;

/**
 * A record representing an error response.
 */
public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors,
        Instant timestamp) {

    /**
     * Creates an error response with the specified code and message.
     *
     * @param code    the error code
     * @param message the error message
     * @return the error response
     */
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null, Instant.now());
    }

    /**
     * Creates an error response for validation failures.
     *
     * @param fieldErrors the list of field errors
     * @return the error response
     */
    public static ErrorResponse ofValidation(List<FieldError> fieldErrors) {
        return new ErrorResponse(ErrorCode.BAD_REQUEST.getCode(), "Validation failed", fieldErrors, Instant.now());
    }

}

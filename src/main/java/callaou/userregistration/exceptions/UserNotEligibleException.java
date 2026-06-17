package callaou.userregistration.exceptions;

import callaou.userregistration.exceptions.enumerations.ErrorCode;

/**
 * Exception thrown when a user is not eligible for registration.
 */
public class UserNotEligibleException extends BusinessException {
    /**
     * The message format for the exception.
     */
    private static final String MSG_USER_NOT_ELIGIBLE = "User is not eligible: %s";

    /**
     * Constructs an instance of the exception with the specified reason.
     *
     * @param reason the reason why the user is not eligible
     */
    public UserNotEligibleException(String reason) {
        super(ErrorCode.USER_NOT_ELIGIBLE, String.format(MSG_USER_NOT_ELIGIBLE, reason));
    }
}

package callaou.userregistration.exceptions;

import callaou.userregistration.exceptions.enumerations.ErrorCode;

/**
 * Exception thrown when an object of a certain type already exists.
 */
public class AlreadyExistsException extends BusinessException {
    /**
     * The message format for the exception.
     */
    private static final String MSG_OBJECT_ALREADY_EXISTS = "Object %s with %s '%s' already exists";

    /**
     * Constructs an instance of the exception with the specified entity name, field
     * name, and field value.
     *
     * @param entityName the name of the entity type
     * @param fieldName  the name of the field
     * @param fieldValue the value of the field
     */
    public AlreadyExistsException(Class<?> entityName, String fieldName, String fieldValue) {
        super(ErrorCode.ALREADY_EXISTS,
                String.format(MSG_OBJECT_ALREADY_EXISTS, entityName.getSimpleName(), fieldName, fieldValue));
    }

}

package callaou.userregistration.exceptions;

import callaou.userregistration.exceptions.enumerations.ErrorCode;

/**
 * Exception thrown when an object of a certain type is not found
 */
public class ObjectNotFoundException extends BusinessException {
    /**
     * The message format for the exception.
     */
    private static final String MSG_OBJECT_NOT_FOUND = "Failed to find object of type %s related to %s: %s";

    /**
     * Constructs an instance of the exception with the specified entity name, field
     * name, and field value
     *
     * @param entityName the name of the entity type
     * @param fieldName  the name of the field
     * @param fieldValue the value of the field
     */
    public ObjectNotFoundException(Class<?> entityName, String fieldName, String fieldValue) {
        super(ErrorCode.OBJECT_NOT_FOUND,
                String.format(MSG_OBJECT_NOT_FOUND, entityName.getSimpleName(), fieldName, fieldValue));
    }

}

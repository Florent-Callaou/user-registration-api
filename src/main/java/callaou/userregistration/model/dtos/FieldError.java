package callaou.userregistration.model.dtos;

/**
 * A record representing a field error in the request.
 */
public record FieldError(
        String field,
        String message,
        Object rejectedValue) {

}

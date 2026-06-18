package callaou.userregistration.exceptions.handlers;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import callaou.userregistration.exceptions.AlreadyExistsException;
import callaou.userregistration.exceptions.UserNotEligibleException;
import callaou.userregistration.exceptions.enumerations.ErrorCode;
import callaou.userregistration.model.dtos.ErrorResponse;
import callaou.userregistration.model.dtos.FieldError;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handles AlreadyExistsException and returns an appropriate error response.
         * 
         * @param alreadyExistsException the exception to handle
         * @return ResponseEntity containing the error response
         */
        @ExceptionHandler(AlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleAlreadyExistsException(
                        AlreadyExistsException alreadyExistsException) {
                return ResponseEntity
                                .status(alreadyExistsException.getErrorCode().getHttpStatus())
                                .body(ErrorResponse.of(alreadyExistsException.getErrorCode(),
                                                alreadyExistsException.getMessage()));
        }

        /**
         * Handles UserNotEligibleException and returns an appropriate error response.
         * 
         * @param userNotEligibleException the exception to handle
         * @return ResponseEntity containing the error response
         */
        @ExceptionHandler(UserNotEligibleException.class)
        public ResponseEntity<ErrorResponse> handleUserNotEligibleException(
                        UserNotEligibleException userNotEligibleException) {
                return ResponseEntity
                                .status(userNotEligibleException.getErrorCode().getHttpStatus())
                                .body(ErrorResponse.of(userNotEligibleException.getErrorCode(),
                                                userNotEligibleException.getMessage()));
        }

        /**
         * Handles MethodArgumentNotValidException (for bad request validation errors)
         * and returns an appropriate error response.
         * 
         * @param methodArgumentNotValidException the exception to handle
         * @return ResponseEntity containing the error response
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(
                        MethodArgumentNotValidException methodArgumentNotValidException) {

                List<FieldError> fieldErrors = methodArgumentNotValidException.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(fieldError -> new FieldError(
                                                fieldError.getField(),
                                                fieldError.getDefaultMessage(),
                                                fieldError.getRejectedValue()))
                                .toList();

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.ofValidation(fieldErrors));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleInvalidEnum(HttpMessageNotReadableException ex) {
                Throwable cause = ex.getMostSpecificCause();
                if (cause instanceof InvalidFormatException invalidFormat
                                && invalidFormat.getTargetType().isEnum()) {

                        String fieldName = invalidFormat.getTargetType().getSimpleName();
                        String invalidValue = invalidFormat.getValue().toString();
                        String allowedValues = Arrays
                                        .toString(invalidFormat.getTargetType().getEnumConstants());

                        String message = String.format(
                                        "Invalid value '%s' for field '%s'. Allowed values: %s",
                                        invalidValue, fieldName, allowedValues);

                        return ResponseEntity
                                        .status(HttpStatus.BAD_REQUEST)
                                        .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, message));
                }

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, "Malformed JSON input"));
        }

        /**
         * Handles unexpected exceptions and returns an appropriate error response.
         * 
         * @param exception the exception to handle
         * @return ResponseEntity containing the error response
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR,
                                                "An unexpected error occurred: " + exception.getMessage()));
        }
}

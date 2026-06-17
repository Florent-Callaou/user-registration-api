package callaou.userregistration.model.dtos;

import java.time.LocalDate;

import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.model.enumerations.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests.
 */
public record UserRequest(
        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,

        @NotNull(message = "Birthdate is required") @Past(message = "Birthdate must be in the past") LocalDate birthdate,

        @NotNull(message = "Country of residence is required") Country countryOfResidence,

        @Size(max = 20, message = "Phone number must be at most 20 characters") @Pattern(regexp = "^\\+?\\d*$", message = "Phone number must contain only digits and an optional leading '+'") String phoneNumber,

        Gender gender) {
}

package callaou.userregistration.model.dtos;

import java.time.LocalDate;

import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.model.enumerations.Gender;

/**
 * Data Transfer Object for user response.
 */
public record UserResponse(
        Long id,
        String username,
        LocalDate birthdate,
        Country countryOfResidence,
        String phoneNumber,
        Gender gender) {
}

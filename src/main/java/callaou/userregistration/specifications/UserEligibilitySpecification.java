package callaou.userregistration.specifications;

import java.time.LocalDate;

import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.model.enumerations.CriteriaType;

public interface UserEligibilitySpecification {

    /**
     * Checks if the user satisfies the eligibility criteria.
     *
     * @param user the user to check
     * @return true if the user is eligible, false otherwise
     */
    boolean isSatisfiedBy(User user);

    /**
     * Creates a specification for checking the country requirement.
     *
     * @param country the required country
     * @return the specification
     */
    static UserEligibilitySpecification countryRequirement(Country country) {
        return user -> country.equals(user.getCountryOfResidence());
    }

    /**
     * Creates a specification for checking the minimum age requirement.
     *
     * @param minimumAge the minimum age
     * @return the specification
     */
    static UserEligibilitySpecification minimumAgeRequirement(int minimumAge) {
        return user -> {
            LocalDate birthdate = user.getBirthdate();
            LocalDate minBirthdate = LocalDate.now().minusYears(minimumAge);
            return !birthdate.isAfter(minBirthdate);
        };
    }

    /**
     * Creates a specification for checking the AND condition of two eligibility
     * specifications.
     *
     * @param userEligibilitySpecification1 the first specification
     * @param userEligibilitySpecification2 the second specification
     * @return the specification
     */
    static UserEligibilitySpecification and(UserEligibilitySpecification userEligibilitySpecification1,
            UserEligibilitySpecification userEligibilitySpecification2) {
        return user -> userEligibilitySpecification1.isSatisfiedBy(user)
                && userEligibilitySpecification2.isSatisfiedBy(user);
    }

    /**
     * Creates a specification for checking the OR condition of two eligibility
     * specifications.
     *
     * @param userEligibilitySpecification1 the first specification
     * @param userEligibilitySpecification2 the second specification
     * @return the specification
     */
    static UserEligibilitySpecification or(UserEligibilitySpecification userEligibilitySpecification1,
            UserEligibilitySpecification userEligibilitySpecification2) {
        return user -> userEligibilitySpecification1.isSatisfiedBy(user)
                || userEligibilitySpecification2.isSatisfiedBy(user);
    }

    /**
     * Creates a specification based on the criteria type and value.
     *
     * @param criteriaType the type of criteria
     * @param value        the value for the criteria
     * @return the specification
     */
    static UserEligibilitySpecification fromCriteriaType(CriteriaType criteriaType, Object value) {
        return switch (criteriaType) {
            case COUNTRY -> countryRequirement((Country) value);
            case AGE_MIN -> minimumAgeRequirement((Integer) value);
            case CUSTOM -> user -> true;
            default -> throw new IllegalArgumentException("Unsupported criteria type: " + criteriaType);
        };
    }
}

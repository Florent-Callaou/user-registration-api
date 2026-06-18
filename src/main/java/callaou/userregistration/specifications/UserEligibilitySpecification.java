package callaou.userregistration.specifications;

import java.time.LocalDate;

import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.CriterionType;
import callaou.userregistration.model.enumerations.Operator;
import callaou.userregistration.specifications.utils.CriteriaValueParser;

public interface UserEligibilitySpecification {

    /**
     * Checks if the user satisfies the eligibility criteria.
     *
     * @param user the user to check
     * @return true if the user is eligible, false otherwise
     */
    boolean isSatisfiedBy(User user);

    /**
     * Creates a specification based on the criteria type and value.
     *
     * @param criterionType the type of criteria
     * @param value         the value for the criteria
     * @return the specification
     */
    static UserEligibilitySpecification fromCriteria(CriterionType criterionType,
            Operator operator,
            String value) {
        return switch (criterionType) {
            case COUNTRY -> countrySpecification(operator, value);
            case AGE -> ageSpecification(operator, value);
            case CUSTOM -> user -> true;
        };
    }

    /**
     * Evaluate country specification
     * 
     * @param operator the operator for the evaluation
     * @param value    the country value
     * @return the UserEligibilitySpecification
     */
    private static UserEligibilitySpecification countrySpecification(Operator operator, String value) {
        return switch (operator) {
            case EQ -> user -> CriteriaValueParser.parseCountry(value)
                    .equals(user.getCountryOfResidence());
            case NEQ -> user -> !CriteriaValueParser.parseCountry(value)
                    .equals(user.getCountryOfResidence());
            case IN -> user -> CriteriaValueParser.parseCountryList(value)
                    .contains(user.getCountryOfResidence());
            case NOT_IN -> user -> !CriteriaValueParser.parseCountryList(value)
                    .contains(user.getCountryOfResidence());
            default -> throw new IllegalArgumentException(
                    "Operator " + operator + " is not supported for COUNTRY criteria");
        };
    }

    /**
     * Evaluate age specification
     * 
     * @param operator the operator for the evaluation
     * @param value    the age value
     * @return the UserEligibilitySpecification
     */
    private static UserEligibilitySpecification ageSpecification(Operator operator, String value) {
        return switch (operator) {
            case GTE -> user -> {
                int minAge = CriteriaValueParser.parseAge(value);
                LocalDate minBirthdate = LocalDate.now().minusYears(minAge);
                return !user.getBirthdate().isAfter(minBirthdate);
            };
            case LTE -> user -> {
                int maxAge = CriteriaValueParser.parseAge(value);
                LocalDate maxBirthdate = LocalDate.now().minusYears(maxAge);
                return !user.getBirthdate().isBefore(maxBirthdate);
            };
            case GT -> user -> {
                int minAge = CriteriaValueParser.parseAge(value);
                LocalDate minBirthdate = LocalDate.now().minusYears(minAge);
                return user.getBirthdate().isBefore(minBirthdate);
            };
            case LT -> user -> {
                int maxAge = CriteriaValueParser.parseAge(value);
                LocalDate maxBirthdate = LocalDate.now().minusYears(maxAge);
                return user.getBirthdate().isAfter(maxBirthdate);
            };
            case BETWEEN -> user -> {
                int[] range = CriteriaValueParser.parseAgeRange(value);
                LocalDate birthdate = user.getBirthdate();
                LocalDate today = LocalDate.now();
                return !birthdate.isAfter(today.minusYears(range[0]))
                        && !birthdate.isBefore(today.minusYears(range[1]));
            };
            default -> throw new IllegalArgumentException(
                    "Operator " + operator + " is not supported for AGE criteria");
        };
    }
}

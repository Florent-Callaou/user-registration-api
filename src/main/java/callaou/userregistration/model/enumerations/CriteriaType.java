package callaou.userregistration.model.enumerations;

/**
 * Eligibility Criteria Type Enum
 * Defines the types of criteria that can be used in eligibility rules
 */
public enum CriteriaType {
    COUNTRY("Country criteria - validates country of residence"),
    AGE_MIN("Minimum age criteria - validates minimum age requirement"),
    AGE_MAX("Maximum age criteria - validates maximum age limit"),
    CUSTOM("Custom criteria - extensible for future requirements");

    private final String description;

    CriteriaType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package callaou.userregistration.model.enumerations;

/**
 * Eligibility Criteria Type Enum
 * Defines the types of criteria that can be used in eligibility rules
 */
public enum CriteriaType {
    COUNTRY("Country criteria - validates country of residence"),
    AGE_MIN("Minimum age criteria - validates minimum age requirement"),
    CUSTOM("Custom criteria - extensible for future requirements");

    /**
     * The description of the criteria type.
     */
    private final String description;

    /**
     * Constructor for the CriteriaType enum.
     *
     * @param description the description of the criteria type
     */
    CriteriaType(String description) {
        this.description = description;
    }

    /**
     * Gets the description of the criteria type.
     *
     * @return the description of the criteria type
     */
    public String getDescription() {
        return description;
    }
}

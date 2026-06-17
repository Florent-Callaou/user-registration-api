package callaou.userregistration.model.enumerations;

/**
 * Eligibility Criteria Type Enum
 * Defines the types of criteria that can be used in eligibility rules
 */
public enum CriteriaType {
    COUNTRY("Country criteria - validates country of residence", "Country"),
    AGE_MIN("Minimum age criteria - validates minimum age requirement", "Age"),
    CUSTOM("Custom criteria - extensible for future requirements", "Custom");

    /**
     * The description of the criteria type.
     */
    private final String description;

    /**
     * The label of the criteria type.
     */
    private final String label;

    /**
     * Constructor for the CriteriaType enum.
     *
     * @param description the description of the criteria type
     * @param label       the label of the criteria type
     */
    CriteriaType(String description, String label) {
        this.description = description;
        this.label = label;
    }

    /**
     * Gets the description of the criteria type.
     *
     * @return the description of the criteria type
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the label of the criteria type.
     *
     * @return the label of the criteria type
     */
    public String getLabel() {
        return label;
    }
}

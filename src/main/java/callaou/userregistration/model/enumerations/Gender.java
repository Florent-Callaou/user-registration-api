package callaou.userregistration.model.enumerations;

/**
 * User Gender Enum
 * Optional field for user profile
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    NON_BINARY("Non-Binary"),
    PREFER_NOT_TO_SAY("Prefer Not to Say"),
    PREFER_TO_SELF_DESCRIBE("Prefer to Self Describe");

    /**
     * The label for the gender.
     */
    private final String label;

    /**
     * Constructor for the Gender enum.
     *
     * @param label the label for the gender
     */
    Gender(String label) {
        this.label = label;
    }

    /**
     * Gets the label for the gender.
     *
     * @return the label for the gender
     */
    public String getLabel() {
        return label;
    }
}

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

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

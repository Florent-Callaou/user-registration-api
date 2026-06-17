package callaou.userregistration.model.enumerations;

/**
 * Operator Enum
 * Defines operators used in eligibility criteria evaluation
 */
public enum Operator {
    EQ("Equal to"),
    NEQ("Not equal to"),
    GTE("Greater than or equal to"),
    LTE("Less than or equal to"),
    GT("Greater than"),
    LT("Less than"),
    BETWEEN("Between (inclusive)"),
    IN("In list"),
    NOT_IN("Not in list");

    private final String description;

    Operator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

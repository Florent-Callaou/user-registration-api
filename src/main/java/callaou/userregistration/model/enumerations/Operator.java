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

    /**
     * Description of the operator.
     */
    private final String description;

    /**
     * Constructor for the Operator enum.
     *
     * @param description the description of the operator
     */
    Operator(String description) {
        this.description = description;
    }

    /**
     * Gets the description of the operator.
     *
     * @return the description of the operator
     */
    public String getDescription() {
        return description;
    }
}

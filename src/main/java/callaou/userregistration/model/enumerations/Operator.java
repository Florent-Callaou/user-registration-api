package callaou.userregistration.model.enumerations;

/**
 * Operator Enum
 * Defines operators used in eligibility criteria evaluation
 */
public enum Operator {
    EQ("equal to"),
    NEQ("not equal to"),
    GTE("greater than or equal to"),
    LTE("less than or equal to"),
    GT("greater than"),
    LT("less than"),
    BETWEEN("between (inclusive)"),
    IN("in list"),
    NOT_IN("not in list");

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

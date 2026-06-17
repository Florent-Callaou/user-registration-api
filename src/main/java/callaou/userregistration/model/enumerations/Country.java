package callaou.userregistration.model.enumerations;

/**
 * ISO 3166-1 Alpha-2 Country Codes
 * Used for user registration eligibility and localization
 */
public enum Country {
    FR("France"),
    US("United States"),
    DE("Germany"),
    IT("Italy"),
    ES("Spain"),
    GB("United Kingdom"),
    SE("Sweden"),
    NO("Norway"),
    NL("Netherlands"),
    BE("Belgium"),
    CH("Switzerland"),
    CA("Canada"),
    MX("Mexico"),
    AU("Australia"),
    JP("Japan");

    private final String label;

    Country(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

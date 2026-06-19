package callaou.userregistration.model.enumerations;

import java.util.Arrays;

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

    /**
     * The label for the country.
     */
    private final String label;

    /**
     * Constructor for the Country enum.
     *
     * @param label the label for the country
     */
    Country(String label) {
        this.label = label;
    }

    /**
     * Gets the label for the country.
     *
     * @return the label for the country
     */
    public String getLabel() {
        return label;
    }

    public static Country fromCode(String code) {
        return Arrays.stream(values())
                .filter(country -> country.name().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown country code: " + code));
    }
}

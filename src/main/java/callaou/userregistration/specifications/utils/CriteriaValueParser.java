package callaou.userregistration.specifications.utils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import callaou.userregistration.model.enumerations.Country;

public final class CriteriaValueParser {

    private CriteriaValueParser() {
    }

    public static Country parseCountry(String raw) {
        return Country.fromCode(raw);
    }

    public static List<Country> parseCountryList(String raw) {
        // expects "FR,DE,ES"
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(Country::fromCode)
                .toList();
    }

    public static int parseAge(String raw) {
        return Integer.parseInt(raw.trim());
    }

    public static int[] parseAgeRange(String raw) {
        String[] parts = raw.split(",");
        return new int[] { Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()) };
    }

    public static LocalDate parseDate(String raw) {
        return LocalDate.parse(raw.trim());
    }
}
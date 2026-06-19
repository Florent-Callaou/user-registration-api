package callaou.userregistration.specifications;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import callaou.userregistration.exceptions.BadRequestException;
import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.model.enumerations.Gender;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GenericSpecification}
 */
@ExtendWith(MockitoExtension.class)
class GenericSpecificationTest {

    /**
     * Inject the user root
     */
    @Mock
    private Root<User> root;

    /**
     * Inject the query
     */
    @Mock
    private CriteriaQuery<?> query;

    /**
     * Inject the CriteriaBuilder
     */
    @Mock
    private CriteriaBuilder criteriaBuilder;

    /**
     * Inject the path
     */
    @Mock
    private Path<Object> path;

    /**
     * Inject the path as String
     */
    @Mock
    private Path<String> stringPath;

    /**
     * Inject the conjunction Predicate
     */
    @Mock
    private Predicate conjunctionPredicate;

    /**
     * Inject the field Predicate
     */
    @Mock
    private Predicate fieldPredicate;

    /**
     * Inject the combined Predicate
     */
    @Mock
    private Predicate combinedPredicate;

    /**
     * Set up before each test
     */
    @BeforeEach
    void setUp() {
        when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);
        lenient().when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
    }

    @Nested
    @DisplayName("String filters")
    class StringFilters {
        @Test
        @DisplayName("should build a case-insensitive LIKE predicate when path javaType is String")
        void shouldBuildLikePredicateForString() {
            Map<String, Object> filters = Map.of("username", "isa");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            stubPath(root, "username", stringPath, String.class);
            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%isa%")).thenReturn(fieldPredicate);

            Predicate result = specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).lower(stringPath);
            verify(criteriaBuilder).like(stringPath, "%isa%");
            assertThat(result).isEqualTo(combinedPredicate);
        }

        @Test
        @DisplayName("should lowercase the search value before building the LIKE pattern")
        void shouldLowercaseSearchValue() {
            Map<String, Object> filters = Map.of("username", "ISA");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            stubPath(root, "username", stringPath, String.class);
            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%isa%")).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).like(stringPath, "%isa%");
        }
    }

    @Nested
    @DisplayName("Enum filters")
    class EnumFilters {
        @Test
        @DisplayName("should build a LIKE predicate using .name() when value is already an enum instance")
        void shouldBuildLikePredicateForEnumInstance() {
            Map<String, Object> filters = Map.of("countryOfResidence", Country.FR);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            stubPath(root, "countryOfResidence", stringPath, Country.class);
            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%fr%")).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).like(stringPath, "%fr%");
        }

        @Test
        @DisplayName("should build a LIKE predicate using toString() when value is a raw String")
        void shouldBuildLikePredicateForEnumRawString() {
            Map<String, Object> filters = Map.of("countryOfResidence", "FR");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            stubPath(root, "countryOfResidence", stringPath, Country.class);
            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%fr%")).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).like(stringPath, "%fr%");
        }

        @Test
        @DisplayName("should match a partial enum name (e.g. 'ref' matches PREFER_*)")
        void shouldMatchPartialEnumName() {
            Map<String, Object> filters = Map.of("gender", Gender.PREFER_TO_SELF_DESCRIBE);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            stubPath(root, "gender", stringPath, Gender.class);
            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%prefer_to_self_describe%")).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).like(stringPath, "%prefer_to_self_describe%");
        }
    }

    @Nested
    @DisplayName("LocalDate filters")
    class DateFilters {
        @Test
        @DisplayName("should build an exact equal predicate when value is already a LocalDate")
        void shouldBuildExactPredicateForLocalDateInstance() {
            LocalDate birthdate = LocalDate.of(1995, 6, 15);
            Map<String, Object> filters = Map.of("birthdate", birthdate);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            Path<LocalDate> datePath = mockPath();
            stubPath(root, "birthdate", datePath, LocalDate.class);
            when(datePath.as(LocalDate.class)).thenReturn(datePath);
            when(criteriaBuilder.equal(datePath, birthdate)).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).equal(datePath, birthdate);
            verify(criteriaBuilder, never()).like(any(), anyString());
        }

        @Test
        @DisplayName("should parse a valid raw String date and build an exact equal predicate")
        void shouldParseValidRawStringDate() {
            Map<String, Object> filters = Map.of("birthdate", "1995-06-15");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            Path<LocalDate> datePath = mockPath();
            stubPath(root, "birthdate", datePath, LocalDate.class);
            when(datePath.as(LocalDate.class)).thenReturn(datePath);
            when(criteriaBuilder.equal(datePath, LocalDate.of(1995, 6, 15))).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).equal(datePath, LocalDate.of(1995, 6, 15));
        }

        @Test
        @DisplayName("should throw BadRequestException when raw String date is malformed")
        void shouldThrowBadRequestExceptionForMalformedDate() {
            Map<String, Object> filters = Map.of("birthdate", "1993/06/30");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            Path<LocalDate> datePath = mockPath();
            stubPath(root, "birthdate", datePath, LocalDate.class);

            assertThatThrownBy(() -> specification.toPredicate(root, query, criteriaBuilder))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("1993/06/30")
                    .hasMessageContaining("yyyy-MM-dd");
        }

        @Test
        @DisplayName("should throw BadRequestException when raw String date is not a date at all")
        void shouldThrowBadRequestExceptionForNonDateString() {
            Map<String, Object> filters = Map.of("birthdate", "not-a-date");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            Path<LocalDate> datePath = mockPath();
            stubPath(root, "birthdate", datePath, LocalDate.class);

            assertThatThrownBy(() -> specification.toPredicate(root, query, criteriaBuilder))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    // ── id / fallback filters ────────────────────────────────────────────────

    @Nested
    @DisplayName("Id and fallback filters")
    class IdFilters {

        @Test
        @DisplayName("should build an exact-match equal predicate for a Long id")
        void shouldBuildExactPredicateForLongId() {
            Map<String, Object> filters = Map.of("id", 42L);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            stubPath(root, "id", path, Long.class);
            when(criteriaBuilder.equal(path, 42L)).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).equal(path, 42L);
            verify(criteriaBuilder, never()).like(any(), anyString());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("should build an isNull predicate when filter value is null")
        void shouldBuildIsNullPredicateForNullValue() {
            Map<String, Object> filters = new LinkedHashMap<>();
            filters.put("phoneNumber", null);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            when(root.get("phoneNumber")).thenAnswer(invocation -> path);
            lenient().when(path.getJavaType()).thenReturn((Class) String.class);
            when(criteriaBuilder.isNull(path)).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).isNull(path);
            // javaType branch should never be reached for a null value
            verify(criteriaBuilder, never()).like(any(), anyString());
        }
    }

    @Nested
    @DisplayName("Nested path resolution")
    class NestedPathResolution {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("should traverse a dot-notation path across multiple segments")
        void shouldTraverseDotNotationPath() {
            Map<String, Object> filters = Map.of("address.city", "Marseille");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            Path<Object> addressPath = mock(Path.class);
            when(root.get("address")).thenReturn(addressPath);
            doReturn(stringPath).when(addressPath).get("city");
            lenient().when(stringPath.getJavaType()).thenReturn((Class) String.class);

            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%marseille%")).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(root).get("address");
            verify(addressPath).get("city");
            verify(criteriaBuilder).like(stringPath, "%marseille%");
        }
    }

    @Nested
    @DisplayName("Multiple filters combined")
    class MultipleFilters {
        @Test
        @DisplayName("should combine all field predicates with AND")
        void shouldCombineAllFiltersWithAnd() {
            Map<String, Object> filters = new LinkedHashMap<>();
            filters.put("username", "isa");
            filters.put("id", 1L);

            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            stubPath(root, "username", stringPath, String.class);
            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%isa%")).thenReturn(fieldPredicate);

            stubPath(root, "id", path, Long.class);
            when(criteriaBuilder.equal(path, 1L)).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder, times(2)).and(any(Predicate.class), any(Predicate.class));
        }

        @Test
        @DisplayName("should return the seed conjunction when filters map is empty")
        void shouldReturnConjunctionWhenNoFilters() {
            GenericSpecification<User> specification = new GenericSpecification<>(Map.of());

            Predicate result = specification.toPredicate(root, query, criteriaBuilder);

            assertThat(result).isEqualTo(conjunctionPredicate);
            verify(criteriaBuilder, never()).and(any(Predicate.class), any(Predicate.class));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void stubPath(Root<User> root, String field, Path<?> returnedPath, Class<?> javaType) {
        when(root.get(field)).thenReturn((Path<Object>) returnedPath);
        lenient().when(((Path) returnedPath).getJavaType()).thenReturn((Class) javaType);
    }

    @SuppressWarnings("unchecked")
    private <X> Path<X> mockPath() {
        return mock(Path.class);
    }
}
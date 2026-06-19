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

import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.model.enumerations.Gender;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
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
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(combinedPredicate);
    }

    @Nested
    @DisplayName("String filters")
    class StringFilters {
        @Test
        @DisplayName("should build a case-insensitive LIKE predicate for a String value")
        void shouldBuildLikePredicateForString() {
            Map<String, Object> filters = Map.of("username", "isa");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            doReturnPathFor(root, "username", stringPath);
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

            doReturnPathFor(root, "username", stringPath);
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
        @DisplayName("should build a case-insensitive LIKE predicate using the enum's name()")
        void shouldBuildLikePredicateForEnum() {
            Map<String, Object> filters = Map.of("countryOfResidence", Country.FR);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            doReturnPathFor(root, "countryOfResidence", stringPath);
            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%fr%")).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).like(stringPath, "%fr%");
        }

        @Test
        @DisplayName("should match a partial enum name (e.g. 'REF' matches PREFER_*)")
        void shouldMatchPartialEnumName() {
            Map<String, Object> filters = Map.of("gender", Gender.PREFER_TO_SELF_DESCRIBE);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            doReturnPathFor(root, "gender", stringPath);
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
        @DisplayName("should build an exact-match equal predicate for a LocalDate value")
        void shouldBuildExactPredicateForDate() {
            LocalDate birthdate = LocalDate.of(1995, 6, 15);
            Map<String, Object> filters = Map.of("birthdate", birthdate);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            Path<LocalDate> datePath = mockDatePath();
            doReturnPathFor(root, "birthdate", datePath);
            when(datePath.as(LocalDate.class)).thenReturn(datePath);
            when(criteriaBuilder.equal(datePath, birthdate)).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).equal(datePath, birthdate);
            verify(criteriaBuilder, never()).like(any(), anyString());
        }
    }

    @Nested
    @DisplayName("Id and fallback filters")
    class IdFilters {
        @Test
        @DisplayName("should build an exact-match equal predicate for a Long id")
        void shouldBuildExactPredicateForLongId() {
            Map<String, Object> filters = Map.of("id", 42L);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            doReturnPathFor(root, "id", path);
            when(criteriaBuilder.equal(path, 42L)).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).equal(path, 42L);
            verify(criteriaBuilder, never()).like(any(), anyString());
        }

        @Test
        @DisplayName("should build an isNull predicate when filter value is null")
        void shouldBuildIsNullPredicateForNullValue() {
            Map<String, Object> filters = new LinkedHashMap<>();
            filters.put("phoneNumber", null);
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            doReturnPathFor(root, "phoneNumber", path);
            when(criteriaBuilder.isNull(path)).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).isNull(path);
        }
    }

    @Nested
    @DisplayName("Nested path resolution")
    class NestedPathResolution {
        @Test
        @DisplayName("should traverse a dot-notation path across multiple segments")
        void shouldTraverseDotNotationPath() {
            Map<String, Object> filters = Map.of("address.city", "Marseille");
            GenericSpecification<User> specification = new GenericSpecification<>(filters);

            @SuppressWarnings("unchecked")
            Path<Object> addressPath = mock(Path.class);
            when(root.get("address")).thenReturn(addressPath);
            doReturn(stringPath).when(addressPath).get("city");

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

            doReturnPathFor(root, "username", stringPath);
            when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
            when(criteriaBuilder.like(stringPath, "%isa%")).thenReturn(fieldPredicate);

            doReturnPathFor(root, "id", path);
            when(criteriaBuilder.equal(path, 1L)).thenReturn(fieldPredicate);

            specification.toPredicate(root, query, criteriaBuilder);

            // conjunction() seeds, then and() is called once per filter
            verify(criteriaBuilder, times(2)).and(any(Predicate.class), any(Predicate.class));
        }
    }

    @SuppressWarnings("unchecked")
    private void doReturnPathFor(Root<User> root, String field, Path<?> returnedPath) {
        when(root.get(field)).thenReturn((Path<Object>) returnedPath);
    }

    @SuppressWarnings("unchecked")
    private Path<LocalDate> mockDatePath() {
        return mock(Path.class);
    }
}
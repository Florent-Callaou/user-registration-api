package callaou.userregistration.specifications;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Generic JPA Specification to handle entity filters
 */
public class GenericSpecification<T> implements Specification<T> {

    /**
     * The map of filters
     */
    private final transient Map<String, Object> filters;

    /**
     * Create a copy of the filters map
     * 
     * @param filters the map of filters
     */
    public GenericSpecification(Map<String, Object> filters) {
        this.filters = Collections.unmodifiableMap(new LinkedHashMap<>(filters));
    }

    /**
     * Build entire predicate with filters
     * 
     * @param root            the root
     * @param query           the query
     * @param criteriaBuilder the criteria builder
     * @param fieldPath       the full field path
     * @return the full predicate
     */
    @Override
    public @Nullable Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Predicate predicate = criteriaBuilder.conjunction();

        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            Predicate fieldPredicate = buildPredicate(
                    root,
                    criteriaBuilder,
                    entry.getKey(),
                    entry.getValue());
            predicate = criteriaBuilder.and(predicate, fieldPredicate);
        }

        return predicate;
    }

    /**
     * Build predicate with filter value depending on filter path instance
     * 
     * @param root            the root
     * @param criteriaBuilder the criteria builder
     * @param fieldPath       the full field path
     * @param value           the filter value
     * @return the built predicate
     */
    private Predicate buildPredicate(Root<T> root, CriteriaBuilder criteriaBuilder, String fieldPath, Object value) {
        Path<?> path = resolvePath(root, fieldPath);

        if (value == null) {
            return criteriaBuilder.isNull(path);
        }

        if (value instanceof String stringValue) {
            return likeIgnoreCase(criteriaBuilder, path, stringValue);
        }

        if (value instanceof Enum<?> enumValue) {
            return likeIgnoreCase(criteriaBuilder, path, enumValue.name());
        }

        if (value instanceof LocalDate dateValue) {
            return criteriaBuilder.equal(path.as(LocalDate.class), dateValue);
        }

        return criteriaBuilder.equal(path, value);
    }

    /**
     * Return a predicate of like filter ignoring case
     * 
     * @param criteriaBuilder
     * @param path
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    private Predicate likeIgnoreCase(CriteriaBuilder criteriaBuilder, Path<?> path, String value) {
        Path<String> stringPath = (Path<String>) path;
        return criteriaBuilder.like(criteriaBuilder.lower(stringPath), "%" + value.toLowerCase() + "%");
    }

    /**
     * Resolves a dot-notation path into a JPA
     *
     * @param root      the query root
     * @param fieldPath the field path
     * @return the resolved path
     */
    private Path<?> resolvePath(Root<T> root, String fieldPath) {
        String[] segments = fieldPath.split("\\.");
        Path<?> path = root;

        for (String segment : segments) {
            path = path.get(segment);
        }

        return path;
    }

}

package callaou.userregistration.model.entities;

import callaou.userregistration.model.enumerations.CriteriaType;
import callaou.userregistration.model.enumerations.Operator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Eligibility Criteria Entity
 * Individual criteria that make up an eligibility rule
 * Example: COUNTRY EQ FR, AGE_MIN GTE 18
 */
@Entity
@Table(name = "eligibility_criteria")
@Getter
@Setter
@NoArgsConstructor
public class EligibilityCriteria {
    /**
     * The unique identifier for the eligibility criteria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The type of the eligibility criteria.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CriteriaType criteriaType;

    /**
     * The operator used for evaluating the criteria.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Operator operator;

    /**
     * The value associated with the criteria.
     */
    @Column(nullable = false, length = 255)
    private String value;

    /**
     * The maximum value for the criteria (used for BETWEEN operator).
     */
    @Column(length = 255)
    private String maxValue;

    /**
     * A description of the eligibility criteria.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The priority of the eligibility criteria.
     */
    @Column(nullable = false)
    private int priority = 0;

    /**
     * The eligibility rule to which this criteria belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private EligibilityRule eligibilityRule;
}

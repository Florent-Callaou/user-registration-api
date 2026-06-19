package callaou.userregistration.model.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Eligibility Rule Entity
 * Stores eligibility rules that determine user registration eligibility
 * Rules are composed of multiple criteria joined by AND/OR operators
 */
@Entity
@Table(name = "eligibility_rules")
@Getter
@Setter
@NoArgsConstructor
public class EligibilityRule {
    /**
     * The unique identifier for the eligibility rule.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the eligibility rule.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * The description of the eligibility rule.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Indicates whether the eligibility rule is enabled or disabled.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * The operator used to combine criteria within the rule (AND/OR).
     */
    @Column(nullable = false, length = 10)
    private String operator = "AND";

    /**
     * The set of eligibility criteria associated with the eligibility rule.
     */
    @OneToMany(mappedBy = "eligibilityRule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<EligibilityCriteria> criteria = new HashSet<>();
}

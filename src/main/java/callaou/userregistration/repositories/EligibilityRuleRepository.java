package callaou.userregistration.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import callaou.userregistration.model.entities.EligibilityRule;

/**
 * Repository interface for managing ElibilityRule entity.
 */
public interface EligibilityRuleRepository extends JpaRepository<EligibilityRule, Long> {

    /**
     * Find eligibility rules by their enability
     * 
     * @param enabled the enabled value
     * @return the eligibility rules found
     */
    List<EligibilityRule> findByEnabled(boolean enabled);
}

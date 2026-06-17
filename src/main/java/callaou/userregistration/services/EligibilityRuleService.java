package callaou.userregistration.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import callaou.userregistration.exceptions.UserNotEligibleException;
import callaou.userregistration.model.entities.EligibilityCriteria;
import callaou.userregistration.model.entities.EligibilityRule;
import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Operator;
import callaou.userregistration.repositories.EligibilityRuleRepository;
import callaou.userregistration.specifications.UserEligibilitySpecification;

/**
 * Service to retreive eligibility rules and check users eligibility
 */
@Service
public class EligibilityRuleService {

    /**
     * The EligibilityRuleRepository
     */
    private final EligibilityRuleRepository eligibilityRuleRepository;

    /**
     * Inject the different components
     * 
     * @param eligibilityRuleRepository the EligibilityRuleRepository
     */
    public EligibilityRuleService(EligibilityRuleRepository eligibilityRuleRepository) {
        this.eligibilityRuleRepository = eligibilityRuleRepository;
    }

    /**
     * Verify User eligibility to register
     * 
     * @param user the User to verify
     */
    public void verifyUserEligibility(User user) {
        List<EligibilityRule> eligibilityRules = eligibilityRuleRepository.findByEnabled(true);

        for (EligibilityRule eligibilityRule : eligibilityRules) {
            List<EligibilityCriteria> failedCriteria = checkEligibilityRule(user, eligibilityRule);

            if (!failedCriteria.isEmpty()) {
                throw new UserNotEligibleException(generateIneligibilityMessage(failedCriteria));
            }
        }
    }

    /**
     * Check if user meets eligibility rule
     * 
     * @param user            the user to check
     * @param eligibilityRule the rule
     * @return true if the user meets the rule, false otherwise
     */
    private List<EligibilityCriteria> checkEligibilityRule(User user, EligibilityRule eligibilityRule) {
        List<EligibilityCriteria> criteria = eligibilityRule.getCriteria()
                .stream()
                .sorted(Comparator.comparingInt(EligibilityCriteria::getPriority))
                .toList();

        if (criteria.isEmpty()) {
            return List.of();
        }

        List<EligibilityCriteria> failedCriteria = new ArrayList<>();

        for (EligibilityCriteria eligibilityCriterion : failedCriteria) {
            UserEligibilitySpecification userEligibilitySpecification = UserEligibilitySpecification.fromCriteria(
                    eligibilityCriterion.getCriteriaType(),
                    eligibilityCriterion.getValue());

            if (!userEligibilitySpecification.isSatisfiedBy(user)) {
                failedCriteria.add(eligibilityCriterion);
            }
        }

        if ("OR".equals(eligibilityRule.getOperator()) && (failedCriteria.size() != criteria.size())) {
            return List.of();
        }

        return failedCriteria;
    }

    /**
     * Generate eligibility message from failed criteria
     * 
     * @param failedCriteria the failed EligibilityCriteria
     * @return the message generated
     */
    private String generateIneligibilityMessage(List<EligibilityCriteria> failedCriteria) {
        return failedCriteria
                .stream()
                .map(criterion -> (String.format("%s must be %s %s",
                        criterion.getCriteriaType().getLabel(),
                        criterion.getOperator().getDescription(),
                        criterion.getValue())))
                .collect(Collectors.joining(", "));
    }
}

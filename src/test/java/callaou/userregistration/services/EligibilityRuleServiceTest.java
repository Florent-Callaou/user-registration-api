package callaou.userregistration.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import callaou.userregistration.exceptions.UserNotEligibleException;
import callaou.userregistration.model.entities.EligibilityCriteria;
import callaou.userregistration.model.entities.EligibilityRule;
import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.model.enumerations.CriterionType;
import callaou.userregistration.model.enumerations.Operator;
import callaou.userregistration.repositories.EligibilityRuleRepository;

/**
 * Unit tests for {@link EligibilityRuleService}
 */
@ExtendWith(MockitoExtension.class)
class EligibilityRuleServiceTest {

        /**
         * Inject the EligibilityRuleService
         */
        @InjectMocks
        private EligibilityRuleService eligibilityRuleService;

        /**
         * Inject the EligibilityRuleRepository
         */
        @Mock
        private EligibilityRuleRepository eligibilityRuleRepository;

        /**
         * Object generator
         */
        private final EasyRandom generator = new EasyRandom();

        /**
         * Build user given country and birthdate
         * 
         * @param country   the country of residence
         * @param birthdate the birthdate
         * @return the user built
         */
        private User buildUser(Country country, LocalDate birthdate) {
                User user = generator.nextObject(User.class);
                user.setCountryOfResidence(country);
                user.setBirthdate(birthdate);
                return user;
        }

        /**
         * Build an EligibilityRule
         * 
         * @param operator the rule operator
         * @param criteria the rule criteria list
         * @return the built rule
         */
        private EligibilityRule buildRule(String operator, Set<EligibilityCriteria> criteria) {
                EligibilityRule eligibilityRule = new EligibilityRule();
                eligibilityRule.setOperator(operator);
                eligibilityRule.setCriteria(criteria);
                return eligibilityRule;
        }

        /**
         * Build an EligibilityCriteria
         * 
         * @param type     the criterion type
         * @param operator the criterion operator
         * @param value    the criterion value
         * @param priority the criterion priority
         * @return the criterion built
         */
        private EligibilityCriteria buildCriteria(CriterionType type, Operator operator,
                        String value, int priority) {
                EligibilityCriteria eligibilityCriteria = new EligibilityCriteria();
                eligibilityCriteria.setCriterionType(type);
                eligibilityCriteria.setOperator(operator);
                eligibilityCriteria.setCriterionValue(value);
                eligibilityCriteria.setPriority(priority);
                return eligibilityCriteria;
        }

        @Nested
        @DisplayName("verifyUserEligibility - no rules")
        class NoRules {
                @Test
                @DisplayName("should pass silently when repository returns empty list")
                void shouldPassWhenNoRules() {
                        User user = mock(User.class);

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of());

                        eligibilityRuleService.verifyUserEligibility(user);

                        verify(eligibilityRuleRepository).findByEnabled(true);
                }
        }

        @Nested
        @DisplayName("verifyUserEligibility - rule with no criteria")
        class RuleWithNoCriteria {
                @Test
                @DisplayName("should pass silently when the rule has an empty criteria list")
                void shouldPassWhenCriteriaListEmpty() {
                        User user = mock(User.class);

                        EligibilityRule eligibilityRule = mock(EligibilityRule.class);

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));
                        when(eligibilityRule.getCriteria()).thenReturn(new HashSet<>());

                        eligibilityRuleService.verifyUserEligibility(user);

                        verify(eligibilityRuleRepository).findByEnabled(true);
                }
        }

        @Nested
        @DisplayName("CUSTOM criteria")
        class CustomCriteria {
                @Test
                @DisplayName("CUSTOME criteria should always pass (until further use)")
                void customPass() {
                        User eligibleUser = (buildUser(Country.JP, LocalDate.now().minusYears(12)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.CUSTOM, Operator.EQ, "FR", 1),
                                                        buildCriteria(CriterionType.CUSTOM, Operator.GTE, "18", 2))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }
        }

        @Nested
        @DisplayName("COUNTRY criteria - all operators")
        class CountryCriteria {
                @Test
                @DisplayName("EQ - should pass when country matches")
                void eqPass() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.EQ, "FR", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @Test
                @DisplayName("EQ - should throw when country does not match")
                void eqFail() {
                        User ineligibleUser = (buildUser(Country.JP, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.EQ, "FR", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Country must be equal to FR")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("NEQ - should pass when country is different from excluded one")
                void neqPass() {
                        User eligibleUser = (buildUser(Country.US, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.NEQ, "DE", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @Test
                @DisplayName("NEQ - should throw when country matches the excluded one")
                void neqFail() {
                        User ineligibleUser = (buildUser(Country.DE, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.NEQ, "DE", 1))));
                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Country must be different than DE")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("IN - should pass when country is in the allowed list")
                void inPass() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.IN, "FR,DE,ES",
                                                                        1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @Test
                @DisplayName("IN - should throw when country is not in the allowed list")
                void inFail() {
                        User ineligibleUser = (buildUser(Country.NL, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.IN, "DE,ES",
                                                                        1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Country must be in list DE,ES")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("NOT_IN - should pass when country is not in the excluded list")
                void notInPass() {
                        User eligibleUser = (buildUser(Country.JP, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.NOT_IN, "DE,ES",
                                                                        1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @Test
                @DisplayName("NOT_IN - should throw when country is in the excluded list")
                void notInFail() {
                        User ineligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.NOT_IN, "FR,DE",
                                                                        1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Country must be different than FR,DE")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("should throw when unsupported country operator used")
                void shouldThrowWhenUnsupportedCountryOperator() {
                        User user = (buildUser(Country.JP, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.GTE, "JP", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(user))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessage("Operator GTE is not supported for COUNTRY criteria");
                }
        }

        @Nested
        @DisplayName("AGE criteria - all operators")
        class AgeCriteria {
                @Test
                @DisplayName("GTE - should pass when user is exactly the minimum age")
                void gtePassExact() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(18)));

                        EligibilityRule eligibilityRule = buildRule("AND", new HashSet<>(Arrays.asList(
                                        buildCriteria(CriterionType.AGE, Operator.GTE, "18", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @Test
                @DisplayName("GTE - should pass when user is older than minimum age")
                void gtePassOlder() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND", new HashSet<>(Arrays.asList(
                                        buildCriteria(CriterionType.AGE, Operator.GTE, "18", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @Test
                @DisplayName("GTE - should throw when user is younger than minimum age")
                void gteFail() {
                        User ineligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(15)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.GTE, "18", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be greater than or equal to 18")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("LTE - should pass when user is exactly the maximum age")
                void ltePassExact() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.LTE, "25", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @ParameterizedTest(name = "{0}")
                @MethodSource("lteCases")
                @DisplayName("LTE - age less than or equal to 65")
                void lte(String description, LocalDate birthdate, String expectedMessage) {
                        User user = buildUser(Country.FR, birthdate);

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(List.of(
                                                        buildCriteria(CriterionType.AGE, Operator.LTE, "65", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        if (expectedMessage == null) {
                                eligibilityRuleService.verifyUserEligibility(user);
                        } else {
                                assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(user))
                                                .isInstanceOf(UserNotEligibleException.class)
                                                .hasMessage(expectedMessage)
                                                .extracting("errorCode.httpStatus")
                                                .isEqualTo(HttpStatus.valueOf(422));
                        }
                }

                static Stream<Arguments> lteCases() {
                        return Stream.of(
                                        Arguments.of("younger than maximum age (36)", LocalDate.now().minusYears(36),
                                                        null),
                                        Arguments.of("older than maximum age (105)", LocalDate.now().minusYears(105),
                                                        "User is not eligible: Age must be less than or equal to 65"));
                }

                @Test
                @DisplayName("GT - should pass when user is older than minimum age")
                void gtPassOlder() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND", new HashSet<>(Arrays.asList(
                                        buildCriteria(CriterionType.AGE, Operator.GT, "18", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @Test
                @DisplayName("GT - should throw when user is exactly the minimum age")
                void gtFailExact() {
                        User ineligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(18)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.GT, "18", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be greater than 18")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("GT - should throw when user is younger than minimum age")
                void gtFail() {
                        User ineligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(15)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.GT, "18", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be greater than 18")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("LT - should pass when user is younger than maximum age")
                void ltPass() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(60)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.LTE, "75", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);
                }

                @Test
                @DisplayName("LT - should throw when user is older than maximum age")
                void ltFail() {
                        User ineligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(76)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.LT, "74", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be less than 74")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("LT - should throw when user is exactly the maximum age")
                void ltFailExactly() {
                        User ineligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(80)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.LT, "80", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be less than 80")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @ParameterizedTest(name = "{0}")
                @MethodSource("betweenPassCases")
                @DisplayName("BETWEEN - should pass for ages within range [18,65] inclusive")
                void betweenPass(String description, LocalDate birthdate) {
                        User eligibileUser = buildUser(Country.FR, birthdate);

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(List.of(
                                                        buildCriteria(CriterionType.AGE, Operator.BETWEEN, "18,65",
                                                                        1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibileUser);
                }

                static Stream<Arguments> betweenPassCases() {
                        return Stream.of(
                                        Arguments.of("age within range (26)", LocalDate.now().minusYears(26)),
                                        Arguments.of("age exactly lower bound (18)", LocalDate.now().minusYears(18)),
                                        Arguments.of("age exactly upper bound (65)", LocalDate.now().minusYears(65)));
                }

                @Test
                @DisplayName("BETWEEN - should throw when age is below the lower bound")
                void betweenFailBelow() {
                        User ineligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(17)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.BETWEEN, "18,65",
                                                                        1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be between (inclusive) 18,65")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("BETWEEN - should throw when age is above the upper bound")
                void betweenFailAbove() {
                        User ineligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(66)));

                        EligibilityRule eligibilityRule = buildRule("AND", new HashSet<>(Arrays.asList(
                                        buildCriteria(CriterionType.AGE, Operator.BETWEEN, "18,65", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be between (inclusive) 18,65")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("should throw when unsupported age operator used")
                void shouldThrowWhenUnsupportedAgeOperator() {
                        User user = (buildUser(Country.JP, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.EQ, "25", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(user))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessage("Operator EQ is not supported for AGE criteria");
                }
        }

        @Nested
        @DisplayName("verifyUserEligibility - AND rules")
        class AndRules {
                @Test
                @DisplayName("should pass when user satisfies all AND criteria")
                void shouldPassWhenAllCriteriaMet() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(27)));

                        EligibilityRule andRule = buildRule("AND", new HashSet<>(Arrays.asList(
                                        buildCriteria(CriterionType.COUNTRY, Operator.EQ, "FR", 1),
                                        buildCriteria(CriterionType.AGE, Operator.GTE, "18", 2))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(andRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);

                        verify(eligibilityRuleRepository).findByEnabled(true);
                }

                @Test
                @DisplayName("should throw when one AND criterion fails")
                void shouldThrowWhenOneCriterionFails() {
                        User ineligibleUser = (buildUser(Country.US, LocalDate.now().minusYears(17)));

                        EligibilityRule andRule = buildRule("AND", new HashSet<>(Arrays.asList(
                                        buildCriteria(CriterionType.COUNTRY, Operator.EQ, "US", 1),
                                        buildCriteria(CriterionType.AGE, Operator.GTE, "21", 2))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(andRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be greater than or equal to 21")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("should throw with message containing all failed criteria labels and values")
                void shouldThrowWithFullMessageForAllFailedCriteria() {
                        User ineligibleUser = (buildUser(Country.DE, LocalDate.now().minusYears(89)));

                        EligibilityRule andRule = buildRule("AND", new HashSet<>(Arrays.asList(
                                        buildCriteria(CriterionType.COUNTRY, Operator.NEQ, "DE", 2),
                                        buildCriteria(CriterionType.AGE, Operator.LTE, "80", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(andRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage(
                                                        "User is not eligible: Age must be less than or equal to 80, Country must be different than DE")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }

                @Test
                @DisplayName("criteria should be evaluated in priority order")
                void shouldEvaluateCriteriaInPriorityOrder() {
                        User ineligibleUser = (buildUser(Country.DE, LocalDate.now().minusYears(7)));

                        EligibilityRule eligibilityRule = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.GTE, "18", 2),
                                                        buildCriteria(CriterionType.COUNTRY, Operator.EQ, "FR", 1),
                                                        buildCriteria(CriterionType.COUNTRY, Operator.NEQ, "DE", 3))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage(
                                                        "User is not eligible: Country must be equal to FR, Age must be greater than or equal to 18, Country must be different than DE")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }
        }

        @Nested
        @DisplayName("verifyUserEligibility - OR rules")
        class OrRules {
                @Test
                @DisplayName("should pass when all OR criteria are satisfied")
                void shouldPassWhenAllCriteriaMet() {
                        User eligibleUser = (buildUser(Country.JP, LocalDate.now().minusYears(115)));

                        EligibilityRule eligibilityRule = buildRule("OR",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.IN, "FR,JP", 1),
                                                        buildCriteria(CriterionType.AGE, Operator.GT, "51", 2))));

                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);

                        verify(eligibilityRuleRepository).findByEnabled(true);
                }

                @Test
                @DisplayName("should pass when only one OR criterion is satisfied")
                void shouldPassWhenOnlyOneCriterionMet() {
                        User eligibleUser = (buildUser(Country.JP, LocalDate.now().minusYears(19)));

                        EligibilityRule eligibilityRule = buildRule("OR",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.EQ, "FR", 1),
                                                        buildCriteria(CriterionType.AGE, Operator.GTE, "18", 2))));
                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);

                        verify(eligibilityRuleRepository).findByEnabled(true);
                }

                @Test
                @DisplayName("should throw when ALL OR criteria fail")
                void shouldThrowWhenAllFail() {
                        User ineligibleUser = (buildUser(Country.US, LocalDate.now().minusYears(115)));

                        EligibilityRule eligibilityRule = buildRule("OR",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.NOT_IN, "US,BE",
                                                                        1),
                                                        buildCriteria(CriterionType.AGE, Operator.LTE, "100", 2))));
                        when(eligibilityRuleRepository.findByEnabled(true)).thenReturn(List.of(eligibilityRule));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage(
                                                        "User is not eligible: Country must be different than US,BE, Age must be less than or equal to 100")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }
        }

        @Nested
        @DisplayName("verifyUserEligibility - multiple rules")
        class MultipleRules {
                @Test
                @DisplayName("should pass when user satisfies all active rules")
                void shouldPassWhenAllRulesMet() {
                        User eligibleUser = (buildUser(Country.FR, LocalDate.now().minusYears(24)));

                        EligibilityRule eligibilityRule1 = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.EQ, "FR", 1))));
                        EligibilityRule eligibilityRule2 = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.GTE, "18", 1))));

                        when(eligibilityRuleRepository.findByEnabled(true))
                                        .thenReturn(List.of(eligibilityRule1, eligibilityRule2));

                        eligibilityRuleService.verifyUserEligibility(eligibleUser);

                        verify(eligibilityRuleRepository).findByEnabled(true);
                }

                @Test
                @DisplayName("should throw on the first failing rule and not evaluate the second")
                void shouldThrowOnFirstFailingRule() {
                        User ineligibleUser = (buildUser(Country.JP, LocalDate.now().minusYears(25)));

                        EligibilityRule eligibilityRule1 = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.COUNTRY, Operator.EQ, "FR", 1))));
                        EligibilityRule eligibilityRule2 = buildRule("AND",
                                        new HashSet<>(Arrays.asList(
                                                        buildCriteria(CriterionType.AGE, Operator.GTE, "18", 1))));
                        when(eligibilityRuleRepository.findByEnabled(true))
                                        .thenReturn(List.of(eligibilityRule1, eligibilityRule2));

                        assertThatThrownBy(() -> eligibilityRuleService.verifyUserEligibility(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Country must be equal to FR")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));
                }
        }
}

package callaou.userregistration.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import callaou.userregistration.exceptions.AlreadyExistsException;
import callaou.userregistration.exceptions.UserNotEligibleException;
import callaou.userregistration.mappers.UserMapper;
import callaou.userregistration.model.dtos.UserRequest;
import callaou.userregistration.model.dtos.UserResponse;
import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.model.enumerations.Gender;
import callaou.userregistration.repositories.UserRepository;

/**
 * Unit tests for {@link UserService}
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

        /**
         * Inject the UserService
         */
        @InjectMocks
        private UserService userService;

        /**
         * Inject the UserRepository
         */
        @Mock
        private UserRepository userRepository;

        /**
         * Inject the EligibilityRuleService
         */
        @Mock
        private EligibilityRuleService eligibilityRuleService;

        /**
         * Spy on the UserMapper
         */
        @Spy
        private UserMapper userMapper;

        /**
         * Object generator
         */
        private final EasyRandom generator = new EasyRandom();

        @Nested
        @DisplayName("registerUser - no errors")
        class NoErrors {
                @Test
                @DisplayName("should return UserResponse when user is new and eligible")
                void shouldRegisterSuccessfully() {
                        UserRequest validRequest = new UserRequest(
                                        "jean.jacques",
                                        LocalDate.now().minusYears(25),
                                        Country.FR,
                                        "+33014852636",
                                        Gender.MALE);

                        when(userRepository.existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any()))
                                        .thenReturn(false);

                        doNothing().when(eligibilityRuleService).verifyUserEligibility(any(User.class));

                        User savedUser = new User();
                        savedUser.setUsername(validRequest.username());
                        savedUser.setBirthdate(validRequest.birthdate());
                        savedUser.setCountryOfResidence(validRequest.countryOfResidence());
                        savedUser.setPhoneNumber(validRequest.phoneNumber());
                        savedUser.setGender(validRequest.gender());

                        when(userRepository.save(any(User.class))).thenReturn(savedUser);

                        UserResponse response = userService.registerUser(validRequest);

                        assertThat(response).isNotNull();
                        assertThat(response.username()).isEqualTo(validRequest.username());
                        assertThat(response.birthdate()).isEqualTo(validRequest.birthdate());
                        assertThat(response.countryOfResidence()).isEqualTo(validRequest.countryOfResidence());
                        assertThat(response.phoneNumber()).isEqualTo(validRequest.phoneNumber());
                        assertThat(response.gender()).isEqualTo(validRequest.gender());

                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any());
                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(
                                        "jean.jacques",
                                        LocalDate.now().minusYears(25),
                                        Country.FR);

                        verify(eligibilityRuleService).verifyUserEligibility(any());

                        verify(userRepository).save(any());
                }

                @Test
                @DisplayName("should map request to entity and pass it to eligibility check")
                void shouldPassMappedEntityToEligibilityService() {
                        UserRequest validRequest = new UserRequest(
                                        "martha.tack",
                                        LocalDate.now().minusYears(27),
                                        Country.DE,
                                        "0778529684",
                                        Gender.FEMALE);

                        when(userRepository.existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(),
                                        any())).thenReturn(false);

                        doNothing().when(eligibilityRuleService).verifyUserEligibility(any(User.class));

                        when(userRepository.save(any(User.class))).thenReturn(generator.nextObject(User.class));

                        userService.registerUser(validRequest);

                        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

                        verify(eligibilityRuleService).verifyUserEligibility(captor.capture());
                        User captured = captor.getValue();

                        assertThat(captured.getUsername()).isEqualTo(validRequest.username());
                        assertThat(captured.getBirthdate()).isEqualTo(validRequest.birthdate());
                        assertThat(captured.getCountryOfResidence()).isEqualTo(validRequest.countryOfResidence());
                        assertThat(captured.getPhoneNumber()).isEqualTo(validRequest.phoneNumber());
                        assertThat(captured.getGender()).isEqualTo(validRequest.gender());

                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any());
                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(
                                        "martha.tack",
                                        LocalDate.now().minusYears(27),
                                        Country.DE);

                        verify(eligibilityRuleService).verifyUserEligibility(any());

                        verify(userRepository).save(any());
                }

                @Test
                @DisplayName("should pass the mapped entity to repository save")
                void shouldPassMappedEntityToRepository() {
                        UserRequest validRequest = new UserRequest(
                                        "julien.bras",
                                        LocalDate.now().minusYears(65),
                                        Country.US,
                                        "+54856995214",
                                        Gender.NON_BINARY);

                        when(userRepository.existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any()))
                                        .thenReturn(false);

                        doNothing().when(eligibilityRuleService).verifyUserEligibility(any(User.class));

                        User savedUser = generator.nextObject(User.class);
                        when(userRepository.save(any(User.class))).thenReturn(savedUser);

                        userService.registerUser(validRequest);

                        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

                        verify(userRepository).save(captor.capture());
                        User captured = captor.getValue();

                        assertThat(captured.getUsername()).isEqualTo(validRequest.username());
                        assertThat(captured.getBirthdate()).isEqualTo(validRequest.birthdate());
                        assertThat(captured.getCountryOfResidence()).isEqualTo(validRequest.countryOfResidence());
                        assertThat(captured.getPhoneNumber()).isEqualTo(validRequest.phoneNumber());
                        assertThat(captured.getGender()).isEqualTo(validRequest.gender());

                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any());
                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(
                                        "julien.bras",
                                        LocalDate.now().minusYears(65),
                                        Country.US);

                        verify(eligibilityRuleService).verifyUserEligibility(any());

                        verify(userRepository).save(any());
                }
        }

        @Nested
        @DisplayName("registerUser - duplicate user")
        class DuplicateUser {
                @Test
                @DisplayName("should throw AlreadyExistsException when user already exists")
                void shouldThrowWhenUserAlreadyExists() {
                        UserRequest existingUser = new UserRequest(
                                        "pierre.raoul",
                                        LocalDate.of(2000, 02, 05),
                                        Country.FR,
                                        "+54858965214",
                                        Gender.PREFER_TO_SELF_DESCRIBE);

                        when(userRepository.existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any()))
                                        .thenReturn(true);

                        assertThatThrownBy(() -> userService.registerUser(existingUser))
                                        .isInstanceOf(AlreadyExistsException.class)
                                        .hasMessage(
                                                        "Object User with username, birthdate, country: 'pierre.raoul, 2000-02-05, France' already exists")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.CONFLICT);

                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any());
                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(
                                        "pierre.raoul",
                                        LocalDate.of(2000, 02, 05),
                                        Country.FR);

                        verify(eligibilityRuleService, never()).verifyUserEligibility(any());
                        verify(userRepository, never()).save(any());
                }
        }

        @Nested
        @DisplayName("registerUser - eligibility failure")
        class EligibilityFailure {
                @Test
                @DisplayName("should propagate UserNotEligibleException from eligibility service")
                void shouldPropagateEligibilityException() {
                        UserRequest ineligibleUser = new UserRequest(
                                        "michel.bertrand",
                                        LocalDate.now().minusYears(52),
                                        Country.FR,
                                        "0698745263",
                                        Gender.MALE);

                        when(userRepository.existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any()))
                                        .thenReturn(false);

                        doThrow(new UserNotEligibleException("Age must be greater than or equal to 18"))
                                        .when(eligibilityRuleService).verifyUserEligibility(any(User.class));

                        assertThatThrownBy(() -> userService.registerUser(ineligibleUser))
                                        .isInstanceOf(UserNotEligibleException.class)
                                        .hasMessage("User is not eligible: Age must be greater than or equal to 18")
                                        .extracting("errorCode.httpStatus")
                                        .isEqualTo(HttpStatus.valueOf(422));

                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(any(), any(), any());
                        verify(userRepository).existsByUsernameAndBirthdateAndCountryOfResidence(
                                        "michel.bertrand",
                                        LocalDate.now().minusYears(52),
                                        Country.FR);

                        verify(eligibilityRuleService).verifyUserEligibility(any());

                        verify(userRepository, never()).save(any());
                }
        }

}

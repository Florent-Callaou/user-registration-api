package callaou.userregistration.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import callaou.userregistration.exceptions.AlreadyExistsException;
import callaou.userregistration.exceptions.UserNotEligibleException;
import callaou.userregistration.model.dtos.UserRequest;
import callaou.userregistration.model.dtos.UserResponse;
import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.model.enumerations.Gender;
import callaou.userregistration.services.UserService;

/**
 * Unit tests for {@link UserController}
 */
@WebMvcTest(controllers = UserController.class)
@WithMockUser
class UserControllerTest {

    /**
     * Inject the MockMvc
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Inject the UserService
     */
    @MockitoBean
    private UserService userService;

    /**
     * Configure the ObjectMapper
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Valid UserResponse
     */
    private UserResponse validResponse;

    /**
     * Fill valid UserResponse before each test
     */
    @BeforeEach
    void setUp() {
        validResponse = new UserResponse(
                1L,
                "patrick.murin",
                LocalDate.now().minusYears(25),
                Country.FR,
                "+33612345678",
                Gender.MALE);
    }

    @Nested
    @DisplayName("POST /api/users — no errors")
    class NoErrors {
        @Test
        @DisplayName("should return 201 with UserResponse body when registration succeeds")
        void shouldReturn201WithBody() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(validResponse.id()))
                    .andExpect(jsonPath("$.username").value(validResponse.username()))
                    .andExpect(jsonPath("$.birthdate").value(validResponse.birthdate().toString()))
                    .andExpect(jsonPath("$.countryOfResidence").value(validResponse.countryOfResidence().name()))
                    .andExpect(jsonPath("$.phoneNumber").value(validResponse.phoneNumber()))
                    .andExpect(jsonPath("$.gender").value(validResponse.gender().name()));
        }

        @Test
        @DisplayName("should pass with null phoneNumber — field is optional")
        void shouldReturn201WithNullPhoneNumber() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    null,
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should pass with null gender — field is optional")
        void shouldReturn201WithNullGender() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    "+33612345678",
                    null);

            when(userService.registerUser(any(UserRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /api/users — username validation")
    class UsernameValidation {
        @Test
        @DisplayName("should return 400 when username is null")
        void shouldReturn400WhenUsernameNull() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    null,
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                    .andExpect(jsonPath("$.fieldErrors[0].message").value("Username is required"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should return 400 when username is blank")
        void shouldReturn400WhenUsernameBlank() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "",
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("username"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should return 400 when username is too short (under 3 chars)")
        void shouldReturn400WhenUsernameTooShort() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "pa",
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                    .andExpect(jsonPath("$.fieldErrors[0].message")
                            .value("Username must be between 3 and 50 characters"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should return 400 when username is too long (over 50 chars)")
        void shouldReturn400WhenUsernameTooLong() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "a".repeat(51),
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                    .andExpect(jsonPath("$.fieldErrors[0].message")
                            .value("Username must be between 3 and 50 characters"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should pass when username is exactly 3 chars — lower boundary")
        void shouldPassWhenUsernameExactly3Chars() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "aaa",
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should pass when username is exactly 50 chars — upper boundary")
        void shouldPassWhenUsernameExactly50Chars() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "a".repeat(50),
                    LocalDate.now().minusYears(25),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /api/users — birthdate validation")
    class BirthdateValidation {
        @Test
        @DisplayName("should return 400 when birthdate is null")
        void shouldReturn400WhenBirthdateNull() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "patrick.murin",
                    null,
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("birthdate"))
                    .andExpect(jsonPath("$.fieldErrors[0].message").value("Birthdate is required"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should return 400 when birthdate is today — not in the past")
        void shouldReturn400WhenBirthdateIsToday() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now(),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("birthdate"))
                    .andExpect(jsonPath("$.fieldErrors[0].message").value("Birthdate must be in the past"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should return 400 when birthdate is in the future")
        void shouldReturn400WhenBirthdateInFuture() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().plusDays(1),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("birthdate"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should pass when birthdate is yesterday — valid past date")
        void shouldPassWhenBirthdateIsYesterday() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusDays(1),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /api/users — countryOfResidence validation")
    class CountryValidation {
        @Test
        @DisplayName("should return 400 when countryOfResidence is null")
        void shouldReturn400WhenCountryNull() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(19),
                    null,
                    "+33612345678",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("countryOfResidence"))
                    .andExpect(jsonPath("$.fieldErrors[0].message").value("Country of residence is required"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should return 400 when countryOfResidence is invalid")
        void shouldReturn400WhenCountryInvalid() throws Exception {
            String invalidPayload = """
                    {
                        "username": "richard.jour",
                        "birthdate": "1994-02-25",
                        "countryOfResidence": "INVALID",
                        "phoneNumber": "+33702454575",
                        "gender": "MALE"
                    }
                                        """;

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            "Invalid value 'INVALID' for field 'Country'. Allowed values: [FR, US, DE, IT, ES, GB, SE, NO, NL, BE, CH, CA, MX, AU, JP]"));

            verify(userService, never()).registerUser(any());
        }
    }

    @Nested
    @DisplayName("POST /api/users — phoneNumber validation")
    class PhoneNumberValidation {
        @Test
        @DisplayName("should return 400 when phoneNumber exceeds 20 characters")
        void shouldReturn400WhenPhoneNumberTooLong() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(19),
                    Country.FR,
                    "+3361234567729575917725712568",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("phoneNumber"))
                    .andExpect(jsonPath("$.fieldErrors[0].message")
                            .value("Phone number must be at most 20 characters"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should return 400 when phoneNumber contains invalid characters")
        void shouldReturn400WhenPhoneNumberInvalidChars() throws Exception {
            UserRequest invalidRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(19),
                    Country.FR,
                    "fez458",
                    Gender.MALE);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("phoneNumber"))
                    .andExpect(jsonPath("$.fieldErrors[0].message")
                            .value("Phone number must contain only digits and an optional leading '+'"));

            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("should pass when phoneNumber has only digits")
        void shouldPassWhenPhoneNumberOnlyDigits() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(19),
                    Country.FR,
                    "0605789636",
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should pass when phoneNumber starts with +")
        void shouldPassWhenPhoneNumberStartsWithPlus() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(19),
                    Country.FR,
                    "+33612345678",
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("POST /api/users — gender validation")
    class GenderValidation {
        @Test
        @DisplayName("should return 400 when Gender is invalid")
        void shouldReturn400WhenGenderInvalid() throws Exception {
            String invalidPayload = """
                    {
                        "username": "richard.jour",
                        "birthdate": "1994-02-25",
                        "countryOfResidence": "FR",
                        "phoneNumber": "+33702454575",
                        "gender": "INVALID"
                    }
                                        """;

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            "Invalid value 'INVALID' for field 'Gender'. Allowed values: [MALE, FEMALE, NON_BINARY, PREFER_NOT_TO_SAY, PREFER_TO_SELF_DESCRIBE]"));

            verify(userService, never()).registerUser(any());
        }
    }

    @Nested
    @DisplayName("POST /api/users — json validation")
    class JsonValidation {
        @Test
        @DisplayName("should return 400 when json is malformed")
        void shouldReturn400WhenJsonMalformed() throws Exception {
            String invalidPayload = """
                    {
                        "username": "richard.jour",
                        "birthdate": "1994-02-25",
                        "countryOfResidence": "FR",
                        "phoneNumber": "+33702454575",
                        "gender": "MALE
                    }
                                        """;

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            "Malformed JSON input"));

            verify(userService, never()).registerUser(any());
        }
    }

    @Nested
    @DisplayName("POST /api/users — unexcpected exception")
    class UnexcpectedException {
        @Test
        @DisplayName("should return 500 when unexpected exception occurs")
        void shouldReturn500OnUnexpectedException() throws Exception {
            when(userService.registerUser(any())).thenThrow(new IllegalStateException("Simulated unexpected failure"));

            String validPayload = """
                    {
                        "username": "richard.jour",
                        "birthdate": "1994-02-25",
                        "countryOfResidence": "FR",
                        "phoneNumber": "+33702454575",
                        "gender": "MALE"
                    }
                    """;

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validPayload))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"))
                    .andExpect(
                            jsonPath("$.message").value("An unexpected error occurred: Simulated unexpected failure"));
        }
    }

    @Nested
    @DisplayName("POST /api/users — service exceptions")
    class ServiceExceptions {
        @Test
        @DisplayName("should return 409 when service throws AlreadyExistsException")
        void shouldReturn409WhenAlreadyExists() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(19),
                    Country.FR,
                    "0605789636",
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class)))
                    .thenThrow(new AlreadyExistsException(
                            User.class,
                            "username, birthdate, country",
                            "patrick.murin, " + LocalDate.now().minusYears(25) + ", FR"));

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("409"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should return 422 when service throws UserNotEligibleException")
        void shouldReturn422WhenNotEligible() throws Exception {
            UserRequest validRequest = new UserRequest(
                    "patrick.murin",
                    LocalDate.now().minusYears(19),
                    Country.FR,
                    "0605789636",
                    Gender.MALE);

            when(userService.registerUser(any(UserRequest.class)))
                    .thenThrow(new UserNotEligibleException("Age must be greater than or equal to 18"));

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(jsonPath("$.code").value("422"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

}

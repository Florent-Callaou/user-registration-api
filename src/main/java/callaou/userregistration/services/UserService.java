package callaou.userregistration.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import callaou.userregistration.exceptions.AlreadyExistsException;
import callaou.userregistration.mappers.UserMapper;
import callaou.userregistration.model.dtos.UserRequest;
import callaou.userregistration.model.dtos.UserResponse;
import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Country;
import callaou.userregistration.repositories.UserRepository;

/**
 * Business logic for user registration and retrieval
 */
@Service
public class UserService {

    /**
     * The UserRepository
     */
    private final UserRepository userRepository;

    /**
     * The EligibilityRuleService
     */
    private final EligibilityRuleService eligibilityRuleService;

    /**
     * The UserMapper
     */
    private final UserMapper userMapper;

    /**
     * Inject the different components
     * 
     * @param userRepository         the UserRepository
     * @param eligibilityRuleService the EligibilityRuleService
     * @param userMapper             the UserMapper
     */
    public UserService(UserRepository userRepository, EligibilityRuleService eligibilityRuleService,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.eligibilityRuleService = eligibilityRuleService;
        this.userMapper = userMapper;
    }

    /**
     * Register a user after checking validity and eligibility
     * 
     * @param userRequest the user info to save
     * @return the user info
     */
    public UserResponse registerUser(UserRequest userRequest) {
        String username = userRequest.username();
        LocalDate birthdate = userRequest.birthdate();
        Country countryOfResidence = userRequest.countryOfResidence();

        if (userRepository.existsByUsernameAndBirthdateAndCountry(username, birthdate, countryOfResidence)) {
            String messageValues = String.join(", ", username, birthdate.toString(), countryOfResidence.toString());
            throw new AlreadyExistsException(User.class, "username, birthdate, country", messageValues);
        }

        User user = userMapper.toEntity(userRequest);

        eligibilityRuleService.verifyUserEligibility(user);

        User userSaved = userRepository.save(user);

        return userMapper.toResponse(userSaved);
    }

}

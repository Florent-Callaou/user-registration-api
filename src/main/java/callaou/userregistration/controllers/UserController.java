package callaou.userregistration.controllers;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import callaou.userregistration.logging.Loggable;
import callaou.userregistration.model.dtos.UserRequest;
import callaou.userregistration.model.dtos.UserResponse;
import callaou.userregistration.services.UserService;
import jakarta.validation.Valid;

/**
 * Controller for User endpoints
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * The UserService
     */
    private final UserService userService;

    /**
     * Inject the different components
     * 
     * @param userService the UserService
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     * 
     * @param userRequest the user info
     * @return the user info after creation
     */
    @PostMapping
    @Loggable(logArgs = true, logResult = true)
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse response = userService.registerUser(userRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a page of users given filters
     * 
     * @param pageable the pageblae informations
     * @param filters  the filters
     * @return the page of users found
     */
    @GetMapping
    @Loggable(logArgs = true, logResult = true)
    public ResponseEntity<Page<UserResponse>> getUsersByCriteria(
            @PageableDefault(sort = "username") Pageable pageable,
            @RequestParam Map<String, Object> filters) {
        filters.remove("page");
        filters.remove("size");
        filters.remove("sort");

        Page<UserResponse> pageResponse = userService.findUsersByCriteria(pageable, filters);

        return ResponseEntity.ok().body(pageResponse);
    }
}

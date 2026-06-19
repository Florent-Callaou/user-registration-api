package callaou.userregistration.repositories;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import callaou.userregistration.model.entities.User;
import callaou.userregistration.model.enumerations.Country;

/**
 * Repository interface for managing User entities.
 * Extends JpaRepository to provide CRUD operations and additional query
 * methods.
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByUsernameAndBirthdateAndCountryOfResidence(String username, LocalDate birthdate, Country country);
}
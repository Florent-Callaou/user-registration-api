package callaou.userregistration.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import callaou.userregistration.model.entities.User;

/**
 * Repository interface for managing User entities.
 * Extends JpaRepository to provide CRUD operations and additional query
 * methods.
 */
public interface UserRepository extends JpaRepository<User, Long> {

}
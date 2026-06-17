package callaou.userregistration.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import callaou.userregistration.model.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
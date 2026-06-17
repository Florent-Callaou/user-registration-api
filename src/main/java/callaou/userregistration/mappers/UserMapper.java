package callaou.userregistration.mappers;

import callaou.userregistration.model.dtos.UserRequest;
import callaou.userregistration.model.dtos.UserResponse;
import callaou.userregistration.model.entities.User;

/**
 * Mapper for converting between User entities and DTOs.
 * 
 * @param <User>         the User entity type
 * @param <UserRequest>  the UserRequest DTO type
 * @param <UserResponse> the UserResponse DTO type
 */
public class UserMapper implements BaseMapper<User, UserRequest, UserResponse> {

    /**
     * Maps a UserRequest DTO to a User entity.
     * 
     * @param request the UserRequest DTO to be mapped
     * @return the mapped User entity
     */
    @Override
    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setUsername(request.username());
        user.setBirthdate(request.birthdate());
        user.setCountryOfResidence(request.countryOfResidence());
        user.setPhoneNumber(request.phoneNumber());
        user.setGender(request.gender());
        return user;
    }

    /**
     * Maps a User entity to a UserResponse DTO.
     * 
     * @param entity the User entity to be mapped
     * @return the mapped UserResponse DTO
     */
    @Override
    public UserResponse toResponse(User entity) {
        if (entity == null) {
            return null;
        }
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getBirthdate(),
                entity.getCountryOfResidence(),
                entity.getPhoneNumber(),
                entity.getGender());
    }
}

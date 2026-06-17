package callaou.userregistration.mappers;

/**
 * Base interface for mappers between entities and data transfer objects.
 * 
 * @param <E> Entity type
 * @param <R> Request DTO type
 * @param <S> Response DTO type
 */
public interface BaseMapper<E, R, S> {
    /**
     * Maps a request DTO to an entity.
     * 
     * @param request The request DTO to be mapped
     * @return The mapped entity
     */
    E toEntity(R request);

    /**
     * Maps an entity to a response DTO.
     * 
     * @param entity The entity to be mapped
     * @return The mapped response DTO
     */
    S toResponse(E entity);
}

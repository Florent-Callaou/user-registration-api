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
     * @param request the request DTO to be mapped
     * @return the mapped entity
     */
    E toEntity(R request);

    /**
     * Maps an entity to a response DTO.
     * 
     * @param entity the entity to be mapped
     * @return the mapped response DTO
     */
    S toResponse(E entity);
}

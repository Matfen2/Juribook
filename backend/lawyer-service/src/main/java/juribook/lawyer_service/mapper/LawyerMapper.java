package juribook.lawyer_service.mapper;

import juribook.lawyer_service.dto.response.LawyerResponse;
import juribook.lawyer_service.entity.Lawyer;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct : convertit l'entité Lawyer en DTO LawyerResponse.
 *
 * @Mapper(componentModel = "spring") : MapStruct génère LawyerMapperImpl
 * à la compilation, annotée @Component, injectable dans LawyerService.
 *
 * Aucune annotation @Mapping nécessaire ici : tous les champs de
 * LawyerResponse (id, userId, barNumber, speciality, city, status)
 * portent exactement les mêmes noms que dans Lawyer -> mapping automatique.
 */
@Mapper(componentModel = "spring")
public interface LawyerMapper {
    LawyerResponse toResponse(Lawyer entity);
}
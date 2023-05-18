package se.magnus.microservices.core.recommendation.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mappings({
            @Mapping(target = "rate", source="entity.rating"), // 매핑하는 필드 값이 다를 경우, 매핑할 객체에서 값을 꺼내서 사용한다.
            @Mapping(target = "serviceAddress", ignore = true)})
    Recommendation entityToApi(RecommendationEntity entity);

    @Mappings({
            @Mapping(target = "rating", source="api.rate"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)})
    RecommendationEntity apiToEntity(Recommendation api);

    /** 매핑 메서드를 만들고 컬렉션으로 반환 하는 것도 가능 */
    List<Recommendation> entityListToApiList(List<RecommendationEntity> entity);
    List<RecommendationEntity> apiListToEntityList(List<Recommendation> api);
}


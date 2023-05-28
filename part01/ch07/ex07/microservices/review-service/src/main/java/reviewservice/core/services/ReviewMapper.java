package reviewservice.core.services;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import reviewservice.core.persistence.ReviewEntity;
import se.magnus.util.core.review.Review;

import java.util.List;

/**
 * MapStruct 를 사용하면 스프링 데이터 엔티티 객체와 API 모델 클래스를 쉽게 상호 변환할 수 있다.
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Review entityToApi(ReviewEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    ReviewEntity apiToEntity(Review api);

    List<Review> entityListToApiList(List<ReviewEntity> entity);
    List<ReviewEntity> apiListToEntityList(List<Review> api);
}

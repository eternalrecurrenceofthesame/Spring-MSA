package se.magnus.microservices.core.recommendation.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {

    /**
     * 값이 0 ~ 다수일 경우 리스트 컬렉션으로 값을 받는다.
     */
    List<RecommendationEntity> findByProductId(int productId);
}

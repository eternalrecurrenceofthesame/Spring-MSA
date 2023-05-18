package se.magnus.microservices.core.review.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ReviewRepository extends CrudRepository<ReviewEntity, Integer> {

    @Transactional(readOnly = true) // 트랜잭션 유형을 읽기 전용으로 지정한다.
    List<ReviewEntity> findByProductId(int productId);
}

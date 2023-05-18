package se.magnus.microservices.core.product.persistence;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, String> {// 페이징과 소팅 기능을 제공하는 스프링 데이터

    /**
     * 값이 0 개 또는 1 개일 경우 Optional 을 사용하면 null 체크를 편하게 할 수 있다.
     */
    Optional<ProductEntity> findByProductId(int productId);
}

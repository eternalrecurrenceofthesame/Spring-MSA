package se.magnus.microservices.core.product.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.product.Product;
import se.magnus.microservices.core.product.persistence.ProductEntity;

/**
 * 컴포넌트 모델을 spring 으로 설정하면 런타임에 매퍼를 빈으로 주입받아서 사용할 수 있다.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * dto -> entity
     *
     * Product 엔티티에는 서비스 어드레스 값이 필요 없으므로 매핑을 무시하도록 설계한다.
     */
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Product entityToApi(ProductEntity entity);

    /**
     * entity -> dto
     *
     * 아이디와 버전 값은 데이터베이스가 직접 생성하기 때문에 api DTO 로 매핑시 매핑을 무시한다.
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    ProductEntity apiToEntity(Product api);
}

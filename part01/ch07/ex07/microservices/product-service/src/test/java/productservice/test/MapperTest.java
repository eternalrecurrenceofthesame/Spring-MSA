package productservice.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import productservice.core.persistence.ProductEntity;
import productservice.core.services.ProductMapper;
import se.magnus.util.core.product.Product;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTest {

    // 매퍼 꺼내기
    private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests(){

        // 매퍼 null 체크
        assertNotNull(mapper);

        Product api = new Product(1, "n", 1, "sa");

        // apiToEntity 테스트
        ProductEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(),entity.getProductId());
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getWeight(), entity.getWeight());

        // entityToApi 테스트
        Product api2 = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(),      api2.getName());
        assertEquals(api.getWeight(),    api2.getWeight());
        assertNull(api2.getServiceAddress());

    }
}

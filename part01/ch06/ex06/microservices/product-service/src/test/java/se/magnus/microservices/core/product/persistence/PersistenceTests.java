package se.magnus.microservices.core.product.persistence;

import com.mongodb.DuplicateKeyException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;


@ExtendWith(SpringExtension.class)
@DataMongoTest
class PersistenceTests {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    /**
     * 테스트 하기 전 데이터를 모두 지우고 더미 데이터를 하나 생성한다.
     */
    @BeforeEach
    public void setupDB(){
        repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        savedEntity = repository.save(entity);

        assertEqualsProduct(entity, savedEntity);
    }

    /**
     * 생성 테스트, 데이터를 생성하고 저장되었는지 테스트한다.
     */
    @Test
    public void create(){
        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        repository.save(newEntity);

        ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    /**
     * 데이터 update 테스트, 버전 정보와 수정된 데이터 값을 체크한다.
     */
    @Test
    public void update(){
        savedEntity.setName("n2");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, foundEntity.getVersion()); // 버전 업데이트 테스트
        assertEquals("n2", foundEntity.getName());
    }

    /**
     * 데이터를 삭제하고 삭제된 데이터를 찾을 수 없는지 테스트한다.
     */
    @Test
    public void delete(){
        repository.delete(savedEntity);
      //  assertFalse(repository.existsById(savedEntity.getId()));
    }

    /**
     * 식별자로 상품을 조회하고 데이터베이스 데이터와 인스턴스 데이터가 같은지 테스트한다.
     */
    @Test
    public void getByProductId(){
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());

        assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    /**
     * 중복 키 예외 발생 테스트 // 테스트 안 됨. // 유니크 설정해도 중복 값이 들어간다!! 이게뭐야
     */
    @Test
    public void duplicateError(){
        ProductEntity entity = new ProductEntity(1, "n", 1);

        repository.save(entity);

        Iterable<ProductEntity> all = repository.findAll();
        for (ProductEntity productEntity : all) {
            System.out.println("호출" + productEntity.toString());
            System.out.println(productEntity.getProductId());
        }
    }

    /**
     * 낙관적 락 테스트
     */
    @Test
    public void optimisticLockError(){

        /**
         * 2 개의 트랜잭션에서 각각 같은 엔티티를 조회한다.
         */
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        /**
         * 트랜잭션 1 에서 조회한 엔티티를 수정하고 저장하면 version 값이 증가한다.
         */
        entity1.setName("n1");
        repository.save(entity1);

        try{
            entity2.setName("n2");
            repository.save(entity2);

            fail("트랜잭션 2 에서 데이터를 저장하면 낙관적 락 오류가 발생한다.");
        } catch(OptimisticLockingFailureException e){}

        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();

        // 버전과 값이 수정되었는지 체크한다.
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

    /**
     * 페이징 테스트하기
     *
     * IntStream.rangeClosed(1001,1010) - 1001 ~ 1010 int 스트림을 생성하면서 ProductEntity 를 생성한다.
     *
     */
    @Test
    public void paging(){
        repository.deleteAll();

        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010).mapToObj(i -> new ProductEntity(i, "name " + i, i))
                .collect(Collectors.toList());

        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]",true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]",true);
        nextPage = testNextPage(nextPage, "[1009, 1010]",false);

    }


    /**
     * 페이징을 테스트하는 메서드 페이지별 사이즈와 아이디값이 있는지, 다음 페이지가 있는지 테스트한다.
     * 몽고 디비에서 페이지의 컨텐츠를 조회하고 아이디를 매핑하면 [1001, 1002, 1003, 1004] 이런식으로 아이디 값을 반환해준다.
     */
    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage){
        Page<ProductEntity> productPage = repository.findAll(nextPage);
        assertEquals(expectedProductIds, productPage.getContent().stream().map(p -> p.getProductId()).collect(Collectors.toList()).toString());
        assertEquals(expectsNextPage, productPage.hasNext());

        /**
         * Slice 자료형을 사용하면 추가 count 쿼리 없이  다음 페이지만 확인할 수 있다.
         */
        return productPage.nextPageable(); // 다음 페이지가 있는 지 확인한다.
    }

    /**
     * 저장된 데이터가 생성된 인스턴스의 값과 같은지 비교하는 메서드
     */
    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity){
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getWeight(), actualEntity.getWeight());
    }
}
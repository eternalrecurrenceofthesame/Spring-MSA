package productservice.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import productservice.core.persistence.ProductEntity;
import productservice.core.persistence.ProductRepository;
import reactor.test.StepVerifier;


//@ExtendWith(SpringExtension.class) 안적어줘도 된다.
@DataMongoTest(properties = {"spring.data.mongodb.auto-index-creation: true"})
public class ProductPersistenceTests extends MongoDbTestBase{

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    /**
     * 리액티브 타입을 테스트 할 때는 StepVerifier 헬퍼 클래스를 사용해서 검증 가능한 비동기 이벤트 시퀀스를 선언하다.
     * (이벤트 스트림을 구현한다는 의미 같음 259 p )
     *
     *  이벤트 시퀀스 선언 후 verifyComplete() 를 호출하면 검증 시퀀스가 시작된다.
     */
    @BeforeEach
    public void setupDb(){
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return areProductEqual(entity, savedEntity);
                })
                .verifyComplete();
    }

    @DisplayName("아이디로 상품 조회")
    @Test
    public void getByProductId(){
        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
                .verifyComplete();
    }

    @DisplayName("상품 업데이트 테스트")
    @Test
    public void update(){
        savedEntity.setName("n2");
        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity ->
                        foundEntity.getVersion() == 1 &&
                        foundEntity.getName().equals("n2"))
                .verifyComplete();
    }

    @DisplayName("상품 삭제 테스트")
    @Test
    public void delete(){
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @DisplayName("중복 키 에러 테스트")
    @Test
    public void duplicateError(){
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify(); // 오류체크시 verify 를 사용하는듯?
    }

    @DisplayName("낙관적 락 테스트하기")
    @Test
    public void optimisticLockError(){

        // 같은 엔티티 두 개를 조회 후 리액티브 스트림을 block 해서 같은 엔티티를 반환받는다.
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        // block 상태에서 트랜잭션 1 이 엔티티를 업데이트한다.
        entity1.setName("n1");
        repository.save(entity1).block();

        // 버전에는 @Version 기본 애노테이션이 있기 떄문에 낙관적 락이 적용된다.
        // 트랜잭션 1 이 엔티티를 수정해서 버전 정보가 수정되었기 때문에 트랜잭션 2는 엔티티를 수정할 수 없다.
        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

        // 버전은 0 부터 시작한다. 수정된 값을 확인한다.
        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity ->
                        foundEntity.getVersion() == 1 &&
                        foundEntity.getName().equals("n1"))
                .verifyComplete();
    }


    /**
     * 생성된 엔티티가 예상 엔티티와 동일한지 체크하는 편의 메서드
     */
    private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity){
        return
                (expectedEntity.getId().equals(actualEntity.getId())) &&
                        (expectedEntity.getVersion() == actualEntity.getVersion()) &&
                        (expectedEntity.getProductId() == actualEntity.getProductId()) &&
                        (expectedEntity.getName().equals(actualEntity.getName())) &&
                        (expectedEntity.getWeight() == actualEntity.getWeight());
    }

}

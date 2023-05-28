package reviewservice.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reviewservice.core.persistence.ReviewEntity;
import reviewservice.core.persistence.ReviewRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED) // 트랜잭션 합류 x 새 트랜잭션을 사용
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersistenceTests extends MySqlTestBase{

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @BeforeEach
    public void setupDb(){
        repository.deleteAll();

        ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
        savedEntity = repository.save(entity);

        assertEqualsReview(entity, savedEntity);
    }

    /**
     * 리뷰 생성
     */
    @Test
    void create(){

        ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c");
        repository.save(newEntity);

        ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsReview(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }
    /**
     * 리뷰 수정 테스트
     */
    @Test
    public void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    /**
     * 리뷰 삭제
     */
    @Test
    void delete(){
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }


    /**
     * 상품 아이디로 리뷰 조회
     */
    @Test
    public void getByProductId(){
        List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertThat(entityList, hasSize(1));
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    /**
     * 중복 키 저장 오류
     * DuplicationKeyException 보다 데이터 인티그리티 바이올레이션 익셉션을 사용하는 것을 권장한다.
     *
     */
    @Test
    void duplicateError(){
        assertThrows(DataIntegrityViolationException.class, () -> {
            ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
            repository.save(entity);
        });
    }

    /**
     * 낙관 락 테스트
     */
    @Test
    void optimisticLockError(){

        // 같은 엔티티 조회
        ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

        // 트랜잭션 1 에서 수정한 후 먼저 저장하고 버전 하나 증가
        entity1.setAuthor("a1");
        repository.save(entity1);

        // 저장시 오류 터짐
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("a2");
            repository.save(entity2);
        });

        // 업데이트 버전체크
        ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }



    /**
     * 편의 메서드
     */
    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(),        actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),   actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getReviewId(),  actualEntity.getReviewId());
        assertEquals(expectedEntity.getAuthor(),    actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(),   actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(),   actualEntity.getContent());
    }
}

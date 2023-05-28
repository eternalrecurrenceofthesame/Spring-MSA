package reviewservice.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reviewservice.core.persistence.ReviewEntity;
import reviewservice.core.persistence.ReviewRepository;
import se.magnus.util.core.review.Review;
import se.magnus.util.core.review.ReviewService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;

import static java.util.logging.Level.FINE;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    private final ServiceUtil serviceUtil;

    /**
     * 스케줄러란? 일정 수의 스레드를 보유한 전용 스레드 풀의 스레드에서 블로킹 코드를 실행하는 브로커
     * 부트 스트랩 클래스에서 주입받는 형태로 설계했다.
     */
    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewServiceImpl(@Qualifier("jdbcScheduler") Scheduler scheduler, ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil) {
        this.jdbcScheduler = scheduler; // 퀄리파이어를 사용해서 jdbcScheduler 를 주입받는다.
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }



    /**
     * 상품 아이디로 리뷰 일괄 조회
     */
    @Override
    public Flux<Review> getReviews(int productId) {
        if(productId < 1)
            throw new InvalidInputException("Invalid productId: " + productId);

        LOG.info("Will get reviews for product with id={}", productId);

        /**
         * 조회한 리뷰 엔티티들을 스케줄러 스레드 풀에서 flux 타입으로 변환하는 작업을 수행한다.
         *
         * flatMapMany 를 사용해서 방출되는 mono 들을 flux 로 변환한다.
         * flatMap 과 subscribeOn 을 함께 사용하면 리액터 타입 변환을 비동기적으로 수행할 수 있다.
         */
        return Mono.fromCallable(() -> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log(LOG.getName(), FINE)
                .subscribeOn(jdbcScheduler);}

    private List<Review> internalGetReviews(int productId){
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("Response size: {}", list.size());

        return list;
    }

    /**
     * 리뷰 생성
     */
    @Override
    public Mono<Review> createReview(Review body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }

        return Mono.fromCallable(() -> internalCreateReview(body)) // Create a Mono producing its value using the provided Callable
                .subscribeOn(jdbcScheduler);
    }

    private Review internalCreateReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }
    /**
     * 리뷰 삭제
     */
    @Override
    public Mono<Void> deleteReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return Mono.fromRunnable(() -> internalDeleteReviews(productId))
                .subscribeOn(jdbcScheduler).then(); // 수동 구독시 반환 타입이 void 의 경우 then 을 붙여준다.
    }

    private void internalDeleteReviews(int productId) {

        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);

        repository.deleteAll(repository.findByProductId(productId));
    }
}

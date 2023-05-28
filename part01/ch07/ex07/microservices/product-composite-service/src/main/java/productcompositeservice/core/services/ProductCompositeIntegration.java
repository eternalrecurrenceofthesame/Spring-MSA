package productcompositeservice.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.util.core.product.Product;
import se.magnus.util.core.product.ProductService;
import se.magnus.util.core.recommendation.Recommendation;
import se.magnus.util.core.recommendation.RecommendationService;
import se.magnus.util.core.review.Review;
import se.magnus.util.core.review.ReviewService;
import se.magnus.util.event.Event;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;

import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;
import static se.magnus.util.event.Event.Type.CREATE;
import static se.magnus.util.event.Event.Type.DELETE;

/**
 * 핵심 마이크로서비스로의 발신 요청을 처리하는 통합 컴포넌트 클래스
 */
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";
    private final StreamBridge streamBridge; // A class which allows user to send data to an output(출력) binding
    private final Scheduler publishEventScheduler;

    @Autowired
    public ProductCompositeIntegration(
            @Qualifier("publishEventScheduler")Scheduler publishEvnetScheduler, // 직접 생성한 스케줄러 빈을 주입받아서 사용한다.

            WebClient.Builder webClient, //Non-blocking, reactive client to perform HTTP requests
            ObjectMapper mapper,
            StreamBridge streamBridge

          //  @Value("${app.product-service.host}") String productServiceHost,
          //  @Value("${app.product-service.port}") String productServicePort,

          //  @Value("${app.recommendation-service.host}") String recommendationServiceHost,
          //  @Value("${app.recommendation-service.port}") int  recommendationServicePort,

          //  @Value("${app.review-service.host}") String reviewServiceHost,
          //  @Value("${app.review-service.port}") int  reviewServicePort
    ) {

        this.publishEventScheduler = publishEvnetScheduler;
        this.webClient = webClient.build(); // webClient 빈 초기화
        this.mapper = mapper;
        this.streamBridge = streamBridge;

    }


    /**
     * 복합 마이크로서비스는 핵심 마이크로서비스 컴포넌트를 호출한다. (여러 조직이 공통 메시징 시스템을 공유할 수없다 ??)
     * 그렇기 때문에 동기 API 로 개발한다.
     *
     * 핵심 마이크로서비스의 생성과 조회는 이벤트 기반의 비동기 방식을 사용하므로 이벤트를 생성하고 리액티브 스트림 타입으로
     * 메시지에 값을 추가한다.
     *
     * 스케줄러를 사용해서 동기 방식으로 메시지를 생성할 때 메시지가 블로킹 상태로 방치되는 것을 방지하기 위해
     * 스케줄러를 사용해서 스레드풀에서 스레드를 할당받아서 사용한다.
     * (마이크로서비스의 논블로킹 처리에 영향을 주지 않기 위함)
     *
     */
    @Override
    public Mono<Product> getProduct(int productId) {
        String url = PRODUCT_SERVICE_URL + "/product/" + productId;
        LOG.debug("Will call the getProduct API on URL: {}", url);

        return webClient.get().uri(url).retrieve().bodyToMono(Product.class).log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    /**
     * Callable 을 사용해서 스레드를 생성하면 결괏값을 받을 수 있고 체크드 익셉션을 날릴 수 있다.
     */
    @Override
    public Mono<Product> createProduct(Product body) {
        return Mono.fromCallable(() -> { // Callable 을 사용해서 스레드를 할당하고 리액티브 스트림 모노를 생성한다.
            sendMessage("products-out-0", new Event(CREATE, body.getProductId(), body)); // 메시지  생산

            return body;
        }).subscribeOn(publishEventScheduler); // 구독자를 만들고 Operation 한다.
    }
    @Override
    public Mono<Void> deleteProduct(int productId) {

        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then(); // then 을 사용하면 결과물을 버린다. 한마디로 void
    }

    /**
     * 추천 컴포넌트
     */
    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;

        LOG.debug("Will call the getRecommendations API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class)
                .log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {

        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    /**
     * 조회 컴포넌트
     */
    @Override
    public Flux<Review> getReviews(int productId) {

        String url = REVIEW_SERVICE_URL + "/review?productId=" + productId;

        LOG.debug("Will call the getReviews API on URL: {}", url);

        // 응답을 생성하지 못하면 빈 값을 반환한다.
        return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }
    @Override
    public Mono<Review> createReview(Review body) {

        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }
    @Override
    public Mono<Void> deleteReviews(int productId) {

        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }


    /**
     * 메시지 전송 편의 메서드
     */
    private void sendMessage(String bindingName, Event event){
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);

        Message<Event> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();

        /**
         * 바인딩은 메시지의 생산자(Output Binding) 와 소비자(Input Binding) 을 일컫는다.
         */
        streamBridge.send(bindingName, message);
    }
    private Throwable handleException(Throwable ex){
        if(!(ex instanceof WebClientResponseException)){
            LOG.warn("Got a unexpected error: {} will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;

        switch(HttpStatus.resolve(wcre.getStatusCode().value())){

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre)); // 에러 메세지를 찾고 메시지를 다시 던진다.

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());

                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex){
        try{
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch(IOException ioex){
            return ex.getMessage();
        }
    }

    /**
     * 헬스 체크 메서드
     */
     private Mono<Health> getHealth(String url){
         url += "/actuator/health";
         LOG.debug("Will call the Health API on URL: {}", url); // 헬스 체크할 마이크로서비스 url
         return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                 .map(s -> new Health.Builder().up().build())
                 .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                 .log(LOG.getName(), FINE);
     }

     public Mono<Health> getProductHealth(){
         return getHealth(PRODUCT_SERVICE_URL); // 상품 url 로 헬스 조회 로직을 호출한다.
     }
    public Mono<Health> getRecommendationHealth() {
        return getHealth(RECOMMENDATION_SERVICE_URL);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(REVIEW_SERVICE_URL);
    }


}

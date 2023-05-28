package productcompositeservice.composite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.util.composite.product.ProductAggregate;
import se.magnus.util.composite.product.RecommendationSummary;
import se.magnus.util.composite.product.ReviewSummary;
import se.magnus.util.core.product.Product;
import se.magnus.util.core.recommendation.Recommendation;
import se.magnus.util.core.review.Review;
import se.magnus.util.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static productcompositeservice.composite.IsSameEvent.sameEventExceptCreatedAt;
import static reactor.core.publisher.Mono.just;
import static se.magnus.util.event.Event.Type.CREATE;
import static se.magnus.util.event.Event.Type.DELETE;


@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                      "eureka.client.enabled=false"})
@Import({TestChannelBinderConfiguration.class}) // 테스트 바인더 주입
class MessagingTests {

    private static final Logger LOG = LoggerFactory.getLogger(MessagingTests.class);

    @Autowired
    private WebTestClient client;

    @Autowired
    private OutputDestination target; // 출력 채널 주입


    /**
     * 각 핵심 마이크로서비스 모듈별로 바인더가 있다고 가정하고 테스트 전 바인더와 결합된 채널에서 메시지를 요청한다.
     */
    @BeforeEach
    void setUp(){
        purgeMessages("products");
        purgeMessages("recommendations");
        purgeMessages("reviews");
    }

    @Test
    void createCompositeProduct1(){

        ProductAggregate composite = new ProductAggregate(1, "name", 1, null, null, null); // 핵심 msa 3 개 null
        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // 생성된 메시지 체크 생성된 product 채널에서만 메시지를 가져온다.
        assertEquals(1, productMessages.size());

        // 메시지에 생성된 이벤트와 비교하기 위한 이벤트 객체
        Event<Integer, Product> expectedEvent
                = new Event<>(CREATE, composite.getProductId(), new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));

        /**
         * MatcherAssert.assertThat, Matcher.is 사용,
         *
         * IsSameEventTests 클래스의 편의 메서드를 사용해서 같은 이벤트인지 검증한다. (해당 클래스 참고)
         */
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedEvent)));

        assertEquals(0, recommendationMessages.size());
        assertEquals(0, reviewMessages.size());
    }


    @Test
    void createCompositeProduct2(){

        ProductAggregate composite = new ProductAggregate(1, "name", 1, singletonList(new RecommendationSummary(1, "a", 1, "c")),
                singletonList(new ReviewSummary(1, "a", "s", "c")), null);

        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // product event 예상 테스트
        assertEquals(1, productMessages.size());

        Event<Integer, Product> expectedProductEvent = new Event<>(CREATE, composite.getProductId(), new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));

        // recommendation event expected 테스트
        assertEquals(1, recommendationMessages.size());

        RecommendationSummary rec = composite.getRecommendations().get(0);

        Event<Integer, Recommendation> expectedRecommendationEvent = new Event<>(CREATE, composite.getProductId(),
                new Recommendation(composite.getProductId(), rec.getRecommendationId(), rec.getAuthor(), rec.getRate(), rec.getContent(), null));
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // review event expected 테스트
        assertEquals(1, reviewMessages.size());

        ReviewSummary rev = composite.getReviews().get(0);
        Event<Integer, Product> expectedReviewEvent =
                new Event(CREATE, composite.getProductId(), new Review(composite.getProductId(), rev.getReviewId(), rev.getAuthor(), rev.getSubject(), rev.getContent(), null));
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));

    }

    @Test
    void deleteCompositeProduct() {
        deleteAndVerifyProduct(1, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        assertEquals(1, productMessages.size());

        // product 삭제 테스트
        Event<Integer, Product> expectedProductEvent = new Event(DELETE, 1, null);
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));

        assertEquals(1, recommendationMessages.size());

        // recommendation 삭제 테스트
        Event<Integer, Product> expectedRecommendationEvent = new Event(DELETE, 1, null);
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        assertEquals(1, reviewMessages.size());

        // review 삭제 테스트
        Event<Integer, Product> expectedReviewEvent = new Event(DELETE, 1, null);
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    /**
     * 편의 메서드
     */
    private void purgeMessages(String bindingName){ // purge(제거하다) Messages 메시징 시스템에서 메시지를 소비한다는 의미
        getMessages(bindingName);
    }

    private List<String> getMessages(String bindingName){
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;

        while(anyMoreMessages){
            Message<byte[]> message = getMessage(bindingName);

            if(message == null){
                anyMoreMessages = false;
            } else{
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    private Message<byte[]> getMessage(String bindingName){ // 바인더로 메시지 시스템과 통합한다.

        try{
            return target.receive(0, bindingName);
        }catch(NullPointerException npe){
            /** 빈 메시지를 받게되면 npe 를 던지기 때문에 캐치하고 메시지가 없다는 것을 나타내야 한다.
             *
             * If the messageQueues member variable in the target object contains no queues when the receive method is called, it will cause a NPE to be thrown.
             * So we catch the NPE here and return null to indicate that no messages were found
             */
            LOG.error("getMessage() received a NPE with binding = {}", bindingName);

            return null;
        }
    }
    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus){
        client.post()
                .uri("/product-composite")
                .body(just(compositeProduct),ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus){
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}
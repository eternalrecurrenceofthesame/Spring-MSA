package reviewservice.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reviewservice.core.persistence.ReviewRepository;
import se.magnus.util.core.review.Review;
import se.magnus.util.event.Event;
import se.magnus.util.exceptions.InvalidInputException;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties ={
        "spring.cloud.stream.defaultBinder=rabbit", // 메시징 설정 이건 왜 해준거지
        "logging.level.se.magnus=DEBUG",
        "eureka.client.enabled=false"})
public class ReviewServiceApplicationTests extends MySqlTestBase {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ReviewRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Review>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    /**
     * 아이디로 리뷰 찾기
     */
    @Test
    void getReviewsByProductId() {

        int productId = 1;

        assertEquals(0, repository.findByProductId(productId).size());

        sendCreateReviewEvent(productId, 1);
        sendCreateReviewEvent(productId, 2);
        sendCreateReviewEvent(productId, 3);

        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyReviewsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].reviewId").isEqualTo(3);
    }

    /**
     * 중복 키 오류
     */
    @Test
    void duplicateError() {

        int productId = 1;
        int reviewId = 1;

        assertEquals(0, repository.count());

        sendCreateReviewEvent(productId, reviewId);

        assertEquals(1, repository.count());

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> sendCreateReviewEvent(productId, reviewId),
                "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: 1, Review Id:1", thrown.getMessage());

        assertEquals(1, repository.count());
    }

    /**
     * 리뷰 삭제 테스트
     */
    @Test
    void deleteReviews() {

        int productId = 1;
        int reviewId = 1;

        sendCreateReviewEvent(productId, reviewId);
        assertEquals(1, repository.findByProductId(productId).size());

        sendDeleteReviewEvent(productId);
        assertEquals(0, repository.findByProductId(productId).size());

        sendDeleteReviewEvent(productId);
    }

    /**
     * 400 오류
     */
    // null
    @Test
    void getReviewsMissingParameter() {

        getAndVerifyReviewsByProductId("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
    }
    // type mismatch
    @Test
    void getReviewsInvalidParameter() {

        getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }
    /**
     * 404 NOT FOUND
     */
    @Test
    void getReviewsNotFound() {
        getAndVerifyReviewsByProductId("?productId=213", OK)
                .jsonPath("$.length()").isEqualTo(0);
    }
    /**
     * 유효하지 않은 파라미터
     */
    @Test
    void getReviewsInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }
    /**
     * 편의 메서드
     */
    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
        return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/review" + productIdQuery)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateReviewEvent(int productId, int reviewId) {
        Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
        Event<Integer, Review> event = new Event(Event.Type.CREATE, productId, review);
        messageProcessor.accept(event);
    }

    private void sendDeleteReviewEvent(int productId) {
        Event<Integer, Review> event = new Event(Event.Type.DELETE, productId, null);
        messageProcessor.accept(event);
    }
}

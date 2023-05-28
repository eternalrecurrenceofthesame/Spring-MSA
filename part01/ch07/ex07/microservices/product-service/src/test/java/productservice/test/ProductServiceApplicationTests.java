package productservice.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import productservice.core.persistence.ProductRepository;
import se.magnus.util.core.product.Product;
import se.magnus.util.event.Event;
import se.magnus.util.exceptions.InvalidInputException;


import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@SpringBootTest(webEnvironment = RANDOM_PORT,
        properties = {"spring.data.mongodb.auto-index-creation: true",
                      "eureka.client.enabled=false"}) // 통합테스트시 유레카를 사용하지 않기 떄문에 false 로
public class ProductServiceApplicationTests extends MongoDbTestBase{

    @Autowired
    private WebTestClient client; // api 테스트를 위한 클라이언트

    @Autowired
    private ProductRepository repository;

    /**
     * messageProcessor 빈을 주입해서 사용한다 메시지 프로세서는 토픽의 이벤트 수신을 한다.
     *
     * MessageProcessorConfig 참고
     */
    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Product>> messageProcessor;

    @BeforeEach
    void setUpDb(){
        repository.deleteAll().block();
    }

    /**
     * 아이디로 조회
     */
    @Test
    void getProductById(){

        int productId = 1;

        assertNull(repository.findByProductId(productId).block());
        assertEquals(0, (long)repository.count().block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());
        assertEquals(1, (long)repository.count().block());

        getAndVerifyProduct(productId, HttpStatus.OK)
                .jsonPath("$.productId").isEqualTo(productId);
    }

    /**
     * 중복 키 에러
     */
    @Test
    void duplicateError(){
        int productId = 1;

        assertNull(repository.findByProductId(productId).block());

        sendCreateProductEvent(productId);

        assertNotNull(repository.findByProductId(productId).block());

        InvalidInputException thrown =
                assertThrows(InvalidInputException.class,
                        () -> sendCreateProductEvent(productId),
                "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: " + productId, thrown.getMessage());
    }


    /**
     * 삭제
     */
    @Test
    void deleteProduct() {

        int productId = 1;

        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);
        assertNull(repository.findByProductId(productId).block());

        sendDeleteProductEvent(productId);
    }

    /**
     * 400 BAD_REQUEST
     */
    @Test
    void getProductInvalidParameterString() {

        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    /**
     * 404 Not_Found
     */
    @Test
    void getProductNotFound() {

        int productIdNotFound = 13;
        getAndVerifyProduct(productIdNotFound, HttpStatus.NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }

    /**
     * 유효하지 않은 파라미터 int
     */
    @Test
    void getProductInvalidParameterNegativeValue(){

        int productIdInvalid = -1;

        getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    /**
     * WebTestClient 를 사용해서 API 요청하는 메서드
     */
    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }
    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus){
        return client.get()
                .uri("/product" + productIdPath)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    /**
     * 편의 메서드
     */
    private void sendCreateProductEvent(int productId){
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        Event<Integer, Product> event = new Event(Event.Type.CREATE, productId, product);
        messageProcessor.accept(event);
    }

    private void sendDeleteProductEvent(int productId) {
        Event<Integer, Product> event = new Event(Event.Type.DELETE, productId, null);
        messageProcessor.accept(event);
    }
}

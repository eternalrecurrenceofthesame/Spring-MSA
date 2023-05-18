package se.magnus.microservices.core.product.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
                properties = {"spring.data.mongodb.port: 0",
                              "spring.data.mongodb.auto-index-creation: true"})
public class ProductServiceApplicationTests {

    /**
     * 스프링 리액티브가 제공하는 WebTestClient 를 사용하면 간편하게 api 테스트를 진행할 수 있다.
     */
    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    public void setUpDb(){
        repository.deleteAll();
    }

    /**
     * 상품 조회 테스트
     */
    @Test
    public void getProductById(){
        int productId = 1;

        postAndVerifyProduct(productId, OK);
        assertTrue(repository.findByProductId(productId).isPresent()); // product 생성

        getAndVerifyProduct(productId, OK)
                .jsonPath("$.productId").isEqualTo(productId); // product 조회 후 제이슨 아이디 값 체크
    }

    /**
     * 중복 키 예외 발생 테스트
     */
    @Test
    public void duplicateError(){

        int productId = 1;

        postAndVerifyProduct(productId, OK);
        assertTrue(repository.findByProductId(productId).isPresent());

        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId);

    }


    /**
     * 상품 삭제 테스트
     */
    @Test
    public void deleteProduct(){
        int productId = 1;

        postAndVerifyProduct(productId, OK);
        assertTrue(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId, OK);
        assertFalse(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId, OK);
    }

    /**
     * 유효하지 않은 아이디 테스트
     */
    @Test
    public void getProductInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid); // 오류 메시지 호출시 $.message 를 사용한다.
    }

    /**
     * 404 테스트
     */
    @Test
    public void getProductNotFound() {

        int productIdNotFound = 13;
        getAndVerifyProduct(productIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }

    /**
     * GET Product 검증 로직
     */
    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    /**
     * 응답 바디의 값을 자료형으로 사용하는 POST Product 검증 로직
     */
    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus){
        // 전송 JSON 객체 생성
        Product product = new Product(productId, "Name " + productId, productId, "SA");

        return client.post()
                .uri("/product")// 호출 서비스
                .body(Mono.just(product), Product.class) // 전송할 리액티브 타입 생성 + 데이터
                .accept(APPLICATION_JSON) // 요청 헤더
                .exchange() // 요청 교환
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }
    /**
     * Delete 검증 로직
     */
    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus){
        return client.delete()
                .uri("/product/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody(); // Consume and decode the response body
    }
}

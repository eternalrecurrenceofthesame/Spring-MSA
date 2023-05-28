package se.magnus.util.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface ProductService {

    /**
     * 상품 조회
     *
     * 핵심 마이크로서비스가 제공하는 읽기 서비스는 응답을 기다리는 최종 사용자가 있기 때문에 동기 API 로 개발한다.
     */
    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json")
    Mono<Product> getProduct(@PathVariable int productId);

    Mono<Product> createProduct(Product body);

    Mono<Void> deleteProduct(int productId);

}

package se.magnus.api.core.product;

import org.springframework.web.bind.annotation.*;

public interface ProductService {

    /**
     * curl $HOST:$PORT/product/productId
     */
    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json")
    Product getProduct(@PathVariable int productId);

    /**
     * curl -X POST $HOST:$PORT/product \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123, "name":"product 123", "weight":123}'
     */
    @PostMapping(
            value = "/product",
            consumes = "application/json",
            produces = "application/json")
    Product createProduct(@RequestBody Product body);

    /**
     * curl -X DELETE $HOST:$PORT/product/1
     */
    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable int productId);

}


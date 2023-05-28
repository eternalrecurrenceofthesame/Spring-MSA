package se.magnus.util.composite.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 복합 마이크로서비스가 공개하는 생성, 읽기, 삭제 서비스는 동기 API 를 기반으로 한다.
 */
@Tag(name = "ProductComposite", description = "REST API for composite product information.")
public interface ProductCompositeService {

    /**
     * 상품 조회
     *
     * curl $HOST:$PORT/product-composite/1
     */

   @Operation(
           summary = "${api.product-composite.get-composite-product.description}",
           description = "${api.product-composite.get-composite-product.notes}")
   @ApiResponses(value = {
           @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
           @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
           @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
           @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
   })
   @GetMapping(
            value    = "/product-composite/{productId}",
            produces = "application/json")
    Mono<ProductAggregate> getProduct(@PathVariable int productId);

    /**
     * 상품 생성
     *
     * curl -X POST $HOST:$PORT/product-composite \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123, "name":"product 123", "weight":123}'
     */

    @Operation(
            summary = "${api.product-composite.create-composite-product.description}",
            description = "${api.product-composite.create-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(
            value = "/product-composite",
            consumes = "application/json")
    Mono<Void> createProduct(@RequestBody ProductAggregate body);


    /**
     * 상품 삭제
     *
     * curl -X DELETE $HOST:$PORT/product-composite/1
     */
    @Operation(
            summary = "${api.product-composite.delete-composite-product.description}",
            description = "${api.product-composite.delete-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping(value = "/product-composite/{productId}")
    Mono<Void> deleteProduct(@PathVariable int productId);


}

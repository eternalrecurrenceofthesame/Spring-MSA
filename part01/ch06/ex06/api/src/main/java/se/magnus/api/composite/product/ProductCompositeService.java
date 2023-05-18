package se.magnus.api.composite.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

/**
 * 복합 마이크로서비스를 호출하는 API 서비스로써 복합 마이크로서비스는 핵심 마이크로서비스 별로 각각 데이터를 저장하거나,
 * 필요한 데이터를 복합 상품으로 만들고 값을 반환하는 논리를 구현한다.
 */

/**
 * Api description 추가시 스프링 폭스를 빌드할 때 tag 로 description 을 만들고 가져와서 사용해야 한다. 189 p 바뀜
 */
@Api(tags = "product-composite-service-impl")
public interface ProductCompositeService {

    /**
     * 복합 상품 조회
     *
     * curl $HOST:$PORT/product-composite/1
     */
    @ApiOperation(
            value = "${api.product-composite.get-composite-product.description}",
            notes = "${api.product-composite.get-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "잘못된 형식의 요청입니다, 응답 메시지에서 정보를 확인하세요."),
            @ApiResponse(code = 404, message = "데이터를 찾을 수 없습니다, 아이디가 존재하지 않습니다."),
            @ApiResponse(code = 422, message = "파라미터 요청이 잘못됐습니다, 응답 메시지에서 정보를 확인하세요.")
    })
    @GetMapping(
            value = "/product-composite/{productId}",
            produces = "application/json")
    ProductAggregate getCompositeProduct(@PathVariable int productId);

    /**
     * 복합 상품 생성
     *
     * curl -X POST $HOST:$PORT/product-composite \
     * -H "Content-Type: application/json" --data \
     * '{"productId":123, "name":"product 123", "weight":123}'
     */
    @ApiOperation(
            value = "${api.product-composite.create-composite-product.description}",
            notes = "${api.product-composite.create-composite-proiduct.notes}")
    @ApiResponses(value ={
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message")})
    @PostMapping(
            value = "/product-composite",
            consumes = "application/json")
    void createCompositeProduct(@RequestBody ProductAggregate body);


    /**
     * 복합 상품 삭제
     *
     * curl -X DELETE $HOST:$PORT/product-composite/1
     */
    @ApiOperation(
            value = "${api.product-composite.delete-composite-product.description}",
            notes = "${api.product-composite.delete-composite-product.notes}")
    @ApiResponses(value={
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "UnProcessable entity, input parameters caused the processing to fail. see response message for more information.")
    })
    @DeleteMapping(value = "/product-composite/{productId}")
    void deleteCompositeProduct(@PathVariable int productId);

}

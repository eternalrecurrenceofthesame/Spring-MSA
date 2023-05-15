package se.magnus.api.composite.product;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Api description 추가시 스프링 폭스를 빌드할 때 tag 로 description 을 만들고 가져와서 사용해야 한다. 189 p 바뀜
 */
@Api(tags = "product-composite-service-impl")
public interface ProductCompositeService {

    /**
     * 단일 API 오퍼레이션 문서화
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
    ProductAggregate getProduct(@PathVariable int productId);


}

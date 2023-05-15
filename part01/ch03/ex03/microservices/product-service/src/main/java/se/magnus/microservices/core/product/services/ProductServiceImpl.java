package se.magnus.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;


@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil){
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {

        LOG.debug("/product return the found product for productId={}", productId);

        // 간단한 테스트를 위한 조회 데이터
        if(productId < 1) throw new InvalidInputException("유효하지 않은 아이디 입니다.");
        if(productId == 13) throw new NotFoundException("상품 아이디를 찾을 수 없습니다");

        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }
}

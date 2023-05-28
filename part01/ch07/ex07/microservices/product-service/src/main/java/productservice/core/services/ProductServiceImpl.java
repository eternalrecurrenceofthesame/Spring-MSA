package productservice.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import productservice.core.persistence.ProductEntity;
import productservice.core.persistence.ProductRepository;
import reactor.core.publisher.Mono;
import se.magnus.util.core.product.Product;
import se.magnus.util.core.product.ProductService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.logging.Level;

import static java.util.logging.Level.FINE;

/**
 * 상품 API 구현 클래스
 */
@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;

    private final ProductRepository repository;

    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * 핵심 마이크로서비스에서 조회는 동기 API 를 사용한다. 아이디를 전달받고 API 로 변환해서 반환한다.
     */
    @Override
    public Mono<Product> getProduct(int productId) {

        if(productId < 1){
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.info("Will get product info for id={}", productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(LOG.getName(), FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> setServiceAddress(e));
    }

    /**
     * 핵심 마이크로서비스에서 생성과 삭제는 메시징 시스템을 사용해서 비동기로 처리한다.
     * (참고로 리액티브 스트림 타입을 사용하면 프레임워크에서 적절하게 구독을 수행한다.)
     */
    @Override
    public Mono<Product> createProduct(Product body) {

        if(body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }

        ProductEntity entity = mapper.apiToEntity(body);
        Mono<Product> newEntity = repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap( // Function 을 사용해서 오류를 전환하면 유틸리티 모듈에서 오류를 던진다.
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity;
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if(productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId).log(LOG.getName(), FINE).map(e -> repository.delete(e))
                .flatMap(e -> e); // void 로 변경
    }

    /**
     * 주소값 할당 편의 메서드
     */
    private Product setServiceAddress(Product e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}

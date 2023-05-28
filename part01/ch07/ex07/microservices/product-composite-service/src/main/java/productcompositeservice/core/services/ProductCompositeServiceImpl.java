package productcompositeservice.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.util.composite.product.*;
import se.magnus.util.core.product.Product;
import se.magnus.util.core.recommendation.Recommendation;
import se.magnus.util.core.review.Review;
import se.magnus.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.logging.Level.FINE;

/**
 * 복합 마이크로서비스 API 구현
 */
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }


    /**
     * 복합 마이크로서비스가 공개하는 생성, 읽기, 삭제 서비스는 동기 API 를 기반으로 한다. 복합 마이크로서비스는 웹 및
     * 모바일 플랫폼을 대상으로, 시스템 환경을 운영하는 조직이 아닌 다른 조직의 클라이언트를 주로 상대한다고 가정하기 때문에
     * 동기 API 가 적합하다.
     */
    @Override
    public Mono<ProductAggregate> getProduct(int productId) {

        LOG.info("Will get composite product info for product.id = {}", productId);

        /**
         * 리액티브 스트림을 사용해서 값을 조합할 때는 zip 을 사용한다. 논블로킹 상태로 값을 집어넣기 위함인듯
         *
         * zip 메서드는 다수의 병렬 요청을 처리하며, 처리가 완료되면 하나로 압축한다.
         * (첫 번째 인자로 zip 파일을 생성하고 파라미터 값을 넘겨받는다.)
         */
        return Mono.zip(
                values -> createProductAggregate((Product) values[0], (List<Recommendation>) values[1], (List<Review>) values[2], serviceUtil.getServiceAddress()),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                        .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
                        .log(LOG.getName(), FINE);
    }

    /**
     * onSubscribe(scheduler) 를 사용해서 논블로킹하게 메시지를 리액티브 타입으로 생성하려면 반환 타입이 필요하다. 상품의 생성과 삭제는
     * 반환 값이 API 에서 필요 없으므로 리액티브 스트림 오퍼레이션을 수행한 작업을 비우고 void 로 반환하는 핸들링이 필요하다.
     */
    @Override
    public Mono<Void> createProduct(ProductAggregate body) {
        try{
            List<Mono> monoList = new ArrayList<>();

            LOG.info("Will create a new composite entity for product.id: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            monoList.add(integration.createProduct(product)); // 상품 생성후 추가


            if(body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    monoList.add(integration.createRecommendation(recommendation));
                });
            }

                if (body.getReviews() != null) {
                    body.getReviews().forEach(r -> {
                        Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                        monoList.add(integration.createReview(review));
                    });
                }

                LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());


            /**
             * 반환 값이 없으므로 첫 번째 인자로 zip 파일을 생성할 필요가 없이 반환된 컬렉션을 비워주면 된다.
             */
            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                     .doOnError(ex -> LOG.warn("createCompositeProduct failed: {}", ex.toString()))
                     .then(); // 반환값을 받지않는 then

        } catch(RuntimeException re){
            LOG.warn("createCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        try{
            LOG.info("Will delete a product aggregate for product.id: {}", productId);

            /**
             * 반환 값이 없으므로 집 파일을 생성하지 않고 삭제만 해주면 된다.
             */
           return Mono.zip(
                   r -> "",
                   integration.deleteProduct(productId),
                   integration.deleteRecommendations(productId),
                   integration.deleteReviews(productId))
                   .doOnError(ex -> LOG.warn("delete faild: {}", ex.toString()))
                   .log(LOG.getName(), FINE).then();

        } catch(RuntimeException re){
            LOG.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    /**
     * 복합 상품을 생성하는 편의 메서드
     */
    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress){

        // 1. 상품 정보
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. 추천 정보
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());

        // 3. 리뷰 정보
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
                reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // 4. 마이크로 서비스 주소
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        // 5. 복합 상품으로 조립해서 반환
        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);

    }


}

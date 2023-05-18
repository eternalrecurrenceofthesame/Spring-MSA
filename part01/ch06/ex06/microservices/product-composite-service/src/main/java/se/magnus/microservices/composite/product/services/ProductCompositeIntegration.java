package se.magnus.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

/**
 * 핵심 서비스로의 발신 요청을 처리하는 통합 컴포넌트 클래스
 */
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper; // ??

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,

            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,

            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,

            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort)

    {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        productServiceUrl        = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
    }



    /**
     * 상품 메서드 (생성, 조회, 삭제 요청)
     */
    @Override
    public Product createProduct(Product body) {
        try{
            String url = productServiceUrl;
            LOG.debug("Will post a new product to URL: {}", url);

            Product product = restTemplate.postForObject(url, body, Product.class);
            LOG.debug("Created a product with id: {}", product.getProductId());

            return product;
        } catch(HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Product getProduct(int productId){
         try{
             String url = productServiceUrl + productId;
             LOG.debug("Will can getProduct API on URL: {}", url); // 상품 조회 호출 로그

             Product product = restTemplate.getForObject(url, Product.class);
             LOG.debug("Found a product with id: {}", product.getProductId());

             return product;
         }catch (HttpClientErrorException ex){

             switch(ex.getStatusCode()){

                 case NOT_FOUND :
                 throw new NotFoundException(getErrorMessage(ex));

                 case UNPROCESSABLE_ENTITY :
                     throw new InvalidInputException(getErrorMessage(ex));

                 default:
                     LOG.warn("Got a unexpected HTTP error: {} will rethrow it", ex.getStatusCode());
                     LOG.warn("Error body: {}", ex.getResponseBodyAsString());

                     throw ex;
             }
         }
    }
    @Override
    public void deleteProduct(int productId) {
        try{
            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call the deleteProduct API on URL: {}", url);

            restTemplate.delete(url);
        }catch(HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }


    /**
     * 추천 메서드 (생성, 조회, 삭제)
     */
    @Override
    public Recommendation createRecommendation(Recommendation body) {

        try{
            String url = recommendationServiceUrl;
            LOG.debug("Will post a new recommendation to URL: {}", url);

            Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
            LOG.debug("Created a recommendation with id: {}", recommendation.getProductId());

            return recommendation;
        } catch(HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }

    }
    @Override
    public List<Recommendation> getRecommendations(int productId){
        try{
            String url = recommendationServiceUrl + productId;

            LOG.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations =
                    restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();

            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;
        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>(); // 빈 컬렉션 반환
        }
    }
    @Override
    public void deleteRecommendations(int productId) {
        try{
            String url = recommendationServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the deleteRecommendations API on URL: {}", url);

            restTemplate.delete(url);
        }catch(HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }

    /**
     * 리뷰 메서드 (생성, 조회, 삭제)
     */
    @Override
    public Review createReview(Review body) {
        try{
            String url = reviewServiceUrl;
            LOG.debug("Will post a new review to URL: {}", url);

            Review review = restTemplate.postForObject(url, body, Review.class);
            LOG.debug("Created a review with id: {}", review.getProductId());

            return review;
        }catch(HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }
    @Override
    public List<Review> getReviews(int productId) {

        try {
            String url = reviewServiceUrl + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {}).getBody();

            LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteReviews(int productId) {
        try{
            String url = reviewServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the deleteReviews API on URL: {}", url);

            restTemplate.delete(url);
        }catch(HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
    }

    /**
     * 요청 오류 처리 핸들러
     */
    private RuntimeException handleHttpClientException(HttpClientErrorException ex){
        switch(ex.getStatusCode()){

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(ex));
            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());

                return ex;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex){
        try{
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage(); // ??
        }catch(IOException ioex){
            return ex.getMessage();
        }
    }
}

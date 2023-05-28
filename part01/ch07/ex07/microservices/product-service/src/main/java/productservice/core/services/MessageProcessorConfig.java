package productservice.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.magnus.util.core.product.Product;
import se.magnus.util.core.product.ProductService;
import se.magnus.util.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

import java.util.function.Consumer;

/**
 * 토픽의 이벤트 수신을 위한 메시지 프로세서 자바 설정
 */

@EnableRabbit // 래빗 몽고 조합 사용시 @EnableRabbit 애노테이션을 추가한다.
@Configuration
public class MessageProcessorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final ProductService productService;

    @Autowired
    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 토픽에 게시된 이벤트를 소비하는 컨슈머를 만든다.
     */
    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor(){

        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt()); // 메시지가 만들어진 곳을 로그로 찍는다.

            switch(event.getEventType()) {

                case CREATE:
                    Product product = event.getData();
                    LOG.info("Create product with ID: {}", product.getProductId());

                    /**
                     * API 바디 값을 인자로 전달. 로직 수행후 block() 으로 스트림을 정지한다.
                     * 정지하는 이유는 서비스에서 오류가 발생했을 경우 오류를 던지기 위함이다.  288 p
                     *
                     * block() 를 호출하지 않으면 메시징 시스템이 서비스 구현에서 발생한 오류를
                     * 처리하지 못하므로 이벤트가 대기열로 다시 들어가지 못하고 데드 레터 대기열로 이동한다.
                     *
                     * In case the Mono errors, the original exception is thrown
                     * (wrapped in a RuntimeException if it was a checked exception).
                     */
                    productService.createProduct(product).block();
                    break; // 탈출

                case DELETE:
                    int productId = event.getKey();
                    LOG.info("Delete recommendations with ProductID: {}", productId);
                    productService.deleteProduct(productId).block();
                    break;


                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
            }

        LOG.info("Message processing done!");
    };
    }

}

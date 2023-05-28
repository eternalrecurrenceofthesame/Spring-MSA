package productcompositeservice.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import productcompositeservice.core.services.ProductCompositeIntegration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthCheckConfiguration {

    @Autowired
    ProductCompositeIntegration integration; // 핵심 MSA 컴포넌트 호출 클래스

    @Bean
    ReactiveHealthContributor coreServices(){

        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("product", () -> integration.getProductHealth());
        registry.put("recommendation", () -> integration.getRecommendationHealth());
        registry.put("review", () -> integration.getReviewHealth());

        return CompositeReactiveHealthContributor.fromMap(registry);

    }
}


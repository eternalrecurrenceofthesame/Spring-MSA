package productcompositeservice;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan({"se.magnus", "productcompositeservice"})
public class ProductCompositeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}

	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceApplication.class);

	@Value("${api.common.version}")         String apiVersion;
	@Value("${api.common.title}")           String apiTitle;
	@Value("${api.common.description}")     String apiDescription;
	@Value("${api.common.termsOfService}")  String apiTermsOfService;
	@Value("${api.common.license}")         String apiLicense;
	@Value("${api.common.licenseUrl}")      String apiLicenseUrl;
	@Value("${api.common.externalDocDesc}") String apiExternalDocDesc;
	@Value("${api.common.externalDocUrl}")  String apiExternalDocUrl;
	@Value("${api.common.contact.name}")    String apiContactName;
	@Value("${api.common.contact.url}")     String apiContactUrl;
	@Value("${api.common.contact.email}")   String apiContactEmail;
	/**
	 * $HOST:$PORT/swagger-ui.html
	 *
	 * @return the common OpenAPI documentation
	 */
	@Bean
	public OpenAPI getOpenApiDocumentation(){
		return new OpenAPI()
				.info(new Info().title(apiTitle)
						.description(apiDescription)
						.version(apiVersion)
						.contact(new Contact()
								.name(apiContactName)
								.url(apiContactUrl)
								.email(apiContactEmail))
						.termsOfService(apiTermsOfService)
						.license(new License()
								.name(apiLicense)
								.url(apiLicenseUrl)))
				.externalDocs(new ExternalDocumentation()
						.description(apiExternalDocDesc)
						.url(apiExternalDocUrl));

	}

	/**
	 * Scheduler 에서 사용할 스레드 풀의 사이즈와 큐 사이즈
	 */
	private final Integer threadPoolSize;
	private final Integer taskQueueSize;

	@Autowired
	public ProductCompositeServiceApplication(
			@Value("${app.threadPoolSize:10}") Integer threadPoolSize,
			@Value("${app.taskQueueSize:100}") Integer taskQueueSize)
	{
		this.threadPoolSize = threadPoolSize;
		this.taskQueueSize = taskQueueSize;
	}

	@Bean
	public Scheduler publishEventScheduler(){
		LOG.info("Creates a messagingScheduler with connectionPoolSize ={}", threadPoolSize);
		return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool"); // 긴 스레드 작업에 최적화된 BoundedElastic() 메서드
	}

	/**
	 * @LoadBalaced 애노테이션은 스프링이 WebClient.Builder 빈에 로드 밸런서 관련 필터를 주입하게 한다.
	 */
	@Bean
	@LoadBalanced
	public WebClient.Builder loadBalancedWebClientBuilder(){
		return WebClient.builder();
	}

}

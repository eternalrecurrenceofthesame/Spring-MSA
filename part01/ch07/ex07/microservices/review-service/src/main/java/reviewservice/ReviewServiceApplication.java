package reviewservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@SpringBootApplication
@ComponentScan({"se.magnus", "reviewservice"})
public class ReviewServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);

	private final Integer threadPoolSize;
	private final Integer taskQueueSize;

	/**
	 * @param threadPoolSize 스레드 사이즈
	 * @param taskQueueSize 처리할 수 있는 블로킹 데이터의 수
	 */
	@Autowired
	public ReviewServiceApplication(
			@Value("${app.threadPoolSize:10}") Integer threadPoolSize,
			@Value("${app.taskQueueSize:100}") Integer taskQueueSize)
	{
		this.threadPoolSize = threadPoolSize;
		this.taskQueueSize = taskQueueSize;
	}

	@Bean
	public Scheduler jdbcScheduler(){
		LOG.info("Creates a jdbcScheduler with thread pool size = {}", threadPoolSize);
		return Schedulers.newBoundedElastic(threadPoolSize,taskQueueSize,"jdbc-pool"); // 긴 스레드 작업에 최적화된 BoundedElastic() 메서드
	}


	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);

		/**
		 * 데이터베이스에 연결할 때 로그로 연결된 mysql Uri 를 남긴다.
		 */
		String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
		LOG.info("Connected to MySQL: " + mysqlUri);
	}



}

package nk.springprojects.reactive;

import java.util.concurrent.Executor;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@EnableAsync
@EnableScheduling
public class AppConfiguration {
	
	@Bean
	GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("public-apis")
			.pathsToMatch("/**")
			.build();
	}
	
	@Bean
	OpenAPI CustomOpenApi() {
		return new OpenAPI()
			.info(new Info().title("open spring kafka api").version("v1"));
	}

	@Bean
	Executor TaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("MyExecutor-");
		executor.initialize();
		return executor;
	}
}

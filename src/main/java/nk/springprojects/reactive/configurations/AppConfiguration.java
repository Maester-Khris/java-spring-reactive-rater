package nk.springprojects.reactive.configurations;

import java.util.concurrent.Executor;

import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
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
			.info(new Info().title("SKill Rater Public Rest api").version("v1"));
	}

    @Bean
    public WebFilter loggingFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod().toString();
            String headers = request.getHeaders().toString().replaceAll("(?i)Authorization: .*", "Authorization: [MASKED]");
            log.info("[skillrater] INFO | Incoming request | {} | {} | headers={} ", method, path, headers);
            return chain.filter(exchange)
                    .doOnSuccess(aVoid -> log.debug("[skillrater] DEBUG | Completed handling request | {} | {}", method, path));
        };
    }

//	@Bean
//	Executor TaskExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(5);
//		executor.setMaxPoolSize(10);
//		executor.setQueueCapacity(100);
//		executor.setThreadNamePrefix("MyExecutor-");
//		executor.initialize();
//		return executor;
//	}
}

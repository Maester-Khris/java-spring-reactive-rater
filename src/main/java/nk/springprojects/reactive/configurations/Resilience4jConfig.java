package nk.springprojects.reactive.configurations;

import com.github.jasync.sql.db.exceptions.DatabaseException;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.Retry;
import io.r2dbc.spi.R2dbcTimeoutException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {
    public static final String DB_WRITE_RETRY = "dbWriteRetry";

    @Bean
    public Retry dbWriteRetry(RetryRegistry retryRegistry){
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(interval -> 1000L * interval)
                .retryExceptions(
                        DatabaseException.class,
                        R2dbcTimeoutException.class,
                        java.net.ConnectException.class
                )
                .build();

        return retryRegistry.retry(DB_WRITE_RETRY, retryConfig);
    }
}

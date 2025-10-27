package nk.springprojects.reactive.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class voteExecutor {
    @Bean(name = "voteSimulationExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Sets the number of threads in the pool
        executor.setCorePoolSize(5);
        // Sets the maximum number of threads
        executor.setMaxPoolSize(10);
        // Defines the capacity of the queue for tasks waiting to be executed
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("VoteSim-");
        executor.initialize();
        return executor;
    }
}

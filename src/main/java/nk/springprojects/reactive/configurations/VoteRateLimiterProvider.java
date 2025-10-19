package nk.springprojects.reactive.configurations;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class VoteRateLimiterProvider {
    private final RateLimiter publicVoteLimiter;

    public VoteRateLimiterProvider(RateLimiterRegistry registry) {
        this.publicVoteLimiter = registry.rateLimiter("publicVoteLimiter");
    }
}

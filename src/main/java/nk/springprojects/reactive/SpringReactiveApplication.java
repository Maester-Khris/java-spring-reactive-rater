package nk.springprojects.reactive;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import nk.springprojects.reactive.service.SkillRatingService;
import nk.springprojects.reactive.model.Skill;
import reactor.core.publisher.Mono;

@SpringBootApplication
@OpenAPIDefinition
@EnableCaching
@Slf4j
public class SpringReactiveApplication {
	
	@Value("classpath:data/alliconsv1.json")
	private Resource resource;
	
	record Language(String name, String[] aliases) {}
	record Icon(String name, String icon) {}

    private static final String APP_NAME = "skillrater";

    public static void main(String[] args) {
        log.info("[{}] INFO | Application main starting", APP_NAME);
        SpringApplication.run(SpringReactiveApplication.class, args);
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("[{}] INFO | Application started and ready", APP_NAME);
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        log.info("[{}] INFO | Application stopping", APP_NAME);
    }
}

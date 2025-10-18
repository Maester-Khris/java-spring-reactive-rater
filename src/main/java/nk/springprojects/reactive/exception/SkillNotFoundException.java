package nk.springprojects.reactive.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SkillNotFoundException extends RuntimeException {
	public SkillNotFoundException(String message) {
        super(message);
        log.error("[skillrater] ERROR | SkillNotFoundException");
    }
}

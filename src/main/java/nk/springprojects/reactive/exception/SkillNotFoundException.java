package nk.springprojects.reactive.exception;

public class SkillNotFoundException extends RuntimeException {
	public SkillNotFoundException(String message) {
        super(message);
    }
}

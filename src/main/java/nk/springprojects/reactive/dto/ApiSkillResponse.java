package nk.springprojects.reactive.dto;

import nk.springprojects.reactive.model.SkillDefinition;

public record ApiSkillResponse(String message, SkillDefinition skill) {
}

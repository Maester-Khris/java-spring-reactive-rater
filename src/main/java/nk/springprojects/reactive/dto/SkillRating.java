package nk.springprojects.reactive.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
//@JsonProperty("skillUuid")
public record SkillRating( String skilluuid, int rating, int proficiency) { }
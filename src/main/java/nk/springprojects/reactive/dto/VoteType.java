package nk.springprojects.reactive.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VoteType {
    UPVOTE,
    DOWNVOTE;

    @JsonCreator
    public static VoteType fromString(String value) {
        if (value == null) return null;
        return VoteType.valueOf(value.trim().toUpperCase());
    }
}
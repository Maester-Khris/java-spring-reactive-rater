package nk.springprojects.reactive.model;

import nk.springprojects.reactive.dto.VoteType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="skills")
@ToString
public class Skill implements SkillDefinition{
	
	@Id
	private Integer id;

	private String skillname;
	
	private String skilluuid;
	
	private String skillicon;

    private int upvote;

    private int downvote;

	private int rating;

    @Version
    private int version; // optimistic locking


    public Skill(String skillname, String skilluuid, String skillicon){
        this.skillname = skillname;
        this.skilluuid = skilluuid;
        this.skillicon = skillicon;
    }

    public void updateRating() {
        this.rating = Math.max(this.upvote - this.downvote, 0);
    }

    public void applyVote(VoteType voteType) {
        if (voteType == VoteType.UPVOTE) {
            upvote++;
        } else if (voteType == VoteType.DOWNVOTE) {
            downvote++;
        }
        this.updateRating();
    }

}

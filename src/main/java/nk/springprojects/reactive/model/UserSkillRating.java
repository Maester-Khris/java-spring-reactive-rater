package nk.springprojects.reactive.model;

import org.springframework.data.annotation.Id;
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
@ToString
@Table("user_skill_ratings")
public class UserSkillRating implements SkillDefinition{

	@Id
    private Integer id;
	
    private Integer userid; // Foreign key to User
    
    private Integer skillid; // Foreign key to Skill
    
    private int rating;

    private int proficiency;
}

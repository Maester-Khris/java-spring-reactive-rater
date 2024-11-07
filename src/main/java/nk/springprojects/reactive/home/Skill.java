package nk.springprojects.reactive.home;

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
@Table(name="skills")
@ToString
public class Skill {
	
	@Id
	private Integer id;

	private String skillname;
	
	private String skilluuid;
	
	private int rating;
}

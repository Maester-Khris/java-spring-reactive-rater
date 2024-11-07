package nk.springprojects.reactive.student;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="students")
public class Student {
	
	@Id
	private Integer id;

	private String firstname;
	
	private String lastname;
	
	private int age;
	
}

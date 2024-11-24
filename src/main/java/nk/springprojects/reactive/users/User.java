package nk.springprojects.reactive.users;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nk.springprojects.reactive.student.Student;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name="users")
public class User{

	@Id
	private Integer id;

	private String useruuid;
	
	private String username;
	
	private String password;
	
	private LocalDateTime created_at;
}

package marcostar.project.store_project.dtos.auth;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
	@NotBlank
	private String username;
	@NotBlank
	private String firstname;
	@NotBlank
	private String lastname;
	@NotBlank
	private String email;
	private UUID roleId;
	@NotBlank
	private String password;
}
package marcostar.project.store_project.dtos.auth;

import java.util.Date;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import marcostar.project.store_project.entities.User;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
	private User user;
	private String token;
    private Date expireAt;
}
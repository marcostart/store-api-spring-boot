package marcostar.project.store_project.services;


import java.util.List;
import marcostar.project.store_project.entities.Role;

import marcostar.project.store_project.dtos.auth.AuthRequest;
import marcostar.project.store_project.dtos.auth.AuthResponse;
import marcostar.project.store_project.dtos.auth.RegisterRequest;
import marcostar.project.store_project.dtos.auth.RegisterResponse;

public interface AuthenticationService {
	RegisterResponse register(RegisterRequest request);
	AuthResponse login(AuthRequest request);
	void logout(String token);
	List<Role> getRoles();
}
package marcostar.project.store_project.services.implementations;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.config.security.JwtService;
import marcostar.project.store_project.dtos.auth.AuthRequest;
import marcostar.project.store_project.dtos.auth.AuthResponse;
import marcostar.project.store_project.dtos.auth.RegisterRequest;
import marcostar.project.store_project.dtos.auth.RegisterResponse;
import marcostar.project.store_project.entities.Role;
import marcostar.project.store_project.entities.User;
import marcostar.project.store_project.repositories.RoleRepository;
import marcostar.project.store_project.repositories.UserRepository;
import marcostar.project.store_project.config.security.TokenBlacklistService;
import marcostar.project.store_project.services.AuthenticationService;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final TokenBlacklistService tokenBlacklistService;

	@Override
	public RegisterResponse register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username already in use");
		}
		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new IllegalArgumentException("Role not found"));

		User user = User.builder()
				.username(request.getUsername())
				.firstname(request.getFirstname())
				.lastname(request.getLastname())
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.role(role)
				.build();

		// userRepository.save(user);
		// String token = jwtService.generateToken(user);
		user = userRepository.save(user);
		return RegisterResponse.builder()
				.user(user)
				.message("User registered successfully. Please log in.")
				.build();
	}

	@Override
	public AuthResponse login(AuthRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		if (!authentication.isAuthenticated()) {
			throw new IllegalArgumentException("Invalid username or password");
		}
		User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() 
				-> new UsernameNotFoundException("User not found"));
		String token = jwtService.generateToken(user);

		return new AuthResponse(user, token, jwtService.extractExpiration(token));
	}

	@Override
	public void logout(String token) {
		tokenBlacklistService.blacklist(token);
	}

	@Override
	public List<Role> getRoles() {
		return roleRepository.findAll();
	}
}

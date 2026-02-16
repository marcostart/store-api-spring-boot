package marcostar.project.store_project.services.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import marcostar.project.store_project.config.security.JwtService;
import marcostar.project.store_project.config.security.TokenBlacklistService;
import marcostar.project.store_project.dtos.auth.AuthRequest;
import marcostar.project.store_project.dtos.auth.AuthResponse;
import marcostar.project.store_project.dtos.auth.RegisterRequest;
import marcostar.project.store_project.dtos.auth.RegisterResponse;
import marcostar.project.store_project.entities.Role;
import marcostar.project.store_project.entities.User;
import marcostar.project.store_project.repositories.RoleRepository;
import marcostar.project.store_project.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl Unit Tests")
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User testUser;
    private Role testRole;
    private RegisterRequest testRegisterRequest;
    private AuthRequest testAuthRequest;
    private UUID testUserId;
    private UUID testRoleId;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRoleId = UUID.randomUUID();
        testToken = "test.jwt.token";

        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("USER");

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(testRole)
                .build();

        testRegisterRequest = new RegisterRequest();
        testRegisterRequest.setUsername("newuser");
        testRegisterRequest.setFirstname("New");
        testRegisterRequest.setLastname("User");
        testRegisterRequest.setEmail("new@example.com");
        testRegisterRequest.setPassword("password123");
        testRegisterRequest.setRoleId(testRoleId);

        testAuthRequest = new AuthRequest();
        testAuthRequest.setUsername("testuser");
        testAuthRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterSuccess() {
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(testUserId);
            return user;
        });

        RegisterResponse result = authenticationService.register(testRegisterRequest);

        assertNotNull(result);
        assertNotNull(result.getUser());
        assertEquals("User registered successfully. Please log in.", result.getMessage());
        assertEquals("newuser", result.getUser().getUsername());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(roleRepository, times(1)).findById(testRoleId);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterUsernameAlreadyExists() {
        
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.register(testRegisterRequest)
        );
        assertEquals("Username already in use", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when role not found during registration")
    void testRegisterRoleNotFound() {
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.register(testRegisterRequest)
        );
        assertEquals("Role not found", exception.getMessage());
        verify(roleRepository, times(1)).findById(testRoleId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully and return token")
    void testLoginSuccess() {
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(testToken);
        when(jwtService.extractExpiration(testToken)).thenReturn(Date.from(Instant.now().plusSeconds(3600)));

        AuthResponse result = authenticationService.login(testAuthRequest);

        assertNotNull(result);
        assertEquals(testToken, result.getToken());
        assertNotNull(result.getUser());
        assertEquals("testuser", result.getUser().getUsername());
        assertNotNull(result.getExpireAt());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtService, times(1)).generateToken(testUser);
        verify(jwtService, times(1)).extractExpiration(testToken);
    }

    @Test
    @DisplayName("Should throw exception when authentication fails")
    void testLoginAuthenticationFailed() {
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.login(testAuthRequest)
        );
        assertEquals("Invalid username or password", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void testLoginUserNotFound() {
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authenticationService.login(testAuthRequest)
        );
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should logout successfully by blacklisting token")
    void testLogoutSuccess() {
        
        String token = "valid.jwt.token";

        
        authenticationService.logout(token);

        
        verify(tokenBlacklistService, times(1)).blacklist(token);
    }

    @Test
    @DisplayName("Should return all roles")
    void testGetRoles() {
        
        Role role1 = new Role();
        role1.setId(UUID.randomUUID());
        role1.setName("USER");
        Role role2 = new Role();
        role2.setId(UUID.randomUUID());
        role2.setName("ADMIN");
        List<Role> roles = Arrays.asList(role1, role2);
        when(roleRepository.findAll()).thenReturn(roles);

        
        List<Role> result = authenticationService.getRoles();

        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("USER", result.get(0).getName());
        assertEquals("ADMIN", result.get(1).getName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should encode password during registration")
    void testPasswordEncoding() {
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPasswordHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(testUserId);
            return user;
        });

        
        RegisterResponse result = authenticationService.register(testRegisterRequest);

        
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getPassword().equals("$2a$10$encodedPasswordHash")
        ));
    }

    @Test
    @DisplayName("Should create user with correct role during registration")
    void testRegisterWithCorrectRole() {
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(testUserId);
            return user;
        });

        
        RegisterResponse result = authenticationService.register(testRegisterRequest);

        
        assertNotNull(result);
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getRole().equals(testRole) && user.getRole().getName().equals("USER")
        ));
    }

    @Test
    @DisplayName("Should use authentication manager for login verification")
    void testLoginUsesAuthenticationManager() {
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(testToken);
        when(jwtService.extractExpiration(testToken)).thenReturn(Date.from(Instant.now().plusSeconds(3600)));

        
        AuthResponse result = authenticationService.login(testAuthRequest);

        
        assertNotNull(result);
        verify(authenticationManager, times(1)).authenticate(argThat(token -> 
            token instanceof UsernamePasswordAuthenticationToken &&
            token.getPrincipal().equals("testuser") &&
            token.getCredentials().equals("password123")
        ));
    }

    @Test
    @DisplayName("Should generate JWT token with user details")
    void testLoginGeneratesTokenWithUserDetails() {
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(testToken);
        Date expirationDate = Date.from(Instant.now().plusSeconds(3600));
        when(jwtService.extractExpiration(testToken)).thenReturn(expirationDate);

        
        AuthResponse result = authenticationService.login(testAuthRequest);

        
        assertNotNull(result);
        assertEquals(testToken, result.getToken());
        assertEquals(expirationDate, result.getExpireAt());
        verify(jwtService, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Should handle registration with all user fields")
    void testRegisterWithAllFields() {
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(testUserId);
            return user;
        });

        
        RegisterResponse result = authenticationService.register(testRegisterRequest);

        
        assertNotNull(result);
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getUsername().equals("newuser") &&
            user.getFirstname().equals("New") &&
            user.getLastname().equals("User") &&
            user.getEmail().equals("new@example.com") &&
            user.getPassword().equals("encodedPassword") &&
            user.getRole().equals(testRole)
        ));
    }
}

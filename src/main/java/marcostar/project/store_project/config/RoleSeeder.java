package marcostar.project.store_project.config;

import java.util.EnumMap;
import java.util.Map;
import marcostar.project.store_project.entities.Role;
import marcostar.project.store_project.entities.enums.TypeRole;
import marcostar.project.store_project.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder implements CommandLineRunner {

	private final RoleRepository roleRepository;

	public RoleSeeder(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	public void run(String... args) {
		Map<TypeRole, String> descriptions = new EnumMap<>(TypeRole.class);
		descriptions.put(TypeRole.USER, "Default user role");
		descriptions.put(TypeRole.ADMIN, "Administrator role");
		descriptions.put(TypeRole.SUPER_ADMIN, "Super administrator role");

        if (!roleRepository.existsByTypeRole(TypeRole.USER)) {
            Role role = new Role();
            role.setName("Simple User");
            role.setTypeRole(TypeRole.USER);
            role.setDescription(descriptions.get(TypeRole.USER));
            roleRepository.save(role);
        }
        if (!roleRepository.existsByTypeRole(TypeRole.ADMIN)) {
            Role role = new Role();
            role.setName("Administrator");
            role.setTypeRole(TypeRole.ADMIN);
            role.setDescription(descriptions.get(TypeRole.ADMIN));
            roleRepository.save(role);
        }
        if (!roleRepository.existsByTypeRole(TypeRole.SUPER_ADMIN)) {
            Role role = new Role();
            role.setName("Super Administrator");
            role.setTypeRole(TypeRole.SUPER_ADMIN);
            role.setDescription(descriptions.get(TypeRole.SUPER_ADMIN));
            roleRepository.save(role);
        }
	}
}
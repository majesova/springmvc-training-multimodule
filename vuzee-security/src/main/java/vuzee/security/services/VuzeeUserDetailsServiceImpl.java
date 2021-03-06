package vuzee.security.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vuzee.security.entities.Privilege;
import vuzee.security.entities.Role;
import vuzee.security.entities.User;
import vuzee.security.models.AuthenticatedUser;
import vuzee.security.models.VuzeeUser;
import vuzee.security.repositories.RoleRepository;
import vuzee.security.repositories.UserRepository;

@Service
@Transactional
public class VuzeeUserDetailsServiceImpl implements VuzeeUserDetailsService{

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private RoleRepository roleRepository;
	
	/**
	 * @param userRepository
	 * @param passwordEncoder
	 * @param roleRepository
	 */
	public VuzeeUserDetailsServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
			RoleRepository roleRepository) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUserName(username);
		if (user == null) {
			throw new UsernameNotFoundException("Could not find user " + username);
		}
		Collection<Role> roles = (List<Role>) user.getRoles();
		int numRoles = roles.size();
		List<String> privs = getPrivilegies(roles);
		List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(privs);
		return new AuthenticatedUser(user, grantedAuthorities);
	}

	@Override
	public AuthenticatedUser registerNewAccount(VuzeeUser userDto) throws EmailExistsException {
		if (emailExist(userDto.getEmail())) {
			throw new EmailExistsException("Ya existe una cuenta con el email:" + userDto.getEmail());
		}
		User user = new User();
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setLocked(false);
		user.setUserName(userDto.getEmail());
		user.setEmailConfirmed(true);
		user.setPassword(passwordEncoder.encode(userDto.getPassword()));
		/**Poner un rol adecuado en el sistema**/
		Role role = roleRepository.findOne("ROL_SUP");
		List<Role> roles = new ArrayList<>();
		roles.add(role);
		user.setRoles(roles);
		user.setEmail(userDto.getEmail());
		if (userRepository.save(user) != null) {
			List<String> privs = getPrivilegies(roles);
			List<GrantedAuthority> auths = getGrantedAuthorities(privs);
			AuthenticatedUser authenticatedUser = new AuthenticatedUser(user, auths);
			Authentication auth = new UsernamePasswordAuthenticationToken(authenticatedUser, null,
					auths);
		
			SecurityContextHolder.getContext().setAuthentication(auth);
				
			return authenticatedUser;
		}

		return null;
	}
	
	
	private boolean emailExist(String email) {
		return userRepository.findByEmail(email) != null;
	}
	
	private List<String> getPrivilegies(Collection<Role> roles){
		List<String> privileges = new ArrayList<>();
        List<Privilege> privilegesList = new ArrayList<>();
        for (Role role : roles) {
        	privilegesList.addAll(role.getPrivileges());
        }
        for(Privilege item : privilegesList) {
        	privileges.add(item.getName());
        }
        return privileges;
	}
	
	private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }
	
	private List<GrantedAuthority> getGrantedAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Collection<Role> roles = user.getRoles();
        List<String> privileges = getPrivilegies(roles);
        authorities = getGrantedAuthorities(privileges);
        return authorities;
    }
	
}

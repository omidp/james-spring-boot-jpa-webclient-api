package org.apache.james.webapi.app.security;

import static org.springframework.security.core.userdetails.User.withUsername;

import java.util.Optional;

import org.apache.james.adapter.mailbox.store.UserRepositoryAuthenticator;
import org.apache.james.core.Username;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.api.UsersRepositoryException;
import org.apache.james.user.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Service to associate user with password and roles setup in the database.
 *
 */
@Component
public class JamesUserDetailsService implements UserDetailsService {
	@Autowired
	private UsersRepository userRepository;

	@Autowired
	JwtProvider jwtProvider;

	@Override
	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
		try {
			User user = userRepository.getUserByName(Username.of(s));
			if (user == null)
				throw new UsernameNotFoundException("user not found");
			return withUsername(user.getUserName().asString()).password("").authorities("").accountExpired(false)
					.accountLocked(false).credentialsExpired(false).disabled(false).build();
		} catch (UsersRepositoryException ue) {
			throw new UsernameNotFoundException(ue.getMessage());
		}
	}

	/**
	 * Extract username and roles from a validated jwt string.
	 *
	 * @param jwtToken jwt string
	 * @return UserDetails if valid, Empty otherwise
	 */
	public Optional<UserDetails> loadUserByJwtToken(String jwtToken) {
		if (jwtProvider.isValidToken(jwtToken)) {
			return Optional.of(withUsername(jwtProvider.getUsername(jwtToken))
					.authorities(jwtProvider.getRoles(jwtToken)).password("") // token does not have password but field
																				// may not be empty
					.accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(false).build());
		}
		return Optional.empty();
	}

	/**
	 * Extract the username from the JWT then lookup the user in the database.
	 *
	 * @param jwtToken
	 * @return
	 */
	public Optional<UserDetails> loadUserByJwtTokenAndDatabase(String jwtToken) {
		if (jwtProvider.isValidToken(jwtToken)) {
			return Optional.of(loadUserByUsername(jwtProvider.getUsername(jwtToken)));
		} else {
			return Optional.empty();
		}
	}

}
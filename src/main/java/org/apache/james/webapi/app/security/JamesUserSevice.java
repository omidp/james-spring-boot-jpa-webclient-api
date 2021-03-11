package org.apache.james.webapi.app.security;

import java.util.Optional;

import org.apache.james.adapter.mailbox.store.UserRepositoryAuthenticator;
import org.apache.james.core.Username;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.user.api.UsersRepositoryException;
import org.apache.james.user.jpa.JPAUsersRepository;
import org.apache.james.webapi.app.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JamesUserSevice {

	@Autowired
	UserRepositoryAuthenticator userRepositoryAuthenticator;

	@Autowired
	AuthenticationProvider authenticationProvider;

	@Autowired
	JwtProvider jwtProvider;

	@Autowired
	JPAUsersRepository jpaUsersRepository;

	public Optional<String> login(String username, String password) {
		Optional<String> token = Optional.empty();
		try {
			boolean authentic = userRepositoryAuthenticator.isAuthentic(Username.of(username), password);

			if (authentic) {
				try {
					authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
					token = Optional.of(jwtProvider.createToken(username));
				} catch (AuthenticationException e) {
					log.info("Log in failed for user {}", username);
				}
			}
		} catch (MailboxException me) {
			log.info("MailboxException {}", me);
		}
		return token;
	}

	public void add(String username, String password) {
		Username uname = Username.of(username);
		try {
			jpaUsersRepository.addUser(uname, password);
		} catch (UsersRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}
}

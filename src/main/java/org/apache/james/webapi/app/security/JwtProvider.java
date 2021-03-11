package org.apache.james.webapi.app.security;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.james.webapi.app.WebApiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Utility Class for common Java Web Token operations
 *
 */
@Component
public class JwtProvider {

	private final String ROLES_KEY = "roles";

	private JwtParser parser;

	private String secretKey;
	private long validityInMilliseconds;

	@Autowired
	public JwtProvider(WebApiConfiguration conf) {

		this.secretKey = Base64.getEncoder().encodeToString(conf.getTokenSecretKey().getBytes());
//    	this.secretKey = new String(Base64.encode(conf.getTokenSecretKey().getBytes()));
		this.validityInMilliseconds = conf.getTokenValidity();
	}

	/**
	 * Create JWT string given username and roles.
	 *
	 * @param username
	 * @param roles
	 * @return jwt string
	 */
	public String createToken(String username) {
		Claims claims = Jwts.claims().setSubject(username);
		claims.put(ROLES_KEY, Arrays.asList(new SimpleGrantedAuthority("ROLE_API")));
		Date now = new Date();
		Date expiresAt = new Date(now.getTime() + validityInMilliseconds);
		return Jwts.builder().setClaims(claims).setIssuedAt(now).setExpiration(expiresAt)
				.signWith(SignatureAlgorithm.HS256, secretKey).compact();
	}

	/**
	 * Validate the JWT String
	 *
	 * @param token JWT string
	 * @return true if valid, false otherwise
	 */
	public boolean isValidToken(String token) {
		try {
			Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Get the username from the token string
	 *
	 * @param token jwt
	 * @return username
	 */
	public String getUsername(String token) {
		return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
	}

	/**
	 * Get the roles from the token string
	 *
	 * @param token jwt
	 * @return username
	 */
	public List<GrantedAuthority> getRoles(String token) {
		List<Map<String, String>> roleClaims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody()
				.get(ROLES_KEY, List.class);
		return roleClaims.stream().map(roleClaim -> new SimpleGrantedAuthority(roleClaim.get("authority")))
				.collect(Collectors.toList());
	}
}
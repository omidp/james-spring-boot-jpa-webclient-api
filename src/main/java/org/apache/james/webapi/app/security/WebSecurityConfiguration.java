package org.apache.james.webapi.app.security;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.google.common.hash.Hashing;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private JamesUserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Entry points
        http.authorizeRequests()
                .antMatchers("/api/v1/login").permitAll()
                // Disallow everything else..
                .anyRequest().authenticated();

        // Disable CSRF (cross site request forgery)
        http.csrf().disable();

        // No session will be created or used by spring security
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(new JwtTokenFilter(userDetailsService), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Allow swagger to be accessed without authentication
        web.ignoring().antMatchers("/v2/api-docs")//
                .antMatchers("/swagger-resources/**")//
                .antMatchers("/swagger-ui.html")//
                .antMatchers("/configuration/**")//
                .antMatchers("/webjars/**")//
                .antMatchers("/public");
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	auth.authenticationProvider(authenticationProvider()).eraseCredentials(true).userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
    
    @Bean
    AuthenticationProvider authenticationProvider()
    {
    	return new AuthenticationProvider() {
			
			@Override
			public boolean supports(Class<?> authentication) {
				return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
			}
			
			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				if (!supports(authentication.getClass())) {
					return null;
				}
				UsernamePasswordAuthenticationToken preauthtoken = ((UsernamePasswordAuthenticationToken)authentication);
				
				return authentication;
			}
		};
    }

    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
			
			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				if (rawPassword == null) {
					throw new IllegalArgumentException("rawPassword cannot be null");
				}

				if (encodedPassword == null || encodedPassword.length() == 0) {
					return false;
				}
				return Hashing.md5().hashString(rawPassword, StandardCharsets.UTF_8).toString().equals(encodedPassword);
			}
			
			@Override
			public String encode(CharSequence rawPassword) {
				return Hashing.md5().hashString(rawPassword, StandardCharsets.UTF_8).toString();
			}
		};
    }

}
package org.apache.james.webapi.internal;

import java.util.Iterator;
import java.util.Optional;

import org.apache.james.webapi.app.security.JwtProvider;
import org.apache.james.webapi.app.service.InvalidTokenException;
import org.apache.james.webapi.app.service.ServiceException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author Omid Pourhadi
 *
 */
public class TokenInfoArgumentResolver implements HandlerMethodArgumentResolver {

	JwtProvider jwtProvider;

	private static final String BEARER = "Bearer";
	
	public TokenInfoArgumentResolver(JwtProvider jwtProvider) {
		this.jwtProvider = jwtProvider;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return TokenInfo.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		String authorizationHeader = getAuthorizationHeader(webRequest);
		String token = getBearerToken(authorizationHeader).orElseThrow(()->new InvalidTokenException("Invalid token"));
		String username = jwtProvider.getUsername(token);
		return new TokenInfoImpl(username);
	}
	
	private Optional<String> getBearerToken(String headerVal) {
        if (headerVal != null && (headerVal.startsWith(BEARER) || headerVal.startsWith("bearer"))) {
            return Optional.of(headerVal.substring(BEARER.length()).trim());
        }
        return Optional.empty();
    } 

	private String getAuthorizationHeader(NativeWebRequest webRequest) {
		Iterator<String> headerNames = webRequest.getHeaderNames();
		while (headerNames.hasNext()) {
			String name = (String) headerNames.next();
			if ("X-Authorization".equals(name)) {
				return webRequest.getHeader(name);
			}
			if ("authorization".equals(name) || "Authorization".equals(name)) {
				return webRequest.getHeader(name);
			}
		}
		return null;
	}

}
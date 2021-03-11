package org.apache.james.webapi.app;

import java.util.List;

import org.apache.james.webapi.app.security.JwtProvider;
import org.apache.james.webapi.internal.TokenInfoArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{

    @Autowired
    JwtProvider jwtProvider;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
    {
        resolvers.add(new TokenInfoArgumentResolver(jwtProvider));
    }


}
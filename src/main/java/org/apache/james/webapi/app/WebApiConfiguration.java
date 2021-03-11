package org.apache.james.webapi.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(prefix = "webapi")
@Data
@NoArgsConstructor
public class WebApiConfiguration
{

    private String driverClassName;
    
    private String user;
    
    private String pwd;
    
    private String url;
    
    @Value("${webapi.jwt.token.secretKey}")
    private String tokenSecretKey;
    
    @Value("${webapi.jwt.token.expiration}")
    private long tokenValidity;
    
    
    @Value("${webapi.rabbit.uri}")
    private String amqpUri;
    
    @Value("${webapi.rabbit.management.uri}")
    private String amqpManagementUri;
    
    @Value("${webapi.rabbit.management.user}")
    private String amqpManagementUser;
    
    @Value("${webapi.rabbit.management.password}")
    private String amqpManagementPass;
    

}

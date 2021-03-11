package org.apache.james.webapi.model;

import lombok.Data;

@Data
public class LoginVO {
	private String username;
	private String password;
}
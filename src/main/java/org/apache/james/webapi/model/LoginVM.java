package org.apache.james.webapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVM {
	private String token;

	public boolean isSuccess() {
		return token != null && token.length() > 0;
	}
}
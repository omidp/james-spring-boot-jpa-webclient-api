package org.apache.james.webapi.internal;

import java.io.Serializable;

public interface TokenInfo extends Serializable{

	String getUsername();
	
}
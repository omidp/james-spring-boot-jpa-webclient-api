package org.apache.james.webapi.model;

import lombok.Data;

@Data
public class SendMailVO {
	private String from, to, subject, msg;
}
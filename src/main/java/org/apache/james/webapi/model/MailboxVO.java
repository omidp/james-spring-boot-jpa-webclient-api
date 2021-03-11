package org.apache.james.webapi.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class MailboxVO implements Serializable {
	private String name;
	private String id;
	private String namespace;
}
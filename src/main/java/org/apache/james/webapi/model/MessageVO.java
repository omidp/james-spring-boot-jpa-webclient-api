package org.apache.james.webapi.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class MessageVO implements Serializable {
	private String from;
	private String to;
	private String subject;
	private Long messageId;
	private Long mailboxId;
	private Long modeSeq;
	private String contentBody;
	private boolean withAttachment;
	private boolean seen;
	private boolean recent;
}
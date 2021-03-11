package org.apache.james.webapi.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageContent {
	private String textContent;
	private String contentType;
	List<MessageAttchment> attachments = new ArrayList<>();

}
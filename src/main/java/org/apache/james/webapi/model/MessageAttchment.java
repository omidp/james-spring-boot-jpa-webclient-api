package org.apache.james.webapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageAttchment {
	private boolean inline;
	private String name;
	private String id;
	private long size;
}
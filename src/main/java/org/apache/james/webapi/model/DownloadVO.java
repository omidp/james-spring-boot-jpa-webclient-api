package org.apache.james.webapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DownloadVO {
	private String fileName;
	private String fileContentType;
	private byte[] content;
}
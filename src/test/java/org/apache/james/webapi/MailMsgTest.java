package org.apache.james.webapi;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.james.webapi.app.service.MessageParser;
import org.apache.james.webapi.model.MessageContent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import junit.framework.Assert;

@Disabled
public class MailMsgTest {

	@Test
	@Disabled
	public void testBoundary() throws IOException {
		String string = IOUtils.toString(MailMsgTest.class.getResourceAsStream("/boundary.txt"));
		String boundaryText = new MessageParser().getBoundaryText(string);
		Assert.assertEquals(boundaryText, "------------05DCAB82E83F4FD1DDFF2BBB");
	}
	
	
	
	@Test
	@Disabled
	public void testMsg() throws IOException {
		String string = IOUtils.toString(MailMsgTest.class.getResourceAsStream("/boundary.txt"));
		String boundaryText = new MessageParser().getBoundaryText(string);
		String msgFileContent = IOUtils.toString(MailMsgTest.class.getResourceAsStream("/msg.txt"));
		MessageContent parseContent = new MessageParser().parseContent(msgFileContent, boundaryText);
//		Assert.assertEquals(parseContent.getTextContent(), );
	}

}

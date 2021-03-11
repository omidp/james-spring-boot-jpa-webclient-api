package org.apache.james.webapi.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Header;
import org.apache.james.mailbox.model.Headers;
import org.apache.james.mailbox.model.MessageResult;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.webapi.model.DownloadVO;
import org.apache.james.webapi.model.MessageContent;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Component
public class MessageParser {

	public static final Pattern boundaryPattern = Pattern.compile("\"([^\"]*)\"");

	public void parseEmail(InputStream instream) throws MimeException, IOException {
		MimeConfig config = MimeConfig.PERMISSIVE;
		MimeStreamParser parser = new MimeStreamParser(config);
		parser.setContentDecoding(true);
		parser.setContentHandler(new ContentHandler() {

			@Override
			public void startMultipart(BodyDescriptor bd) throws MimeException {
				System.out.println("startMultipart");
			}

			@Override
			public void startMessage() throws MimeException {
				System.out.println("startMessage");
			}

			@Override
			public void startHeader() throws MimeException {
				System.out.println("startHeader");
			}

			@Override
			public void startBodyPart() throws MimeException {
				System.out.println("startBodyPart");
			}

			@Override
			public void raw(InputStream is) throws MimeException, IOException {
				System.out.println("raw");
			}

			@Override
			public void preamble(InputStream is) throws MimeException, IOException {
				System.out.println("preamble");
			}

			@Override
			public void field(Field rawField) throws MimeException {
				System.out.println("field");
				String name = rawField.getName();
				System.out.println(name);
				String body = rawField.getBody();
				System.out.println(body);
			}

			@Override
			public void epilogue(InputStream is) throws MimeException, IOException {
				System.out.println("epilogue");
			}

			@Override
			public void endMultipart() throws MimeException {
				System.out.println("endMultipart");
			}

			@Override
			public void endMessage() throws MimeException {
				System.out.println("endMessage");
			}

			@Override
			public void endHeader() throws MimeException {
				System.out.println("endHeader");
			}

			@Override
			public void endBodyPart() throws MimeException {
				System.out.println("endBodyPart");
//				parser.stop();
			}

			@Override
			public void body(BodyDescriptor bd, InputStream is) throws MimeException, IOException {
				System.out.println("body");
				String body = IOUtils.toString(is);
				System.out.println(body);
				String transferEncoding = bd.getTransferEncoding();
				System.out.println(transferEncoding);
				System.out.println(bd.getBoundary());
				System.out.println(bd.getCharset());
				System.out.println(bd.getMediaType());
				System.out.println(bd.getMimeType());
				System.out.println(bd.getSubType());
				System.out.println(bd.getContentLength());
			}
		});

		try {
			parser.parse(instream);
		} finally {
			instream.close();
		}
	}

	public MessageContent parseContent(String multipartMessage, String boundaryText) {
		MessageContent mc = new MessageContent();
		Pattern p = Pattern.compile(String.format("%s(.+?)%s(.+?)%s", boundaryText, boundaryText, boundaryText),
				Pattern.DOTALL);
		Matcher textMatcher = p.matcher(multipartMessage);
		if (textMatcher.find()) {
			String actualMsg = textMatcher.group(1);
			Scanner scannerMsg = new Scanner(actualMsg);
			int j = 0;
			StringBuilder txtContent = new StringBuilder();
			while (scannerMsg.hasNextLine()) {
				String line = scannerMsg.nextLine();
				if (j > 3 && !line.startsWith("--"))
					txtContent.append(line);
				j++;
			}
			scannerMsg.close();
			mc.setTextContent(txtContent.toString());
			
		}
		return mc;
	}

	
	
	

	public String getBoundaryText(String multipartMessage) {
		int indexOf = multipartMessage.indexOf("boundary=");
		if (indexOf > 0) {
			String boundary = multipartMessage.substring(indexOf + "boundary=".length());
			Matcher m = boundaryPattern.matcher(boundary);
			if (m.find()) {
				String boundaryText = m.group(1);
				return boundaryText;
			}
		}
		return "";
	}

	public MessageContent parseMessageResult(MessageResult messageResult) throws MailboxException, IOException {
		String bdContentType = "";
		Headers headers = messageResult.getHeaders();
		Iterator<Header> headersIt = headers.headers();
		while (headersIt.hasNext()) {
			Header h = (Header) headersIt.next();
			if ("Content-Type".equals(h.getName())) {
				bdContentType = h.getValue().trim();
			}
		}
		boolean multipart = bdContentType.startsWith("multipart");
		String body = IOUtils.toString(messageResult.getBody().getInputStream());
		if (multipart) {
			String boundaryText = getBoundaryText(bdContentType);
			MessageContent parse = parseContent(body, boundaryText);
			parse.setContentType(bdContentType);
			return parse;
		}
		return new MessageContent(body, bdContentType, new ArrayList<>());
	}

	public DownloadVO parseAttachment(MessageResult messageResult) throws MailboxException, IOException
	{
		String bdContentType = "";
		Headers headers = messageResult.getHeaders();
		Iterator<Header> headersIt = headers.headers();
		while (headersIt.hasNext()) {
			Header h = (Header) headersIt.next();
			if ("Content-Type".equals(h.getName())) {
				bdContentType = h.getValue().trim();
			}
		}
		boolean multipart = bdContentType.startsWith("multipart");
		String boundaryText = getBoundaryText(bdContentType);
		String multipartMessage = IOUtils.toString(messageResult.getBody().getInputStream());
		Pattern p = Pattern.compile(String.format("%s(.+?)%s(.+?)%s", boundaryText, boundaryText, boundaryText),
				Pattern.DOTALL);
		Matcher textMatcher = p.matcher(multipartMessage);
		if (textMatcher.find()) {
			String attachedContent = textMatcher.group(2);
			Scanner scanner = new Scanner(attachedContent);
			int i = 0;
			StringBuilder fileContent = new StringBuilder();
			String fileContentType = "";
			String fileName = "";
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				// process the line
				if (i > 6 && !line.startsWith("--"))
					fileContent.append(line);
				else {
					if (line.trim().startsWith("Content-Transfer-Encoding:")) {
						fileContentType = line.substring("Content-Transfer-Encoding:".length()).trim();
					}
					if (line.trim().startsWith("filename=")) {
						fileName = line.substring("filename=".length()).trim();
					}
				}
				i++;
			}
			scanner.close();
			return new DownloadVO(fileName, fileContentType, null);
		}
		return new DownloadVO();
	}
	
	
	
}

package org.apache.james.webapi.app.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.james.webapi.app.security.JwtProvider;
import org.apache.james.webapi.app.service.MailboxService;
import org.apache.james.webapi.internal.TokenInfo;
import org.apache.james.webapi.model.DownloadVO;
import org.apache.james.webapi.model.MailboxVO;
import org.apache.james.webapi.model.MessageContent;
import org.apache.james.webapi.model.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/mailbox")
public class MailboxController {

	@Autowired
	MailboxService mailboxService;

	@Autowired
	JwtProvider jwtProvider;

	@GetMapping("")
	public ResponseEntity<List<MailboxVO>> list(TokenInfo tokenInfo) {
		String username = tokenInfo.getUsername();
		log.info(username);
		List<MailboxVO> mailboxes = mailboxService.getMailbox(username);
		return ResponseEntity.ok(mailboxes);
	}

	@GetMapping("/messages/{mailboxid}")
	public ResponseEntity<List<MessageVO>> messagesList(TokenInfo tokenInfo,
			@PathVariable("mailboxid") Long mailboxid) {
		String username = tokenInfo.getUsername();
		List<MessageVO> messages = mailboxService.getMailboxMessages(username, mailboxid);
		return ResponseEntity.ok(messages);
	}

	@GetMapping("/messages/{mailboxid}/{msgId}")
	public ResponseEntity<MessageContent> messages(TokenInfo tokenInfo, @PathVariable("mailboxid") Long mailboxid,
			@PathVariable("msgId") Long msgId) {
		String username = tokenInfo.getUsername();
		MessageContent messages = mailboxService.getMessage(username, mailboxid, msgId);
		return ResponseEntity.ok(messages);
	}

	@GetMapping(value = "/messages/download/{mailboxid}/{msgId}/{attachmentId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void download(TokenInfo tokenInfo, @PathVariable("mailboxid") Long mailboxid,
			@PathVariable("msgId") Long msgId, @PathVariable("attachmentId") String attachmentId,
			HttpServletResponse response) throws IOException {
		String username = tokenInfo.getUsername();
		DownloadVO content = mailboxService.getAttachment(username, mailboxid, msgId, attachmentId);
		if (content != null) {
			response.setContentType(content.getFileContentType());
			final OutputStream os = response.getOutputStream();
			response.setHeader("Content-Disposition",
					"attachment; filename=\"" + MimeUtility.encodeWord(content.getFileName(), "UTF-8", "Q") + "\"");
			IOUtils.copyLarge(new ByteArrayInputStream(content.getContent()), os);
			os.flush();
			os.close();
			response.flushBuffer();
		}

	}

	

}

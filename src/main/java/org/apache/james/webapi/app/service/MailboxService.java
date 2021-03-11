package org.apache.james.webapi.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.Flags;

import org.apache.james.core.Username;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.jpa.JPAId;
import org.apache.james.mailbox.jpa.mail.JPAMailboxMapper;
import org.apache.james.mailbox.jpa.mail.JPAMessageMapper;
import org.apache.james.mailbox.jpa.openjpa.OpenJPAMailboxManager;
import org.apache.james.mailbox.model.FetchGroup;
import org.apache.james.mailbox.model.Header;
import org.apache.james.mailbox.model.Mailbox;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.MessageAttachment;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.model.MessageResult;
import org.apache.james.mailbox.model.MessageResultIterator;
import org.apache.james.mailbox.model.search.MailboxQuery;
import org.apache.james.mailbox.model.search.Wildcard;
import org.apache.james.mailbox.store.ResultUtils;
import org.apache.james.mailbox.store.mail.MessageMapper.FetchType;
import org.apache.james.mailbox.store.mail.model.MailboxMessage;
import org.apache.james.webapi.model.DownloadVO;
import org.apache.james.webapi.model.MailboxVO;
import org.apache.james.webapi.model.MessageAttchment;
import org.apache.james.webapi.model.MessageContent;
import org.apache.james.webapi.model.MessageVO;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailboxService {

	JPAMailboxMapper jpaMailboxMapper;

	OpenJPAMailboxManager openJPAMailboxManager;

	JPAMessageMapper jpaMessageMapper;

	public MailboxService(JPAMailboxMapper jpaMailboxMapper, OpenJPAMailboxManager openJPAMailboxManager,
			JPAMessageMapper jpaMessageMapper) {
		this.jpaMailboxMapper = jpaMailboxMapper;
		this.openJPAMailboxManager = openJPAMailboxManager;
		this.jpaMessageMapper = jpaMessageMapper;
	}

	public List<MailboxVO> getMailbox(String username) {
		MailboxPath mailboxPath = MailboxPath.inbox(Username.of(username));
		try {
			List<Mailbox> findMailboxesByUser = jpaMailboxMapper.findMailboxWithPathLike(MailboxQuery.builder()
					.userAndNamespaceFrom(mailboxPath).expression(Wildcard.INSTANCE).build().asUserBound());
			List<MailboxVO> collect = findMailboxesByUser.stream().map(m -> {
				MailboxVO vo = new MailboxVO();
				vo.setName(m.getName());
				vo.setId(m.getMailboxId().toString());
				vo.setNamespace(m.getNamespace());
				return vo;
			}).collect(Collectors.toList());
			return collect;
		} catch (MailboxException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public List<MessageVO> getMailboxMessages(String username, Long mailboxid) {
		List<MessageVO> messageList = new ArrayList<>();
		Username uname = Username.of(username);
		MailboxSession session = openJPAMailboxManager.createSystemSession(uname);
		try {
			Mailbox mailbox = jpaMailboxMapper.findMailboxById(new JPAId(mailboxid));
			MessageResultIterator messages = openJPAMailboxManager
					.getMailbox(MailboxPath.forUser(uname, mailbox.getName()), session)
					.getMessages(MessageRange.all(), FetchGroup.FULL_CONTENT, session);
			while (messages.hasNext()) {
				MessageVO msg = new MessageVO();
				msg.setMailboxId(mailboxid);
				MessageResult messageResult = (MessageResult) messages.next();
				msg.setMessageId(messageResult.getUid().asLong());
				msg.setModeSeq(messageResult.getModSeq().asLong());
				Iterator<Header> headers = messageResult.getHeaders().headers();
				while (headers.hasNext()) {
					Header header = (Header) headers.next();
					if ("From".equals(header.getName()))
						msg.setFrom(header.getValue());
					if ("To".equals(header.getName()))
						msg.setTo(header.getValue());
					if ("Subject".equals(header.getName()))
						msg.setSubject(header.getValue());
				}
				msg.setWithAttachment(messageResult.hasAttachments());
				if (messageResult.getFlags().contains(Flags.Flag.SEEN))
					msg.setSeen(true);
				if (messageResult.getFlags().contains(Flags.Flag.RECENT))
					msg.setRecent(true);
				messageList.add(msg);
			}

		} catch (MailboxException me) {
			log.info("MailboxException {}", me);
		}
		return messageList;
	}

	public MessageContent getMessage(String username, Long mailboxId, Long msgId) {
		try {
			Mailbox mailbox = jpaMailboxMapper.findMailboxById(new JPAId(mailboxId));
			Iterator<MailboxMessage> findInMailbox = jpaMessageMapper.findInMailbox(mailbox,
					MessageRange.one(MessageUid.of(msgId)), FetchType.Full, 1);
			if (findInMailbox.hasNext()) {

				MailboxMessage mm = findInMailbox.next();
				MessageResult messageResult = ResultUtils.loadMessageResult(mm, FetchGroup.FULL_CONTENT);
				List<MessageAttachment> attachments = mm.getAttachments();
				MessageContent parseMessageResult = new MessageParser().parseMessageResult(messageResult);
				List<MessageAttchment> attachList = new ArrayList<>();
				if (attachments != null) {
					attachments.forEach(a -> {
						MessageAttchment ma = new MessageAttchment();
						ma.setName(a.getName().orElse(""));
						ma.setId(a.getAttachmentId().getId());
						ma.setSize(a.getAttachment().getSize());
						ma.setInline(a.isInline());
						attachList.add(ma);
					});
				}
				parseMessageResult.setAttachments(attachList);
				return parseMessageResult;
			}
		} catch (MailboxException me) {
			me.printStackTrace();
			log.info("", me);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DownloadVO getAttachment(String username, Long mailboxId, Long msgId, String attachmentId) {
		try {
			Mailbox mailbox = jpaMailboxMapper.findMailboxById(new JPAId(mailboxId));
			Iterator<MailboxMessage> findInMailbox = jpaMessageMapper.findInMailbox(mailbox,
					MessageRange.one(MessageUid.of(msgId)), FetchType.Full, 1);
			if (findInMailbox.hasNext()) {
				MailboxMessage mm = findInMailbox.next();
				MessageResult messageResult = ResultUtils.loadMessageResult(mm, FetchGroup.FULL_CONTENT);
				System.out.println(messageResult.getLoadedAttachments().size());
				Optional<MessageAttachment> ma = mm.getAttachments().stream()
						.filter(f -> f.getAttachmentId().getId().equals(attachmentId)).findFirst();
				if (ma.isPresent()) {
					MessageAttachment messageAttachment = ma.get();
					DownloadVO dvo = new MessageParser().parseAttachment(messageResult);
					dvo.setContent(messageAttachment.getAttachment().getBytes());
					return dvo;
				}
				return null;
			}
		} catch (MailboxException me) {
			log.info("", me);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

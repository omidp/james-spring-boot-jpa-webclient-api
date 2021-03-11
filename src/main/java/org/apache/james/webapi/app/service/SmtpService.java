package org.apache.james.webapi.app.service;

import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Template;

@Service
public class SmtpService {

	
	@Autowired
	JavaMailSender mailSender;

	@Autowired
	freemarker.template.Configuration freemarkerConfiguration;
	
	
	/**
     * sends html template using freemarker
     */
	public void send(String from, String to, String subject, String templateLocation, Map<String, Object> model)
    {
        MessagePreparator mp = new MessagePreparator(from, to, subject, templateLocation, model);
        try
        {
            mailSender.send(mp);
        }
        catch (MailException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new MailPreparationException(ex);
        }
    }
	
	/**
     * sends an email with attachement and template
     * 
     */
    public void send(String from, String to, String subject, String templateLocation, Map<String, Object> model, String attachmentPath,
            String attachmentName)
    {
        MessagePreparator mp = new MessagePreparator(from, to, subject, templateLocation, model, attachmentPath, attachmentName);
        mailSender.send(mp);
    }

	
	
	/**
     * send a simple text message
     * 
     * @param from
     * @param to
     * @param subject
     * @param msg
     */
    public void send(String from, String to, String subject, String msg)
    {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(msg);
        mailSender.send(message);
    }
	
	
	private class MessagePreparator implements MimeMessagePreparator
    {

        private String from;
        private String to;
        private String subject;
        private String templateLocation;
        private Map<String, Object> model;
        private String attachmentPath;
        private String attachmentName;

        public MessagePreparator(String from, String to, String subject, String templateLocation, Map<String, Object> model)
        {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.templateLocation = templateLocation;
            this.model = model;
        }

        public MessagePreparator(String from, String to, String subject, String templateLocation, Map<String, Object> model,
                String attachmentPath, String attachmentName)
        {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.templateLocation = templateLocation;
            this.model = model;
            this.attachmentPath = attachmentPath;
            this.attachmentName = attachmentName;
        }

        public void prepare(MimeMessage mimeMessage) throws Exception
        {
//            String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateLocation, "UTF-8", model);
            Template template = freemarkerConfiguration.getTemplate(templateLocation);
            String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text, true);
            if (attachmentPath != null  && attachmentName.trim().length() > 0)
            {
                FileSystemResource file = new FileSystemResource(attachmentPath);
                message.addAttachment(attachmentName, file);
            }
        }

    }

	
}

package cn.burgeon.bos.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class MailUtil {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private TemplateEngine templateEngine;

	/**
	 * 发送简单文本邮件
	 * 
	 * @param from
	 * @param to
	 * @param subject
	 * @param content
	 */
	public void sendSimpleMail(String from, String to, String subject, String content) {
		SimpleMailMessage message = new SimpleMailMessage();

		try {
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(content);

			mailSender.send(message);
		} catch (Exception e) {
			logger.error("发送简单邮件时发生异常！", e);
		}
	}

	/**
	 * 发送 html 格式邮件
	 * 
	 * @param from
	 * @param to
	 * @param subject
	 * @param content
	 */
	public void sendHtmlMail(String from, String to, String subject, String content) {
		MimeMessage message = mailSender.createMimeMessage();

		try {
			// true表示需要创建一个multipart message
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content, true);

			mailSender.send(message);
		} catch (MessagingException e) {
			logger.error("发送html邮件时发生异常！", e);
		}
	}

	/**
	 * 发送带附件的邮件
	 * 
	 * @param from
	 * @param to
	 * @param subject
	 * @param content
	 * @param filePath
	 */
	public void sendAttachmentsMail(String from, String to, String subject, String content, String filePath) {
		MimeMessage message = mailSender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content, true);

			FileSystemResource file = new FileSystemResource(new File(filePath));
			helper.addAttachment(file.getFilename(), file);

			mailSender.send(message);
		} catch (MessagingException e) {
			logger.error("发送带附件的邮件时发生异常！", e);
		}
	}

	/**
	 * 发送带静态资源的邮件
	 * 
	 * @param from
	 * @param to
	 * @param subject
	 * @param content
	 * @param maps
	 * @throws Exception
	 */
	public void sendInlineResourceMail(String from, List<String> tos, List<String> ccs, String subject, String content,
			Map<String, File> maps) {
		try {
			if (null == tos || tos.isEmpty())
				throw new Exception("收件人不能为空");

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(from);

			InternetAddress[] sendTo = new InternetAddress[tos.size()];
			for (int i = 0; i < tos.size(); i++)
				sendTo[i] = new InternetAddress(tos.get(i));
			helper.setTo(sendTo);
			if (null != ccs && !ccs.isEmpty()) {
				InternetAddress[] ccTo = new InternetAddress[ccs.size()];
				for (int i = 0; i < ccs.size(); i++)
					ccTo[i] = new InternetAddress(ccs.get(i));
				helper.setCc(ccTo);
			}
			helper.setSubject(subject);
			helper.setText(content, true);

			if (null != maps && !maps.isEmpty())
				for (String key : maps.keySet()) {
					FileSystemResource res = new FileSystemResource(maps.get(key));
					helper.addInline(key, res);
				}

			mailSender.send(message);
		} catch (MessagingException e) {
			logger.error("发送嵌入静态资源的邮件时发生异常！", e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 发送带模版邮件
	 * 
	 * @param from
	 * @param fromName
	 * @param tos
	 * @param ccs
	 * @param subject
	 * @param templateName
	 * @param context
	 * @param maps
	 * @throws Exception
	 */
	public void sendTemplateMail(String from, String fromName, String[] tos, String[] ccs, String subject,
			String templateName, Context context, Map<String, File> maps) {
		try {
			if (null == tos || tos.length == 0)
				throw new Exception("收件人不能为空");

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(from,fromName);

			InternetAddress[] sendTo = new InternetAddress[tos.length];
			for (int i = 0; i < tos.length; i++)
				sendTo[i] = new InternetAddress(tos[i]);
			helper.setTo(sendTo);
			if (null != ccs && ccs.length != 0) {
				InternetAddress[] ccTo = new InternetAddress[ccs.length];
				for (int i = 0; i < ccs.length; i++)
					ccTo[i] = new InternetAddress(ccs[i]);
				helper.setCc(ccTo);
			}
			helper.setSubject(subject);

			String content = templateEngine.process(templateName, context);
			helper.setText(content, true);

			if (null != maps && !maps.isEmpty())
				for (String key : maps.keySet()) {
					FileSystemResource res = new FileSystemResource(maps.get(key));
					helper.addInline(key, res);
				}

			mailSender.send(message);
		} catch (MessagingException e) {
			logger.error("发送模版邮件时发生异常！", e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}

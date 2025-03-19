package com.aiswift.Common.Service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MultiEmailService {
	private final JavaMailSender devMailSender;
	private final JavaMailSender adminMailSender;

	public MultiEmailService(@Qualifier("devMailSender") JavaMailSender devMailSender,
			@Qualifier("adminMailSender") JavaMailSender adminMailSender) {
		this.devMailSender = devMailSender;
		this.adminMailSender = adminMailSender;
	}

	public void sendEmail(String toEmail, String subject, String body, boolean isDeveloper) throws MessagingException {
		JavaMailSender mailSender = isDeveloper ? devMailSender : adminMailSender;

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		String senderEmail = ((JavaMailSenderImpl) mailSender).getUsername();
		helper.setFrom(senderEmail);
		helper.setTo(toEmail);
		helper.setSubject(subject);
		helper.setText(body);

		mailSender.send(message);
	}

}

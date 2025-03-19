package com.aiswift.Config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
	@Autowired
	private Environment env;

	public String getDevEmail() {
		return env.getProperty("spring.mail.dev.username");
	}

	public String getDevPassword() {
		return env.getProperty("spring.mail.dev.password");
	}

	public String getAdminEmail() {
		return env.getProperty("spring.mail.admin.username");
	}

	public String getAdminPassword() {
		return env.getProperty("spring.mail.admin.password");
	}

	@Bean(name = "devMailSender")
	@Primary
	public JavaMailSender devMailSender() {
		return createMailSender("smtp.gmail.com", 587, getDevEmail(), getDevPassword());
	}

	@Bean(name = "adminMailSender")
	public JavaMailSender adminMailSender() {
		return createMailSender("smtp.gmail.com", 587, getAdminEmail(), getAdminPassword());
	}

	private JavaMailSender createMailSender(String host, int port, String username, String password) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(username);
		mailSender.setPassword(password);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.connectiontimeout", "5000");
		props.put("mail.smtp.timeout", "5000");
		props.put("mail.smtp.writetimeout", "5000");

		return mailSender;
	}

}

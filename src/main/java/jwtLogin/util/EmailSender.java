package jwtLogin.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

public class EmailSender {

	public static void sendVerifyCode(ServletContext ctx, String toEmail, String code) throws Exception {

		// 🔥 context.xml Parameter 읽기
		String username = ctx.getInitParameter("SMTP_GMAIL_USERNAME");
		String appPassword = ctx.getInitParameter("SMTP_GMAIL_APP_PASSWORD");
		String host = ctx.getInitParameter("SMTP_GMAIL_HOST");
		String port = ctx.getInitParameter("SMTP_GMAIL_PORT");

		if (username == null || appPassword == null || host == null || port == null) {
			throw new IllegalStateException("context.xml SMTP 설정 누락: SMTP_GMAIL_*");
		}

		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");

		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, appPassword);
			}
		});

		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(username, "Omok"));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
		msg.setSubject("[Omok] 이메일 인증코드");
		msg.setText("인증코드: " + code + "\n5분 내 입력하세요.");

		Transport.send(msg);
	}
}

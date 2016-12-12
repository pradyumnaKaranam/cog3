package com.ibm.research.cogassist.qprocessor.util;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Email {

	public static void main(String[] args) throws IOException {
		
		InputReader in;
		if (System.console() != null) {
			in = new ConsoleReader();
		} else {
			in = new StandardInputReader();
		}
		
		String smtpHost = in.readLine("SMTP Server Host", "smtp.gmail.com");
		String smtpPort = in.readLine("SMTP Server Port", "25");
		String username = in.readLine("User");
		String password = in.readPassword("Password");
		String fromEmail = in.readLine("From", username);
		String toEmail = in.readLine("To");
		String subject = in.readLine("Subject");
		String message = in.readLine("Message");
		
		// Test using gmail 
		try {
			System.out.println("Sending email...");
			Email.sendEmail(smtpHost, smtpPort,
					username, password, 
					fromEmail, toEmail,
					subject, message);
			System.out.println("Email sent!");
		} catch (AddressException e) {
			System.err.println(e.getMessage());
		} catch (MessagingException e) {
			System.err.println(e.getMessage());
		}
	}
	/**
	 * Send EMAIL using smtp server
	 * 
	 * @param smptpHost
	 * @param fromEmail
	 * @param toEmail
	 * @param subject
	 * @param messageText
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	public static void sendEmail(final String smptpHost, final String smtpPort, 
			final String username, final String password, 
			final String fromEmail, final String toEmail, 
			final String subject, final String messageText) throws AddressException, MessagingException {

		Properties props = new Properties();
		props.put("mail.smtp.host", smptpHost);
		// props.put("mail.smtp.socketFactory.port", 465);
		// props.put("mail.smtp.socketFactory.class",
		//		"javax.net.ssl.SSLSocketFactory");
		if (username != null && username.isEmpty() == false) {
			props.put("mail.smtp.auth", "true");
		}
		props.put("mail.smtp.port", smtpPort);
		// props.put("mail.smtp.connectiontimeout", 5000);
		// props.put("mail.smtp.timeout", 5000);

		Session session = (username == null || username.isEmpty()) ? 
				 Session.getDefaultInstance(props) :
				 Session.getDefaultInstance(props,
						new javax.mail.Authenticator() {
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(
										username, password);
							}
			});
		// Create a default MimeMessage object.
		MimeMessage message = new MimeMessage(session);

		// Set From: header field of the header.
		message.setFrom(new InternetAddress(fromEmail));

		// Set To: header field of the header.
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				toEmail));

		// Set Subject: header field
		message.setSubject(subject);

		// Now set the actual message
		message.setText(messageText);

		// Send message
		Transport.send(message);
	}

}

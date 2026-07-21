package com.ogm.hrms.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends transactional account emails. Uses the configured {@link JavaMailSender} when SMTP is
 * available ({@code spring.mail.host} set); otherwise logs the message so flows work in dev/test
 * without an SMTP server. Only the raw token is passed in — links are assembled here.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String baseUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${hrms.mail.from:no-reply@ogm-hrms.local}") String from,
                        @Value("${hrms.app.base-url:http://localhost:8080}") String baseUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.baseUrl = baseUrl;
    }

    public void sendPasswordResetEmail(String to, String rawToken) {
        String link = baseUrl + "/reset-password?token=" + rawToken;
        send(to, "Reset your OGM HRMS password",
                "We received a request to reset your password.\n\nUse this link (valid for a limited time):\n"
                        + link + "\n\nIf you did not request this, you can ignore this email.");
    }

    public void sendEmailVerificationEmail(String to, String rawToken) {
        String link = baseUrl + "/verify-email?token=" + rawToken;
        send(to, "Verify your OGM HRMS email",
                "Please verify your email address by visiting:\n" + link
                        + "\n\nIf you did not expect this, you can ignore this email.");
    }

    /** Sends an email with a single binary attachment (e.g. a payslip PDF); logs if SMTP is absent. */
    public void sendWithAttachment(String to, String subject, String body, String filename, byte[] attachment) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.info("[email w/ attachment suppressed — no SMTP] to={} subject=\"{}\" attachment={} ({} bytes)",
                    to, subject, filename, attachment != null ? attachment.length : 0);
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            if (attachment != null) {
                helper.addAttachment(filename, new ByteArrayResource(attachment));
            }
            sender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email with attachment", e);
        }
    }

    /**
     * Sends an HTML email (used by the Email Template Engine after rendering a template); logs the
     * rendered message if SMTP is absent so template flows work in dev/test.
     */
    public void sendHtml(String to, String subject, String htmlBody) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.info("[html email suppressed — no SMTP] to={} subject=\"{}\"\n{}", to, subject, htmlBody);
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            sender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send HTML email", e);
        }
    }

    private void send(String to, String subject, String body) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.info("[email suppressed — no SMTP configured] to={} subject=\"{}\"\n{}", to, subject, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        sender.send(message);
    }
}

package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.UserSession;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.mail.*;
import javax.mail.internet.*;

public class SendEmail {
    //mail.men2corp.com
    private final String smtpHost = UserSession.getInstance().getEmailCredentials().getHost();  // SMTP server
    private final int smtpPort = UserSession.getInstance().getEmailCredentials().getPort();  // SMTP Port (465 for SSL)
    private final String username = UserSession.getInstance().getEmailCredentials().getEmail();  // Your email
    private final String password = UserSession.getInstance().getEmailCredentials().getPassword();  // Your email password

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Sends an email with the given parameters asynchronously.
     *
     * @param to        Recipient email address
     * @param subject   Subject of the email
     * @param messageText Body of the email
     */
    public void sendEmailAsync(String to, String subject, String messageText) {
        executorService.submit(() -> {
            try {
                // Set SMTP properties
                Properties props = new Properties();
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.port", smtpPort);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.starttls.enable", "true");

                // Authenticate
                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                // Compose email
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(messageText);

                // Send email
                Transport.send(message);
            } catch (MessagingException e) {
                DialogUtils.showErrorMessage("Error", "Error sending email notification: " + e.getMessage());
            }
        });
    }

    /**
     * Shuts down the executor service. This method should be called when the application is closing.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

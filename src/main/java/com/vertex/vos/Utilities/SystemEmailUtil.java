package com.vertex.vos.Utilities;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemEmailUtil {

    // System email configuration
    private static final String SMTP_HOST = "support.men2corp.com";  // Replace with your system email SMTP server
    private static final int SMTP_PORT = 465;                    // Replace with the correct port (465 for SSL or 587 for TLS)
    private static final String USERNAME = "vertex@support.men2corp.com";  // Replace with your system email address
    private static final String PASSWORD = "VertexAccess24";        // Replace with your system email password

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Sends an email with the given parameters asynchronously.
     *
     * @param to          Recipient email address
     * @param subject     Subject of the email
     * @param messageText Body of the email
     */
    public void sendEmailAsync(String to, String subject, String messageText) {
        executorService.submit(() -> {
            try {
                // Set SMTP properties
                Properties props = new Properties();
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", SMTP_PORT);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.starttls.enable", "true");

                // Authenticate
                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });

                // Compose email
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(USERNAME));
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

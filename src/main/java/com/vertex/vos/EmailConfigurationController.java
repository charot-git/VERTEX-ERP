package com.vertex.vos;

import com.vertex.vos.DAO.EmailDAO;
import com.vertex.vos.Objects.EmailCredentials;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.security.SecureRandom;

public class EmailConfigurationController {

    @FXML
    private TextField email;

    @FXML
    private TextField host;

    @FXML
    private PasswordField password;

    @FXML
    private TextField port;

    @FXML
    private Button sendCode;

    private String generatedPin;

    private final SystemEmailUtil systemEmailUtil = new SystemEmailUtil();  // Assuming you have a SendEmail utility class
    private final EmailDAO emailDAO = new EmailDAO();       // DAO for storing the credentials

    @FXML
    public void initialize() {
        sendCode.setOnAction(event -> {
            // Validate inputs
            if (email.getText().isEmpty() || host.getText().isEmpty() || password.getText().isEmpty() || port.getText().isEmpty()) {
                DialogUtils.showErrorMessage("Error", "Please fill in all fields.");
                return;
            }

            // Generate the PIN
            generatedPin = generatePin();

            // Send the email with the PIN
            sendPinToEmail(email.getText(), generatedPin);
        });
    }

    /**
     * Generates a random 6-digit PIN code.
     *
     * @return A 6-digit PIN code as a string.
     */
    private String generatePin() {
        SecureRandom random = new SecureRandom();
        int pin = 100000 + random.nextInt(900000); // Generate a 6-digit PIN
        return String.valueOf(pin);
    }

    /**
     * Sends the generated PIN to the provided email and verifies it.
     *
     * @param recipientEmail The email address to send the PIN.
     * @param pin            The PIN to be sent.
     */
    private void sendPinToEmail(String recipientEmail, String pin) {
        String subject = "Your Verification PIN";
        String message = "Your verification PIN is: " + pin;

        try {
            systemEmailUtil.sendEmailAsync(recipientEmail, subject, message);
            String enteredPin = EntryAlert.showEntryAlert("Verify Email", "Enter the pin in your email to verify", "Please enter the 6-digit PIN code sent to your email:");

            if (enteredPin.equals(generatedPin)) {
                DialogUtils.showCompletionDialog("Verification Successful", "Your email has been verified.");
                saveEmailCredentials();  // Save credentials after successful verification
            } else {
                DialogUtils.showErrorMessage("Verification Failed", "The PIN you entered is incorrect. Please try again.");
            }

        } catch (Exception e) {
            DialogUtils.showErrorMessage("Error", "Failed to send the email: " + e.getMessage());
        }
    }

    /**
     * Saves the email credentials after PIN verification.
     */
    private void saveEmailCredentials() {
        try {
            EmailCredentials credentials = new EmailCredentials();
            credentials.setEmail(email.getText());
            credentials.setHost(host.getText());
            credentials.setPort(Integer.parseInt(port.getText()));
            credentials.setPassword(password.getText());  // Store the plain password

            emailDAO.addUserEmail(credentials, UserSession.getInstance().getUserId());  // Assuming you have a UserSession class

            DialogUtils.showCompletionDialog("Success", "Email configuration saved successfully.");
        } catch (Exception e) {
            DialogUtils.showErrorMessage("Error", "Failed to save email configuration: " + e.getMessage());
        }
    }

    public void setCredentials(EmailCredentials emailCredentials) {
        email.setText(emailCredentials.getEmail());
        host.setText(emailCredentials.getHost());
        port.setText(String.valueOf(emailCredentials.getPort()));
        password.setText(emailCredentials.getPassword());
    }
}

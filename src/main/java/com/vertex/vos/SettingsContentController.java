package com.vertex.vos;

import com.vertex.vos.Objects.DatabaseConfig;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsContentController implements Initializable {
    public Label version;
    public Label environment;
    public Button emailConfiguration;
    @FXML
    private ImageView profilePic;
    @FXML
    private HBox changePicButton;
    @FXML
    private Label nameHeader;
    @FXML
    private Label departmentHeader;
    @FXML
    private Label positionHeader;
    @FXML
    private TextField email;
    @FXML
    private TextField mobileNo;
    @FXML
    private TextField firstName;
    @FXML
    private TextField middleName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField department;
    @FXML
    private TextField position;
    @FXML
    private TextField dateHired;
    @FXML
    private TextField birthday;
    @FXML
    private TextField tin;
    @FXML
    private TextField philhealth;
    @FXML
    private TextField pagibig;
    @FXML
    private Button confirmButton;
    @FXML
    private Button changePassButton;

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    void setContentPane(AnchorPane contentPane) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ImageCircle.circular(profilePic);
        environment.setText(DatabaseConfig.getEnvironment().name());

        changePicButton.setOnMouseClicked(mouseEvent -> uploadToDataBase());
        // Fetch the current user's data from the database
        int currentUserId = UserSession.getInstance().getUserId(); // Implement a method to get the current user's ID
        if (currentUserId != -1) {
            try (Connection connection = dataSource.getConnection()) {
                String sql = "SELECT * FROM user WHERE user_id = ?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, currentUserId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String fullName = "%s %s %s".formatted(resultSet.getString("user_fname"), resultSet.getString("user_mname"), resultSet.getString("user_lname"));

                    nameHeader.setText(fullName);
                    departmentHeader.setText(resultSet.getString("user_department"));
                    positionHeader.setText(resultSet.getString("user_position"));


                    email.setText(resultSet.getString("user_email"));
                    mobileNo.setText(resultSet.getString("user_contact"));
                    firstName.setText(resultSet.getString("user_fname"));
                    middleName.setText(resultSet.getString("user_mname"));
                    lastName.setText(resultSet.getString("user_lname"));
                    department.setText(resultSet.getString("user_department"));
                    position.setText(resultSet.getString("user_position"));
                    dateHired.setText(resultSet.getDate("user_dateOfHire").toString());
                    birthday.setText(resultSet.getDate("user_bday").toString());
                    tin.setText(resultSet.getString("user_tin"));
                    philhealth.setText(resultSet.getString("user_philhealth"));
                    pagibig.setText(resultSet.getString("user_sss"));
                    String userImageURI = resultSet.getString("user_image");
                    if (userImageURI != null && !userImageURI.isEmpty()) {
                        // Use Paths to get a valid file URL
                        File imageFile = new File(userImageURI);
                        String absolutePath = imageFile.toURI().toString();
                        Image image = new Image(absolutePath);

                        profilePic.setImage(image);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }
        }
        changePassButton.setOnMouseClicked(mouseEvent -> changePassword());
        emailConfiguration.setOnMouseClicked(mouseEvent -> configureEmail());
    }

    private void configureEmail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EmailConfiguration.fxml"));
            Parent content = loader.load();

            EmailConfigurationController controller = loader.getController();
            if (UserSession.getInstance().getEmailCredentials() != null) {
                controller.setCredentials(UserSession.getInstance().getEmailCredentials());
            }
            Stage stage = new Stage();
            stage.setTitle("Configure Email" + " - " + UserSession.getInstance().getUserFirstName());
            stage.setScene(new Scene(content));
            stage.setResizable(false);
            stage.initStyle(StageStyle.UTILITY);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changePassword() {
        // Show confirmation alert
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Password Change", "Are you sure you want to change your password?", "", false);
        boolean isConfirmed = confirmationAlert.showAndWait();

        if (isConfirmed) {
            // Show password input dialog
            Optional<String> optionalPassword = EntryAlert.showPasswordDialog("Enter Password", "Password Required", "Please enter your new password:");

            if (optionalPassword.isPresent() && !optionalPassword.get().isEmpty()) {
                String password = optionalPassword.get();
                EmployeeDAO employeeDAO = new EmployeeDAO();

                // Attempt to change the password
                boolean isPasswordChanged = employeeDAO.changePassword(UserSession.getInstance().getUserId(), password);

                if (isPasswordChanged) {
                    DialogUtils.showCompletionDialog("Success", "Password change successful");
                } else {
                    DialogUtils.showErrorMessage("Error", "Password change unsuccessful");
                }
            } else {
                DialogUtils.showErrorMessage("Invalid Password", "Password is empty or null. Password change canceled.");
            }
        } else {
            DialogUtils.showCompletionDialog("Cancelled", "Password change cancelled");
        }
    }


    private void uploadToDataBase() {
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("Bitmap", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(fileChooserStage);

        if (selectedFile != null) {
            boolean success = ServerUtility.uploadImageAndStoreInDB(selectedFile);
            if (success) {
                DialogUtils.showCompletionDialog("Profile Image Updated", "User image update successful");
            } else {
                DialogUtils.showErrorMessage("Profile Image Error", "There has been an error in updating your profile image.");
            }

        }
    }

}

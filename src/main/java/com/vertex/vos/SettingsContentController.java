package com.vertex.vos;

import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SettingsContentController implements Initializable {
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
                    // Set other text fields and UI elements as needed
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }
        }
    }

}

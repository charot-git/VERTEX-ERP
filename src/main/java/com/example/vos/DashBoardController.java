package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashBoardController implements Initializable {

    public ImageView logoutButton;
    private HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    @FXML
    private Label nameText, positionText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try (Connection connection = dataSource.getConnection()) {
            // Assuming you have stored the user's name and position in the user session
            UserSession userSession = UserSession.getInstance();
            String name = userSession.getUserFirstName() + " " + userSession.getUserLastName();
            String position = userSession.getUserPosition();

            // Set the labels to your name and position
            nameText.setText(name);
            positionText.setText(position);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleLogout() {
        System.out.println("logout");
    }

    public void loadChatContent(MouseEvent mouseEvent) {
    }

    public void loadAdminContent(MouseEvent mouseEvent) {
    }

    public void loadAccountingContent(MouseEvent mouseEvent) {
    }

    public void loadIOpsContent(MouseEvent mouseEvent) {
    }

    public void loadEOpsContent(MouseEvent mouseEvent) {
    }

    public void loadFSContent(MouseEvent mouseEvent) {
    }

    public void loadCalendarContent(MouseEvent mouseEvent) {
    }
}

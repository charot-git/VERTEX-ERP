package com.vertex.vos;

import com.vertex.vos.Constructors.User;
import com.vertex.vos.Utilities.ConfirmationAlert;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.DatePicker;


import java.net.URL;
import java.util.ResourceBundle;

public class EmployeeDetailsController implements Initializable {
    public TextField fname, mname, lname;
    public DatePicker birthDay, hiredDay;
    public TextField sss;
    public TextField tin;

    public TextField contact;
    public TextField email;
    public TextField province, city, brgy;

    public TextField department;
    public Label fullName, position;
    public Label roles;
    public ImageView profilePic;
    public ImageView deleteButton, editButton;
    public TextField philHealth;

    public void pickDate(MouseEvent mouseEvent) {

    }


    public void initData(User selectedUser) {
        Platform.runLater(() -> {
            setDetails(selectedUser);
        });
    }

    private void setDetails(User selectedUser) {
        String completeName = selectedUser.getUser_fname()
                + " " + selectedUser.getUser_mname()
                + " " + selectedUser.getUser_lname();
        fullName.setText(completeName);
        position.setText(selectedUser.getUser_position());
        roles.setText(selectedUser.getUser_tags());
        fname.setText(selectedUser.getUser_fname());
        mname.setText(selectedUser.getUser_mname());
        lname.setText(selectedUser.getUser_lname());
        province.setText(selectedUser.getUser_province());
        city.setText(selectedUser.getUser_city());
        brgy.setText(selectedUser.getUser_brgy());
        contact.setText(selectedUser.getUser_contact());
        email.setText(selectedUser.getUser_email());
        department.setText(selectedUser.getUser_department());
        tin.setText(selectedUser.getUser_tin());
        sss.setText(selectedUser.getUser_sss());
        philHealth.setText(selectedUser.getUser_philhealth());
        Label bday = new Label();
        Label dateOfHire = new Label();
        bday.setText(selectedUser.getUser_bday().toString());
        dateOfHire.setText(selectedUser.getUser_dateOfHire().toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        deleteButton.setOnMouseClicked(mouseEvent -> {
            // Handle the click event here
            System.out.println("Delete button clicked!");
            // Add your logic for handling the click event
        });

        editButton.setOnMouseClicked(mouseEvent -> {
            editUserDetails();
        });

    }

    private void editUserDetails() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("User editing", "Edit user", fullName.getText());

        boolean result = confirmationAlert.showAndWait();

        Boolean isEditable = result;

        fname.setEditable(isEditable);
        mname.setEditable(isEditable);
        lname.setEditable(isEditable);
        sss.setEditable(isEditable);
        tin.setEditable(isEditable);
        contact.setEditable(isEditable);
        email.setEditable(isEditable);
        province.setEditable(isEditable);
        city.setEditable(isEditable);
        brgy.setEditable(isEditable);
        department.setEditable(isEditable);
        philHealth.setEditable(isEditable);
    }
}

package com.vertex.vos;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class CalendarContentController implements Initializable {
    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private ImageView back;
    @FXML
    private Label YearMonth;
    @FXML
    private ImageView next;
    @FXML
    private GridPane calendarPane;
    @FXML
    private GridPane calendarDays;


    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    private Calendar currentCalendar;

    private final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentCalendar = Calendar.getInstance();
        updateCalendar();
        back.setOnMouseClicked(event -> {
            // Handle back button click action here
            goToPreviousMonth();
        });

        // Set onClick listener for the next ImageView
        next.setOnMouseClicked(event -> {
            // Handle next button click action here
            goToNextMonth();
        });
    }

    private void updateCalendar() {
        // Clear previous content in the calendarPane
        calendarPane.getChildren().clear();
        for (int i = 0; i < DAY_NAMES.length; i++) {
            Label dayNameLabel = new Label(DAY_NAMES[i]);
            dayNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: whitesmoke");
            dayNameLabel.setAlignment(Pos.CENTER);

            // Create a StackPane and add the dayNameLabel to it
            StackPane cellContainer = new StackPane(dayNameLabel);
            cellContainer.getStyleClass().add("cell-container"); // You can add styles to the cell container if needed
            cellContainer.setStyle("-fx-background-color: #155E98");
            // Add the cellContainer to the calendarDays grid
            calendarDays.add(cellContainer, i, 0);
        }


        int month = currentCalendar.get(Calendar.MONTH); // Calendar months start from 0
        int year = currentCalendar.get(Calendar.YEAR);
        YearMonth.setText(new DateFormatSymbols().getMonths()[month] + " " + year);
        int daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Get the first day of the month (day of the week)
        int firstDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1; // Calendar days start from 1 (Sunday)

        // Populate the calendarPane with the days of the month
        int row = 0; // Start from row 1 to display dates below day names
        int col = firstDayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            StackPane cellContainer = new StackPane(dayLabel); // Wrap the label inside a StackPane
            calendarPane.add(cellContainer, col, row);
            cellContainer.getStyleClass().add("cell-container");
            // Get the current system's calendar instance
            Calendar systemCalendar = Calendar.getInstance();

            // Check if the current day matches the system's current day
            if (systemCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                    systemCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    systemCalendar.get(Calendar.DAY_OF_MONTH) == day) {
                // Set background color to #155E98 and text color to white for the entire cell container
                cellContainer.setStyle("-fx-background-color: #155E98;-fx-border-color: white; -fx-border-width: 1");
                dayLabel.setStyle("-fx-text-fill: white;");
            }
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }



    }

    @FXML
    private void goToPreviousMonth() {
        // Move to the previous month
        currentCalendar.add(Calendar.MONTH, -1);
        updateCalendar();
    }

    @FXML
    private void goToNextMonth() {
        // Move to the next month
        currentCalendar.add(Calendar.MONTH, 1);
        updateCalendar();
    }
}

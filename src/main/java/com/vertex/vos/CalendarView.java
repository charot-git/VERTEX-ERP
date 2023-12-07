package com.vertex.vos;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.security.Key;
import java.time.LocalDate;
import java.util.EventListener;

interface DateSelectedCallback {
    void onDateSelected(LocalDate selectedDate);
}

public class CalendarView extends Application {

    private final DateSelectedCallback dateSelectedCallback; // Callback interface instance

    // Constructor to receive the DateSelectedCallback interface
    public CalendarView(DateSelectedCallback callback) {
        this.dateSelectedCallback = callback;
    }

    private static boolean isWindowOpen = false; // Flag to track if the window is open

    private int currentYear;
    private int currentMonth;
    private GridPane calendarGrid;

    private LocalDate clickedDate;
    private TextField selectedDateTextField = new TextField(); // TextField to display selected date


    private void setClickedDate(LocalDate date) {
        this.clickedDate = date;
    }

    // Method to get the clicked date
    public LocalDate getClickedDate() {
        return clickedDate;
    }

    Text monthYearLabel = new Text(); // Create a Text object for month and year

    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    @Override
    public void start(Stage primaryStage) {
        if (!isWindowOpen) {
            isWindowOpen = true;
            LocalDate today = LocalDate.now();
            currentYear = today.getYear();
            currentMonth = today.getMonthValue();
            Text monthYearLabel = new Text();

            calendarGrid = new GridPane();
            refreshCalendar(currentYear, currentMonth);

            Image previousImage = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/back.png"));
            Image nextImage = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Forward.png"));

            // Create the TextField for displaying the selected date
            selectedDateTextField.setEditable(false); // Make it read-only
            selectedDateTextField.setPromptText("Selected Date"); // Placeholder text


            ImageView previousImageView = new ImageView(previousImage);
            previousImageView.setFitHeight(30);
            previousImageView.setPreserveRatio(true);
            previousImageView.setOnMouseClicked(event -> showPreviousMonth());


            ImageView nextImageView = new ImageView(nextImage);
            nextImageView.setFitHeight(30);
            nextImageView.setPreserveRatio(true);
            nextImageView.setOnMouseClicked(event -> showNextMonth());

            // Create an HBox to hold the previous and next buttons
            HBox buttonBox = new HBox(10); // 10 pixels spacing between buttons
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(previousImageView, nextImageView);

            // Label to display the current month and year
            updateMonthYearLabel(monthYearLabel);

            VBox root = new VBox(10);
            root.setAlignment(Pos.CENTER);
            root.getChildren().addAll(monthYearLabel, buttonBox, calendarGrid, selectedDateTextField);

            Scene scene = new Scene(root);

            String cssPath = getClass().getResource("/com/vertex/vos/assets/calendar.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            scene.setFill(null); // Make the stage background transparent
            root.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 20; -fx-border-color: #155E98;"); // Set background color, border radius, and border color


            primaryStage.setTitle("Calendar View");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                isWindowOpen = false; // Set the flag to false when the window is closed
            });
        } else {
            // If the window is already open, prevent opening another instance
            System.out.println("CalendarView window is already open.");
            primaryStage.close(); // Close the new instance
        }
    }

    private void refreshCalendar(int year, int month) {
        calendarGrid.getChildren().clear();
        for (int i = 0; i < 7; i++) {
            Text dayLabel = new Text(DAY_NAMES[i]);
            calendarGrid.add(dayLabel, i, 0);
        }

        int firstDayOfMonth = LocalDate.of(year, month, 1).getDayOfWeek().getValue() % 7;
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            int column = (firstDayOfMonth + day - 1) % 7;
            int row = (firstDayOfMonth + day - 1) / 7 + 1;

            Button dayButton = new Button(String.valueOf(day));
            final int currentDay = day; // Create a final variable for day

            dayButton.setOnAction(event -> {
                int clickedDay = currentDay;
                int clickedMonth = currentMonth;
                int clickedYear = currentYear;

                // Create a LocalDate object with the clicked date
                clickedDate = LocalDate.of(clickedYear, clickedMonth, clickedDay);

                // Convert LocalDate to Date (if needed)
                java.util.Date utilDate = java.sql.Date.valueOf(clickedDate);

                // Handle the clickedDate (and utilDate) as needed
                System.out.println("Clicked date as LocalDate: " + clickedDate);

                updateSelectedDateTextField(clickedDate);

                // Call the onDateSelected method of the callback interface
                if (dateSelectedCallback != null) {
                    dateSelectedCallback.onDateSelected(clickedDate);
                }

                // Close the stage
                Stage stage = (Stage) dayButton.getScene().getWindow();
                stage.close();

                isWindowOpen = false;

            });


            calendarGrid.add(dayButton, column, row);
        }
    }

    private void showPreviousMonth() {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        refreshCalendar(currentYear, currentMonth);
        updateMonthYearLabel(monthYearLabel);
    }

    private void showNextMonth() {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        refreshCalendar(currentYear, currentMonth);
        updateMonthYearLabel(monthYearLabel);
    }

    // Updated method to update the month and year label
    private void updateMonthYearLabel(Text label) {
        String monthName = LocalDate.of(currentYear, currentMonth, 1).getMonth().toString();
        String capitalizedMonth = monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase();
        label.setText(capitalizedMonth + " " + currentYear);
    }

    private void updateSelectedDateTextField(LocalDate date) {
        selectedDateTextField.setText(date.toString()); // Update TextField with the selected date
    }


    public static void main(String[] args) {
        launch(args);
    }
}

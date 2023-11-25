package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.sql.SQLException;
import java.util.*;

public class CourseSearchController {

    @FXML
    private TextField searchSubjectField;
    @FXML
    private TextField searchNumberField;
    @FXML
    private TextField searchTitleField;
    @FXML
    private ListView<Course> coursesListView;
    @FXML
    private Button logOutButton;
    @FXML
    private Button myReviewsButton;
    @FXML
    private Button addButton;
    @FXML
    private Label messageLabel;

    private DatabaseDriver dbDriver;

    // Initialize method
    @FXML
    public void initialize() {
        Configuration configuration = new Configuration();
        dbDriver = new DatabaseDriver(configuration.getDatabaseFilename());
        try {
            dbDriver.connect();
            loadCourses();
        } catch (SQLException e) {
            messageLabel.setText("Unable to connect to database.");
        }
    }

    private void loadCourses() {
        try {
            List<Course> courses = dbDriver.getAllCoursesWithRatings();
            // You might want to calculate average ratings for these courses here
            coursesListView.getItems().setAll(courses);
        } catch (SQLException e) {
            messageLabel.setText("Unable to load courses.");
        }
    }

    @FXML
    protected void handleSearch(ActionEvent event) {
        try {
            String subject = searchSubjectField.getText().trim();
            String numberStr = searchNumberField.getText().trim();
            String title = searchTitleField.getText().trim();

            List<Course> filteredCourses = new ArrayList<>();

            if (!subject.isEmpty()) {
                // Fetch courses by subject
                filteredCourses.addAll(dbDriver.getCoursesBySubject(subject));
            }
            if (!numberStr.isEmpty()) {
                // Fetch courses by number
                int number = Integer.parseInt(numberStr);
                filteredCourses.addAll(dbDriver.getCoursesByNumber(number));
            }
            if (!title.isEmpty()) {
                // Fetch courses by title
                filteredCourses.addAll(dbDriver.getCoursesByTitle(title));
            }

            if (subject.isEmpty() && numberStr.isEmpty() && title.isEmpty()) {
                // No search criteria
                messageLabel.setText("Please enter at least one search criteria.");
                return;
            }

            if (filteredCourses.isEmpty()) {
                // No courses found
                messageLabel.setText("No courses found.");
                return;
            }

            // Remove duplicates if any criteria overlap
            Set<Course> uniqueCourses = new HashSet<>(filteredCourses);
            coursesListView.getItems().setAll(uniqueCourses);
        } catch (SQLException e) {
            // Handle SQL Exception
            messageLabel.setText("Unable to search courses.");
        } catch (NumberFormatException e) {
            // Handle case where the number field is not a valid integer
            messageLabel.setText("Invalid course number.");
        }
    }

    @FXML
    protected void handleCourseAdd(ActionEvent event) {
        // Implement course add logics
        String subject = searchSubjectField.getText().trim().toUpperCase(); // Convert to uppercase
        String numberStr = searchNumberField.getText().trim();
        String title = searchTitleField.getText().trim();

        // Validate inputs
        if (!subject.matches("[A-Z]{2,4}") || numberStr.length() != 4 || title.isEmpty() || title.length() > 50) {
            // Show validation error message
            messageLabel.setText("Invalid input.");
            return;
        }

        int number = Integer.parseInt(numberStr);
        Course newCourse = new Course(0, subject, number, title, null);

        try {
            dbDriver.addCourse(newCourse);
            dbDriver.commit();
            loadCourses(); // Reload the courses list
        } catch (SQLException e) {
            messageLabel.setText("Unable to add course.");
        }
    }

    @FXML
    protected void handleCourseSelect() {
        Course selectedCourse = coursesListView.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            // Transition to course review screen with selectedCourse
            // You'll need to pass the selectedCourse to the next controller
        }
    }


    @FXML
    protected void handleLogOut(ActionEvent event) {

        try {
            if (dbDriver != null) {
                dbDriver.disconnect();
            }
            // Navigate to login screen or close application

        } catch (SQLException e) {
            // Handle disconnection error
            messageLabel.setText("Unable to disconnect from database.");
        }
    }

    @FXML
    protected void handleMyReviews(ActionEvent event) {
        // Implement navigation to My Reviews screen
    }
}
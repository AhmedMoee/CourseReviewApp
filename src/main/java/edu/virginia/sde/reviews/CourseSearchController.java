package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CourseSearchController {

    @FXML
    private TextField searchSubjectField, searchNumberField, searchTitleField;
    @FXML
    private ListView<Course> coursesListView;
    @FXML
    private Button logOutButton, myReviewsButton, addButton, selectCourseButton;
    @FXML
    private Label messageLabel;

    private DatabaseDriver dbDriver;
    private CourseReviewApplication application;

    private User currentUser;

    public void setApplication(CourseReviewApplication application) {
        this.application = application;
    }

    public void setDatabaseDriver(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        Configuration configuration = new Configuration();
        dbDriver = DatabaseDriver.getInstance(configuration.getDatabaseFilename());
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
            coursesListView.getItems().setAll(courses);
        } catch (SQLException e) {
            messageLabel.setText("Unable to load courses.");
        }
    }

    @FXML
    protected void handleSearch(ActionEvent event) {
        try {
            String subject = searchSubjectField.getText().trim().toUpperCase();
            String numberStr = searchNumberField.getText().trim();
            String title = searchTitleField.getText().trim();

            if (subject.isEmpty() && numberStr.isEmpty() && title.isEmpty()) {
                // No search criteria
                messageLabel.setText("Please enter at least one search criteria.");
                // Show all courses
                coursesListView.getItems().setAll(dbDriver.getAllCoursesWithRatings());
                return;

            } else if (!subject.isEmpty() && !subject.matches("[A-Z]{2,4}")) {
                messageLabel.setText("Invalid subject input. Please enter a 4 letter subject code.");
                return;
            } else if (!numberStr.isEmpty() && numberStr.length() != 4) {
                messageLabel.setText("Invalid course number. Please enter a 4 digit course number.");
                return;
            } else if (title.length() > 50) {
                messageLabel.setText("Title cannot be longer than 50 characters.");
                return;
            }

            var filteredCourses = dbDriver.getAllCoursesWithRatings();

            if (!subject.isEmpty()) {
                // Fetch courses by subject
                filteredCourses = filteredCourses.stream()
                        .filter(course -> course.getSubject().equalsIgnoreCase(subject))
                        .sorted(Comparator.comparingInt(Course::getCourseNumber))
                        .collect(Collectors.toList());
            }

            if (!title.isEmpty()) {
                // Fetch courses by title
                filteredCourses = filteredCourses.stream()
                        .filter(course -> course.getTitle().toUpperCase().contains(title.toUpperCase()))
                        .sorted(Comparator.comparingInt(Course::getCourseNumber))
                        .collect(Collectors.toList());
            }

            if (!numberStr.isEmpty()) {
                // Fetch courses by number
                int number = Integer.parseInt(numberStr);
                filteredCourses = filteredCourses.stream()
                        .filter(course -> course.getCourseNumber() == number)
                        .sorted(Comparator.comparing(Course::getSubject).thenComparingInt(Course::getCourseNumber))
                        .collect(Collectors.toList());
            }

            filteredCourses.sort(Comparator.comparingInt(Course::getCourseNumber));

            if (filteredCourses.isEmpty()) {
                // No courses found
                messageLabel.setText("No courses found.");
                return;
            }else {
                coursesListView.getItems().setAll(filteredCourses);
            }

            // Remove duplicates if any criteria overlap
            coursesListView.getItems().setAll(filteredCourses.stream().distinct().collect(Collectors.toList()));
            messageLabel.setText("");
        } catch (SQLException e) {
            // Handle SQL Exception
            messageLabel.setText("Unable to search courses.");
        } catch (NumberFormatException e) {
            // Handle case where the number field is not a valid integer
            messageLabel.setText("Invalid course number. Please enter a 4 digit course number.");
        }
    }

    @FXML
    protected void handleCourseAdd(ActionEvent event) {
        String subject = searchSubjectField.getText().trim().toUpperCase(); // Convert to uppercase
        String numberStr = searchNumberField.getText().trim();
        String title = searchTitleField.getText().trim();

        // Validate inputs
        if (!subject.matches("[A-Z]{2,4}")) {
            // Show validation error message
            messageLabel.setText("Invalid subject input. Please enter a 4 letter subject code.");
            return;
        } else if (numberStr.length() != 4) {
            messageLabel.setText("Invalid course number. Please enter a 4 digit course number.");
            return;
        } else if (title.isEmpty()) {
            messageLabel.setText("Title cannot be empty. Please enter a title.");
            return;
        } else if (subject.isEmpty()) {
            messageLabel.setText("Subject cannot be empty. Please enter a subject.");
            return;
        } else if (title.length() > 50) {
            messageLabel.setText("Title cannot be longer than 50 characters.");
            return;
        }

        try {
            int number = Integer.parseInt(numberStr);
            Course newCourse = new Course(0, subject, number, title, null);

            if (!dbDriver.courseAlreadyExists(subject, number, title)) {
                dbDriver.addCourse(newCourse);
                loadCourses(); // Reload the courses list
                messageLabel.setText("Course added successfully.");
            } else {
                messageLabel.setText("Course already exists.");
            }
        } catch (SQLException e) {
            messageLabel.setText("Unable to add course.");
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid course number. Please enter a 4 digit course number.");
        }
    }

    @FXML
    protected void handleCourseSelect() {
        Course selectedCourse = coursesListView.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            application.switchToCourseReviewScreen(selectedCourse, currentUser, "CourseSearchScreen");
        } else {
            messageLabel.setText("Please select a course first.");
        }
    }


    @FXML
    protected void handleLogOut(ActionEvent event) {

        if (this.application != null) {
            application.switchToLoginScreen();
        } else {
            messageLabel.setText("Error while logging out.");
        }
    }

    @FXML
    protected void handleMyReviews(ActionEvent event) {
        if (this.application != null) {
            application.switchToMyReviewsScreen(currentUser);
        } else {
            messageLabel.setText("Error while navigating to My Reviews screen.");
        }
    }
}
package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class CourseReviewController {

    @FXML
    private Label courseInfoLabel, averageRatingLabel, messageLabel;
    @FXML
    private ListView<Review> reviewsListView;
    @FXML
    private TextField ratingField, commentField;

    private Course currentCourse;
    private User currentUser;
    private DatabaseDriver dbDriver;
    private CourseReviewApplication application;

    // Setters for application and database driver
    public void setApplication(CourseReviewApplication application) {
        this.application = application;
    }

    public void setDatabaseDriver() {
        Configuration configuration = new Configuration();
        this.dbDriver = DatabaseDriver.getInstance(configuration.getDatabaseFilename());
        System.out.println("Database Driver set in CourseReviewController: " + this.dbDriver);
    }

    public void setCurrentCourseAndUser(Course course, User user) {
        this.currentCourse = course;
        this.currentUser = user;
        loadReviews();
    }

    private void loadReviews() {
        try {
            List<Review> reviews = dbDriver.getReviewsForCourse(currentCourse);
            reviewsListView.getItems().setAll(reviews);
            updateCourseInfo();
        } catch (SQLException e) {
            messageLabel.setText("Error loading reviews: " + e.getMessage());
        }
    }

    private void updateCourseInfo() {
        try {
            courseInfoLabel.setText(currentCourse.getSubject() + " " + currentCourse.getCourseNumber()
                    + ": " + currentCourse.getTitle());
            double avgRating = dbDriver.calculateAverageReviewForCourse(currentCourse).orElse(0.0);
            averageRatingLabel.setText(String.format("Average Rating: %.2f", avgRating));
        } catch (SQLException e) {
            messageLabel.setText("Error calculating average rating: " + e.getMessage());
        }
    }

    @FXML
    protected void handleSubmitReview() {
        if (currentUser == null) {
            messageLabel.setText("Error: User not set.");
            return;
        }

        try {
            int rating = parseRating();
//            if (rating == -1) {
//                messageLabel.setText("Invalid rating. Please enter a number between 1 and 5.");
//                return;
//            }
            String comment = commentField.getText();

            Optional<Review> existingReview = dbDriver.getReviewFromUserForCourse(currentUser, currentCourse);
            Review review = new Review(0, currentUser.getUserID(), currentCourse.getCourseID(),
                    rating, new Timestamp(System.currentTimeMillis()), comment);

            if (existingReview.isPresent()) {
                dbDriver.editReview(existingReview.get(), review);
            } else {
                System.out.println("Add Review:" + currentCourse.getCourseID());
                System.out.println(currentUser.getUserID());
                dbDriver.addReview(review);
            }

            dbDriver.commit();
            loadReviews(); // Reload reviews
            messageLabel.setText("Review submitted successfully.");
        } catch (NumberFormatException ne) {
            messageLabel.setText("Invalid rating. Please enter a number between 1 and 5.");
        } catch (IllegalArgumentException e) {
            messageLabel.setText("Illegal Argument Exception");
        } catch (SQLException e) {
            messageLabel.setText("Error submitting review: " + e.getMessage());
        }
    }

    private int parseRating() {
        try {
            int rating = Integer.parseInt(ratingField.getText());
            if (rating < 1 || rating > 5) {
                messageLabel.setText("Rating must be between 1 and 5.");
            }
            return rating;
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid rating format.");
        }
        return -1;
    }

    @FXML
    protected void handleDeleteReview() {
        try {
            dbDriver.removeReview(currentCourse, currentUser);
            dbDriver.commit();
            loadReviews();
            messageLabel.setText("Review deleted successfully.");
        } catch (SQLException e) {
            messageLabel.setText("Error deleting review: " + e.getMessage());
        }
    }

    @FXML
    protected void handleBack() {
        application.switchToCourseSearch(currentUser);
    }
}

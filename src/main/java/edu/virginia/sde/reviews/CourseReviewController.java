package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

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

    public void setDatabaseDriver(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
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
        try {
            int rating = parseRating();
            String comment = commentField.getText();
            Review review = new Review(0, currentCourse.getCourseID(), currentUser.getUserID(),
                    rating, new Timestamp(System.currentTimeMillis()), comment);

            if (dbDriver.getReviewFromUserForCourse(currentUser, currentCourse).isPresent()) {
                dbDriver.editReview(null, review); // Should this be oldReview, review?
            } else {
                dbDriver.addReview(review);
            }

            dbDriver.commit();
            loadReviews(); // Reload reviews
            messageLabel.setText("Review submitted successfully.");
        } catch (SQLException | IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private int parseRating() throws IllegalArgumentException {
        int rating = Integer.parseInt(ratingField.getText());
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        return rating;
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
        application.switchToCourseSearch();
    }
}

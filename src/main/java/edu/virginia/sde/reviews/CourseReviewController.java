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
    private TextField ratingField;
    @FXML
    private TextArea commentField;

    private Course currentCourse;
    private User currentUser;
    private DatabaseDriver dbDriver;
    private CourseReviewApplication application;
    private String previousScene;
    private boolean editMode = false;

    public void setApplication(CourseReviewApplication application) {
        this.application = application;
    }

    public void setDatabaseDriver() {
        Configuration configuration = new Configuration();
        this.dbDriver = DatabaseDriver.getInstance(configuration.getDatabaseFilename());
    }

    public void setCurrentCourseAndUser(Course course, User user, String previousScene) {
        this.currentCourse = course;
        this.currentUser = user;
        this.previousScene = previousScene;
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
            Integer rating = parseRating();
            if (rating == null) return;
            String comment = commentField.getText();

            Optional<Review> existingReview = dbDriver.getReviewFromUserForCourse(currentUser, currentCourse);
            Review review = new Review(0, currentUser.getUserID(), currentCourse.getCourseID(),
                    rating, new Timestamp(System.currentTimeMillis()), comment);

            if (existingReview.isPresent()) {
                if (!editMode) {
                    messageLabel.setText("You have already submitted a review for this course.");
                    return;
                } else {
                    dbDriver.editReview(existingReview.get(), review);
                    messageLabel.setText("Review updated successfully.");
                }
            } else {
                dbDriver.addReview(review);
                messageLabel.setText("Review submitted successfully.");
            }

            editMode = false;
            loadReviews(); // Reload reviews
        } catch (SQLException e) {
            messageLabel.setText("Error submitting review: " + e.getMessage());
        }
    }

    private Integer parseRating() {
        if (ratingField.getText().trim().isEmpty()) {
            messageLabel.setText("Please enter a rating.");
            return null;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingField.getText());
            if (rating < 1 || rating > 5) {
                messageLabel.setText("Rating must be between 1 and 5.");
                return null;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid rating format. Please enter a number between 1 and 5.");
            return null;
        }
        return rating;
    }

    @FXML
    protected void handleEditReview() {
        Review selectedReview = reviewsListView.getSelectionModel().getSelectedItem();

        if (selectedReview == null) {
            messageLabel.setText("No review selected.");
            return;
        }
        if (currentUser == null) {
            messageLabel.setText("User not set.");
            return;
        }

        // Check if the selected review belongs to the current user
        if (selectedReview.getUserID() != currentUser.getUserID()) {
            messageLabel.setText("Cannot edit other users' reviews.");
            return;
        }

        editMode = true;
        messageLabel.setText("");
        // Load existing review details into the form
        ratingField.setText(String.valueOf(selectedReview.getRating()));
        commentField.setText(selectedReview.getComment());
    }

    @FXML
    protected void handleDeleteReview() {
        Review selectedReview = reviewsListView.getSelectionModel().getSelectedItem();

        if (selectedReview == null) {
            messageLabel.setText("No review selected.");
            return;
        }
        if (currentUser == null) {
            messageLabel.setText("User not set.");
            return;
        }

        // Check if the selected review belongs to the current user
        if (selectedReview.getUserID() != currentUser.getUserID()) {
            messageLabel.setText("Cannot delete other users' reviews.");
            return;
        }

        try {
            dbDriver.removeReview(currentCourse, currentUser);
            loadReviews();
            messageLabel.setText("Review deleted successfully.");
        } catch (SQLException e) {
            messageLabel.setText("Error deleting review: " + e.getMessage());
        }
    }

    @FXML
    protected void handleBack() {
        if ("MyReviews".equals(previousScene)) {
            application.switchToMyReviewsScreen(currentUser);
        } else {
            application.switchToCourseSearch(currentUser);
        }
    }
}

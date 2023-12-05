package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class MyReviewsController {

    public Button selectReviewButton;
    @FXML
    private ListView<Review> reviewsListView;
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
        loadUserReviews();
    }

    private void loadUserReviews() {
        try {
            List<Review> userReviews = dbDriver.getReviewsFromUser(currentUser);
            reviewsListView.getItems().setAll(userReviews);
            setCustomCellFactory();

        } catch (SQLException e) {
            messageLabel.setText("Error loading reviews: " + e.getMessage());
        }
    }

    private void setCustomCellFactory() {
        reviewsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null) {
                    setText(null);
                } else {
                    displayReview(review);
                }
            }

            private void displayReview(Review review) {
                try {
                    Course course = dbDriver.getCourseById(review.getCourseID()).orElse(null);
                    if (course != null) {
                        String displayText = String.format("%s %d: %s\nRating: %d/5\nComment: %s",
                                course.getSubject(), course.getCourseNumber(), course.getTitle(),
                                review.getRating(), review.getComment());
                        setText(displayText);
                    }
                } catch (SQLException e) {
                    setText("Error loading course details.");
                }
            }
        });
    }

    @FXML
    protected void handleReviewSelect() {
        Review selectedReview = reviewsListView.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            navigateToCourseReview(selectedReview);
        }
    }

    private void navigateToCourseReview(Review selectedReview) {
        try {
            dbDriver.getCourseById(selectedReview.getCourseID()).ifPresent(course ->
                    application.switchToCourseReviewScreen(course, currentUser, "MyReviews")
            );
        } catch (SQLException e) {
            messageLabel.setText("Error loading course: " + e.getMessage());
        }
    }

    @FXML
    protected void handleBack() {
        application.switchToCourseSearch(currentUser);
    }
}

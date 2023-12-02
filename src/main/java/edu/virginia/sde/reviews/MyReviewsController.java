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
    String previousScene = "CourseSearchScreen";

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
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading reviews: " + e.getMessage());
        }
    }

    @FXML
    protected void handleReviewSelect() {
        Review selectedReview = reviewsListView.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            try {
                dbDriver.getCourseById(selectedReview.getCourseID()).ifPresent(course ->
                        application.switchToCourseReviewScreen(course, currentUser, "MyReviews")
                );
            } catch (SQLException e) {
                e.printStackTrace();
                messageLabel.setText("Error loading course: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleBack() {
        application.switchToCourseSearch(currentUser);
    }
}

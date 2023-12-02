package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class MyReviewsController {

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

    }

    @FXML
    protected void handleReviewSelect() {

    }

    @FXML
    protected void handleBack() {
        application.switchToCourseSearch(currentUser);
    }
}

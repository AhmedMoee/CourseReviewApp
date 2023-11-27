package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class CourseReviewApplication extends Application {
    private DatabaseDriver databaseDriver;
    private Stage primaryStage;
    private CourseSearchController courseSearchController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.databaseDriver = DatabaseDriver.getInstance(new Configuration().getDatabaseFilename());
        databaseDriver.connect();

        this.primaryStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        LoginController loginController = fxmlLoader.getController();
        loginController.setApplication(this);

        stage.setTitle("Course Review Application - Login");
        stage.setScene(scene);
        stage.show();
    }

    public void switchToCourseSearch(User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseSearchScreen.fxml"));
            Parent root = loader.load();

            CourseSearchController controller = loader.getController();
            controller.setApplication(this);
            controller = loader.getController();
            controller.setDatabaseDriver(databaseDriver);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Course Review Application - Course Search");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setDatabaseDriver(databaseDriver);
            loginController.setApplication(this);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Course Review Application - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToCourseReviewScreen(Course selectedCourse, User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseReviewScreen.fxml"));
            Parent root = loader.load();

            CourseReviewController controller = loader.getController();
            controller.setDatabaseDriver(); // Set the database driver
            controller.setApplication(this);
            controller.setCurrentCourseAndUser(selectedCourse, currentUser);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Course Review Application - Course Review");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Close the database connection when the application is stopped
        if (databaseDriver != null) {
            try {
                databaseDriver.disconnect();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

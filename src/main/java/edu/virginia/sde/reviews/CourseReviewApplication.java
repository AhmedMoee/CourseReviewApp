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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        LoginController loginController = fxmlLoader.getController();
        loginController.setApplication(this);

        stage.setTitle("Course Review Application");
        stage.setScene(scene);
        stage.show();
    }

    public void switchToCourseSearch() throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseSearchScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error appropriately
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

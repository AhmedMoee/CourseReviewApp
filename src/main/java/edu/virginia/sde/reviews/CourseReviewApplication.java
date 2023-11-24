package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CourseReviewApplication extends Application {
    private DatabaseDriver databaseDriver;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Course Review Application - Login");
        stage.setScene(scene);
        stage.show();
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

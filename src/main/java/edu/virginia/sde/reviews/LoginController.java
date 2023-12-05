package edu.virginia.sde.reviews;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;
    @FXML
    private AnchorPane rootPane;

    private DatabaseDriver dbDriver;
    private CourseReviewApplication application;
    private User currentUser;
    private CourseSearchController courseSearchController;

    @FXML
    public void initialize() {
        Platform.runLater(() -> rootPane.requestFocus());
    }

    public LoginController() {
        Configuration configuration = new Configuration();
        this.dbDriver = DatabaseDriver.getInstance(configuration.getDatabaseFilename());
        try {
            this.dbDriver.connect();
            this.dbDriver.createTables();
        } catch (SQLException e) {
            messageLabel.setText("Unable to connect to database.");
        }
    }

    public void setApplication(CourseReviewApplication application) {
        this.application = application;
    }

    public void setDatabaseDriver(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @FXML
    public void handleLogin(javafx.event.ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username or password cannot be empty.");
            return;
        }
        else if (password.length() < 8) {
            messageLabel.setText("Password must be at least 8 characters long.");
            return;
        }

        try {
            if (dbDriver.userExists(username)) {
                Optional<String> storedPassword = dbDriver.getPasswordForUser(new User(username, ""));
                if (storedPassword.isPresent() && storedPassword.get().equals(password)) {
                    // Login successful - proceed to next scene
                    messageLabel.setText("Login successful.");
                    User currentUser = dbDriver.getUserByUsername(username).orElseThrow(() ->
                            new SQLException("User not found"));
                    Platform.runLater(() -> {
                        try {
                            application.switchToCourseSearch(currentUser);
                        } catch (Exception e) {
                            messageLabel.setText("Error while switching to course search screen.");
                        }
                    });
                } else {
                    // Invalid password
                    messageLabel.setText("Invalid password. Please try again.");
                }
            } else {
                // User does not exist
                messageLabel.setText("User does not exist. Please create an account.");
            }
        } catch (Exception e) {
            messageLabel.setText("Error while logging in.");
        }
    }

    @FXML
    protected void handleCreateUser(javafx.event.ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username or password cannot be empty.");
            return;
        } else if (password.length() < 8) {
            messageLabel.setText("Password must be at least 8 characters long.");
            return;
        }

        try {
            if (dbDriver.userExists(username)) {
                messageLabel.setText("User already exists. Please login.");
            } else {
                dbDriver.addUser(new User(username, password));
                messageLabel.setText("Registration successful. User created.");
            }
        } catch (SQLException e) {
            try {
                dbDriver.rollback();
            } catch (SQLException ex) {
                messageLabel.setText("Error while creating user.");
            }
            messageLabel.setText("Error while creating user.");
        }

    }

    @FXML
    protected void handleClose() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}

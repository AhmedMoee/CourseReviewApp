package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final DatabaseDriver dbDriver;

    public LoginController() {
        Configuration configuration = new Configuration();
        this.dbDriver = new DatabaseDriver(configuration.getDatabaseFilename());
        try {
            this.dbDriver.connect();
            this.dbDriver.createTables();
        } catch (SQLException e) {
            messageLabel.setText("Unable to connect to database.");
        }
    }

    @FXML
    public void handleLogin(javafx.event.ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        try {
            if (dbDriver.userExists(username)) {
                Optional<String> storedPassword = dbDriver.getPasswordForUser(new User(username, ""));
                if (storedPassword.isPresent() && storedPassword.get().equals(password)) {
                    // Login successful - proceed to next scene
                    messageLabel.setText("Login successful.");
                } else {
                    // Invalid password
                    messageLabel.setText("Invalid username or password.");
                }
            } else {
                // User does not exist
                messageLabel.setText("Invalid username or password.");
            }
        } catch (SQLException e) {
            messageLabel.setText("Error while logging in.");
        }
    }

    @FXML
    protected void handleCreateUser(javafx.event.ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        try {
            if (dbDriver.userExists(username)) {
                messageLabel.setText("User already exists. Please login.");
            } else {
                dbDriver.addUser(new User(username, password));
                dbDriver.commit();
                messageLabel.setText("Registration successful. User created.");
            }
        } catch (SQLException e) {
            try {
                dbDriver.rollback();
            } catch (SQLException ex) {
                messageLabel.setText("Error while creating user.");
            }
            e.printStackTrace();
            messageLabel.setText("Error while creating user.");
        }

    }

    @FXML
    protected void handleClose() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}

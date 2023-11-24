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
    private final String dbPath = "jdbc:sqlite:identifier.sqlite.db";

    public LoginController() {
        this.dbDriver = new DatabaseDriver(dbPath);
        try {
            this.dbDriver.connect();
        } catch (SQLException e) {
            messageLabel.setText("Unable to connect to database.");
        }
    }

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        try {
            if (dbDriver.userExists(username)) {
                Optional<String> storedPassword = dbDriver.getPasswordForUser(new User(0, username, ""));
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
    protected void handleCreateUser(ActionEvent event) {
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
                dbDriver.addUser(new User(0, username, password));
                dbDriver.commit();
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

package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        if (authenticateUser(username, password)) {
            messageLabel.setText("Login successful!");
            // Navigate to the next screen or perform other actions upon successful login
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

    private boolean authenticateUser(String username, String password) {
        // Database URL
        String dbUrl = "jdbc:sqlite:identifier.sqlite.db";

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                // If the query finds a row, the credentials are correct
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // Handle exception - you might want to log this or show an error message
            return false;
        }
    }

    @FXML
    protected void handleCreateUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
    }

    @FXML
    protected void handleClose() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}

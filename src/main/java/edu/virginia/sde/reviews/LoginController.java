package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

package jdm.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import jdm.model.SystemUser;
import jdm.service.SessionContext;
import jdm.service.UserFactory;

public class LoginController {

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label message;

    @FXML
    private void handleLogin() {
        message.setText("");
        UserFactory.login(username.getText(), password.getText()).ifPresentOrElse(user -> {
            SessionContext.setCurrent(user);
            message.setText("Login successful");
            if (user.getRole() == SystemUser.Role.DOCTOR) {
                SceneManager.switchScene("doctor.fxml", "JDM — Doctor dashboard");
            } else {
                SceneManager.switchScene("patient.fxml", "JDM — My record");
            }
        }, () -> message.setText("Invalid username or password."));
    }
}

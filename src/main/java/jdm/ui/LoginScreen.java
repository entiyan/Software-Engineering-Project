package jdm.ui;

import jdm.model.SystemUser;
import jdm.service.DataStore;
import jdm.util.Display;

import java.util.Optional;

public class LoginScreen {

    public Optional<SystemUser> show() {
        Display.header("JDM Patient Management System");
        System.out.println();
        Display.info("Please log in to continue.");
        System.out.println();
        Display.menuItem(1, "Log in as Doctor");
        Display.menuItem(2, "Log in as Patient");
        Display.menuItem(0, "Exit");
        System.out.println();

        int choice = InputHelper.promptInt("Select: ");
        return switch (choice) {
            case 1 -> loginDoctor();
            case 2 -> loginPatient();
            default -> Optional.empty();
        };
    }

    private Optional<SystemUser> loginDoctor() {
        System.out.println();
        Display.section("Doctor Login");
        String username = InputHelper.prompt("  Username : ");
        String password = InputHelper.prompt("  Password : ");
        Optional<SystemUser> user = DataStore.getInstance()
                .authenticateDoctor(username, password);
        if (user.isEmpty()) Display.error("Invalid credentials.");
        return user;
    }

    private Optional<SystemUser> loginPatient() {
        System.out.println();
        Display.section("Patient Login");
        Display.info("Use your Patient ID and password (default: patient123).");
        String id  = InputHelper.prompt("  Patient ID: ");
        String pwd = InputHelper.prompt("  Password  : ");
        Optional<SystemUser> user = DataStore.getInstance()
                .authenticatePatient(id, pwd);
        if (user.isEmpty()) Display.error("Invalid credentials.");
        return user;
    }
}

package jdm;

import jdm.model.*;
import jdm.service.DataLoader;
import jdm.ui.*;
import jdm.util.Display;

import java.util.Optional;

/**
 * JDM Patient Management System
 * Entry point – loads data, handles login, routes to correct dashboard.
 */
public class Main {

    public static void main(String[] args) {
        // Determine data directory (default: current directory, or first arg)
        String dataDir = args.length > 0 ? args[0] : ".";

        System.out.println();
        Display.header("JDM Patient Management System — Starting Up");
        System.out.println(Display.BLUE + "  Loading data from: " + dataDir + Display.RESET);
        System.out.println();

        try {
            DataLoader.load(dataDir);
        } catch (Exception e) {
            Display.error("Failed to load data: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        Display.success("Data loaded successfully.");
        System.out.println();

        // Login loop
        LoginScreen    loginScreen    = new LoginScreen();
        DoctorDashboard doctorDash   = new DoctorDashboard();
        PatientDashboard patientDash = new PatientDashboard();

        while (true) {
            Optional<SystemUser> maybeUser = loginScreen.show();

            if (maybeUser.isEmpty()) {
                Display.info("Exiting. Goodbye!");
                break;
            }

            SystemUser user = maybeUser.get();
            Display.success("Welcome, " + user.getDisplayName() + "! Role: " + user.getRole());

            if (user.getRole() == SystemUser.Role.DOCTOR) {
                doctorDash.show(user);
            } else {
                patientDash.show(user);
            }
        }
    }
}

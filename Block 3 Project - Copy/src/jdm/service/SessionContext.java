package jdm.service;

import jdm.model.SystemUser;

/**
 * Holds the logged-in user for the current JavaFX session.
 */
public final class SessionContext {

    private static SystemUser current;

    private SessionContext() {}

    public static void setCurrent(SystemUser user) {
        current = user;
    }

    public static SystemUser getCurrent() {
        return current;
    }

    public static void clear() {
        current = null;
    }
}

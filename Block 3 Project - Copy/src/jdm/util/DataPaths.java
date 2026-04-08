package jdm.util;

import java.io.File;

/**
 * Resolves the CSV data directory for {@link jdm.service.DataLoader}.
 */
public final class DataPaths {

    private DataPaths() {}

    public static String resolveDataDir(String[] args) {
        if (args != null && args.length > 0 && args[0] != null && !args[0].isBlank()) {
            return args[0].trim();
        }
        String env = System.getenv("JDM_DATA_DIR");
        if (env != null && !env.isBlank()) return env.trim();
        if (new File("data").isDirectory()) return "data";
        File up = new File(".." + File.separator + "data");
        if (up.isDirectory()) return up.getPath();
        return "data";
    }
}

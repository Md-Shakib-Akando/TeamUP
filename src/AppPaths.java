import java.io.File;

/**
 * Where TeamUp writes its own files.
 *
 * Installed app (built with package.bat / jpackage — the launcher sets "jpackage.app-path"):
 *   the install folder is read-only, so
 *     offline cache  -> %APPDATA%\TeamUp\data
 *     backups        -> %APPDATA%\TeamUp\backups
 *     admin exports  -> Documents\TeamUp   (easy for the admin to find)
 * Running from the project folder (run.bat, tests): everything stays where it always was.
 * -Dteamup.home=<folder> puts all of it in one folder instead.
 */
public final class AppPaths {

    private AppPaths() {}

    /** True when running as the installed TeamUp app. */
    public static boolean isInstalled() {
        return System.getProperty("jpackage.app-path") != null;
    }

    private static String override() {
        String h = System.getProperty("teamup.home");
        return h == null || h.trim().isEmpty() ? null : h.trim();
    }

    private static File base() {
        if (override() != null) return new File(override());
        if (!isInstalled()) return new File(".");
        String appData = System.getenv("APPDATA");
        File root = appData != null && !appData.isEmpty() ? new File(appData) : new File(System.getProperty("user.home"));
        return new File(root, "TeamUp");
    }

    /** Offline login cache and early-access leads (users.csv, leads.csv). */
    public static File dataDir() {
        return new File(base(), "data");
    }

    /** Admin "Create backup" files. */
    public static File backupsDir() {
        return new File(base(), "backups");
    }

    /** Admin CSV / JSON exports. Creates the folder if needed. */
    public static File exportFile(String name) {
        File dir;
        if (override() != null) dir = base();
        else if (!isInstalled()) dir = new File(".");
        else dir = new File(new File(System.getProperty("user.home"), "Documents"), "TeamUp");
        dir.mkdirs();
        return new File(dir, name);
    }
}

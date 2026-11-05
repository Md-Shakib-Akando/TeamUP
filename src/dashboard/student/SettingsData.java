import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dynamic data model for the Settings page and all 4 sub-views:
 * 1. Account
 * 2. Profile & Privacy
 * 3. Collaboration Preferences
 * 4. Danger Zone
 * Compatible with Java 8+.
 */
public class SettingsData {

    public enum SettingsTab {
        ACCOUNT("Account"),
        PROFILE_PRIVACY("Profile & Privacy"),
        COLLAB_PREFERENCES("Collaboration Preferences"),
        DANGER_ZONE("Danger Zone");

        public final String label;
        SettingsTab(String label) {
            this.label = label;
        }
    }

    public SettingsTab activeTab = SettingsTab.ACCOUNT;

    // =========================================================================
    // TAB 1: ACCOUNT DETAILS
    // =========================================================================
    public String fullName = "Alex Morgan Rivera";
    public String originalFullName = "Alex Morgan";
    public String username = "alexmorgan";
    public String email = "alex.morgan@example.com";
    public boolean hasUnsavedChanges = true;
    public String passwordLastChanged = "September 12, 2026";
    public String memberSince = "March 12, 2026";
    public String accountId = "TU-001482";

    // Text field focus tracking
    public enum ActiveField {
        NONE, FULL_NAME, USERNAME, EMAIL, ADD_CATEGORY
    }
    public ActiveField focusedField = ActiveField.NONE;

    // =========================================================================
    // TAB 2: PROFILE & PRIVACY
    // =========================================================================
    public enum ProfileVisibility {
        PUBLIC("Public", "Anyone on TeamUp can find your profile and invite you to collaborate."),
        TEAM_MEMBERS("Team members", "Only people in your teams can see your full profile."),
        PRIVATE("Private", "Your profile is hidden from discovery and new collaboration requests.");

        public final String title;
        public final String desc;
        ProfileVisibility(String title, String desc) {
            this.title = title;
            this.desc = desc;
        }
    }
    public ProfileVisibility visibility = ProfileVisibility.PUBLIC;
    public boolean showSkills = true;
    public boolean showActivity = false;

    // =========================================================================
    // TAB 3: COLLABORATION PREFERENCES
    // =========================================================================
    public final List<String> categories = new ArrayList<String>(Arrays.asList(
            "Web Development",
            "Sustainability",
            "AI & Machine Learning"
    ));

    public boolean roleFrontendDev = true;
    public boolean roleUiDev = true;
    public boolean roleBackendDev = false;
    public boolean roleProjectLead = false;

    public String weeklyAvailability = "10–15 hours per week";
    public String preferredCommitment = "1–3 months";

    public enum CollabMode {
        REMOTE("Remote"),
        IN_PERSON("In person"),
        EITHER("Either works");

        public final String label;
        CollabMode(String label) {
            this.label = label;
        }
    }
    public CollabMode collabMode = CollabMode.REMOTE;
    public String timeZoneInfo = "Your time zone: Central Time (UTC–05:00). You can update this on your profile.";

    // =========================================================================
    // TAB 4: DANGER ZONE
    // =========================================================================
    public String ownedProject = "Campus Carbon Tracker";
    public String supportEmail = "support@teamup.com";

    // Dynamic operations
    public void saveAccountChanges() {
        this.originalFullName = this.fullName;
        this.hasUnsavedChanges = false;
    }

    public void cancelAccountChanges() {
        this.fullName = this.originalFullName;
        this.hasUnsavedChanges = false;
    }

    public void removeCategory(int index) {
        if (index >= 0 && index < categories.size()) {
            categories.remove(index);
        }
    }

    public void addCategory(String cat) {
        if (cat != null && !cat.trim().isEmpty() && !categories.contains(cat.trim())) {
            categories.add(cat.trim());
        }
    }

    public void toggleWeeklyAvailability() {
        if ("10–15 hours per week".equals(weeklyAvailability)) {
            weeklyAvailability = "15–20 hours per week";
        } else if ("15–20 hours per week".equals(weeklyAvailability)) {
            weeklyAvailability = "5–10 hours per week";
        } else {
            weeklyAvailability = "10–15 hours per week";
        }
    }

    public void toggleCommitment() {
        if ("1–3 months".equals(preferredCommitment)) {
            preferredCommitment = "3–6 months";
        } else if ("3–6 months".equals(preferredCommitment)) {
            preferredCommitment = "< 1 month";
        } else {
            preferredCommitment = "1–3 months";
        }
    }
}


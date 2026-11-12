import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dynamic data model for the User Profile ("My Profile") dashboard view.
 * Fully aligned with Theme.java landing page color tokens.
 * Compatible with Java 8+.
 */
public class ProfileData {

    public static class ProfileProject {
        public String title;
        public String role;
        public String description;
        public String status;
        public List<String> tags;
        public int teamSize;
        public int maxTeamSize;
        public Color accentColor;

        public ProfileProject(String title, String role, String description, String status,
                              List<String> tags, int teamSize, int maxTeamSize, Color accentColor) {
            this.title = title;
            this.role = role;
            this.description = description;
            this.status = status;
            this.tags = tags;
            this.teamSize = teamSize;
            this.maxTeamSize = maxTeamSize;
            this.accentColor = accentColor;
        }
    }

    public static class ProfileSkill {
        public String name;
        public String category;
        public int endorsements;
        public boolean userEndorsed;

        public ProfileSkill(String name, String category, int endorsements, boolean userEndorsed) {
            this.name = name;
            this.category = category;
            this.endorsements = endorsements;
            this.userEndorsed = userEndorsed;
        }
    }

    public static class PeerReview {
        public String reviewerName;
        public String reviewerInitials;
        public String reviewerRole;
        public String projectName;
        public String feedback;
        public int ratingStars;
        public String date;
        public Color avatarBg;
        public Color avatarFg;

        public PeerReview(String reviewerName, String reviewerInitials, String reviewerRole,
                          String projectName, String feedback, int ratingStars, String date,
                          Color avatarBg, Color avatarFg) {
            this.reviewerName = reviewerName;
            this.reviewerInitials = reviewerInitials;
            this.reviewerRole = reviewerRole;
            this.projectName = projectName;
            this.feedback = feedback;
            this.ratingStars = ratingStars;
            this.date = date;
            this.avatarBg = avatarBg;
            this.avatarFg = avatarFg;
        }
    }

    // Identity & Meta
    public String name = "Alex Morgan";
    public String handle = "@alexmorgan";
    public String headline = "Full-Stack Developer & UI/UX Enthusiast";
    public String university = "University of Washington";
    public String department = "Paul G. Allen School of Computer Science";
    public String gradYear = "Class of 2027";
    public String location = "Seattle, WA · Central Time (UTC-05:00)";
    public String email = "alex.morgan@example.com";
    public String github = "github.com/alexmorgan";
    public String portfolio = "alexmorgan.dev";
    public String bio = "Building software that bridges design and engineering. Passionate about campus sustainability, real-time team collaboration tools, and intuitive human-centered interfaces.";

    public String availabilityStatus = "Available for new teams (10–15 hrs/wk)";

    // Collaboration KPIs
    public int activeProjectsCount = 3;
    public int totalTeammatesCount = 14;
    public int completionRatePercent = 98;
    public float peerRating = 4.9f;
    public int totalReviewsCount = 12;

    // Tabs
    public final List<String> tabs = new ArrayList<String>(Arrays.asList("Overview", "Projects (3)", "Skills & Endorsements", "Peer Reviews (12)"));
    public int activeTabIndex = 0;

    // Collections
    public final List<ProfileProject> projects = new ArrayList<ProfileProject>();
    public final List<ProfileSkill> skills = new ArrayList<ProfileSkill>();
    public final List<PeerReview> reviews = new ArrayList<PeerReview>();

    public ProfileData() {
        resetToDefault();
    }

    public void resetToDefault() {
        // Projects aligned with Theme tokens
        projects.clear();
        projects.add(new ProfileProject(
                "CampusConnect", "Lead Full-Stack Dev",
                "Skill-based peer matchmaking and workspace desktop application for student project teams.",
                "Active · Sprint 3",
                new ArrayList<String>(Arrays.asList("Java2D", "Architecture", "REST", "AWT")),
                5, 7, Theme.BRAND_PINK
        ));
        projects.add(new ProfileProject(
                "GreenRoute", "Frontend Engineer",
                "Campus eco-navigation mapping and carbon tracking tool for sustainable student commutes.",
                "Active · Milestone 2",
                new ArrayList<String>(Arrays.asList("React", "Maps API", "Node.js")),
                4, 6, Theme.BRAND_PINK
        ));
        projects.add(new ProfileProject(
                "StudySync", "Core Contributor",
                "Collaborative study session scheduling and whiteboard sharing platform for research groups.",
                "Completed · Q3",
                new ArrayList<String>(Arrays.asList("TypeScript", "PostgreSQL", "WebSockets")),
                3, 5, Theme.BRAND_PINK
        ));

        // Skills
        skills.clear();
        skills.add(new ProfileSkill("Java", "Languages", 18, true));
        skills.add(new ProfileSkill("React", "Frameworks", 15, true));
        skills.add(new ProfileSkill("TypeScript", "Languages", 14, false));
        skills.add(new ProfileSkill("UI/UX Design", "Design", 12, true));
        skills.add(new ProfileSkill("Node.js", "Frameworks", 11, false));
        skills.add(new ProfileSkill("PostgreSQL", "Database", 9, false));
        skills.add(new ProfileSkill("Figma", "Design", 13, true));
        skills.add(new ProfileSkill("REST APIs", "Tools", 16, true));

        // Peer reviews
        reviews.clear();
        reviews.add(new PeerReview(
                "Priya Shah", "PS", "Design Lead", "CampusConnect",
                "Alex has exceptional frontend engineering skill and always respects the design specs down to the pixel. Fantastic teammate to collaborate with!",
                5, "Oct 18, 2026", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET
        ));
        reviews.add(new PeerReview(
                "Daniel Kim", "DK", "Backend Dev", "CampusConnect",
                "Clear communication, robust architecture, and always delivers sprint tasks on time. Made API integrations completely painless.",
                5, "Oct 14, 2026", Theme.PASTEL_MINT, Theme.SUCCESS_GREEN
        ));
        reviews.add(new PeerReview(
                "Maya Patel", "MP", "Project Lead", "GreenRoute",
                "Alex solved our map rendering performance bottle-necks in record time. Highly recommended for any engineering team!",
                5, "Sep 28, 2026", Theme.PASTEL_SUNNY, Theme.HIGHLIGHT_WARM
        ));
    }

    public void toggleEndorsement(int index) {
        if (index >= 0 && index < skills.size()) {
            ProfileSkill s = skills.get(index);
            s.userEndorsed = !s.userEndorsed;
            s.endorsements += s.userEndorsed ? 1 : -1;
        }
    }

    public void cycleAvailability() {
        if (availabilityStatus.contains("10–15")) {
            availabilityStatus = "Available for new teams (15–20 hrs/wk)";
        } else if (availabilityStatus.contains("15–20")) {
            availabilityStatus = "Available for new teams (5–10 hrs/wk)";
        } else if (availabilityStatus.contains("5–10")) {
            availabilityStatus = "Limited availability (< 5 hrs/wk)";
        } else if (availabilityStatus.contains("Limited")) {
            availabilityStatus = "Not looking for new teams";
        } else {
            availabilityStatus = "Available for new teams (10–15 hrs/wk)";
        }
    }

    public void addSkill(String name, String category) {
        if (name != null && !name.trim().isEmpty()) {
            for (ProfileSkill s : skills) {
                if (s.name.equalsIgnoreCase(name.trim())) {
                    s.endorsements++;
                    s.userEndorsed = true;
                    return;
                }
            }
            skills.add(new ProfileSkill(name.trim(), category, 1, true));
        }
    }

    public String addNextSkill() {
        String[][] pool = {
            {"Docker", "DevOps"},
            {"Spring Boot", "Backend"},
            {"GraphQL", "Full Stack"},
            {"Kubernetes", "DevOps"},
            {"Redis", "Database"},
            {"TypeScript", "Frontend"},
            {"PostgreSQL", "Database"}
        };
        for (String[] entry : pool) {
            boolean exists = false;
            for (ProfileSkill s : skills) {
                if (s.name.equalsIgnoreCase(entry[0])) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                skills.add(new ProfileSkill(entry[0], entry[1], 1, true));
                return entry[0];
            }
        }
        String custom = "Skill #" + (skills.size() + 1);
        skills.add(new ProfileSkill(custom, "General", 1, true));
        return custom;
    }

    public void addReview(String reviewerName, String initials, String role, String project, String feedback, int stars) {
        reviews.add(0, new PeerReview(reviewerName, initials, role, project, feedback, stars, "Just now", Theme.PASTEL_PINK, Theme.BRAND_PINK));
        totalReviewsCount = reviews.size();
        tabs.set(3, "Peer Reviews (" + totalReviewsCount + ")");
    }
}

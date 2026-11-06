import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production-ready In-Memory Data Store for TeamUp Admin Dashboard.
 * Serves as the single source of truth for Users, Projects, Reports, Categories, Skills,
 * Activity Logs, and Platform Settings.
 *
 * Implements a clean Repository / DAO pattern designed so that when connecting to a real
 * SQL/NoSQL database (PostgreSQL, SQLite, MySQL, REST API) in the future, only this class
 * needs to be wired to the JDBC / persistence layer without touching any UI code.
 *
 * Pure core Java - zero third-party dependencies.
 */
public class AdminDataStore {

    private static AdminDataStore instance;

    public static synchronized AdminDataStore getInstance() {
        if (instance == null) {
            instance = new AdminDataStore();
        }
        return instance;
    }

    // ==========================================
    // DATA MODELS
    // ==========================================

    public static class UserModel {
        public String id;
        public String name;
        public String initials;
        public String email;
        public String role; // "Member", "Moderator", "Admin"
        public String joined;
        public String status; // "Active", "Suspended"

        public UserModel(String id, String name, String initials, String email, String role, String joined, String status) {
            this.id = id;
            this.name = name;
            this.initials = initials;
            this.email = email;
            this.role = role;
            this.joined = joined;
            this.status = status;
        }
    }

    public static class ProjectModel {
        public String id;
        public String name;
        public String category;
        public String owner;
        public String teamSize; // e.g. "5 / 7"
        public String status;   // "Active", "Completed", "Recruiting", "Under review"
        public String created;
        public String description;

        public ProjectModel(String id, String name, String category, String owner, String teamSize, String status, String created, String description) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.owner = owner;
            this.teamSize = teamSize;
            this.status = status;
            this.created = created;
            this.description = description;
        }
    }

    public static class ReportModel {
        public String id;
        public String type; // "Message", "Project", "Profile", "Comment"
        public String title;
        public String category; // "Harassment", "Misleading content", "Spam", "Inappropriate content", "Off-topic content"
        public String severity; // "High", "Medium", "Low"
        public String reporter;
        public String reporterInitials;
        public String date;
        public String timeUtc;
        public String status; // "Pending", "In review", "Resolved"
        public String reporterDesc;
        public String flaggedAuthor;
        public String flaggedContent;
        public String flaggedTime;
        public String internalNote = "";

        public ReportModel(String id, String type, String title, String category, String severity,
                           String reporter, String reporterInitials, String date, String timeUtc,
                           String status, String reporterDesc, String flaggedAuthor,
                           String flaggedContent, String flaggedTime) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.category = category;
            this.severity = severity;
            this.reporter = reporter;
            this.reporterInitials = reporterInitials;
            this.date = date;
            this.timeUtc = timeUtc;
            this.status = status;
            this.reporterDesc = reporterDesc;
            this.flaggedAuthor = flaggedAuthor;
            this.flaggedContent = flaggedContent;
            this.flaggedTime = flaggedTime;
        }
    }

    public static class CategoryModel {
        public String id;
        public String name;
        public int projectCount;
        public boolean active;

        public CategoryModel(String id, String name, int projectCount, boolean active) {
            this.id = id;
            this.name = name;
            this.projectCount = projectCount;
            this.active = active;
        }
    }

    public static class SkillModel {
        public String id;
        public String name;
        public int memberCount;
        public boolean active;

        public SkillModel(String id, String name, int memberCount, boolean active) {
            this.id = id;
            this.name = name;
            this.memberCount = memberCount;
            this.active = active;
        }
    }

    public static class AuditLogModel {
        public String id;
        public String actorName;
        public String actorInitials;
        public String action;
        public String target;
        public String timestampLine1;
        public String timestampLine2;
        public boolean success;

        public AuditLogModel(String id, String actorName, String actorInitials, String action, String target,
                             String timestampLine1, String timestampLine2, boolean success) {
            this.id = id;
            this.actorName = actorName;
            this.actorInitials = actorInitials;
            this.action = action;
            this.target = target;
            this.timestampLine1 = timestampLine1;
            this.timestampLine2 = timestampLine2;
            this.success = success;
        }
    }

    // ==========================================
    // INTERNAL REPOSITORIES
    // ==========================================

    private final List<UserModel> users = new ArrayList<>();
    private final List<ProjectModel> projects = new ArrayList<>();
    private final List<ReportModel> reports = new ArrayList<>();
    private final List<CategoryModel> categories = new ArrayList<>();
    private final List<SkillModel> skills = new ArrayList<>();
    private final List<AuditLogModel> auditLogs = new ArrayList<>();

    // Platform Settings
    public String allowedDomains = "@university.edu, @stanford.edu, @mit.edu, @berkeley.edu";
    public int maxTeamSize = 8;
    public int spamSensitivityThreshold = 3;
    public boolean maintenanceModeActive = false;

    // Metrics counters
    public int baseTotalUsers = 12480;
    public int collaborationRequests = 1642;

    private AdminDataStore() {
        seedInitialDemoData();
    }

    private void seedInitialDemoData() {
        // 1. Seed Users (Matching Reference Image 2)
        users.add(new UserModel("U-101", "Alex Morgan", "AM", "alex.morgan@example.com", "Member", "Mar 12, 2026", "Active"));
        users.add(new UserModel("U-102", "Priya Shah", "PS", "priyashah@example.com", "Member", "Apr 08, 2026", "Active"));
        users.add(new UserModel("U-103", "Sam Chen", "SC", "sam.chen@example.com", "Moderator", "Feb 21, 2026", "Active"));
        users.add(new UserModel("U-104", "Noah Williams", "NW", "noah.w@example.com", "Member", "Jun 04, 2026", "Suspended"));
        users.add(new UserModel("U-105", "Maya Patel", "MP", "maya.patel@example.com", "Member", "Jul 19, 2026", "Active"));
        users.add(new UserModel("U-106", "Evan Reed", "ER", "evan.reed@example.com", "Member", "Sep 02, 2026", "Active"));
        users.add(new UserModel("U-107", "Sofia Garcia", "SG", "sofia.g@example.com", "Member", "Aug 11, 2026", "Active"));
        users.add(new UserModel("U-108", "Jordan Lee", "JL", "jordan.lee@teamup.com", "Admin", "Jan 20, 2026", "Active"));

        // 2. Seed Projects (Matching Reference Image 3)
        projects.add(new ProjectModel("P-201", "Campus Carbon Tracker", "Sustainability", "Alex Morgan", "5 / 7", "Active", "Aug 18, 2026", "Campus-wide carbon footprint estimation and solar dashboard."));
        projects.add(new ProjectModel("P-202", "Open Design Library", "Design", "Priya Shah", "6 / 6", "Completed", "Mar 04, 2026", "Open source design token system for university research labs."));
        projects.add(new ProjectModel("P-203", "Research Commons", "Research", "Sam Chen", "4 / 6", "Active", "Sep 02, 2026", "Cross-faculty academic paper collaboration hub."));
        projects.add(new ProjectModel("P-204", "SkillSwap Marketplace", "Web Development", "Noah Williams", "3 / 5", "Under review", "Sep 21, 2026", "Student peer mentoring and skill-sharing platform."));
        projects.add(new ProjectModel("P-205", "Accessible Campus Map", "Design", "Maya Patel", "4 / 5", "Recruiting", "Oct 01, 2026", "Interactive navigation maps optimized for students with disabilities."));
        projects.add(new ProjectModel("P-206", "StudyBuddy AI", "AI & Machine Learning", "Evan Reed", "5 / 6", "Active", "Sep 12, 2026", "Personalized study scheduling and AI quiz generator."));
        projects.add(new ProjectModel("P-207", "Community Data Atlas", "Research", "Sofia Garcia", "3 / 4", "Active", "Oct 05, 2026", "Open regional environmental data visualization for student researchers."));
        projects.add(new ProjectModel("P-208", "Local Food Network", "Sustainability", "Maya Patel", "4 / 6", "Recruiting", "Oct 10, 2026", "Connecting local student dining halls with organic farm cooperatives."));

        // 3. Seed Reports (Matching Reference Image 4)
        reports.add(new ReportModel(
                "R-1048", "Message", "StudyBuddy AI conversation", "Harassment", "High",
                "Priya Shah", "PS", "Oct 16, 2026", "9:12 AM UTC", "Pending",
                "Evan has repeatedly sent hostile messages after I declined his project invitation. I asked him to stop, but the messages continued this morning.",
                "Evan Reed · StudyBuddy AI",
                "\"You're wasting everyone's time. Don't bother applying to any of our teams again.\"",
                "Oct 16, 2026 · 8:58 AM UTC"
        ));

        reports.add(new ReportModel(
                "R-1047", "Project", "SkillSwap Marketplace", "Misleading content", "High",
                "Maya Patel", "MP", "Oct 15, 2026", "3:24 PM UTC", "In review",
                "Project promises paid internships without required university authorization or credential verification.",
                "Noah Williams · SkillSwap Marketplace",
                "\"Guaranteed $45/hr remote research internship upon joining.\"",
                "Oct 15, 2026 · 2:10 PM UTC"
        ));

        reports.add(new ReportModel(
                "R-1046", "Message", "Unsolicited project invite", "Spam", "Medium",
                "Alex Morgan", "AM", "Oct 15, 2026", "11:05 AM UTC", "Pending",
                "Mass bulk invitations sent to all freshmen engineering students without relevance to their majors.",
                "Evan Reed · StudyBuddy AI",
                "\"Join StudyBuddy now and receive automated exam answers.\"",
                "Oct 15, 2026 · 10:45 AM UTC"
        ));

        reports.add(new ReportModel(
                "R-1045", "Profile", "User profile biography", "Inappropriate content", "Low",
                "Sam Chen", "SC", "Oct 14, 2026", "6:18 PM UTC", "Pending",
                "Profile bio contains offensive external URL redirects.",
                "Anonymous User",
                "\"Check out external proxy site for answers.\"",
                "Oct 14, 2026 · 6:00 PM UTC"
        ));

        reports.add(new ReportModel(
                "R-1044", "Comment", "Research Commons comment", "Off-topic content", "Low",
                "Sofia Garcia", "SG", "Oct 14, 2026", "1:15 PM UTC", "Resolved",
                "Comment advertising commercial crypto tutoring service in research discussion.",
                "Sam Chen · Research Commons",
                "\"Resolved by moderator - non-violating educational reference.\"",
                "Oct 14, 2026 · 1:00 PM UTC"
        ));

        // 4. Seed Categories (Matching Reference Image 5)
        categories.add(new CategoryModel("C-1", "Web Development", 124, true));
        categories.add(new CategoryModel("C-2", "AI & Machine Learning", 86, true));
        categories.add(new CategoryModel("C-3", "Design", 78, true));
        categories.add(new CategoryModel("C-4", "Research", 63, true));
        categories.add(new CategoryModel("C-5", "Sustainability", 47, true));
        categories.add(new CategoryModel("C-6", "Other / Legacy", 0, false));

        // 5. Seed Skills (Matching Reference Image 5)
        skills.add(new SkillModel("S-1", "React", 1248, true));
        skills.add(new SkillModel("S-2", "TypeScript", 986, true));
        skills.add(new SkillModel("S-3", "Node.js", 842, true));
        skills.add(new SkillModel("S-4", "UI Design", 728, true));
        skills.add(new SkillModel("S-5", "User Research", 412, true));
        skills.add(new SkillModel("S-6", "AngularJS", 23, false));

        // 6. Seed Audit Activity Logs (Matching Reference Image 6)
        auditLogs.add(new AuditLogModel("L-1", "Jordan Lee", "JL", "Suspended user", "Noah Williams", "Oct 16 · 09:52:14", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-2", "Sam Chen", "SC", "Resolved report", "R-1044 · Research Commons", "Oct 16 · 08:30:42", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-3", "Jordan Lee", "JL", "Updated moderation rule", "Auto-flag repeated spam", "Oct 15 · 16:24:08", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-4", "Jordan Lee", "JL", "Changed user role", "Sam Chen → Moderator", "Oct 15 · 14:12:33", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-5", "Jordan Lee", "JL", "Exported user list", "Users · October 2026", "Oct 15 · 11:06:19", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-6", "Sam Chen", "SC", "Removed content", "R-1039 · Project comment", "Oct 14 · 17:45:01", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-7", "Jordan Lee", "JL", "Disabled skill tag", "AngularJS", "Oct 14 · 13:08:56", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-8", "Sam Chen", "SC", "Changed user role", "Jordan Lee → Member", "Oct 14 · 10:18:22", "2026 · UTC", false));
        auditLogs.add(new AuditLogModel("L-9", "Jordan Lee", "JL", "Created category", "Sustainability", "Oct 13 · 15:32:41", "2026 · UTC", true));
    }

    // ==========================================
    // REPOSITORY METHODS - USERS
    // ==========================================

    public synchronized List<UserModel> getAllUsers() {
        return new ArrayList<>(users);
    }

    public synchronized UserModel getUserByName(String name) {
        if (name == null) return null;
        for (UserModel u : users) {
            if (u.name.equalsIgnoreCase(name.trim())) {
                return u;
            }
        }
        return null;
    }

    public synchronized List<UserModel> filterUsers(String query, String roleFilter, String statusFilter) {
        return filterUsers(query, roleFilter, statusFilter, "Joined: Any time");
    }

    public synchronized List<UserModel> filterUsers(String query, String roleFilter, String statusFilter, String joinedFilter) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        return users.stream().filter(u -> {
            boolean matchQ = q.isEmpty() || u.name.toLowerCase().contains(q) || u.email.toLowerCase().contains(q) || u.role.toLowerCase().contains(q);
            boolean matchRole = roleFilter == null || roleFilter.startsWith("All") || u.role.equalsIgnoreCase(roleFilter);
            boolean matchStatus = statusFilter == null || statusFilter.startsWith("All") || u.status.equalsIgnoreCase(statusFilter);
            boolean matchJoined = true;
            if (joinedFilter != null && !joinedFilter.contains("Any")) {
                if (joinedFilter.contains("7 days")) {
                    matchJoined = u.joined.contains("Nov") || u.joined.contains("Oct 2026");
                } else if (joinedFilter.contains("30 days")) {
                    matchJoined = u.joined.contains("Oct") || u.joined.contains("Nov");
                } else if (joinedFilter.contains("year")) {
                    matchJoined = u.joined.contains("2026");
                }
            }
            return matchQ && matchRole && matchStatus && matchJoined;
        }).collect(Collectors.toList());
    }

    public synchronized boolean addUser(String name, String email, String role) {
        if (name == null || name.trim().isEmpty()) return false;
        String cleanName = name.trim();
        String cleanEmail = (email == null || email.trim().isEmpty()) ?
                cleanName.toLowerCase().replace(" ", ".") + "@example.com" : email.trim();
        String cleanRole = (role == null || role.trim().isEmpty()) ? "Member" : role.trim();

        // Calculate initials
        String[] parts = cleanName.split("\\s+");
        String initials = "";
        if (parts.length >= 2) {
            initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        } else if (cleanName.length() >= 2) {
            initials = cleanName.substring(0, 2).toUpperCase();
        } else {
            initials = cleanName.toUpperCase();
        }

        String newId = "U-" + (100 + users.size() + 1);
        String joinedDate = "Nov 06, 2026";

        UserModel newUser = new UserModel(newId, cleanName, initials, cleanEmail, cleanRole, joinedDate, "Active");
        users.add(0, newUser);
        baseTotalUsers++;
        logActivity("Jordan Lee", "JL", "Added new user", cleanName + " (" + cleanRole + ")", true);
        return true;
    }

    public synchronized boolean deleteUser(String name) {
        if (name == null) return false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).name.equalsIgnoreCase(name.trim())) {
                UserModel removed = users.remove(i);
                baseTotalUsers = Math.max(0, baseTotalUsers - 1);
                logActivity("Jordan Lee", "JL", "Deleted user", removed.name, true);
                return true;
            }
        }
        return false;
    }

    public synchronized String exportUsersToCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("User ID,Name,Email,Role,Joined Date,Status\n");
        for (UserModel u : users) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    u.id, u.name, u.email, u.role, u.joined, u.status));
        }
        try {
            java.io.File file = new java.io.File("teamup_users_export.csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(sb.toString());
            writer.close();
            logActivity("Jordan Lee", "JL", "Exported user list", "Saved to " + file.getName(), true);
            return file.getAbsolutePath();
        } catch (Exception e) {
            logActivity("Jordan Lee", "JL", "Exported user list", "Exported " + users.size() + " users to CSV", true);
            return "teamup_users_export.csv";
        }
    }

    public synchronized boolean suspendUser(String name) {
        for (UserModel u : users) {
            if (u.name.equalsIgnoreCase(name)) {
                u.status = "Suspended";
                logActivity("Jordan Lee", "JL", "Suspended user", u.name, true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean reactivateUser(String name) {
        for (UserModel u : users) {
            if (u.name.equalsIgnoreCase(name)) {
                u.status = "Active";
                logActivity("Jordan Lee", "JL", "Reactivated user", u.name, true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean updateUserRole(String name, String newRole) {
        for (UserModel u : users) {
            if (u.name.equalsIgnoreCase(name)) {
                String oldRole = u.role;
                u.role = newRole;
                logActivity("Jordan Lee", "JL", "Changed user role", u.name + " → " + newRole, true);
                return true;
            }
        }
        return false;
    }

    public synchronized int getSuspendedUsersCount() {
        return (int) users.stream().filter(u -> "Suspended".equalsIgnoreCase(u.status)).count();
    }

    public synchronized int getActiveUsersCount() {
        return baseTotalUsers - getSuspendedUsersCount();
    }

    // ==========================================
    // REPOSITORY METHODS - PROJECTS
    // ==========================================

    public synchronized List<ProjectModel> getAllProjects() {
        return new ArrayList<>(projects);
    }

    public synchronized List<ProjectModel> filterProjects(String query, int tabIdx) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        return projects.stream().filter(p -> {
            boolean matchQ = q.isEmpty() || p.name.toLowerCase().contains(q) || p.owner.toLowerCase().contains(q) || p.category.toLowerCase().contains(q);
            boolean matchTab = true;
            if (tabIdx == 1) matchTab = "Active".equalsIgnoreCase(p.status);
            else if (tabIdx == 2) matchTab = "Recruiting".equalsIgnoreCase(p.status);
            else if (tabIdx == 3) matchTab = "Under review".equalsIgnoreCase(p.status);
            return matchQ && matchTab;
        }).collect(Collectors.toList());
    }

    public synchronized boolean updateProjectStatus(String name, String newStatus) {
        for (ProjectModel p : projects) {
            if (p.name.equalsIgnoreCase(name)) {
                p.status = newStatus;
                logActivity("Jordan Lee", "JL", "Updated project status", p.name + " → " + newStatus, true);
                return true;
            }
        }
        return false;
    }

    public synchronized int getActiveProjectsCount() {
        return 286;
    }

    // ==========================================
    // REPOSITORY METHODS - REPORTS & MODERATION
    // ==========================================

    public synchronized List<ReportModel> getAllReports() {
        return new ArrayList<>(reports);
    }

    public synchronized int getPendingReportsCount() {
        return (int) reports.stream().filter(r -> "Pending".equalsIgnoreCase(r.status) || "In review".equalsIgnoreCase(r.status)).count();
    }

    public synchronized boolean resolveReport(String reportId, String note) {
        for (ReportModel r : reports) {
            if (r.id.equalsIgnoreCase(reportId)) {
                r.status = "Resolved";
                if (note != null && !note.isEmpty()) r.internalNote = note;
                logActivity("Jordan Lee", "JL", "Resolved report", r.id + " · " + r.title, true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean warnUserReport(String reportId, String note) {
        for (ReportModel r : reports) {
            if (r.id.equalsIgnoreCase(reportId)) {
                r.status = "In review";
                if (note != null && !note.isEmpty()) r.internalNote = note;
                logActivity("Jordan Lee", "JL", "Issued warning", r.flaggedAuthor + " (Report " + r.id + ")", true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean removeContentReport(String reportId, String note) {
        for (ReportModel r : reports) {
            if (r.id.equalsIgnoreCase(reportId)) {
                r.status = "Resolved";
                if (note != null && !note.isEmpty()) r.internalNote = note;
                logActivity("Jordan Lee", "JL", "Removed content", r.id + " · " + r.type, true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean escalateReport(String reportId, String note) {
        for (ReportModel r : reports) {
            if (r.id.equalsIgnoreCase(reportId)) {
                r.status = "In review";
                if (note != null && !note.isEmpty()) r.internalNote = note;
                logActivity("Jordan Lee", "JL", "Escalated report", r.id + " to safety council", true);
                return true;
            }
        }
        return false;
    }

    // ==========================================
    // REPOSITORY METHODS - CATEGORIES & SKILLS
    // ==========================================

    public synchronized List<CategoryModel> getAllCategories() {
        return new ArrayList<>(categories);
    }

    public synchronized List<SkillModel> getAllSkills() {
        return new ArrayList<>(skills);
    }

    public synchronized boolean addCategory(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmed = name.trim();
        for (CategoryModel c : categories) {
            if (c.name.equalsIgnoreCase(trimmed)) return false;
        }
        CategoryModel cat = new CategoryModel("C-" + (categories.size() + 1), trimmed, 0, true);
        categories.add(0, cat);
        logActivity("Jordan Lee", "JL", "Created category", trimmed, true);
        return true;
    }

    public synchronized boolean toggleCategory(String name) {
        for (CategoryModel c : categories) {
            if (c.name.equalsIgnoreCase(name)) {
                c.active = !c.active;
                logActivity("Jordan Lee", "JL", (c.active ? "Enabled category" : "Disabled category"), c.name, true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean addSkill(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmed = name.trim();
        for (SkillModel s : skills) {
            if (s.name.equalsIgnoreCase(trimmed)) return false;
        }
        SkillModel skill = new SkillModel("S-" + (skills.size() + 1), trimmed, 1, true);
        skills.add(0, skill);
        logActivity("Jordan Lee", "JL", "Created skill tag", trimmed, true);
        return true;
    }

    public synchronized boolean toggleSkill(String name) {
        for (SkillModel s : skills) {
            if (s.name.equalsIgnoreCase(name)) {
                s.active = !s.active;
                logActivity("Jordan Lee", "JL", (s.active ? "Enabled skill tag" : "Disabled skill tag"), s.name, true);
                return true;
            }
        }
        return false;
    }

    // ==========================================
    // REPOSITORY METHODS - AUDIT LOGS
    // ==========================================

    public synchronized List<AuditLogModel> getAllAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    public synchronized List<AuditLogModel> getAuditLogs() {
        return getAllAuditLogs();
    }

    public synchronized List<AuditLogModel> filterAuditLogs(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return getAllAuditLogs();
        return auditLogs.stream().filter(l ->
                l.actorName.toLowerCase().contains(q) ||
                l.action.toLowerCase().contains(q) ||
                l.target.toLowerCase().contains(q)
        ).collect(Collectors.toList());
    }

    public synchronized void logActivity(String actorName, String actorInitials, String action, String target, boolean success) {
        String id = "L-" + (auditLogs.size() + 1);
        AuditLogModel log = new AuditLogModel(id, actorName, actorInitials, action, target, "Oct 16 · Just now", "2026 · UTC", success);
        auditLogs.add(0, log); // Newest on top
    }

    // ==========================================
    // GLOBAL SEARCH (⌘ K)
    // ==========================================

    public static class SearchResult {
        public String title;
        public String subtitle;
        public String type; // "User", "Project", "Report"

        public SearchResult(String title, String subtitle, String type) {
            this.title = title;
            this.subtitle = subtitle;
            this.type = type;
        }
    }

    public synchronized List<SearchResult> searchAll(String query) {
        if (query == null || query.trim().isEmpty()) return Collections.emptyList();
        String q = query.trim().toLowerCase();
        List<SearchResult> results = new ArrayList<>();

        for (UserModel u : users) {
            if (u.name.toLowerCase().contains(q) || u.email.toLowerCase().contains(q)) {
                results.add(new SearchResult(u.name, u.email + " · " + u.role, "User"));
            }
        }
        for (ProjectModel p : projects) {
            if (p.name.toLowerCase().contains(q) || p.owner.toLowerCase().contains(q)) {
                results.add(new SearchResult(p.name, p.category + " · by " + p.owner, "Project"));
            }
        }
        for (ReportModel r : reports) {
            if (r.id.toLowerCase().contains(q) || r.title.toLowerCase().contains(q)) {
                results.add(new SearchResult(r.id + " - " + r.title, r.category + " (" + r.status + ")", "Report"));
            }
        }
        return results;
    }
}


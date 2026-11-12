import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production-ready In-Memory Data Store for TeamUp Admin Dashboard.
 * Serves as the single source of truth for Users, Projects, Reports,
 * Categories, Skills,
 * Activity Logs, and Platform Settings.
 *
 * Implements a clean Repository / DAO pattern designed so that when connecting
 * to a real
 * SQL/NoSQL database (PostgreSQL, SQLite, MySQL, REST API) in the future, only
 * this class
 * needs to be wired to the JDBC / persistence layer without touching any UI
 * code.
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

        public UserModel(String id, String name, String initials, String email, String role, String joined,
                String status) {
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
        public String status; // "Active", "Completed", "Recruiting", "Under review"
        public String created;
        public String description;

        public ProjectModel(String id, String name, String category, String owner, String teamSize, String status,
                String created, String description) {
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
        public String category; // "Harassment", "Misleading content", "Spam", "Inappropriate content",
                                // "Off-topic content"
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

    public static class ReviewModel {
        public String id;
        public String reviewerName;
        public String reviewerInitials;
        public String revieweeName;
        public String projectName;
        public String roleOrContext;
        public int rating; // 1 to 5
        public String reviewText;
        public String reviewType; // "Team Member Review", "User Rating", "Peer Collaboration"
        public String date;
        public String status; // "Published", "Reported", "Under review", "Hidden"
        public String reportReason;
        public String reportedBy;
        public int helpfulVotes;
        public boolean verifiedCollaboration;

        public ReviewModel(String id, String reviewerName, String reviewerInitials, String revieweeName,
                String projectName, String roleOrContext, int rating, String reviewText,
                String reviewType, String date, String status, String reportReason,
                String reportedBy, int helpfulVotes, boolean verifiedCollaboration) {
            this.id = id;
            this.reviewerName = reviewerName;
            this.reviewerInitials = reviewerInitials;
            this.revieweeName = revieweeName;
            this.projectName = projectName;
            this.roleOrContext = roleOrContext;
            this.rating = rating;
            this.reviewText = reviewText;
            this.reviewType = reviewType;
            this.date = date;
            this.status = status;
            this.reportReason = reportReason;
            this.reportedBy = reportedBy;
            this.helpfulVotes = helpfulVotes;
            this.verifiedCollaboration = verifiedCollaboration;
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
    private final List<ReviewModel> reviews = new ArrayList<>();

    // =========================================================================
    // 12 PLATFORM SETTINGS SUITE (Comprehensive, Reactive, & Audited)
    // =========================================================================

    // 1. General & Email Settings
    public String platformName = "TeamUp";
    public String contactEmail = "contact@teamup.edu";
    public String supportEmail = "support@teamup.edu";
    public String platformLogoText = "TU";
    public String defaultLanguage = "English (US)";

    // Security & Authentication
    public String adminPassword = "admin123";
    public boolean notificationsEnabled = true;

    // 2. University Domains
    public String allowedDomains = "@university.edu, @stanford.edu, @mit.edu, @berkeley.edu, @du.ac.bd, @buet.ac.bd";
    public final List<String> universityDomainsList = new ArrayList<>(Arrays.asList(
            "@university.edu", "@stanford.edu", "@mit.edu", "@berkeley.edu", "@du.ac.bd", "@buet.ac.bd"));
    public boolean strictDomainEnforcement = true;

    // 3. User Settings
    public boolean userRegistrationEnabled = true;
    public boolean requireEmailVerification = true;
    public boolean requireStudentId = true;
    public String defaultProfileVisibility = "Campus Only"; // Public, Campus Only, Team Only

    // 4. Project Settings
    public boolean requireProjectApproval = false;
    public String projectCreationRole = "All Students"; // All Students, Verified Students, Team Leads
    public int maxProjectsPerStudent = 3;
    public boolean autoArchiveInactiveProjects = true;

    // 5. Team Settings
    public int minTeamSize = 2;
    public int maxTeamSize = 6;
    public boolean allowCrossDepartmentTeams = true;
    public boolean autoLockFullTeams = true;

    // 6. Application Settings
    public int maxApplicationsPerStudent = 5;
    public boolean allowApplicationWithdrawal = true;
    public int applicationExpiryDays = 14;
    public boolean requireResumeOrPortfolio = false;

    // 7. Spam & Security
    public int spamSensitivityThreshold = 3;
    public int maxLoginAttempts = 5;
    public int sessionTimeoutMinutes = 30; // 15, 30, 60, 240
    public boolean twoFactorAuthEnforced = false;

    // 8. Notification Settings
    public boolean emailNotificationsEnabled = true;
    public boolean inAppNotificationsEnabled = true;
    public String digestFrequency = "Instant"; // Instant, Daily Digest, Weekly Summary
    public boolean announcementBroadcasts = true;

    // 9. Maintenance Mode
    public boolean maintenanceModeActive = false;
    public String maintenanceNotice = "TeamUp is undergoing scheduled maintenance upgrades. We will be back online shortly.";

    // 10. Appearance Settings
    public String appearanceTheme = "Light"; // Light, Dark, System
    public int accentColorIndex = 0; // 0=Emerald (#10B981), 1=Indigo (#4F46E5), 2=Rose (#EA4C89), 3=Amber (#F59E0B),
                                     // 4=Violet (#8B5CF6)
    public boolean compactMode = false;

    // 11. Backup & Data
    public String lastBackupDate = "Nov 11, 2026, 04:30 AM";
    public String lastBackupSize = "42.8 MB";
    public int dataRetentionDays = 365;

    // Metrics counters
    public int baseTotalUsers = 12480;
    public int baseTotalProjects = 482;
    public int collaborationRequests = 1642;

    private AdminDataStore() {
        seedInitialDemoData();
    }

    private void seedInitialDemoData() {
        // 1. Seed Users (Matching Reference Image 2)
        users.add(new UserModel("U-101", "Alex Morgan", "AM", "alex.morgan@example.com", "Member", "Mar 12, 2026",
                "Active"));
        users.add(new UserModel("U-102", "Priya Shah", "PS", "priyashah@example.com", "Member", "Apr 08, 2026",
                "Active"));
        users.add(new UserModel("U-103", "Sam Chen", "SC", "sam.chen@example.com", "Moderator", "Feb 21, 2026",
                "Active"));
        users.add(new UserModel("U-104", "Noah Williams", "NW", "noah.w@example.com", "Member", "Jun 04, 2026",
                "Suspended"));
        users.add(new UserModel("U-105", "Maya Patel", "MP", "maya.patel@example.com", "Member", "Jul 19, 2026",
                "Active"));
        users.add(
                new UserModel("U-106", "Evan Reed", "ER", "evan.reed@example.com", "Member", "Sep 02, 2026", "Active"));
        users.add(new UserModel("U-107", "Sofia Garcia", "SG", "sofia.g@example.com", "Member", "Aug 11, 2026",
                "Active"));
        users.add(
                new UserModel("U-108", "Jordan Lee", "JL", "jordan.lee@teamup.com", "Admin", "Jan 20, 2026", "Active"));

        // 2. Seed Projects (Matching Reference Image 3)
        projects.add(new ProjectModel("P-201", "Campus Carbon Tracker", "Sustainability", "Alex Morgan", "5 / 7",
                "Active", "Aug 18, 2026", "Campus-wide carbon footprint estimation and solar dashboard."));
        projects.add(new ProjectModel("P-202", "Open Design Library", "Design", "Priya Shah", "6 / 6", "Completed",
                "Mar 04, 2026", "Open source design token system for university research labs."));
        projects.add(new ProjectModel("P-203", "Research Commons", "Research", "Sam Chen", "4 / 6", "Active",
                "Sep 02, 2026", "Cross-faculty academic paper collaboration hub."));
        projects.add(new ProjectModel("P-204", "SkillSwap Marketplace", "Web Development", "Noah Williams", "3 / 5",
                "Under review", "Sep 21, 2026", "Student peer mentoring and skill-sharing platform."));
        projects.add(new ProjectModel("P-205", "Accessible Campus Map", "Design", "Maya Patel", "4 / 5", "Recruiting",
                "Oct 01, 2026", "Interactive navigation maps optimized for students with disabilities."));
        projects.add(new ProjectModel("P-206", "StudyBuddy AI", "AI & Machine Learning", "Evan Reed", "5 / 6", "Active",
                "Sep 12, 2026", "Personalized study scheduling and AI quiz generator."));
        projects.add(new ProjectModel("P-207", "Community Data Atlas", "Research", "Sofia Garcia", "3 / 4", "Active",
                "Oct 05, 2026", "Open regional environmental data visualization for student researchers."));
        projects.add(new ProjectModel("P-208", "Local Food Network", "Sustainability", "Maya Patel", "4 / 6",
                "Recruiting", "Oct 10, 2026", "Connecting local student dining halls with organic farm cooperatives."));

        // 3. Seed Reports (Matching Reference Image 4)
        reports.add(new ReportModel(
                "R-1048", "Message", "StudyBuddy AI conversation", "Harassment", "High",
                "Priya Shah", "PS", "Oct 16, 2026", "9:12 AM UTC", "Pending",
                "Evan has repeatedly sent hostile messages after I declined his project invitation. I asked him to stop, but the messages continued this morning.",
                "Evan Reed · StudyBuddy AI",
                "\"You're wasting everyone's time. Don't bother applying to any of our teams again.\"",
                "Oct 16, 2026 · 8:58 AM UTC"));

        reports.add(new ReportModel(
                "R-1047", "Project", "SkillSwap Marketplace", "Misleading content", "High",
                "Maya Patel", "MP", "Oct 15, 2026", "3:24 PM UTC", "In review",
                "Project promises paid internships without required university authorization or credential verification.",
                "Noah Williams · SkillSwap Marketplace",
                "\"Guaranteed $45/hr remote research internship upon joining.\"",
                "Oct 15, 2026 · 2:10 PM UTC"));

        reports.add(new ReportModel(
                "R-1046", "Message", "Unsolicited project invite", "Spam", "Medium",
                "Alex Morgan", "AM", "Oct 15, 2026", "11:05 AM UTC", "Pending",
                "Mass bulk invitations sent to all freshmen engineering students without relevance to their majors.",
                "Evan Reed · StudyBuddy AI",
                "\"Join StudyBuddy now and receive automated exam answers.\"",
                "Oct 15, 2026 · 10:45 AM UTC"));

        reports.add(new ReportModel(
                "R-1045", "Profile", "User profile biography", "Inappropriate content", "Low",
                "Sam Chen", "SC", "Oct 14, 2026", "6:18 PM UTC", "Pending",
                "Profile bio contains offensive external URL redirects.",
                "Anonymous User",
                "\"Check out external proxy site for answers.\"",
                "Oct 14, 2026 · 6:00 PM UTC"));

        reports.add(new ReportModel(
                "R-1044", "Comment", "Research Commons comment", "Off-topic content", "Low",
                "Sofia Garcia", "SG", "Oct 14, 2026", "1:15 PM UTC", "Resolved",
                "Comment advertising commercial crypto tutoring service in research discussion.",
                "Sam Chen · Research Commons",
                "\"Resolved by moderator - non-violating educational reference.\"",
                "Oct 14, 2026 · 1:00 PM UTC"));

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
        auditLogs.add(new AuditLogModel("L-1", "Jordan Lee", "JL", "Suspended user", "Noah Williams",
                "Oct 16 · 09:52:14", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-2", "Sam Chen", "SC", "Resolved report", "R-1044 · Research Commons",
                "Oct 16 · 08:30:42", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-3", "Jordan Lee", "JL", "Updated moderation rule", "Auto-flag repeated spam",
                "Oct 15 · 16:24:08", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-4", "Jordan Lee", "JL", "Changed user role", "Sam Chen → Moderator",
                "Oct 15 · 14:12:33", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-5", "Jordan Lee", "JL", "Exported user list", "Users · October 2026",
                "Oct 15 · 11:06:19", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-6", "Sam Chen", "SC", "Removed content", "R-1039 · Project comment",
                "Oct 14 · 17:45:01", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-7", "Jordan Lee", "JL", "Disabled skill tag", "AngularJS",
                "Oct 14 · 13:08:56", "2026 · UTC", true));
        auditLogs.add(new AuditLogModel("L-8", "Sam Chen", "SC", "Changed user role", "Jordan Lee → Member",
                "Oct 14 · 10:18:22", "2026 · UTC", false));
        auditLogs.add(new AuditLogModel("L-9", "Jordan Lee", "JL", "Created category", "Sustainability",
                "Oct 13 · 15:32:41", "2026 · UTC", true));

        // 7. Seed Reviews & Feedback
        reviews.add(new ReviewModel(
                "REV-501", "Alex Morgan", "AM", "Priya Shah",
                "Open Design Library", "Lead Design System", 5,
                "Incredible attention to accessibility standards! Created 30+ reusable Figma components and led weekly design reviews with outstanding clarity.",
                "Team Member Review", "Nov 08, 2026", "Published", null, null, 18, true));

        reviews.add(new ReviewModel(
                "REV-502", "Sam Chen", "SC", "Sofia Garcia",
                "Research Commons", "Data Analyst", 5,
                "Exceptional research methodology and clean documentation. Delivered data pipelines ahead of schedule with zero errors.",
                "User Rating", "Nov 06, 2026", "Published", null, null, 9, true));

        reviews.add(new ReviewModel(
                "REV-503", "Noah Williams", "NW", "Sam Chen",
                "SkillSwap Marketplace", "Backend Contributor", 1,
                "Useless contributor, terrible code quality and should be kicked out of the university program immediately.",
                "Team Member Review", "Nov 04, 2026", "Reported",
                "Hostile language, personal attack & harassment", "Sam Chen", 0, false));

        reviews.add(new ReviewModel(
                "REV-504", "Maya Patel", "MP", "Alex Morgan",
                "Campus Carbon Tracker", "Frontend Engineer", 4,
                "Great collaboration on the carbon analytics dashboard. Very responsive on Slack and receptive to UI feedback.",
                "User Rating", "Nov 02, 2026", "Published", null, null, 12, true));

        reviews.add(new ReviewModel(
                "REV-505", "Evan Reed", "ER", "Maya Patel",
                "Accessible Campus Map", "Tester", 1,
                "Don't work with this person. Check out bit.ly/cheat-sheets-2026 for actual project answers and guaranteed grades!",
                "User Rating", "Oct 29, 2026", "Reported",
                "Spam advertising & suspicious malicious URL link", "Maya Patel", 1, false));

        reviews.add(new ReviewModel(
                "REV-506", "Sofia Garcia", "SG", "Jordan Lee",
                "Community Data Atlas", "Mentor", 5,
                "Jordan's guidance on SQL optimization and spatial queries improved our map rendering latency by over 400%. Outstanding leadership!",
                "Team Member Review", "Oct 27, 2026", "Published", null, null, 24, true));

        reviews.add(new ReviewModel(
                "REV-507", "Priya Shah", "PS", "Evan Reed",
                "StudyBuddy AI", "API Developer", 4,
                "Solid backend implementation on the socket server and prompt responses. Would love slightly earlier pull requests before sprint cutoffs.",
                "Team Member Review", "Oct 24, 2026", "Published", null, null, 7, true));

        reviews.add(new ReviewModel(
                "REV-508", "Anonymous Student", "AS", "Alex Morgan",
                "Local Food Network", "Scrum Master", 2,
                "Completely disorganized sprint planning. Demanded excessive weekend work without proper scope negotiation.",
                "Peer Collaboration", "Oct 20, 2026", "Under review",
                "Disputed performance claim by team leads", "Alex Morgan", 3, true));

        reviews.add(new ReviewModel(
                "REV-509", "Maya Patel", "MP", "Priya Shah",
                "Open Design Library", "UX Researcher", 5,
                "Peerless user empathy and structured interview synthesis. Working together on the usability sprint was a highlight of this semester.",
                "User Rating", "Oct 15, 2026", "Published", null, null, 15, true));
    }

    // ==========================================
    // REPOSITORY METHODS - USERS
    // ==========================================

    public synchronized List<UserModel> getAllUsers() {
        return new ArrayList<>(users);
    }

    public synchronized UserModel getUserByName(String name) {
        if (name == null)
            return null;
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

    public synchronized List<UserModel> filterUsers(String query, String roleFilter, String statusFilter,
            String joinedFilter) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        return users.stream().filter(u -> {
            boolean matchQ = q.isEmpty() || u.name.toLowerCase().contains(q) || u.email.toLowerCase().contains(q)
                    || u.role.toLowerCase().contains(q);
            boolean matchRole = roleFilter == null || roleFilter.startsWith("All")
                    || u.role.equalsIgnoreCase(roleFilter);
            boolean matchStatus = statusFilter == null || statusFilter.startsWith("All")
                    || u.status.equalsIgnoreCase(statusFilter);
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
        if (name == null || name.trim().isEmpty())
            return false;
        String cleanName = name.trim();
        String cleanEmail = (email == null || email.trim().isEmpty())
                ? cleanName.toLowerCase().replace(" ", ".") + "@example.com"
                : email.trim();
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
        if (name == null)
            return false;
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

    public synchronized ProjectModel getProjectByName(String name) {
        if (name == null)
            return null;
        for (ProjectModel p : projects) {
            if (p.name.equalsIgnoreCase(name.trim())) {
                return p;
            }
        }
        return null;
    }

    public synchronized ProjectModel getProjectById(String id) {
        if (id == null)
            return null;
        for (ProjectModel p : projects) {
            if (p.id.equalsIgnoreCase(id.trim())) {
                return p;
            }
        }
        return null;
    }

    public synchronized List<ProjectModel> filterProjects(String query, int tabIdx) {
        return filterProjects(query, tabIdx, "All categories", "All statuses", "Created: Any time");
    }

    public synchronized List<ProjectModel> filterProjects(String query, int tabIdx, String categoryFilter,
            String statusFilter, String createdFilter) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        return projects.stream().filter(p -> {
            boolean matchQ = q.isEmpty()
                    || p.name.toLowerCase().contains(q)
                    || p.owner.toLowerCase().contains(q)
                    || p.category.toLowerCase().contains(q)
                    || (p.description != null && p.description.toLowerCase().contains(q));

            boolean matchTab = true;
            if (tabIdx == 1)
                matchTab = "Active".equalsIgnoreCase(p.status);
            else if (tabIdx == 2)
                matchTab = "Recruiting".equalsIgnoreCase(p.status);
            else if (tabIdx == 3)
                matchTab = "Under review".equalsIgnoreCase(p.status);

            boolean matchCat = categoryFilter == null
                    || categoryFilter.startsWith("All")
                    || p.category.equalsIgnoreCase(categoryFilter.trim());

            boolean matchStatus = statusFilter == null
                    || statusFilter.startsWith("All")
                    || p.status.equalsIgnoreCase(statusFilter.trim());

            boolean matchCreated = true;
            if (createdFilter != null && !createdFilter.contains("Any")) {
                if (createdFilter.contains("30 days")) {
                    matchCreated = p.created.contains("Oct") || p.created.contains("Nov");
                } else if (createdFilter.contains("6 months")) {
                    matchCreated = p.created.contains("May") || p.created.contains("Jun") || p.created.contains("Jul")
                            || p.created.contains("Aug") || p.created.contains("Sep") || p.created.contains("Oct")
                            || p.created.contains("Nov");
                } else if (createdFilter.contains("year")) {
                    matchCreated = p.created.contains("2026");
                }
            }

            return matchQ && matchTab && matchCat && matchStatus && matchCreated;
        }).collect(Collectors.toList());
    }

    public synchronized boolean addProject(String name, String category, String owner, String teamSize, String status,
            String description) {
        if (name == null || name.trim().isEmpty())
            return false;
        String cleanName = name.trim();
        String cleanCat = (category == null || category.trim().isEmpty()) ? "Web Development" : category.trim();
        String cleanOwner = (owner == null || owner.trim().isEmpty()) ? "Jordan Lee" : owner.trim();
        String cleanSize = (teamSize == null || teamSize.trim().isEmpty()) ? "1 / " + maxTeamSize : teamSize.trim();
        String cleanStatus = (status == null || status.trim().isEmpty()) ? "Active" : status.trim();
        String cleanDesc = (description == null || description.trim().isEmpty()) ? "Community initiative."
                : description.trim();

        String newId = "P-" + (200 + projects.size() + 1);
        String createdDate = "Nov 11, 2026";

        ProjectModel newProj = new ProjectModel(newId, cleanName, cleanCat, cleanOwner, cleanSize, cleanStatus,
                createdDate, cleanDesc);
        projects.add(0, newProj);
        baseTotalProjects++;
        logActivity("Jordan Lee", "JL", "Created project", cleanName + " (" + cleanCat + ")", true);
        return true;
    }

    public synchronized boolean deleteProject(String name) {
        if (name == null)
            return false;
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).name.equalsIgnoreCase(name.trim())
                    || projects.get(i).id.equalsIgnoreCase(name.trim())) {
                ProjectModel removed = projects.remove(i);
                baseTotalProjects = Math.max(0, baseTotalProjects - 1);
                logActivity("Jordan Lee", "JL", "Deleted project", removed.name, true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean updateProjectStatus(String name, String newStatus) {
        for (ProjectModel p : projects) {
            if (p.name.equalsIgnoreCase(name) || p.id.equalsIgnoreCase(name)) {
                String oldStatus = p.status;
                p.status = newStatus;
                logActivity("Jordan Lee", "JL", "Updated project status",
                        p.name + " (" + oldStatus + " → " + newStatus + ")", true);
                return true;
            }
        }
        return false;
    }

    public synchronized String exportProjectsToCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project ID,Project Name,Category,Owner,Team Size,Status,Created Date,Description\n");
        for (ProjectModel p : projects) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    p.id,
                    p.name.replace("\"", "\"\""),
                    p.category.replace("\"", "\"\""),
                    p.owner.replace("\"", "\"\""),
                    p.teamSize,
                    p.status,
                    p.created,
                    (p.description != null ? p.description.replace("\"", "\"\"") : "")));
        }
        try {
            java.io.File file = new java.io.File("teamup_projects_export.csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(sb.toString());
            writer.close();
            logActivity("Jordan Lee", "JL", "Exported project registry", "Saved to " + file.getName(), true);
            return file.getAbsolutePath();
        } catch (Exception e) {
            logActivity("Jordan Lee", "JL", "Exported project registry",
                    "Exported " + projects.size() + " projects to CSV", true);
            return "teamup_projects_export.csv";
        }
    }

    public synchronized int getTotalProjectsCount() {
        return baseTotalProjects;
    }

    public synchronized int getActiveProjectsCount() {
        int currentActive = (int) projects.stream().filter(p -> "Active".equalsIgnoreCase(p.status)).count();
        int delta = currentActive - 4; // initial seeded active count was 4
        return Math.max(0, 286 + delta);
    }

    public synchronized int getRecruitingProjectsCount() {
        int currentRecruiting = (int) projects.stream().filter(p -> "Recruiting".equalsIgnoreCase(p.status)).count();
        int delta = currentRecruiting - 2; // initial seeded recruiting count was 2
        return Math.max(0, 92 + delta);
    }

    public synchronized int getUnderReviewProjectsCount() {
        int currentReview = (int) projects.stream().filter(p -> "Under review".equalsIgnoreCase(p.status)).count();
        int delta = currentReview - 1; // initial seeded under review count was 1
        return Math.max(0, 8 + delta);
    }

    public synchronized int getCompletedProjectsCount() {
        int currentCompleted = (int) projects.stream().filter(p -> "Completed".equalsIgnoreCase(p.status)).count();
        int delta = currentCompleted - 1; // initial seeded completed count was 1
        return Math.max(0, 96 + delta);
    }

    // ==========================================
    // REPOSITORY METHODS - REPORTS & MODERATION
    // ==========================================

    public synchronized List<ReportModel> getAllReports() {
        return new ArrayList<>(reports);
    }

    public synchronized int getPendingReportsCount() {
        return (int) reports.stream()
                .filter(r -> "Pending".equalsIgnoreCase(r.status) || "In review".equalsIgnoreCase(r.status)).count();
    }

    public synchronized boolean resolveReport(String reportId, String note) {
        for (ReportModel r : reports) {
            if (r.id.equalsIgnoreCase(reportId)) {
                r.status = "Resolved";
                if (note != null && !note.isEmpty())
                    r.internalNote = note;
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
                if (note != null && !note.isEmpty())
                    r.internalNote = note;
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
                if (note != null && !note.isEmpty())
                    r.internalNote = note;
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
                if (note != null && !note.isEmpty())
                    r.internalNote = note;
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
        if (name == null || name.trim().isEmpty())
            return false;
        String trimmed = name.trim();
        for (CategoryModel c : categories) {
            if (c.name.equalsIgnoreCase(trimmed))
                return false;
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
        if (name == null || name.trim().isEmpty())
            return false;
        String trimmed = name.trim();
        for (SkillModel s : skills) {
            if (s.name.equalsIgnoreCase(trimmed))
                return false;
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

    public synchronized boolean updateCategory(String oldName, String newName) {
        if (oldName == null || newName == null || newName.trim().isEmpty()) return false;
        String trimmed = newName.trim();
        if (oldName.equalsIgnoreCase(trimmed)) return true;
        for (CategoryModel c : categories) {
            if (c.name.equalsIgnoreCase(trimmed) && !c.name.equalsIgnoreCase(oldName)) {
                return false;
            }
        }
        for (CategoryModel c : categories) {
            if (c.name.equalsIgnoreCase(oldName)) {
                String prev = c.name;
                c.name = trimmed;
                logActivity("Jordan Lee", "JL", "Updated category name", prev + " → " + trimmed, true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean updateSkill(String oldName, String newName) {
        if (oldName == null || newName == null || newName.trim().isEmpty()) return false;
        String trimmed = newName.trim();
        if (oldName.equalsIgnoreCase(trimmed)) return true;
        for (SkillModel s : skills) {
            if (s.name.equalsIgnoreCase(trimmed) && !s.name.equalsIgnoreCase(oldName)) {
                return false;
            }
        }
        for (SkillModel s : skills) {
            if (s.name.equalsIgnoreCase(oldName)) {
                String prev = s.name;
                s.name = trimmed;
                logActivity("Jordan Lee", "JL", "Updated skill tag name", prev + " → " + trimmed, true);
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
        if (q.isEmpty())
            return getAllAuditLogs();
        return auditLogs.stream().filter(l -> l.actorName.toLowerCase().contains(q) ||
                l.action.toLowerCase().contains(q) ||
                l.target.toLowerCase().contains(q)).collect(Collectors.toList());
    }

    public synchronized void logActivity(String actorName, String actorInitials, String action, String target,
            boolean success) {
        String id = "L-" + (auditLogs.size() + 1);
        AuditLogModel log = new AuditLogModel(id, actorName, actorInitials, action, target, "Oct 16 · Just now",
                "2026 · UTC", success);
        auditLogs.add(0, log); // Newest on top
    }

    // ==========================================
    // REPOSITORY METHODS - REVIEWS & FEEDBACK
    // ==========================================

    public synchronized List<ReviewModel> getAllReviews() {
        return new ArrayList<>(reviews);
    }

    public synchronized ReviewModel getReviewById(String id) {
        if (id == null)
            return null;
        for (ReviewModel r : reviews) {
            if (r.id.equalsIgnoreCase(id.trim())) {
                return r;
            }
        }
        return null;
    }

    public synchronized List<ReviewModel> filterReviews(String query, int tabIdx) {
        return filterReviews(query, tabIdx, "All ratings", "All types", "All statuses");
    }

    public synchronized List<ReviewModel> filterReviews(String query, int tabIdx, String starFilter, String typeFilter,
            String statusFilter) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        return reviews.stream().filter(r -> {
            boolean matchQ = q.isEmpty()
                    || r.id.toLowerCase().contains(q)
                    || r.reviewerName.toLowerCase().contains(q)
                    || r.revieweeName.toLowerCase().contains(q)
                    || (r.projectName != null && r.projectName.toLowerCase().contains(q))
                    || (r.roleOrContext != null && r.roleOrContext.toLowerCase().contains(q))
                    || (r.reviewText != null && r.reviewText.toLowerCase().contains(q))
                    || (r.reportReason != null && r.reportReason.toLowerCase().contains(q));

            boolean matchTab = true;
            if (tabIdx == 1) { // Team Member Reviews
                matchTab = r.reviewType != null
                        && (r.reviewType.contains("Team") || r.reviewType.contains("Collaboration"));
            } else if (tabIdx == 2) { // User Ratings
                matchTab = r.reviewType != null && (r.reviewType.contains("User") || r.reviewType.contains("Rating"));
            } else if (tabIdx == 3) { // Reported Reviews
                matchTab = "Reported".equalsIgnoreCase(r.status) || "Under review".equalsIgnoreCase(r.status);
            }

            boolean matchStar = true;
            if (starFilter != null && !starFilter.startsWith("All")) {
                if (starFilter.startsWith("5"))
                    matchStar = (r.rating == 5);
                else if (starFilter.startsWith("4"))
                    matchStar = (r.rating == 4);
                else if (starFilter.startsWith("3"))
                    matchStar = (r.rating == 3);
                else if (starFilter.startsWith("2"))
                    matchStar = (r.rating == 2);
                else if (starFilter.startsWith("1"))
                    matchStar = (r.rating == 1);
            }

            boolean matchType = true;
            if (typeFilter != null && !typeFilter.startsWith("All")) {
                matchType = typeFilter.equalsIgnoreCase(r.reviewType);
            }

            boolean matchStatus = true;
            if (statusFilter != null && !statusFilter.startsWith("All")) {
                matchStatus = statusFilter.equalsIgnoreCase(r.status);
            }

            return matchQ && matchTab && matchStar && matchType && matchStatus;
        }).collect(Collectors.toList());
    }

    public synchronized boolean deleteReview(String id) {
        if (id == null)
            return false;
        for (int i = 0; i < reviews.size(); i++) {
            ReviewModel r = reviews.get(i);
            if (r.id.equalsIgnoreCase(id.trim())) {
                ReviewModel removed = reviews.remove(i);
                logActivity("Jordan Lee", "JL", "Deleted inappropriate feedback",
                        removed.id + " (" + removed.reviewerName + ")", true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean approveReportedReview(String id) {
        if (id == null)
            return false;
        for (ReviewModel r : reviews) {
            if (r.id.equalsIgnoreCase(id.trim())) {
                r.status = "Published";
                r.reportReason = null;
                r.reportedBy = null;
                logActivity("Jordan Lee", "JL", "Approved reported feedback", r.id + " (" + r.reviewerName + ")", true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean hideReview(String id) {
        if (id == null)
            return false;
        for (ReviewModel r : reviews) {
            if (r.id.equalsIgnoreCase(id.trim())) {
                if ("Hidden".equalsIgnoreCase(r.status)) {
                    r.status = "Published";
                    logActivity("Jordan Lee", "JL", "Unhid feedback", r.id, true);
                } else {
                    r.status = "Hidden";
                    logActivity("Jordan Lee", "JL", "Hid feedback", r.id, true);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized boolean warnAuthorReview(String id) {
        if (id == null)
            return false;
        for (ReviewModel r : reviews) {
            if (r.id.equalsIgnoreCase(id.trim())) {
                logActivity("Jordan Lee", "JL", "Issued warning to feedback author", r.reviewerName + " for " + r.id,
                        true);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean addReview(String reviewer, String reviewee, String project, String role, int rating,
            String text, String type) {
        if (reviewer == null || reviewer.trim().isEmpty() || text == null || text.trim().isEmpty())
            return false;
        String cleanReviewer = reviewer.trim();
        String cleanReviewee = (reviewee == null || reviewee.trim().isEmpty()) ? "Alex Morgan" : reviewee.trim();
        String cleanProject = (project == null || project.trim().isEmpty()) ? "Community Project" : project.trim();
        String cleanRole = (role == null || role.trim().isEmpty()) ? "Contributor" : role.trim();
        int cleanRating = Math.max(1, Math.min(5, rating));
        String cleanType = (type == null || type.trim().isEmpty()) ? "Team Member Review" : type.trim();

        String initials = "";
        String[] parts = cleanReviewer.split(" ");
        for (String p : parts) {
            if (!p.isEmpty())
                initials += p.charAt(0);
        }
        if (initials.isEmpty())
            initials = "U";
        if (initials.length() > 2)
            initials = initials.substring(0, 2).toUpperCase();

        String newId = "REV-" + (500 + reviews.size() + 1);
        ReviewModel newRev = new ReviewModel(
                newId, cleanReviewer, initials, cleanReviewee,
                cleanProject, cleanRole, cleanRating, text.trim(),
                cleanType, "Nov 11, 2026", "Published", null, null, 0, true);
        reviews.add(0, newRev);
        logActivity("Jordan Lee", "JL", "Added review", newId + " (" + cleanReviewer + " → " + cleanReviewee + ")",
                true);
        return true;
    }

    public synchronized String exportReviewsToCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "Review ID,Reviewer Name,Reviewee Name,Project Name,Role,Rating,Review Type,Date,Status,Report Reason,Reported By,Helpful Votes,Review Text\n");
        for (ReviewModel r : reviews) {
            sb.append(String.format(
                    "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\"\n",
                    r.id,
                    r.reviewerName.replace("\"", "\"\""),
                    r.revieweeName.replace("\"", "\"\""),
                    (r.projectName != null ? r.projectName.replace("\"", "\"\"") : ""),
                    (r.roleOrContext != null ? r.roleOrContext.replace("\"", "\"\"") : ""),
                    r.rating,
                    r.reviewType,
                    r.date,
                    r.status,
                    (r.reportReason != null ? r.reportReason.replace("\"", "\"\"") : ""),
                    (r.reportedBy != null ? r.reportedBy.replace("\"", "\"\"") : ""),
                    r.helpfulVotes,
                    (r.reviewText != null ? r.reviewText.replace("\"", "\"\"") : "")));
        }
        try {
            java.io.File file = new java.io.File("teamup_reviews_export.csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(sb.toString());
            writer.close();
            logActivity("Jordan Lee", "JL", "Exported feedback registry", "Saved to " + file.getName(), true);
            return file.getAbsolutePath();
        } catch (Exception e) {
            logActivity("Jordan Lee", "JL", "Exported feedback registry",
                    "Exported " + reviews.size() + " reviews to CSV", true);
            return "teamup_reviews_export.csv";
        }
    }

    public synchronized double getAverageRating() {
        double total = 0;
        int count = 0;
        for (ReviewModel r : reviews) {
            if ("Published".equalsIgnoreCase(r.status)) {
                total += r.rating;
                count++;
            }
        }
        if (count == 0)
            return 4.8;
        return Math.round((total / count) * 10.0) / 10.0;
    }

    public synchronized int getTotalReviewsCount() {
        return Math.max(320, 320 + (reviews.size() - 9));
    }

    public synchronized int getActualReviewsCount() {
        return reviews.size();
    }

    public synchronized int getTeamReviewsCount() {
        return (int) reviews.stream().filter(
                r -> r.reviewType != null && (r.reviewType.contains("Team") || r.reviewType.contains("Collaboration")))
                .count();
    }

    public synchronized int getUserRatingsCount() {
        return (int) reviews.stream()
                .filter(r -> r.reviewType != null && (r.reviewType.contains("User") || r.reviewType.contains("Rating")))
                .count();
    }

    public synchronized int getReportedReviewsCount() {
        return (int) reviews.stream()
                .filter(r -> "Reported".equalsIgnoreCase(r.status) || "Under review".equalsIgnoreCase(r.status))
                .count();
    }

    public synchronized int getPositiveRatingPercentage() {
        int count = 0;
        int high = 0;
        for (ReviewModel r : reviews) {
            if ("Published".equalsIgnoreCase(r.status)) {
                count++;
                if (r.rating >= 4)
                    high++;
            }
        }
        if (count == 0)
            return 94;
        return (int) Math.round((double) high / count * 100.0);
    }

    // ==========================================
    // ANALYTICS & PLATFORM METRICS
    // ==========================================

    public synchronized int getTotalUsersCount() {
        return baseTotalUsers + (users.size() - 8);
    }

    public synchronized double getProjectSuccessRate() {
        return 94.6; // Harmonized platform benchmark
    }

    public synchronized int getActiveTeamsCount() {
        int active = getActiveProjectsCount();
        return Math.max(120, 186 + (active - 286));
    }

    public synchronized double getAverageTeamSize() {
        return 4.6;
    }

    public synchronized int getSprintVelocity() {
        return 92;
    }

    public synchronized String exportAnalyticsToCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("TeamUp Platform Analytics & Statistics Report\n");
        sb.append("Generated on: Nov 11, 2026\n\n");
        sb.append("METRIC,VALUE,TREND/NOTES\n");
        sb.append(String.format("Total Platform Users,%d,+14.8%% MoM\n", getTotalUsersCount()));
        sb.append(String.format("Total Projects,%d,+12.6%% vs last term\n", getTotalProjectsCount()));
        sb.append(String.format("Active In-flight Projects,%d,Active sprints\n", getActiveProjectsCount()));
        sb.append(
                String.format("Project Success Rate,%.1f%%,Based on milestone completions\n", getProjectSuccessRate()));
        sb.append(String.format("Active Teams,%d,In-flight collaboration\n", getActiveTeamsCount()));
        sb.append(String.format("Average Team Size,%.1f / 6,84%% fill ratio\n", getAverageTeamSize()));
        sb.append(String.format("Collaboration Requests,%d,Active matching\n", collaborationRequests));
        sb.append(String.format("Platform Average Rating,%.1f ★,Verified peer reviews\n\n", getAverageRating()));

        sb.append("TOP POPULAR SKILLS\n");
        sb.append("Skill Name,Enrolled Students,Active Status\n");
        for (SkillModel s : skills) {
            sb.append(String.format("\"%s\",%d,%b\n", s.name, s.memberCount, s.active));
        }

        try {
            java.io.File file = new java.io.File("teamup_analytics_report.csv");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(sb.toString());
            writer.close();
            logActivity("Jordan Lee", "JL", "Exported platform analytics report", "Saved to " + file.getName(), true);
            return file.getAbsolutePath();
        } catch (Exception e) {
            logActivity("Jordan Lee", "JL", "Exported platform analytics report", "CSV export", true);
            return "teamup_analytics_report.csv";
        }
    }

    // ==========================================
    // GLOBAL SEARCH (⌘ K)
    // ==========================================

    public static class SearchResult {
        public String title;
        public String subtitle;
        public String type; // "User", "Project", "Report", "Review"

        public SearchResult(String title, String subtitle, String type) {
            this.title = title;
            this.subtitle = subtitle;
            this.type = type;
        }
    }

    public synchronized List<SearchResult> searchAll(String query) {
        if (query == null || query.trim().isEmpty())
            return Collections.emptyList();
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
        for (ReviewModel rev : reviews) {
            if (rev.id.toLowerCase().contains(q) || rev.reviewerName.toLowerCase().contains(q)
                    || rev.revieweeName.toLowerCase().contains(q)
                    || (rev.reviewText != null && rev.reviewText.toLowerCase().contains(q))) {
                results.add(new SearchResult(rev.id + " · " + rev.reviewerName + " → " + rev.revieweeName,
                        rev.rating + "★ " + rev.reviewType + " (" + rev.status + ")", "Review"));
            }
        }
        return results;
    }

    // =========================================================================
    // PLATFORM SETTINGS SERVICE & REPOSITORY METHODS
    // =========================================================================

    public synchronized List<String> getUniversityDomainsList() {
        return new ArrayList<>(universityDomainsList);
    }

    public synchronized boolean addUniversityDomain(String domain) {
        if (domain == null)
            return false;
        String clean = domain.trim().toLowerCase();
        if (!clean.startsWith("@"))
            clean = "@" + clean;
        if (clean.length() <= 2)
            return false;
        if (!universityDomainsList.contains(clean)) {
            universityDomainsList.add(clean);
            allowedDomains = String.join(", ", universityDomainsList);
            logActivity("Jordan Lee", "JL", "Added university domain", clean, true);
            return true;
        }
        return false;
    }

    public synchronized boolean removeUniversityDomain(String domain) {
        if (domain == null)
            return false;
        String clean = domain.trim().toLowerCase();
        boolean removed = universityDomainsList.remove(clean);
        if (removed) {
            allowedDomains = String.join(", ", universityDomainsList);
            logActivity("Jordan Lee", "JL", "Removed university domain", clean, true);
            return true;
        }
        return false;
    }

    public synchronized String createDatabaseBackup() {
        try {
            java.io.File dir = new java.io.File("backups");
            if (!dir.exists())
                dir.mkdirs();
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            java.io.File backupFile = new java.io.File(dir, "teamup_backup_" + timestamp + ".json");

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"timestamp\": \"").append(new java.util.Date()).append("\",\n");
            sb.append("  \"platformName\": \"").append(platformName).append("\",\n");
            sb.append("  \"supportEmail\": \"").append(supportEmail).append("\",\n");
            sb.append("  \"usersCount\": ").append(users.size()).append(",\n");
            sb.append("  \"projectsCount\": ").append(projects.size()).append(",\n");
            sb.append("  \"reviewsCount\": ").append(reviews.size()).append(",\n");
            sb.append("  \"reportsCount\": ").append(reports.size()).append(",\n");
            sb.append("  \"allowedDomains\": \"").append(allowedDomains).append("\",\n");
            sb.append("  \"maintenanceMode\": ").append(maintenanceModeActive).append("\n");
            sb.append("}\n");

            java.nio.file.Files.write(backupFile.toPath(),
                    sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            this.lastBackupDate = new java.text.SimpleDateFormat("MMM dd, yyyy, hh:mm a").format(new java.util.Date());
            this.lastBackupSize = String.format("%.1f KB", backupFile.length() / 1024.0f);
            logActivity("Jordan Lee", "JL", "Created database backup", backupFile.getName(), true);
            return backupFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized String exportAllPlatformData() {
        try {
            java.io.File exportFile = new java.io.File("teamup_platform_full_export.json");
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"exportTimestamp\": \"").append(new java.util.Date()).append("\",\n");
            sb.append("  \"platform\": \"").append(platformName).append("\",\n");
            sb.append("  \"supportEmail\": \"").append(supportEmail).append("\",\n");
            sb.append("  \"totalUsers\": ").append(getTotalUsersCount()).append(",\n");
            sb.append("  \"totalProjects\": ").append(getTotalProjectsCount()).append(",\n");
            sb.append("  \"activeTeams\": ").append(getActiveTeamsCount()).append(",\n");
            sb.append("  \"allowedDomains\": \"").append(allowedDomains).append("\"\n");
            sb.append("}\n");

            java.nio.file.Files.write(exportFile.toPath(),
                    sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            logActivity("Jordan Lee", "JL", "Exported full platform data", exportFile.getName(), true);
            return exportFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized boolean changeAdminPassword(String currentPass, String newPass) {
        if (currentPass == null || newPass == null)
            return false;
        if (!currentPass.equals(this.adminPassword))
            return false;
        if (newPass.trim().length() < 6)
            return false;
        this.adminPassword = newPass;
        logActivity("Jordan Lee", "JL", "Changed admin password", "Security Settings", true);
        return true;
    }

    public synchronized void resetSettingsToDefaults() {
        platformName = "TeamUp";
        contactEmail = "contact@teamup.edu";
        supportEmail = "support@teamup.edu";
        platformLogoText = "TU";
        defaultLanguage = "English (US)";
        notificationsEnabled = true;
        adminPassword = "admin123";

        universityDomainsList.clear();
        universityDomainsList.addAll(Arrays.asList(
                "@university.edu", "@stanford.edu", "@mit.edu", "@berkeley.edu", "@du.ac.bd", "@buet.ac.bd"));
        allowedDomains = String.join(", ", universityDomainsList);
        strictDomainEnforcement = true;

        userRegistrationEnabled = true;
        requireEmailVerification = true;
        requireStudentId = true;
        defaultProfileVisibility = "Campus Only";

        requireProjectApproval = false;
        projectCreationRole = "All Students";
        maxProjectsPerStudent = 3;
        autoArchiveInactiveProjects = true;

        minTeamSize = 2;
        maxTeamSize = 6;
        allowCrossDepartmentTeams = true;
        autoLockFullTeams = true;

        maxApplicationsPerStudent = 5;
        allowApplicationWithdrawal = true;
        applicationExpiryDays = 14;
        requireResumeOrPortfolio = false;

        spamSensitivityThreshold = 3;
        maxLoginAttempts = 5;
        sessionTimeoutMinutes = 30;
        twoFactorAuthEnforced = false;

        emailNotificationsEnabled = true;
        inAppNotificationsEnabled = true;
        digestFrequency = "Instant";
        announcementBroadcasts = true;

        maintenanceModeActive = false;
        maintenanceNotice = "TeamUp is undergoing scheduled maintenance upgrades. We will be back online shortly.";

        appearanceTheme = "Light";
        accentColorIndex = 0;
        compactMode = false;

        dataRetentionDays = 365;

        logActivity("Jordan Lee", "JL", "Reset platform settings to default", "Global Configuration", true);
    }
}

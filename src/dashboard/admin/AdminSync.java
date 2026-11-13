import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bridges TeamUp Admin Dashboard with the Node.js + MongoDB Atlas backend.
 *
 * - load(): GET /api/admin/bootstrap and copy the result into AdminDataStore.
 * - Action helpers: sync mutations asynchronously to MongoDB Atlas (users, projects,
 *   moderation reports, categories, skills, feedback reviews, platform settings).
 *
 * Runs on a dedicated daemon background thread so UI rendering never freezes.
 */
public final class AdminSync {

    private AdminSync() {}

    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "TeamUp-AdminSync");
        t.setDaemon(true);
        return t;
    });

    public static boolean isOnline() {
        return !AuthStore.isOfflineMode();
    }

    // =========================================================================
    // LOAD BOOTSTRAP DATA
    // =========================================================================

    public static void load(final AdminDashboard d) {
        if (!isOnline()) return;

        IO.submit(() -> {
            try {
                ApiClient.Response r = ApiClient.get("/admin/bootstrap");
                if (r.ok()) {
                    final Map<String, Object> data = Json.map(r.json, "data");
                    if (data != null) {
                        apply(data);
                        if (d != null) {
                            d.showToast("Admin data synchronized with MongoDB Atlas");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[AdminSync] Backend offline or unreachable: " + e.getMessage());
            }
        });
    }

    public static void apply(Map<String, Object> data) {
        if (data == null) return;
        AdminDataStore store = AdminDataStore.getInstance();
        synchronized (store) {
            // 1. Users
            List<Map<String, Object>> userList = Json.maps(data, "users");
            if (userList != null && !userList.isEmpty()) {
                store.clearUsers();
                for (Map<String, Object> u : userList) {
                    AdminDataStore.UserModel um = new AdminDataStore.UserModel(
                            Json.str(u, "id", "U-100"),
                            Json.str(u, "name", ""),
                            Json.str(u, "initials", "TU"),
                            Json.str(u, "email", ""),
                            Json.str(u, "role", "Member"),
                            Json.str(u, "joined", "Nov 14, 2026"),
                            Json.str(u, "status", "Active")
                    );
                    um.mongoId = Json.str(u, "mongoId", "");
                    store.getUsersRaw().add(um);
                }
            }

            // 2. Projects
            List<Map<String, Object>> projList = Json.maps(data, "projects");
            if (projList != null && !projList.isEmpty()) {
                store.clearProjects();
                for (Map<String, Object> p : projList) {
                    AdminDataStore.ProjectModel pm = new AdminDataStore.ProjectModel(
                            Json.str(p, "id", "P-200"),
                            Json.str(p, "name", ""),
                            Json.str(p, "category", "Web Development"),
                            Json.str(p, "owner", "Jordan Lee"),
                            Json.str(p, "teamSize", "1 / 6"),
                            Json.str(p, "status", "Active"),
                            Json.str(p, "created", "Nov 14, 2026"),
                            Json.str(p, "description", "")
                    );
                    pm.mongoId = Json.str(p, "mongoId", "");
                    store.getProjectsRaw().add(pm);
                }
            }

            // 3. Reports
            List<Map<String, Object>> repList = Json.maps(data, "reports");
            if (repList != null && !repList.isEmpty()) {
                store.clearReports();
                for (Map<String, Object> r : repList) {
                    AdminDataStore.ReportModel rm = new AdminDataStore.ReportModel(
                            Json.str(r, "id", "R-100"),
                            Json.str(r, "type", "Message"),
                            Json.str(r, "title", ""),
                            Json.str(r, "category", "Spam"),
                            Json.str(r, "severity", "Medium"),
                            Json.str(r, "reporter", ""),
                            Json.str(r, "reporterInitials", "TU"),
                            Json.str(r, "date", "Oct 16, 2026"),
                            Json.str(r, "timeUtc", "12:00 PM UTC"),
                            Json.str(r, "status", "Pending"),
                            Json.str(r, "reporterDesc", ""),
                            Json.str(r, "flaggedAuthor", ""),
                            Json.str(r, "flaggedContent", ""),
                            Json.str(r, "flaggedTime", "")
                    );
                    rm.internalNote = Json.str(r, "internalNote", "");
                    rm.mongoId = Json.str(r, "mongoId", "");
                    store.getReportsRaw().add(rm);
                }
            }

            // 4. Categories & Skills
            List<Map<String, Object>> catList = Json.maps(data, "categories");
            if (catList != null && !catList.isEmpty()) {
                store.clearCategories();
                for (Map<String, Object> c : catList) {
                    AdminDataStore.CategoryModel cm = new AdminDataStore.CategoryModel(
                            Json.str(c, "id", "C-1"),
                            Json.str(c, "name", ""),
                            Json.integer(c, "projectCount", 0),
                            Json.bool(c, "active", true)
                    );
                    cm.mongoId = Json.str(c, "mongoId", "");
                    store.getCategoriesRaw().add(cm);
                }
            }

            List<Map<String, Object>> skillList = Json.maps(data, "skills");
            if (skillList != null && !skillList.isEmpty()) {
                store.clearSkills();
                for (Map<String, Object> s : skillList) {
                    AdminDataStore.SkillModel sm = new AdminDataStore.SkillModel(
                            Json.str(s, "id", "S-1"),
                            Json.str(s, "name", ""),
                            Json.integer(s, "memberCount", 0),
                            Json.bool(s, "active", true)
                    );
                    sm.mongoId = Json.str(s, "mongoId", "");
                    store.getSkillsRaw().add(sm);
                }
            }

            // 5. Reviews
            List<Map<String, Object>> revList = Json.maps(data, "reviews");
            if (revList != null && !revList.isEmpty()) {
                store.clearReviews();
                for (Map<String, Object> rev : revList) {
                    AdminDataStore.ReviewModel rm = new AdminDataStore.ReviewModel(
                            Json.str(rev, "id", "REV-500"),
                            Json.str(rev, "reviewerName", ""),
                            Json.str(rev, "reviewerInitials", "TU"),
                            Json.str(rev, "revieweeName", ""),
                            Json.str(rev, "projectName", ""),
                            Json.str(rev, "roleOrContext", ""),
                            Json.integer(rev, "rating", 5),
                            Json.str(rev, "reviewText", ""),
                            Json.str(rev, "reviewType", "Team Member Review"),
                            Json.str(rev, "date", "Nov 08, 2026"),
                            Json.str(rev, "status", "Published"),
                            Json.str(rev, "reportReason", null),
                            Json.str(rev, "reportedBy", null),
                            Json.integer(rev, "helpfulVotes", 0),
                            Json.bool(rev, "verifiedCollaboration", true)
                    );
                    rm.mongoId = Json.str(rev, "mongoId", "");
                    store.getReviewsRaw().add(rm);
                }
            }

            // 6. Audit Logs
            List<Map<String, Object>> logList = Json.maps(data, "auditLogs");
            if (logList != null && !logList.isEmpty()) {
                store.clearAuditLogs();
                for (Map<String, Object> l : logList) {
                    AdminDataStore.AuditLogModel lm = new AdminDataStore.AuditLogModel(
                            Json.str(l, "id", "L-1"),
                            Json.str(l, "actorName", "Jordan Lee"),
                            Json.str(l, "actorInitials", "JL"),
                            Json.str(l, "action", ""),
                            Json.str(l, "target", ""),
                            Json.str(l, "timestampLine1", ""),
                            Json.str(l, "timestampLine2", ""),
                            Json.bool(l, "success", true)
                    );
                    lm.mongoId = Json.str(l, "mongoId", "");
                    store.getAuditLogsRaw().add(lm);
                }
            }

            // 7. Settings
            Map<String, Object> set = Json.map(data, "settings");
            if (set != null) {
                store.platformName = Json.str(set, "platformName", store.platformName);
                store.contactEmail = Json.str(set, "contactEmail", store.contactEmail);
                store.supportEmail = Json.str(set, "supportEmail", store.supportEmail);
                store.platformLogoText = Json.str(set, "platformLogoText", store.platformLogoText);
                store.defaultLanguage = Json.str(set, "defaultLanguage", store.defaultLanguage);
                store.adminPassword = Json.str(set, "adminPassword", store.adminPassword);
                store.notificationsEnabled = Json.bool(set, "notificationsEnabled", store.notificationsEnabled);
                store.allowedDomains = Json.str(set, "allowedDomains", store.allowedDomains);
                store.strictDomainEnforcement = Json.bool(set, "strictDomainEnforcement", store.strictDomainEnforcement);
                store.userRegistrationEnabled = Json.bool(set, "userRegistrationEnabled", store.userRegistrationEnabled);
                store.requireEmailVerification = Json.bool(set, "requireEmailVerification", store.requireEmailVerification);
                store.requireStudentId = Json.bool(set, "requireStudentId", store.requireStudentId);
                store.defaultProfileVisibility = Json.str(set, "defaultProfileVisibility", store.defaultProfileVisibility);
                store.requireProjectApproval = Json.bool(set, "requireProjectApproval", store.requireProjectApproval);
                store.projectCreationRole = Json.str(set, "projectCreationRole", store.projectCreationRole);
                store.maxProjectsPerStudent = Json.integer(set, "maxProjectsPerStudent", store.maxProjectsPerStudent);
                store.autoArchiveInactiveProjects = Json.bool(set, "autoArchiveInactiveProjects", store.autoArchiveInactiveProjects);
                store.minTeamSize = Json.integer(set, "minTeamSize", store.minTeamSize);
                store.maxTeamSize = Json.integer(set, "maxTeamSize", store.maxTeamSize);
                store.allowCrossDepartmentTeams = Json.bool(set, "allowCrossDepartmentTeams", store.allowCrossDepartmentTeams);
                store.autoLockFullTeams = Json.bool(set, "autoLockFullTeams", store.autoLockFullTeams);
                store.maxApplicationsPerStudent = Json.integer(set, "maxApplicationsPerStudent", store.maxApplicationsPerStudent);
                store.allowApplicationWithdrawal = Json.bool(set, "allowApplicationWithdrawal", store.allowApplicationWithdrawal);
                store.applicationExpiryDays = Json.integer(set, "applicationExpiryDays", store.applicationExpiryDays);
                store.requireResumeOrPortfolio = Json.bool(set, "requireResumeOrPortfolio", store.requireResumeOrPortfolio);
                store.spamSensitivityThreshold = Json.integer(set, "spamSensitivityThreshold", store.spamSensitivityThreshold);
                store.maxLoginAttempts = Json.integer(set, "maxLoginAttempts", store.maxLoginAttempts);
                store.sessionTimeoutMinutes = Json.integer(set, "sessionTimeoutMinutes", store.sessionTimeoutMinutes);
                store.twoFactorAuthEnforced = Json.bool(set, "twoFactorAuthEnforced", store.twoFactorAuthEnforced);
                store.emailNotificationsEnabled = Json.bool(set, "emailNotificationsEnabled", store.emailNotificationsEnabled);
                store.inAppNotificationsEnabled = Json.bool(set, "inAppNotificationsEnabled", store.inAppNotificationsEnabled);
                store.digestFrequency = Json.str(set, "digestFrequency", store.digestFrequency);
                store.announcementBroadcasts = Json.bool(set, "announcementBroadcasts", store.announcementBroadcasts);
                store.maintenanceModeActive = Json.bool(set, "maintenanceModeActive", store.maintenanceModeActive);
                store.maintenanceNotice = Json.str(set, "maintenanceNotice", store.maintenanceNotice);
                store.appearanceTheme = Json.str(set, "appearanceTheme", store.appearanceTheme);
                store.accentColorIndex = Json.integer(set, "accentColorIndex", store.accentColorIndex);
                store.compactMode = Json.bool(set, "compactMode", store.compactMode);
                store.lastBackupDate = Json.str(set, "lastBackupDate", store.lastBackupDate);
                store.lastBackupSize = Json.str(set, "lastBackupSize", store.lastBackupSize);
                store.dataRetentionDays = Json.integer(set, "dataRetentionDays", store.dataRetentionDays);

                String[] domainArr = Json.strings(set, "universityDomainsList");
                if (domainArr != null && domainArr.length > 0) {
                    store.universityDomainsList.clear();
                    for (String dStr : domainArr) store.universityDomainsList.add(dStr);
                }
            }

            // Stats
            Map<String, Object> stats = Json.map(data, "stats");
            if (stats != null) {
                store.baseTotalUsers = Json.integer(stats, "totalUsers", store.baseTotalUsers);
                store.baseTotalProjects = Json.integer(stats, "totalProjects", store.baseTotalProjects);
                store.collaborationRequests = Json.integer(stats, "collaborationRequests", store.collaborationRequests);
            }
        }
    }

    // =========================================================================
    // ASYNCHRONOUS BACKEND MUTATION HELPERS
    // =========================================================================

    public static void syncAddUser(String name, String email, String role) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.post("/admin/users", Json.obj("name", name, "email", email, "role", role));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync addUser to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncUpdateUserRole(String id, String name, String newRole) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                String segId = ApiClient.seg(id != null && !id.isEmpty() ? id : name);
                ApiClient.put("/admin/users/" + segId + "/role", Json.obj("name", name, "role", newRole));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync updateUserRole to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncUpdateUserStatus(String id, String name, String status) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                String segId = ApiClient.seg(id != null && !id.isEmpty() ? id : name);
                ApiClient.put("/admin/users/" + segId + "/status", Json.obj("name", name, "status", status));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync updateUserStatus to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncDeleteUser(String id, String name) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                String segId = ApiClient.seg(id != null && !id.isEmpty() ? id : name);
                ApiClient.delete("/admin/users/" + segId + "?name=" + ApiClient.seg(name));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync deleteUser to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncAddProject(String name, String category, String owner, String teamSize, String status, String desc) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.post("/admin/projects", Json.obj(
                        "name", name,
                        "category", category,
                        "owner", owner,
                        "teamSize", teamSize,
                        "status", status,
                        "description", desc
                ));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync addProject to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncUpdateProjectStatus(String id, String name, String status) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                String segId = ApiClient.seg(id != null && !id.isEmpty() ? id : name);
                ApiClient.put("/admin/projects/" + segId + "/status", Json.obj("name", name, "status", status));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync updateProjectStatus to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncDeleteProject(String id, String name) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                String segId = ApiClient.seg(id != null && !id.isEmpty() ? id : name);
                ApiClient.delete("/admin/projects/" + segId + "?name=" + ApiClient.seg(name));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync deleteProject to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncResolveReport(String id, String note) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.put("/admin/reports/" + ApiClient.seg(id) + "/resolve", Json.obj("note", note));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync resolveReport to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncActionReport(String id, String action, String note) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.put("/admin/reports/" + ApiClient.seg(id) + "/action", Json.obj("action", action, "note", note));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync actionReport to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncAddCategory(String name) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.post("/admin/categories", Json.obj("name", name));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync addCategory to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncToggleCategory(String id, String name, boolean active) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                String seg = ApiClient.seg(id != null && !id.isEmpty() ? id : name);
                ApiClient.put("/admin/categories/" + seg, Json.obj("name", name, "active", active));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync toggleCategory to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncUpdateCategory(String oldName, String newName) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.put("/admin/categories/" + ApiClient.seg(oldName), Json.obj("oldName", oldName, "name", newName));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync updateCategory to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncAddSkill(String name) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.post("/admin/skills", Json.obj("name", name));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync addSkill to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncToggleSkill(String id, String name, boolean active) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                String seg = ApiClient.seg(id != null && !id.isEmpty() ? id : name);
                ApiClient.put("/admin/skills/" + seg, Json.obj("name", name, "active", active));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync toggleSkill to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncUpdateSkill(String oldName, String newName) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.put("/admin/skills/" + ApiClient.seg(oldName), Json.obj("oldName", oldName, "name", newName));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync updateSkill to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncAddReview(String reviewer, String reviewee, String project, String role, int rating, String text, String type) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.post("/admin/reviews", Json.obj(
                        "reviewer", reviewer,
                        "revieweeName", reviewee,
                        "projectName", project,
                        "roleOrContext", role,
                        "rating", rating,
                        "text", text,
                        "type", type
                ));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync addReview to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncUpdateReview(String id, String action, String status) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.put("/admin/reviews/" + ApiClient.seg(id), Json.obj("action", action, "status", status));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync updateReview to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncDeleteReview(String id) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.delete("/admin/reviews/" + ApiClient.seg(id));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync deleteReview to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncAddDomain(String domain) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.post("/admin/settings/domains", Json.obj("domain", domain));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync addDomain to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncRemoveDomain(String domain) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.delete("/admin/settings/domains?domain=" + ApiClient.seg(domain));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync removeDomain to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncChangePassword(String currentPass, String newPass) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.put("/admin/settings/password", Json.obj("currentPassword", currentPass, "newPassword", newPass));
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync changePassword to MongoDB: " + e.getMessage());
            }
        });
    }

    public static void syncResetSettings() {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.post("/admin/settings/reset", Json.obj());
            } catch (Exception e) {
                System.err.println("[AdminSync] Failed to sync resetSettings to MongoDB: " + e.getMessage());
            }
        });
    }
}


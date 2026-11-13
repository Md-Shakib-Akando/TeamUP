import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bridges the Student Dashboard with the Node.js + MongoDB backend.
 *
 * - load(): GET /api/dashboard and copy the result into every dashboard view model.
 * - action helpers: persist user actions (bookmark, join, invite, tasks, settings...).
 *
 * Network work runs on one background thread (so actions reach the server in the
 * order the user made them). Results are handed back through
 * StudentDashboard.runOnUi(), which applies them on the render thread.
 * When nobody is logged in against the backend every method is a no-op and the
 * dashboard keeps its built-in demo data.
 */
public final class DashboardSync {

    private DashboardSync() {}

    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "TeamUp-DashboardSync");
        t.setDaemon(true);
        return t;
    });

    public static boolean isOnline() {
        return ApiClient.hasSession();
    }

    // =========================================================================
    // LOAD
    // =========================================================================

    public static void load(final StudentDashboard d) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.Response r = ApiClient.get("/dashboard");
                if (r.ok()) {
                    final Map<String, Object> data = r.json;
                    d.runOnUi(() -> apply(d, data));
                } else {
                    final String msg = r.message("HTTP " + r.status);
                    d.runOnUi(() -> d.showToast("Couldn't load your dashboard: " + msg));
                }
            } catch (IOException | RuntimeException e) {
                d.runOnUi(() -> d.showToast("Backend unreachable — showing demo data."));
            }
        });
    }

    /** Copies a /api/dashboard payload into the dashboard models. Must run on the render thread. */
    public static void apply(StudentDashboard d, Map<String, Object> data) {
        applyMe(d, Json.map(data, "me"), Json.map(data, "stats"));
        applyOverview(d, Json.map(data, "overview"), Json.list(data, "notifications"));
        applyDiscover(d, Json.maps(data, "discover"));
        applyTeammates(d, Json.maps(data, "teammates"));
        applyMyProjects(d, Json.maps(data, "myProjects"));
        applyTeam(d.getTeamData(), Json.map(data, "team"));
        applyProfile(d.getProfileData(), Json.map(data, "profile"));
        applySettings(d.getSettingsData(), Json.map(data, "settings"));
        d.markServerDataLoaded();
    }

    private static void applyMe(StudentDashboard d, Map<String, Object> me, Map<String, Object> stats) {
        if (me != null) {
            d.setProfileName(Json.str(me, "name", d.getProfileName()));
            d.setProfileInitials(Json.str(me, "initials", "TU"));
            d.setProfileRole(Json.str(me, "role", "Student"));
            d.setProfileBio(Json.str(me, "bio", ""));
            d.setProfilePortfolio(Json.str(me, "portfolio", ""));
            d.setProfileCompletePercent(Json.integer(me, "profileComplete", 40));
        }
        if (stats != null) {
            d.setActiveProjectsCount(Json.integer(stats, "activeProjects", 0));
            d.setMyTeamsCount(Json.integer(stats, "myTeams", 0));
            d.setPendingRequestsCount(Json.integer(stats, "pendingRequests", 0));
            d.setTasksDueCount(Json.integer(stats, "tasksDue", 0));
            d.setStatSubtitles(new String[]{
                    Json.str(stats, "activeProjectsSub", ""),
                    Json.str(stats, "myTeamsSub", ""),
                    Json.str(stats, "pendingRequestsSub", ""),
                    Json.str(stats, "tasksDueSub", "")
            });
        }
    }

    private static void applyOverview(StudentDashboard d, Map<String, Object> ov, List<Object> notifications) {
        if (ov == null) return;

        List<StudentDashboard.ProjectItem> projects = d.getProjects();
        projects.clear();
        for (Map<String, Object> p : Json.maps(ov, "projects")) {
            StudentDashboard.ProjectItem pi = new StudentDashboard.ProjectItem(
                    Json.str(p, "title", ""), Json.str(p, "author", ""), Json.str(p, "desc", ""),
                    Json.strings(p, "skills"), Json.str(p, "footer", ""),
                    Json.bool(p, "isGreen", true), Json.integer(p, "type", 0));
            pi.remoteId = Json.str(p, "id", null);
            pi.bookmarked = Json.bool(p, "bookmarked", false);
            pi.requested = Json.bool(p, "requested", false);
            projects.add(pi);
        }

        List<StudentDashboard.TeammateItem> teammates = d.getTeammates();
        teammates.clear();
        for (Map<String, Object> t : Json.maps(ov, "teammates")) {
            StudentDashboard.TeammateItem ti = new StudentDashboard.TeammateItem(
                    Json.str(t, "initials", "TU"), color(t, "avatarBg"), color(t, "avatarFg"),
                    Json.str(t, "name", ""), Json.str(t, "role", ""), Json.str(t, "matchScore", ""),
                    Json.str(t, "bio", ""), Json.str(t, "university", ""));
            ti.remoteId = Json.str(t, "id", null);
            ti.invited = Json.bool(t, "invited", false);
            teammates.add(ti);
        }

        List<StudentDashboard.TaskItem> tasks = d.getTasks();
        tasks.clear();
        for (Map<String, Object> t : Json.maps(ov, "tasks")) {
            StudentDashboard.TaskItem ti = new StudentDashboard.TaskItem(
                    Json.str(t, "title", ""), Json.str(t, "subtitle", ""), Json.integer(t, "statusType", 2));
            ti.remoteId = Json.str(t, "id", null);
            ti.completed = Json.bool(t, "completed", false);
            tasks.add(ti);
        }

        List<StudentDashboard.ActivityItem> acts = d.getActivities();
        acts.clear();
        for (Map<String, Object> a : Json.maps(ov, "activities")) {
            acts.add(new StudentDashboard.ActivityItem(
                    Json.str(a, "initials", "TU"), color(a, "avatarBg"), color(a, "avatarFg"),
                    Json.str(a, "title", ""), Json.str(a, "subtitle", ""), Json.str(a, "badgeText", ""),
                    color(a, "badgeBg"), color(a, "badgeFg")));
        }

        List<String> notes = d.getNotifications();
        notes.clear();
        for (Object n : notifications) {
            if (n != null) notes.add(String.valueOf(n));
        }
        if (notes.isEmpty()) notes.add("You're all caught up — no notifications yet.");
    }

    private static void applyDiscover(StudentDashboard d, List<Map<String, Object>> list) {
        List<DashboardProjects.ProjectCard> cards = new ArrayList<DashboardProjects.ProjectCard>();
        int idx = 1;
        for (Map<String, Object> p : list) {
            DashboardProjects.ProjectCard c = new DashboardProjects.ProjectCard(
                    idx++, Json.str(p, "title", ""), Json.str(p, "category", ""), Json.str(p, "experience", "Intermediate"),
                    Json.str(p, "description", ""), Json.strings(p, "skills"), Json.bool(p, "recruiting", false),
                    Json.integer(p, "currentMembers", 0), Json.integer(p, "maxMembers", 1), Json.str(p, "openRoles", ""),
                    Json.str(p, "creatorName", ""), Json.str(p, "creatorInitials", ""), color(p, "creatorColor"),
                    Json.integer(p, "iconType", 0), color(p, "iconBg"), color(p, "iconColor"));
            c.remoteId = Json.str(p, "id", null);
            c.bookmarked = Json.bool(p, "bookmarked", false);
            c.isMember = Json.bool(p, "isMember", false);
            c.requested = Json.bool(p, "requested", false);
            c.openRoleCount = Json.integer(p, "openRoleCount", 0);
            cards.add(c);
        }
        d.getProjectsView().setProjects(cards);
    }

    private static void applyTeammates(StudentDashboard d, List<Map<String, Object>> list) {
        List<DashboardTeammates.TeammateCard> cards = new ArrayList<DashboardTeammates.TeammateCard>();
        int idx = 1;
        for (Map<String, Object> t : list) {
            DashboardTeammates.TeammateCard c = new DashboardTeammates.TeammateCard(
                    idx++, Json.str(t, "name", ""), Json.str(t, "role", ""), Json.str(t, "university", ""),
                    Json.str(t, "initials", "TU"), color(t, "avatarBg"), color(t, "avatarFg"),
                    Json.str(t, "availability", ""), Json.num(t, "rating", 0), Json.integer(t, "reviews", 0),
                    Json.strings(t, "skills"), Json.integer(t, "matchPct", 0) + "% skill match",
                    Json.str(t, "matchDesc", ""), Json.str(t, "portfolio", ""), Json.str(t, "experience", "Beginner"),
                    Json.str(t, "bio", ""));
            c.remoteId = Json.str(t, "id", null);
            c.invited = Json.bool(t, "invited", false);
            c.requested = Json.bool(t, "requested", false);
            cards.add(c);
        }
        d.getTeammatesView().setTeammates(cards);
    }

    private static void applyMyProjects(StudentDashboard d, List<Map<String, Object>> list) {
        List<DashboardMyProjects.MyProjectCard> cards = new ArrayList<DashboardMyProjects.MyProjectCard>();
        int idx = 1;
        for (Map<String, Object> p : list) {
            String status = Json.str(p, "status", "Active");
            Color[] sc = statusColors(status);
            DashboardMyProjects.MyProjectCard c = new DashboardMyProjects.MyProjectCard(
                    idx++, Json.str(p, "title", ""), Json.str(p, "category", ""), Json.str(p, "description", ""),
                    Json.str(p, "myRole", "Member"), Json.bool(p, "isLead", false), status, sc[0], sc[1],
                    Json.integer(p, "progress", 0), Json.strings(p, "skills"), Json.str(p, "nextMilestone", ""),
                    Json.integer(p, "capacity", 1));
            c.remoteId = Json.str(p, "id", null);
            c.isArchived = Json.bool(p, "archived", false);
            for (Map<String, Object> m : Json.maps(p, "members")) {
                c.members.add(new DashboardMyProjects.MemberAvatar(Json.str(m, "initials", "TU"), Json.str(m, "name", ""),
                        Json.str(m, "role", ""), color(m, "bg"), color(m, "fg")));
            }
            for (Map<String, Object> t : Json.maps(p, "tasks")) {
                DashboardMyProjects.ProjectTask task = new DashboardMyProjects.ProjectTask(
                        Json.str(t, "title", ""), Json.str(t, "due", ""), Json.bool(t, "done", false));
                task.remoteId = Json.str(t, "id", null);
                c.tasks.add(task);
            }
            for (Map<String, Object> k : Json.maps(p, "candidates")) {
                DashboardMyProjects.Candidate cand = new DashboardMyProjects.Candidate(
                        Json.str(k, "name", ""), Json.str(k, "role", ""), Json.str(k, "initials", "TU"),
                        color(k, "bg"), color(k, "fg"), Json.str(k, "matchPct", ""));
                cand.remoteId = Json.str(k, "id", null);
                c.candidates.add(cand);
            }
            cards.add(c);
        }
        d.getMyProjectsView().setProjects(cards);
    }

    private static void applyTeam(TeamWorkspaceData td, Map<String, Object> t) {
        td.members.clear();
        td.milestones.clear();
        td.recruitmentRequests.clear();
        td.activities.clear();
        td.files.clear();

        if (t == null) {
            td.projectId = null;
            td.workspaceName = "No team yet";
            td.subtitle = "Create a project or join one from Discover Projects to get a workspace.";
            td.breadcrumbs = "My Teams / Workspace";
            td.progressPercent = 0;
            td.tasksCompleted = 0;
            td.tasksTotal = 0;
            td.currentMembers = 0;
            td.maxMembers = 0;
            td.openPositions = 0;
            td.nextMilestoneDate = "—";
            td.nextMilestoneSub = "No upcoming milestone";
            td.tasksDueSoon = 0;
            td.tasksDueSoonSub = "Nothing due";
            td.taskSummary = new TeamWorkspaceData.TaskSummary(0, 0, 0, 0);
            td.pendingRequestsCount = 0;
            td.syncNotice = "No team sync scheduled yet";
            setTabs(td, Arrays.asList("Overview", "Members (0)", "Tasks (0)", "Files", "Requests (0)"));
            return;
        }

        td.projectId = Json.str(t, "projectId", null);
        td.workspaceName = Json.str(t, "workspaceName", "");
        td.subtitle = Json.str(t, "subtitle", td.subtitle);
        td.breadcrumbs = Json.str(t, "breadcrumbs", "");
        td.progressPercent = Json.integer(t, "progressPercent", 0);
        td.tasksCompleted = Json.integer(t, "tasksCompleted", 0);
        td.tasksTotal = Json.integer(t, "tasksTotal", 0);
        td.currentMembers = Json.integer(t, "currentMembers", 0);
        td.maxMembers = Json.integer(t, "maxMembers", 0);
        td.openPositions = Json.integer(t, "openPositions", 0);
        td.nextMilestoneDate = Json.str(t, "nextMilestoneDate", "—");
        td.nextMilestoneSub = Json.str(t, "nextMilestoneSub", "");
        td.tasksDueSoon = Json.integer(t, "tasksDueSoon", 0);
        td.tasksDueSoonSub = Json.str(t, "tasksDueSoonSub", "");
        Map<String, Object> s = Json.map(t, "taskSummary");
        td.taskSummary = new TeamWorkspaceData.TaskSummary(Json.integer(s, "todo", 0), Json.integer(s, "inProgress", 0),
                Json.integer(s, "inReview", 0), Json.integer(s, "done", 0));
        td.pendingRequestsCount = Json.integer(t, "pendingRequestsCount", 0);
        td.syncNotice = Json.str(t, "syncNotice", "");
        setTabs(td, Arrays.asList(Json.strings(t, "tabs")));

        for (Map<String, Object> m : Json.maps(t, "members")) {
            td.members.add(new TeamWorkspaceData.TeamMember(Json.str(m, "name", ""), Json.str(m, "initials", "TU"),
                    Json.str(m, "role", ""), Json.str(m, "badge", "Member"), color(m, "avatarBg"), color(m, "avatarFg")));
        }
        for (Map<String, Object> m : Json.maps(t, "milestones")) {
            td.milestones.add(new TeamWorkspaceData.MilestoneItem(Json.str(m, "title", ""), Json.str(m, "dueInfo", ""),
                    Json.integer(m, "progress", 0), Json.str(m, "status", "Upcoming")));
        }
        for (Map<String, Object> r : Json.maps(t, "requests")) {
            TeamWorkspaceData.RecruitmentRequest req = new TeamWorkspaceData.RecruitmentRequest(Json.str(r, "name", ""),
                    Json.str(r, "initials", "TU"), Json.str(r, "roleSkill", ""), color(r, "avatarBg"), color(r, "avatarFg"));
            req.remoteId = Json.str(r, "id", null);
            td.recruitmentRequests.add(req);
        }
        for (Map<String, Object> a : Json.maps(t, "activities")) {
            td.activities.add(new TeamWorkspaceData.TeamActivity(Json.str(a, "name", ""), Json.str(a, "initials", "TU"),
                    Json.str(a, "actionText", ""), color(a, "avatarBg"), color(a, "avatarFg")));
        }
        for (Map<String, Object> f : Json.maps(t, "files")) {
            td.files.add(new TeamWorkspaceData.WorkspaceFile(Json.str(f, "name", ""), Json.str(f, "type", ""),
                    Json.str(f, "size", ""), Json.str(f, "updatedAgo", ""), Json.str(f, "author", "")));
        }
    }

    private static void setTabs(TeamWorkspaceData td, List<String> tabs) {
        if (tabs.size() < 5) return;
        td.tabs.clear();
        td.tabs.addAll(tabs);
        if (td.activeTabIndex >= td.tabs.size()) td.activeTabIndex = 0;
    }

    private static void applyProfile(ProfileData pd, Map<String, Object> p) {
        if (p == null) return;
        pd.name = Json.str(p, "name", pd.name);
        pd.handle = Json.str(p, "handle", pd.handle);
        pd.headline = Json.str(p, "headline", "");
        pd.university = Json.str(p, "university", "");
        pd.department = Json.str(p, "department", "");
        pd.gradYear = Json.str(p, "gradYear", "");
        pd.location = Json.str(p, "location", "");
        pd.email = Json.str(p, "email", "");
        pd.github = Json.str(p, "github", "");
        pd.portfolio = Json.str(p, "portfolio", "");
        pd.bio = Json.str(p, "bio", "");
        pd.availabilityStatus = Json.str(p, "availabilityStatus", "");
        pd.activeProjectsCount = Json.integer(p, "activeProjectsCount", 0);
        pd.totalTeammatesCount = Json.integer(p, "totalTeammatesCount", 0);
        pd.completionRatePercent = Json.integer(p, "completionRatePercent", 0);
        pd.peerRating = (float) Json.num(p, "peerRating", 0);
        pd.totalReviewsCount = Json.integer(p, "totalReviewsCount", 0);

        pd.projects.clear();
        for (Map<String, Object> pr : Json.maps(p, "projects")) {
            pd.projects.add(new ProfileData.ProfileProject(Json.str(pr, "title", ""), Json.str(pr, "role", ""),
                    Json.str(pr, "description", ""), Json.str(pr, "status", ""),
                    new ArrayList<String>(Arrays.asList(Json.strings(pr, "tags"))),
                    Json.integer(pr, "teamSize", 0), Json.integer(pr, "maxTeamSize", 0), Theme.BRAND_PINK));
        }
        pd.skills.clear();
        for (Map<String, Object> s : Json.maps(p, "skills")) {
            pd.skills.add(new ProfileData.ProfileSkill(Json.str(s, "name", ""), Json.str(s, "category", ""),
                    Json.integer(s, "endorsements", 0), Json.bool(s, "userEndorsed", false)));
        }
        pd.reviews.clear();
        for (Map<String, Object> r : Json.maps(p, "reviews")) {
            pd.reviews.add(new ProfileData.PeerReview(Json.str(r, "reviewerName", ""), Json.str(r, "reviewerInitials", "TU"),
                    Json.str(r, "reviewerRole", ""), Json.str(r, "projectName", ""), Json.str(r, "feedback", ""),
                    Json.integer(r, "rating", 5), Json.str(r, "date", ""), color(r, "avatarBg"), color(r, "avatarFg")));
        }
        if (pd.tabs.size() >= 4) {
            pd.tabs.set(1, "Projects (" + pd.projects.size() + ")");
            pd.tabs.set(3, "Peer Reviews (" + pd.totalReviewsCount + ")");
        }
    }

    private static void applySettings(SettingsData sd, Map<String, Object> s) {
        if (s == null) return;
        sd.fullName = Json.str(s, "fullName", sd.fullName);
        sd.originalFullName = sd.fullName;
        sd.username = Json.str(s, "username", sd.username);
        sd.email = Json.str(s, "email", sd.email);
        sd.hasUnsavedChanges = false;
        sd.passwordLastChanged = Json.str(s, "passwordLastChanged", "");
        sd.memberSince = Json.str(s, "memberSince", "");
        sd.accountId = Json.str(s, "accountId", "");
        sd.visibility = enumOr(SettingsData.ProfileVisibility.class, Json.str(s, "visibility", ""), SettingsData.ProfileVisibility.PUBLIC);
        sd.showSkills = Json.bool(s, "showSkills", true);
        sd.showActivity = Json.bool(s, "showActivity", false);
        sd.categories.clear();
        sd.categories.addAll(Arrays.asList(Json.strings(s, "categories")));
        sd.roleFrontendDev = Json.bool(s, "roleFrontendDev", false);
        sd.roleUiDev = Json.bool(s, "roleUiDev", false);
        sd.roleBackendDev = Json.bool(s, "roleBackendDev", false);
        sd.roleProjectLead = Json.bool(s, "roleProjectLead", false);
        sd.weeklyAvailability = Json.str(s, "weeklyAvailability", sd.weeklyAvailability);
        sd.preferredCommitment = Json.str(s, "preferredCommitment", sd.preferredCommitment);
        sd.collabMode = enumOr(SettingsData.CollabMode.class, Json.str(s, "collabMode", ""), SettingsData.CollabMode.REMOTE);
        sd.ownedProject = Json.str(s, "ownedProject", "");
        sd.supportEmail = Json.str(s, "supportEmail", sd.supportEmail);
    }

    // =========================================================================
    // ACTIONS (optimistic: the UI already changed locally; we persist and report errors)
    // =========================================================================

    private interface Call {
        ApiClient.Response run() throws IOException;
    }

    private interface OnOk {
        void handle(Map<String, Object> json);
    }

    /**
     * Runs a request in the background. onOk runs on the IO thread (keep it to field writes);
     * successToast / reload are delivered on the render thread.
     */
    private static void submit(final StudentDashboard d, final Call call, final OnOk onOk,
                               final boolean useServerMessage, final boolean reload) {
        if (!isOnline()) return;
        IO.submit(() -> {
            try {
                ApiClient.Response r = call.run();
                if (r == null) return;
                if (r.ok()) {
                    if (onOk != null) onOk.handle(r.json);
                    final String msg = useServerMessage ? r.message(null) : null;
                    if (msg != null) d.runOnUi(() -> d.showToast(msg));
                    if (reload) load(d);
                } else {
                    final String msg = r.message("Request failed (HTTP " + r.status + ")");
                    d.runOnUi(() -> d.showToast("Not saved: " + msg));
                    if (reload) load(d);
                }
            } catch (IOException | RuntimeException e) {
                d.runOnUi(() -> d.showToast("Not saved — backend unreachable."));
            }
        });
    }

    public static void setBookmark(StudentDashboard d, final String projectId, final boolean active) {
        if (projectId == null) return;
        submit(d, () -> ApiClient.post("/projects/" + projectId + "/bookmark", Json.obj("active", active)), null, false, false);
    }

    public static void requestJoin(StudentDashboard d, final String projectId) {
        if (projectId == null) return;
        submit(d, () -> ApiClient.post("/projects/" + projectId + "/join", Json.obj()), null, true, true);
    }

    public static void createProject(StudentDashboard d, String title, String desc, String category, String[] skills) {
        final Map<String, Object> body = Json.obj("title", title, "description", desc, "category", category,
                "skills", Arrays.asList(skills));
        submit(d, () -> ApiClient.post("/projects", body), null, true, true);
    }

    /** Overview / All-tasks toggle. */
    public static void setTaskDone(StudentDashboard d, final String taskId, final boolean done) {
        if (taskId == null) return;
        submit(d, () -> ApiClient.post("/tasks/" + taskId + "/toggle", Json.obj("done", done)), null, false, false);
    }

    /** Project board toggle — reads the id when the request runs, so a just-added task works too. */
    public static void setTaskDone(StudentDashboard d, final DashboardMyProjects.ProjectTask task, final boolean done) {
        submit(d, () -> task.remoteId == null ? null
                : ApiClient.post("/tasks/" + task.remoteId + "/toggle", Json.obj("done", done)), null, false, false);
    }

    public static void addTask(StudentDashboard d, final String projectId, final DashboardMyProjects.ProjectTask task) {
        if (projectId == null) return;
        final Map<String, Object> body = Json.obj("title", task.title);
        submit(d, () -> ApiClient.post("/projects/" + projectId + "/tasks", body),
                json -> task.remoteId = Json.str(Json.map(json, "task"), "id", null), false, false);
    }

    public static void decideRequest(StudentDashboard d, final String requestId, final boolean accept) {
        if (requestId == null) return;
        submit(d, () -> ApiClient.post("/requests/" + requestId + (accept ? "/accept" : "/decline"), Json.obj()),
                null, false, true);
    }

    public static void setInvite(StudentDashboard d, final String userId, final boolean active) {
        if (userId == null) return;
        submit(d, () -> ApiClient.post("/users/" + userId + "/invite", Json.obj("active", active)), null, false, false);
    }

    public static void setCollab(StudentDashboard d, final String userId, final boolean active) {
        if (userId == null) return;
        submit(d, () -> ApiClient.post("/users/" + userId + "/collab", Json.obj("active", active)), null, false, false);
    }

    public static void saveProfile(StudentDashboard d, String name, String bio, String portfolio) {
        final Map<String, Object> body = Json.obj("name", name, "bio", bio, "portfolio", portfolio);
        submit(d, () -> ApiClient.put("/me/profile", body), null, false, true);
    }

    public static void saveAccount(StudentDashboard d, SettingsData s) {
        final Map<String, Object> body = Json.obj("fullName", s.fullName, "username", s.username, "email", s.email);
        submit(d, () -> ApiClient.put("/me/settings", body), null, false, true);
    }

    public static void savePreferences(StudentDashboard d, SettingsData s) {
        final Map<String, Object> body = Json.obj(
                "visibility", s.visibility.name(),
                "showSkills", s.showSkills,
                "showActivity", s.showActivity,
                "categories", new ArrayList<String>(s.categories),
                "roleFrontendDev", s.roleFrontendDev,
                "roleUiDev", s.roleUiDev,
                "roleBackendDev", s.roleBackendDev,
                "roleProjectLead", s.roleProjectLead,
                "weeklyAvailability", s.weeklyAvailability,
                "preferredCommitment", s.preferredCommitment,
                "collabMode", s.collabMode.name());
        submit(d, () -> ApiClient.put("/me/settings", body), null, false, false);
    }

    public static void setEndorsement(StudentDashboard d, final String skillName, final boolean active) {
        submit(d, () -> ApiClient.post("/me/skills/" + ApiClient.seg(skillName) + "/endorse", Json.obj("active", active)),
                null, false, false);
    }

    public static void markNotificationsRead(StudentDashboard d) {
        submit(d, () -> ApiClient.post("/notifications/read", Json.obj()), null, false, false);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static Color color(Map<String, Object> m, String key) {
        return parseColor(Json.str(m, key, null), Theme.BRAND_PINK);
    }

    static Color parseColor(String hex, Color def) {
        if (hex == null) return def;
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() != 6) return def;
        try {
            return new Color(Integer.parseInt(h, 16));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    static Color[] statusColors(String status) {
        if ("Recruiting".equalsIgnoreCase(status)) return new Color[]{new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09)};
        if ("Completed".equalsIgnoreCase(status)) return new Color[]{new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8)};
        return new Color[]{new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D)};
    }

    private static <E extends Enum<E>> E enumOr(Class<E> type, String name, E def) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return def;
        }
    }
}

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dynamic data model for the Team Workspace (My Teams) dashboard view.
 * Uses exact landing page design tokens from Theme.java (Brand Pink #EA4C89, Ink Dark #0D0C22,
 * Warm Canvas #F8F7F4, and Dribbble pastel tints).
 * Compatible with Java 8+.
 */
public class TeamWorkspaceData {

    public static class TeamMember {
        public String name;
        public String initials;
        public String role;
        public String badge; // "Member", "You", "Admin"
        public Color avatarBg;
        public Color avatarFg;

        public TeamMember(String name, String initials, String role, String badge, Color avatarBg, Color avatarFg) {
            this.name = name;
            this.initials = initials;
            this.role = role;
            this.badge = badge;
            this.avatarBg = avatarBg;
            this.avatarFg = avatarFg;
        }
    }

    public static class MilestoneItem {
        public String title;
        public String dueInfo;
        public int progressPercent;
        public String status; // "Completed", "In progress", "Upcoming"

        public MilestoneItem(String title, String dueInfo, int progressPercent, String status) {
            this.title = title;
            this.dueInfo = dueInfo;
            this.progressPercent = progressPercent;
            this.status = status;
        }
    }

    public static class TaskSummary {
        public int todo;
        public int inProgress;
        public int inReview;
        public int done;

        public TaskSummary(int todo, int inProgress, int inReview, int done) {
            this.todo = todo;
            this.inProgress = inProgress;
            this.inReview = inReview;
            this.done = done;
        }

        public int getTotal() {
            return todo + inProgress + inReview + done;
        }
    }

    public static class RecruitmentRequest {
        public String name;
        public String initials;
        public String roleSkill;
        public Color avatarBg;
        public Color avatarFg;

        public RecruitmentRequest(String name, String initials, String roleSkill, Color avatarBg, Color avatarFg) {
            this.name = name;
            this.initials = initials;
            this.roleSkill = roleSkill;
            this.avatarBg = avatarBg;
            this.avatarFg = avatarFg;
        }
    }

    public static class TeamActivity {
        public String name;
        public String initials;
        public String actionText;
        public Color avatarBg;
        public Color avatarFg;

        public TeamActivity(String name, String initials, String actionText, Color avatarBg, Color avatarFg) {
            this.name = name;
            this.initials = initials;
            this.actionText = actionText;
            this.avatarBg = avatarBg;
            this.avatarFg = avatarFg;
        }
    }

    public static class WorkspaceFile {
        public String name;
        public String type;
        public String size;
        public String updatedAgo;
        public String author;

        public WorkspaceFile(String name, String type, String size, String updatedAgo, String author) {
            this.name = name;
            this.type = type;
            this.size = size;
            this.updatedAgo = updatedAgo;
            this.author = author;
        }
    }

    public static class WorkspaceTask {
        public String id;
        public String title;
        public String assignee;
        public String assigneeInitials;
        public Color avatarBg;
        public Color avatarFg;
        public String status; // "todo", "in_progress", "in_review", "done"
        public String due;

        public WorkspaceTask(String id, String title, String assignee, String assigneeInitials,
                             Color avatarBg, Color avatarFg, String status, String due) {
            this.id = id;
            this.title = title;
            this.assignee = assignee;
            this.assigneeInitials = assigneeInitials;
            this.avatarBg = avatarBg;
            this.avatarFg = avatarFg;
            this.status = status;
            this.due = due;
        }
    }

    // Workspace identity & meta
    public String workspaceName = "CampusConnect workspace";
    public String subtitle = "Your team, your progress, your next big thing.";
    public String breadcrumbs = "My Teams / CampusConnect / Workspace";

    // KPI Metrics
    public int progressPercent = 68;
    public int tasksCompleted = 19;
    public int tasksTotal = 28;
    public int currentMembers = 5;
    public int maxMembers = 7;
    public int openPositions = 2;
    public String nextMilestoneDate = "Nov 06";
    public String nextMilestoneSub = "MVP development · 21 days left";
    public int tasksDueSoon = 3;
    public String tasksDueSoonSub = "1 overdue · Needs attention";

    // Workspace navigation tabs
    public List<String> tabs = new ArrayList<String>(Arrays.asList("Overview", "Members (5)", "Tasks (28)", "Files", "Requests (3)"));
    public int activeTabIndex = 0;

    // Collections
    public final List<TeamMember> members = new ArrayList<TeamMember>();
    public final List<MilestoneItem> milestones = new ArrayList<MilestoneItem>();
    public TaskSummary taskSummary = new TaskSummary(3, 3, 3, 19);
    public final List<RecruitmentRequest> recruitmentRequests = new ArrayList<RecruitmentRequest>();
    public int pendingRequestsCount = 3;
    public final List<TeamActivity> activities = new ArrayList<TeamActivity>();
    public String syncNotice = "Last team sync: Oct 14 · Next: Oct 21, 4 PM";
    public final List<WorkspaceFile> files = new ArrayList<WorkspaceFile>();
    public final List<WorkspaceTask> tasks = new ArrayList<WorkspaceTask>();

    public TeamWorkspaceData() {
        resetToDefault();
    }

    public void resetToDefault() {
        workspaceName = "CampusConnect workspace";
        subtitle = "Your team, your progress, your next big thing.";
        breadcrumbs = "My Teams / CampusConnect / Workspace";

        progressPercent = 68;
        tasksCompleted = 19;
        tasksTotal = 28;
        currentMembers = 5;
        maxMembers = 7;
        openPositions = 2;
        nextMilestoneDate = "Nov 06";
        nextMilestoneSub = "MVP development · 21 days left";
        tasksDueSoon = 3;
        tasksDueSoonSub = "1 overdue · Needs attention";

        // Members with landing page palette
        members.clear();
        members.add(new TeamMember("Priya Shah", "PS", "Design lead", "Member", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET));
        members.add(new TeamMember("Alex Morgan", "AM", "Full-stack dev", "You", Theme.PASTEL_PINK, Theme.BRAND_PINK));
        members.add(new TeamMember("Daniel Kim", "DK", "Backend dev", "Member", Theme.PASTEL_MINT, Theme.SUCCESS_GREEN));
        members.add(new TeamMember("Sofia Martinez", "SM", "Frontend dev", "Member", Theme.PASTEL_PINK, Theme.BRAND_PINK));
        members.add(new TeamMember("Ethan Chen", "EC", "QA engineer", "Member", Theme.PASTEL_SUNNY, Theme.HIGHLIGHT_WARM));

        // Milestones
        milestones.clear();
        milestones.add(new MilestoneItem("Research & design", "Due Oct 12 · Completed", 100, "Completed"));
        milestones.add(new MilestoneItem("MVP development", "Due Nov 06 · In progress", 68, "In progress"));
        milestones.add(new MilestoneItem("Beta testing & launch", "Due Nov 20 · Upcoming", 0, "Upcoming"));

        taskSummary = new TaskSummary(3, 3, 3, 19);

        // Recruitment requests
        recruitmentRequests.clear();
        recruitmentRequests.add(new RecruitmentRequest("Jordan Lee", "JL", "Frontend developer · React", Theme.PASTEL_SKY, Theme.ACCENT_SKY));
        recruitmentRequests.add(new RecruitmentRequest("Amara Okafor", "AO", "QA engineer · Playwright", Theme.PASTEL_PINK, Theme.BRAND_PINK));
        pendingRequestsCount = 3;

        // Team activity
        activities.clear();
        activities.add(new TeamActivity("Priya Shah", "PS", "Shared event wireframes · 18m", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET));
        activities.add(new TeamActivity("Daniel Kim", "DK", "Completed event API schema · 42m", Theme.PASTEL_MINT, Theme.SUCCESS_GREEN));
        activities.add(new TeamActivity("Sofia Martinez", "SM", "Updated event card task · 1h", Theme.PASTEL_PINK, Theme.BRAND_PINK));
        syncNotice = "Last team sync: Oct 14 · Next: Oct 21, 4 PM";

        // Files
        files.clear();
        files.add(new WorkspaceFile("campus-wireframes-v3.fig", "Figma", "14.2 MB", "2 hours ago", "Priya Shah"));
        files.add(new WorkspaceFile("api-specs-rest-v1.yaml", "YAML", "420 KB", "Yesterday", "Daniel Kim"));
        files.add(new WorkspaceFile("prd-campusconnect-final.pdf", "PDF", "2.8 MB", "3 days ago", "Alex Morgan"));

        // Sprint Tasks Kanban
        tasks.clear();
        tasks.add(new WorkspaceTask("t1", "Supabase Auth & Session Storage", "Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK, "todo", "Today"));
        tasks.add(new WorkspaceTask("t2", "Campus Map Pin Clustering", "Daniel Kim", "DK", Theme.PASTEL_MINT, Theme.SUCCESS_GREEN, "todo", "Tomorrow"));
        tasks.add(new WorkspaceTask("t3", "Accessibility Contrast Audit", "Priya Shah", "PS", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET, "todo", "Nov 02"));

        tasks.add(new WorkspaceTask("t4", "Event Filter Dropdowns UI", "Sofia Martinez", "SM", Theme.PASTEL_PINK, Theme.BRAND_PINK, "in_progress", "Today"));
        tasks.add(new WorkspaceTask("t5", "REST API Schema for Events", "Daniel Kim", "DK", Theme.PASTEL_MINT, Theme.SUCCESS_GREEN, "in_progress", "Nov 01"));
        tasks.add(new WorkspaceTask("t6", "Realtime WebSocket Notifications", "Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK, "in_progress", "Nov 04"));

        tasks.add(new WorkspaceTask("t7", "Playwright E2E Test Suite", "Ethan Chen", "EC", Theme.PASTEL_SUNNY, Theme.HIGHLIGHT_WARM, "in_review", "Today"));
        tasks.add(new WorkspaceTask("t8", "Mobile Responsive Drawer", "Priya Shah", "PS", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET, "in_review", "Yesterday"));
        tasks.add(new WorkspaceTask("t9", "Figma Design Token Export", "Priya Shah", "PS", Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET, "in_review", "Oct 28"));

        tasks.add(new WorkspaceTask("t10", "Project Scaffold & Build Scripts", "Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK, "done", "Completed"));
        tasks.add(new WorkspaceTask("t11", "Database Schema Migration V1", "Daniel Kim", "DK", Theme.PASTEL_MINT, Theme.SUCCESS_GREEN, "done", "Completed"));
        tasks.add(new WorkspaceTask("t12", "Landing Page Double Buffering", "Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK, "done", "Completed"));
    }

    public void addTask(String title, String assignee, String initials, Color bg, Color fg, String status, String due) {
        tasks.add(0, new WorkspaceTask("t" + (tasks.size() + 1), title, assignee, initials, bg, fg, status, due));
        syncTaskSummary();
    }

    public void advanceTaskStatus(WorkspaceTask t) {
        if ("todo".equalsIgnoreCase(t.status)) {
            t.status = "in_progress";
        } else if ("in_progress".equalsIgnoreCase(t.status)) {
            t.status = "in_review";
        } else if ("in_review".equalsIgnoreCase(t.status)) {
            t.status = "done";
        } else {
            t.status = "todo";
        }
        syncTaskSummary();
    }

    public void syncTaskSummary() {
        int todo = 0, prog = 0, rev = 0, done = 0;
        for (WorkspaceTask t : tasks) {
            if ("todo".equalsIgnoreCase(t.status)) todo++;
            else if ("in_progress".equalsIgnoreCase(t.status)) prog++;
            else if ("in_review".equalsIgnoreCase(t.status)) rev++;
            else if ("done".equalsIgnoreCase(t.status)) done++;
        }
        int baseDone = 16;
        taskSummary = new TaskSummary(todo, prog, rev, done + baseDone);
        tasksCompleted = done + baseDone;
        tasksTotal = todo + prog + rev + done + baseDone;
        updateProgress(tasksCompleted, tasksTotal);
        tabs.set(2, "Tasks (" + tasksTotal + ")");
    }

    public void acceptRequest(int index) {
        if (index >= 0 && index < recruitmentRequests.size()) {
            RecruitmentRequest r = recruitmentRequests.remove(index);
            String roleName = r.roleSkill != null && r.roleSkill.contains("·") ? r.roleSkill.split("·")[0].trim() : r.roleSkill;
            addMember(r.name, roleName, false);
            pendingRequestsCount = Math.max(0, recruitmentRequests.size());
            tabs.set(4, "Requests (" + pendingRequestsCount + ")");
        }
    }

    public void declineRequest(int index) {
        if (index >= 0 && index < recruitmentRequests.size()) {
            recruitmentRequests.remove(index);
            pendingRequestsCount = Math.max(0, recruitmentRequests.size());
            tabs.set(4, "Requests (" + pendingRequestsCount + ")");
        }
    }

    public void addFile(String name, String type, String size, String author) {
        files.add(0, new WorkspaceFile(name, type, size, "Just now", author));
    }

    public void removeFile(int index) {
        if (index >= 0 && index < files.size()) {
            files.remove(index);
        }
    }

    public void addMember(String name, String role, boolean isYou) {
        String[] parts = name.trim().split("\\s+");
        String initials = "";
        if (parts.length >= 2) {
            initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            initials = ("" + parts[0].charAt(0)).toUpperCase();
        } else {
            initials = "TM";
        }
        members.add(new TeamMember(name, initials, role, isYou ? "You" : "Member", Theme.PASTEL_PINK, Theme.BRAND_PINK));
        currentMembers = members.size();
        tabs.set(1, "Members (" + currentMembers + ")");
    }

    public void removeMember(int index) {
        if (index >= 0 && index < members.size()) {
            members.remove(index);
            currentMembers = members.size();
            tabs.set(1, "Members (" + currentMembers + ")");
        }
    }

    public void addActivity(String name, String initials, String action, Color bg, Color fg) {
        activities.add(0, new TeamActivity(name, initials, action, bg, fg));
        if (activities.size() > 8) {
            activities.remove(activities.size() - 1);
        }
    }

    public void addMilestone(String title, String dueInfo, int progress, String status) {
        milestones.add(new MilestoneItem(title, dueInfo, progress, status));
    }

    public void updateProgress(int completed, int total) {
        this.tasksCompleted = completed;
        this.tasksTotal = total;
        if (total > 0) {
            this.progressPercent = Math.min(100, Math.round((completed * 100f) / total));
        }
    }
}

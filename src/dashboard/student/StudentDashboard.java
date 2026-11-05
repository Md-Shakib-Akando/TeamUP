import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clean, modularized Student Workspace Dashboard for TeamUp.
 * Coordinates DashboardSidebar, DashboardHeader, DashboardOverview,
 * DashboardProjects (Discover Projects), DashboardTeammates (Find Teammates), and DashboardModals.
 * Pure core Java - zero third-party dependencies.
 */
public class StudentDashboard {

    public interface DashboardListener {
        void returnToLanding();
        void openModal(String title, String message, boolean isRegister);
        default void switchToAdminDashboard() {}
    }

    private final DashboardListener listener;

    // Modular Sub-components
    private final DashboardSidebar sidebar;
    private final DashboardHeader header;
    private final DashboardOverview overview;
    private final DashboardProjects projectsView;
    private final DashboardTeammates teammatesView;
    private final DashboardMyProjects myProjectsView;
    private final DashboardModals modals;

    // Viewport & scrolling
    private float scrollY = 0f;
    private float targetScrollY = 0f;

    // Sidebar navigation items
    public enum NavItem {
        OVERVIEW("Overview", 0),
        DISCOVER_PROJECTS("Discover Projects", 0),
        FIND_TEAMMATES("Find Teammates", 0),
        MY_PROJECTS("My Projects", 0),
        MY_TEAMS("My Teams", 0),
        MESSAGES("Messages", 3),
        MY_PROFILE("My Profile", 0),
        SETTINGS("Settings", 0);

        public final String label;
        public final int badge;
        NavItem(String label, int badge) {
            this.label = label;
            this.badge = badge;
        }
    }

    // Modal state enum
    public enum ActiveModal {
        NONE,
        CREATE_PROJECT,
        COMPLETE_PROFILE,
        PROJECT_DETAILS,
        TEAMMATE_DETAILS,
        ALL_TASKS,
        MESSAGES,
        TEAMS_LIST
    }

    private NavItem activeNav = NavItem.FIND_TEAMMATES;
    private NavItem lastNav = NavItem.FIND_TEAMMATES;
    private NavItem hoveredNav = null;

    // Interactive Metrics Counters
    private int activeProjectsCount = 3;
    private int myTeamsCount = 4;
    private int pendingRequestsCount = 5;
    private int tasksDueCount = 6;
    private int profileCompletePercent = 80;

    // User Profile state
    private String profileName = "Alex Morgan";
    private String profileBio = "Computer Science student passionate about distributed systems, React, and UX.";
    private String profilePortfolio = "https://alexmorgan.dev";

    // Toast notification
    private String toastMessage = "";
    private float toastTimer = 0f;

    // Selected items for detail modals
    private ProjectItem selectedProject = null;
    private TeammateItem selectedTeammate = null;

    // Data Models
    public static class ProjectItem {
        public String title, author, desc;
        public String[] skills;
        public String footer;
        public boolean isGreen, bookmarked;
        public int type;
        public int hitX, hitY, hitW, hitH;
        public int bmX, bmY;

        public ProjectItem(String title, String author, String desc, String[] skills, String footer, boolean isGreen, int type) {
            this.title = title;
            this.author = author;
            this.desc = desc;
            this.skills = skills;
            this.footer = footer;
            this.isGreen = isGreen;
            this.type = type;
            this.bookmarked = false;
        }
    }

    public static class TeammateItem {
        public String initials;
        public Color avBg, avText;
        public String name, role, matchScore, bio, university;
        public boolean invited;
        public int hitX, hitY, hitW, hitH;

        public TeammateItem(String initials, Color avBg, Color avText, String name, String role, String matchScore, String bio, String university) {
            this.initials = initials;
            this.avBg = avBg;
            this.avText = avText;
            this.name = name;
            this.role = role;
            this.matchScore = matchScore;
            this.bio = bio;
            this.university = university;
            this.invited = false;
        }
    }

    public static class TaskItem {
        public String title, subtitle;
        public int statusType; // 0: Overdue, 1: Today, 2: Normal
        public boolean completed;
        public int hitX, hitY, hitW, hitH;

        public TaskItem(String title, String subtitle, int statusType) {
            this.title = title;
            this.subtitle = subtitle;
            this.statusType = statusType;
            this.completed = false;
        }
    }

    public static class ActivityItem {
        public String initials;
        public Color avBg, avText;
        public String title, subtitle, badgeText;
        public Color badgeBg, badgeTextCol;

        public ActivityItem(String initials, Color avBg, Color avText, String title, String subtitle, String badgeText, Color badgeBg, Color badgeTextCol) {
            this.initials = initials;
            this.avBg = avBg;
            this.avText = avText;
            this.title = title;
            this.subtitle = subtitle;
            this.badgeText = badgeText;
            this.badgeBg = badgeBg;
            this.badgeTextCol = badgeTextCol;
        }
    }

    public static class PillHitbox {
        public String name;
        public int x, y, w, h;
        public PillHitbox(String name, int x, int y, int w, int h) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    // Dynamic Lists for Overview
    private final List<ProjectItem> projects = new ArrayList<>();
    private final List<TeammateItem> teammates = new ArrayList<>();
    private final List<TaskItem> tasks = new ArrayList<>();
    private final List<ActivityItem> activities = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();
    private final List<String> chatHistory = new ArrayList<>(Arrays.asList(
            "Priya Shah: Hey Alex, have you checked the new UI mockups for GreenRoute?",
            "Alex Morgan: Yes! Looks super clean, especially the route mapping cards.",
            "Priya Shah: Awesome! Let's sync today at 4 PM to finalize the schema."
    ));

    public StudentDashboard(DashboardListener listener) {
        this.listener = listener;

        this.sidebar = new DashboardSidebar();
        this.header = new DashboardHeader();
        this.overview = new DashboardOverview();
        this.projectsView = new DashboardProjects();
        this.teammatesView = new DashboardTeammates();
        this.myProjectsView = new DashboardMyProjects();
        this.modals = new DashboardModals();

        // Initial Overview Projects
        projects.add(new ProjectItem(
                "GreenRoute", "Maya Patel · Sustainability",
                "Help students choose cleaner ways to get around campus.",
                new String[]{"React", "Maps API", "Node.js"},
                "2 open roles · 4 / 6 members", true, 0
        ));
        projects.add(new ProjectItem(
                "OpenLab", "Noah Williams · Research",
                "Connect student researchers with labs and shared knowledge.",
                new String[]{"TypeScript", "PostgreSQL"},
                "2 open roles · 3 / 5 members", false, 1
        ));

        // Initial Overview Teammates
        teammates.add(new TeammateItem(
                "PS", DashboardTheme.PURPLE_TINT_BG, DashboardTheme.BRAND_PURPLE,
                "Priya Shah", "UX/UI designer · UW", "96% sample",
                "Senior studying HCDE at UW. Focused on user research, Figma prototypes, and accessible design systems.",
                "University of Washington"
        ));
        teammates.add(new TeammateItem(
                "DK", new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D),
                "Daniel Kim", "Backend developer · UW", "92% sample",
                "Junior studying CS at UW. Experienced in Java Spring, PostgreSQL, and high-performance microservices.",
                "University of Washington"
        ));

        // Initial Overview Tasks
        tasks.add(new TaskItem("Finalize event API contract", "Overdue · Oct 15 · CampusConnect", 0));
        tasks.add(new TaskItem("Build event discovery cards", "Today · Oct 16 · CampusConnect", 1));
        tasks.add(new TaskItem("Accessibility review", "Mon, Oct 19 · CampusConnect", 2));

        // Initial Overview Activities
        activities.add(new ActivityItem(
                "PS", DashboardTheme.PURPLE_TINT_BG, DashboardTheme.BRAND_PURPLE,
                "Priya Shah", "", "Design", DashboardTheme.PURPLE_TINT_BG, DashboardTheme.BRAND_PURPLE
        ));
        activities.add(new ActivityItem(
                "DK", new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D),
                "Daniel Kim", "Completed the event API schema · 42 min ago", "Completed", new Color(0xDC, 0xFC, 0xE7), new Color(0x16, 0xA3, 0x4A)
        ));
        activities.add(new ActivityItem(
                "SM", new Color(0xFF, 0xE4, 0xE6), new Color(0xBE, 0x12, 0x3C),
                "Sofia Martinez", "Mentioned you in \"Build event cards\" · 1 hour ago", "Task", new Color(0xF1, 0xF5, 0xF9), new Color(0x47, 0x55, 0x69)
        ));

        // Initial Notifications
        notifications.add("Priya Shah accepted your invite to GreenRoute (10m ago)");
        notifications.add("Daniel Kim completed the event API schema (42m ago)");
        notifications.add("New teammate match found: Sofia Martinez (1h ago)");
        notifications.add("Deadline reminder: Finalize API contract (Yesterday)");
    }

    public void showToast(String message) {
        this.toastMessage = message;
        this.toastTimer = 3.2f;
    }

    public void openModal(ActiveModal modal) {
        header.closePopups();
        modals.open(modal);
    }

    public void closeModal() {
        modals.closeModal();
    }

    public ActiveModal getActiveModal() {
        return modals.getActiveModal();
    }

    public void returnToLanding() {
        if (listener != null) listener.returnToLanding();
    }

    public void switchToAdminDashboard() {
        if (listener != null) listener.switchToAdminDashboard();
    }

    public void update(float dt) {
        if (activeNav != lastNav) {
            lastNav = activeNav;
            targetScrollY = 0f;
            scrollY = 0f;
        }

        scrollY = Anim.approach(scrollY, targetScrollY, dt, 14f);
        if (toastTimer > 0f) {
            toastTimer -= dt;
            if (toastTimer <= 0f) toastMessage = "";
        }
        modals.update(dt);
    }

    public void paint(Graphics2D g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        // 1. Overall Background Canvas
        g2.setColor(DashboardTheme.BG_CANVAS);
        g2.fillRect(0, 0, width, height);

        int sideW = 230;
        int headerH = 60;

        // 2. Main Scrollable Content Area (Clipped under header & right of sidebar)
        Shape oldClip = g2.getClip();
        g2.setClip(sideW, headerH, width - sideW, height - headerH);

        Graphics2D cg = (Graphics2D) g2.create();
        int contentY = headerH - Math.round(scrollY);

        switch (activeNav) {
            case MY_PROJECTS:
                myProjectsView.paint(cg, sideW, contentY, width - sideW, height, this);
                break;
            case FIND_TEAMMATES:
                teammatesView.paint(cg, sideW, contentY, width - sideW, height, this);
                break;
            case DISCOVER_PROJECTS:
                projectsView.paint(cg, sideW, contentY, width - sideW, height, this);
                break;
            default:
                overview.paint(cg, sideW, contentY, width - sideW, height, this);
                break;
        }
        cg.dispose();

        g2.setClip(oldClip);

        // 3. Left Fixed Sidebar
        sidebar.paint(g2, 0, 0, sideW, height, this);

        // 4. Top Fixed Header
        header.paint(g2, sideW, 0, width - sideW, headerH, this);

        // 5. Scrollbar for Content
        paintContentScrollbar(g2, width - 8, headerH, 6, height - headerH);

        // 6. Active Dropdown Overlays & Profile Modal
        if (activeNav == NavItem.MY_PROJECTS) {
            myProjectsView.paintOverlays(g2, width, height);
        } else if (activeNav == NavItem.DISCOVER_PROJECTS) {
            projectsView.paintDropdownOverlay(g2);
        } else if (activeNav == NavItem.FIND_TEAMMATES) {
            teammatesView.paintOverlays(g2, width, height);
        }

        // 7. Active Modal Dialog (Instant close if NONE)
        modals.paint(g2, width, height, this);

        // 8. Toast Notification (if active)
        if (!toastMessage.isEmpty()) {
            paintToast(g2, width, height);
        }

        g2.dispose();
    }

    private int getCurrentContentHeight() {
        switch (activeNav) {
            case MY_PROJECTS:
                return myProjectsView.getTotalContentHeight();
            case FIND_TEAMMATES:
                return teammatesView.getTotalContentHeight();
            case DISCOVER_PROJECTS:
                return projectsView.getTotalContentHeight();
            default:
                return overview.getTotalContentHeight();
        }
    }

    private void paintContentScrollbar(Graphics2D g, int x, int y, int w, int h) {
        int totalH = getCurrentContentHeight();
        if (totalH <= h) return;
        float ratio = (float) h / (float) totalH;
        int thumbH = Math.max(36, Math.round(h * ratio));
        int maxScroll = totalH - h;
        float pct = scrollY / (float) maxScroll;
        int thumbY = y + Math.round((h - thumbH) * pct);

        RoundRectangle2D thumb = new RoundRectangle2D.Float(x, thumbY, w, thumbH, w, w);
        g.setColor(new Color(0xD1, 0xD5, 0xDB));
        g.fill(thumb);
    }

    private void paintToast(Graphics2D g, int width, int height) {
        g.setFont(DashboardTheme.FONT_BOLD_12);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(toastMessage) + 36;
        int th = 36;
        int tx = (width - tw) / 2;
        int ty = height - 60;

        RoundRectangle2D tShape = new RoundRectangle2D.Float(tx, ty, tw, th, 18, 18);
        g.setColor(new Color(0x1E, 0x29, 0x3B));
        g.fill(tShape);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.setStroke(new BasicStroke(1f));
        g.draw(tShape);

        g.setColor(Color.WHITE);
        g.drawString("✓  " + toastMessage, tx + 18, ty + 22);
    }

    // =========================================================================
    // EVENT HANDLING & USER ACTIONS
    // =========================================================================
    public boolean onClick(int mx, int my) {
        // 1. Modals take priority
        if (modals.onClick(mx, my, this)) {
            return true;
        }

        // 2. Active View's High Z-index overlays take priority (Dropdowns & Profile Modals)
        if (activeNav == NavItem.MY_PROJECTS) {
            if (myProjectsView.onClick(mx, my, this)) return true;
        } else if (activeNav == NavItem.FIND_TEAMMATES) {
            if (teammatesView.onClick(mx, my, this)) return true;
        } else if (activeNav == NavItem.DISCOVER_PROJECTS) {
            if (projectsView.onClick(mx, my, this)) return true;
        }

        // 3. Header controls
        if (header.onClick(mx, my, this)) {
            return true;
        }

        // 4. Sidebar controls
        if (sidebar.onClick(mx, my, this)) {
            return true;
        }

        // 5. Main Content Area controls
        switch (activeNav) {
            case MY_PROJECTS:
                if (myProjectsView.onClick(mx, my, this)) return true;
                break;
            case FIND_TEAMMATES:
                if (teammatesView.onClick(mx, my, this)) return true;
                break;
            case DISCOVER_PROJECTS:
                if (projectsView.onClick(mx, my, this)) return true;
                break;
            default:
                if (overview.onClick(mx, my, this)) return true;
                break;
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        if (modals.onMouseMove(mx, my)) {
            return true;
        }

        boolean hHov = header.onMouseMove(mx, my);
        boolean sHov = sidebar.onMouseMove(mx, my, this);

        boolean vHov = false;
        switch (activeNav) {
            case MY_PROJECTS:
                vHov = myProjectsView.onMouseMove(mx, my);
                break;
            case FIND_TEAMMATES:
                vHov = teammatesView.onMouseMove(mx, my);
                break;
            case DISCOVER_PROJECTS:
                vHov = projectsView.onMouseMove(mx, my);
                break;
            default:
                vHov = overview.onMouseMove(mx, my);
                break;
        }

        return hHov || sHov || vHov;
    }

    public void onMouseWheel(int wheelRotation, int screenH) {
        int maxScroll = Math.max(0, getCurrentContentHeight() - (screenH - 60));
        targetScrollY = Math.max(0, Math.min(targetScrollY + wheelRotation * 45, maxScroll));
    }

    public boolean onKey(KeyEvent e) {
        // Modal keys take priority
        if (modals.onKey(e, this)) {
            return true;
        }

        // Header search keys
        if (header.onKey(e, this)) {
            return true;
        }

        // Active View keys (Search boxes etc.)
        switch (activeNav) {
            case MY_PROJECTS:
                if (myProjectsView.onKey(e)) return true;
                break;
            case FIND_TEAMMATES:
                if (teammatesView.onKey(e)) return true;
                break;
            case DISCOVER_PROJECTS:
                if (projectsView.onKey(e)) return true;
                break;
            default:
                break;
        }

        return false;
    }

    // Getters and Setters
    public NavItem getActiveNav() { return activeNav; }
    public void setActiveNav(NavItem nav) {
        this.activeNav = nav;
        this.targetScrollY = 0f;
        this.scrollY = 0f;
    }

    public NavItem getHoveredNav() { return hoveredNav; }
    public void setHoveredNav(NavItem nav) { this.hoveredNav = nav; }

    public int getActiveProjectsCount() { return activeProjectsCount; }
    public void setActiveProjectsCount(int c) { this.activeProjectsCount = c; }

    public int getMyTeamsCount() { return myTeamsCount; }
    public void setMyTeamsCount(int c) { this.myTeamsCount = c; }

    public int getPendingRequestsCount() { return pendingRequestsCount; }
    public void setPendingRequestsCount(int c) { this.pendingRequestsCount = c; }

    public int getTasksDueCount() { return tasksDueCount; }
    public void setTasksDueCount(int c) { this.tasksDueCount = c; }

    public int getProfileCompletePercent() { return profileCompletePercent; }
    public void setProfileCompletePercent(int p) { this.profileCompletePercent = p; }

    public String getProfileName() { return profileName; }
    public void setProfileName(String n) { this.profileName = n; }

    public String getProfileBio() { return profileBio; }
    public void setProfileBio(String b) { this.profileBio = b; }

    public String getProfilePortfolio() { return profilePortfolio; }
    public void setProfilePortfolio(String u) { this.profilePortfolio = u; }

    public ProjectItem getSelectedProject() { return selectedProject; }
    public void setSelectedProject(ProjectItem p) { this.selectedProject = p; }

    public TeammateItem getSelectedTeammate() { return selectedTeammate; }
    public void setSelectedTeammate(TeammateItem t) { this.selectedTeammate = t; }

    public List<ProjectItem> getProjects() { return projects; }
    public List<TeammateItem> getTeammates() { return teammates; }
    public List<TaskItem> getTasks() { return tasks; }
    public List<ActivityItem> getActivities() { return activities; }
    public List<String> getNotifications() { return notifications; }
    public List<String> getChatHistory() { return chatHistory; }

    public DashboardProjects getProjectsView() { return projectsView; }
    public DashboardTeammates getTeammatesView() { return teammatesView; }
    public DashboardMyProjects getMyProjectsView() { return myProjectsView; }
}

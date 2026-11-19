import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Pixel-perfect Student Workspace Desktop Application for TeamUp.
 * Fully integrates modular views (DashboardOverview, DashboardProjects,
 * DashboardTeammates, DashboardMyProjects)
 * and dedicated interactive views (My Teams, Messages, My Profile, Settings).
 * All 4 dedicated views are 100% unified with the exact landing page design
 * tokens from Theme.java:
 * - Brand Hot Pink (#EA4C89)
 * - Deep Ink Dark (#0D0C22)
 * - Warm Off-White Canvas (#F8F7F4)
 * - Dribbble Pastel Tints (Violet, Mint, Sunny, Sky, Pink)
 * Pure core Java with zero third-party dependencies. Compatible with Java 8+.
 */
public class StudentDashboard {

    public interface DashboardListener {
        void returnToLanding();

        void openModal(String title, String message, boolean isRegister);

        default void switchToAdminDashboard() {
        }
    }

    private final DashboardListener listener;

    // Dedicated Interactive Views Data Models
    private final TeamWorkspaceData teamData = new TeamWorkspaceData();
    private final SettingsData settingsData = new SettingsData();
    private final ProfileData profileData = new ProfileData();

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
    private int totalContentHeight = 1100;

    // Sidebar navigation items
    public enum NavItem {
        OVERVIEW("Overview", 0),
        DISCOVER_PROJECTS("Discover Projects", 0),
        FIND_TEAMMATES("Find Teammates", 0),
        MY_PROJECTS("My Projects", 0),
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
    private String profileInitials = "AM";
    private String profileRole = "Student";

    // Backend sync: stat card subtitles from the API, and work queued for the render thread
    private String[] statSubtitles = null;
    private final ConcurrentLinkedQueue<Runnable> uiQueue = new ConcurrentLinkedQueue<Runnable>();
    private volatile boolean serverDataLoaded = false;

    // Toast notification
    private String toastMessage = "";
    private float toastTimer = 0f;

    // Selected items for detail modals
    private ProjectItem selectedProject = null;
    private TeammateItem selectedTeammate = null;

    public static class ProjectItem {
        public String title, author, desc;
        public String[] skills;
        public String footer;
        public boolean isGreen, bookmarked;
        public String remoteId;   // backend project id (null for demo data)
        public boolean requested; // pending join request already sent
        public int type;
        public int hitX, hitY, hitW, hitH;
        public int bmX, bmY;

        // Optional detail fields (filled when opened from Discover Projects; unknown = null / -1)
        public String category, experience, creatorName, creatorInitials, openRoles;
        public Color creatorColor, iconBg, iconColor;
        public int currentMembers = -1, maxMembers = -1;
        public boolean isMember;
        /** The Discover card this was opened from, so a join request updates the card too. */
        public DashboardProjects.ProjectCard sourceCard;

        public ProjectItem(String title, String author, String desc, String[] skills, String footer, boolean isGreen,
                int type) {
            this.title = title;
            this.author = author;
            this.desc = desc;
            this.skills = skills;
            this.footer = footer;
            this.isGreen = isGreen;
            this.bookmarked = false;
            this.type = type;
        }
    }

    public static class TeammateItem {
        public String initials;
        public Color avBg, avText;
        public String name, role, matchScore, bio, university;
        public String remoteId; // backend user id (null for demo data)
        public boolean invited;
        public int hitX, hitY, hitW, hitH;

        public TeammateItem(String initials, Color avBg, Color avText, String name, String role, String matchScore,
                String bio, String university) {
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
        public String remoteId; // backend task id (null for demo data)
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

        public ActivityItem(String initials, Color avBg, Color avText, String title, String subtitle, String badgeText,
                Color badgeBg, Color badgeTextCol) {
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
    private final List<ProjectItem> projects = new ArrayList<ProjectItem>();
    private final List<TeammateItem> teammates = new ArrayList<TeammateItem>();
    private final List<TaskItem> tasks = new ArrayList<TaskItem>();
    private final List<ActivityItem> activities = new ArrayList<ActivityItem>();
    private final List<String> notifications = new ArrayList<String>();

    // =========================================================================
    // HITBOXES - MY PROFILE
    // =========================================================================
    private final Rectangle editProfileBtnBox = new Rectangle();
    private boolean editProfileHovered = false;
    private final Rectangle shareProfileBtnBox = new Rectangle();
    private boolean shareProfileHovered = false;
    private final Rectangle[] profileTabBounds = new Rectangle[4];
    private int hoveredProfileTab = -1;
    private final List<Rectangle> profileSkillBounds = new ArrayList<Rectangle>();
    private final List<Rectangle> profileProjectBounds = new ArrayList<Rectangle>();

    // =========================================================================
    // HITBOXES - SETTINGS (4 TABS)
    // =========================================================================
    private final Rectangle[] settingsTabBounds = new Rectangle[4];
    private int hoveredSettingsTab = -1;

    // Tab 1: Account
    private final Rectangle fullNameBox = new Rectangle();
    private final Rectangle usernameBox = new Rectangle();
    private final Rectangle emailBox = new Rectangle();
    private final Rectangle saveAccountBtnBox = new Rectangle();
    private boolean saveAccountHovered = false;
    private final Rectangle cancelAccountBtnBox = new Rectangle();
    private boolean cancelAccountHovered = false;
    private final Rectangle changePassBtnBox = new Rectangle();
    private boolean changePassHovered = false;

    // Tab 2: Profile & Privacy
    private final Rectangle radioPublicBox = new Rectangle();
    private final Rectangle radioTeamBox = new Rectangle();
    private final Rectangle radioPrivateBox = new Rectangle();
    private final Rectangle toggleSkillsBox = new Rectangle();
    private final Rectangle toggleActivityBox = new Rectangle();
    private final Rectangle savePrivacyBtnBox = new Rectangle();
    private boolean savePrivacyHovered = false;
    private final Rectangle cancelPrivacyBtnBox = new Rectangle();
    private boolean cancelPrivacyHovered = false;

    // Tab 3: Collaboration Preferences
    private final List<Rectangle> catRemoveBounds = new ArrayList<Rectangle>();
    private final Rectangle addCatBox = new Rectangle();
    private final Rectangle checkFrontendBox = new Rectangle();
    private final Rectangle checkUiBox = new Rectangle();
    private final Rectangle checkBackendBox = new Rectangle();
    private final Rectangle checkLeadBox = new Rectangle();
    private final Rectangle availDropdown = new Rectangle();
    private final Rectangle commitDropdown = new Rectangle();
    private final Rectangle modeRemoteBox = new Rectangle();
    private final Rectangle modeInPersonBox = new Rectangle();
    private final Rectangle modeEitherBox = new Rectangle();
    private final Rectangle saveCollabBtnBox = new Rectangle();
    private boolean saveCollabHovered = false;
    private final Rectangle cancelCollabBtnBox = new Rectangle();
    private boolean cancelCollabHovered = false;

    // Tab 4: Danger Zone
    private final Rectangle deactivateBtnBox = new Rectangle();
    private boolean deactivateHovered = false;
    private final Rectangle deleteAccountBtnBox = new Rectangle();
    private boolean deleteAccountHovered = false;

    // =========================================================================
    // HITBOXES - MY TEAMS
    // =========================================================================
    private int teamSettingsBtnX, teamSettingsBtnY, teamSettingsBtnW, teamSettingsBtnH;
    private boolean teamSettingsBtnHovered = false;
    private final Rectangle[] teamTabBounds = new Rectangle[8];
    private int hoveredTeamTabIndex = -1;
    private int inviteMemberBtnX, inviteMemberBtnY, inviteMemberBtnW, inviteMemberBtnH;
    private boolean inviteMemberBtnHovered = false;
    private int viewRoadmapBtnX, viewRoadmapBtnY, viewRoadmapBtnW, viewRoadmapBtnH;
    private boolean viewRoadmapBtnHovered = false;
    private int openBoardBtnX, openBoardBtnY, openBoardBtnW, openBoardBtnH;
    private boolean openBoardBtnHovered = false;
    private int reviewAllBtnX, reviewAllBtnY, reviewAllBtnW, reviewAllBtnH;
    private boolean reviewAllBtnHovered = false;
    private int reviewAppsBtnX, reviewAppsBtnY, reviewAppsBtnW, reviewAppsBtnH;
    private boolean reviewAppsBtnHovered = false;
    private final List<Rectangle> memberCardBounds = new ArrayList<Rectangle>();
    private final List<Rectangle> requestItemBounds = new ArrayList<Rectangle>();

    // =========================================================================
    // EXACT LANDING PAGE COLOR SYSTEM (THEME.JAVA TOKENS)
    // =========================================================================
    private static final Color BG_CANVAS = Theme.CANVAS_LIGHT; // #F8F7F4 Warm off-white
    private static final Color SIDEBAR_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xEB, 0xE9, 0xE4); // Soft warm border
    private static final Color BORDER_LIGHT = new Color(0xF2, 0xF0, 0xEB);

    // Primary Brand Accent across all pages
    private static final Color BRAND_ACCENT = Theme.BRAND_PINK; // #EA4C89 Hot Pink
    private static final Color BRAND_ACCENT_HOVER = new Color(0xD8, 0x3A, 0x77); // Darker pink on hover
    private static final Color BRAND_TINT_BG = Theme.PASTEL_PINK; // #FFF0F5 Soft pink tint
    private static final Color BRAND_TINT_BORDER = new Color(0xF8, 0xBC, 0xD4); // Soft pink border

    // Text Tokens from Landing Page (Theme.java)
    private static final Color TEXT_DARK = Theme.INK_DARK; // #0D0C22 Deep ink text (Headings & Bold labels)
    private static final Color TEXT_MUTED = new Color(0x56, 0x55, 0x66); // Secondary ink (#565566)
    private static final Color TEXT_LIGHT = new Color(0x9E, 0x9E, 0xAE); // Muted helper text (#9E9EAE)

    // Accent saturated tokens from Theme.java
    private static final Color SUCCESS_GREEN = Theme.SUCCESS_GREEN; // #10B981
    private static final Color SUCCESS_BG = Theme.PASTEL_MINT; // #EBFBF5
    private static final Color SUCCESS_BORDER = new Color(0x3D, 0xDB, 0xB0, 90);

    private static final Color ERROR_RED = Theme.ERROR_RED; // #EF4444
    private static final Color ERROR_RED_HOVER = new Color(0xDC, 0x26, 0x26);
    private static final Color RED_BG = new Color(0xFE, 0xF2, 0xF2);
    private static final Color RED_BORDER = new Color(0xFC, 0xA5, 0xA5);

    private static final Color AMBER_BG = Theme.PASTEL_SUNNY; // #FFFAEC
    private static final Color AMBER_BORDER = new Color(0xFF, 0xE8, 0xB3);
    private static final Color AMBER_TEXT = Theme.HIGHLIGHT_WARM; // #FF8A5B

    private static final Color CONTROL_OFF_BG = new Color(0xDB, 0xD8, 0xD2);
    private static final Color SCROLL_THUMB = new Color(0, 0, 0, 40);

    public StudentDashboard(DashboardListener listener) {
        this.listener = listener;

        for (int i = 0; i < teamTabBounds.length; i++) teamTabBounds[i] = new Rectangle(0, 0, 0, 0);
        for (int i = 0; i < settingsTabBounds.length; i++) settingsTabBounds[i] = new Rectangle(0, 0, 0, 0);
        for (int i = 0; i < profileTabBounds.length; i++) profileTabBounds[i] = new Rectangle(0, 0, 0, 0);

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
        if (header != null)
            header.closePopups();
        if (modals != null) {
            if (modal == ActiveModal.COMPLETE_PROFILE) {
                modals.initProfileForm(this);
            }
            modals.open(modal);
        }
    }

    public void closeModal() {
        if (modals != null)
            modals.closeModal();
    }

    public ActiveModal getActiveModal() {
        return (modals != null) ? modals.getActiveModal() : ActiveModal.NONE;
    }

    public void returnToLanding() {
        if (listener != null)
            listener.returnToLanding();
    }

    public void switchToAdminDashboard() {
        if (listener != null)
            listener.switchToAdminDashboard();
    }

    public void update(float dt) {
        // Apply results from background API calls on the render thread (same thread as paint)
        Runnable job;
        while ((job = uiQueue.poll()) != null) {
            try {
                job.run();
            } catch (RuntimeException e) {
                System.err.println("[Dashboard] Failed to apply server data: " + e);
            }
        }

        if (activeNav != lastNav) {
            lastNav = activeNav;
            targetScrollY = 0f;
            scrollY = 0f;
        }

        scrollY = Anim.approach(scrollY, targetScrollY, dt, 14f);
        if (toastTimer > 0f) {
            toastTimer -= dt;
            if (toastTimer <= 0f)
                toastMessage = "";
        }
        if (modals != null)
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
            case MY_PROFILE:
                paintProfileContent(cg, sideW, contentY, width - sideW, height);
                break;
            case SETTINGS:
                paintSettingsContent(cg, sideW, contentY, width - sideW, height);
                break;
            case OVERVIEW:
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
        if (modals != null && modals.getActiveModal() != ActiveModal.NONE) {
            modals.paint(g2, width, height, this);
        }

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
            case OVERVIEW:
                return overview.getTotalContentHeight();
            case MY_PROFILE:
            case SETTINGS:
            default:
                return totalContentHeight;
        }
    }

    private void paintContentScrollbar(Graphics2D g, int x, int y, int w, int h) {
        int totalH = getCurrentContentHeight();
        if (totalH <= h)
            return;
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
        g.setColor(new Color(0x0D, 0x0C, 0x22, 235));
        g.fill(tShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(tShape);

        g.setColor(Color.WHITE);
        g.drawString("✓  " + toastMessage, tx + 18, ty + 22);
    }

    // =========================================================================
    // 1. MY PROFILE VIEW RENDERING
    // Hero (cover, avatar, identity, about, links) · KPI cards · tabs ·
    // Overview / Projects / Skills & Endorsements / Peer Reviews.
    // Every block adapts to missing data (fresh accounts) and to narrow widths.
    // =========================================================================
    private final Rectangle completeProfileBtnBox = new Rectangle();
    private boolean completeProfileHovered = false;
    /** profileSkillBounds[i] belongs to profileData.skills[profileSkillIndex[i]]. */
    private final List<Integer> profileSkillIndex = new ArrayList<Integer>();
    /** profileProjectBounds[i] belongs to profileData.projects[profileProjectIndex[i]]. */
    private final List<Integer> profileProjectIndex = new ArrayList<Integer>();
    private int hoveredProfileSkill = -1;
    private int hoveredProfileProject = -1;

    private static final Font PF_NAME = new Font(Font.SANS_SERIF, Font.BOLD, 24);
    private static final Font PF_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    private static final Font PF_SECTION = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font PF_BOLD_13 = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final Font PF_BOLD_12 = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font PF_BOLD_11 = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    private static final Font PF_BOLD_10 = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    private static final Font PF_REG_14 = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font PF_REG_13 = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font PF_REG_12 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font PF_REG_11 = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final Font PF_METRIC = new Font(Font.SANS_SERIF, Font.BOLD, 26);

    private static final int ICON_PIN = 0, ICON_MAIL = 1, ICON_CODE = 2, ICON_LINK = 3, ICON_CAP = 4,
            ICON_FOLDER = 5, ICON_PEOPLE = 6, ICON_CHECK = 7, ICON_STAR = 8, ICON_EDIT = 9, ICON_SHARE = 10;

    /** Where "Edit profile" was last painted (screen coordinates) — used by UI tests. */
    public Rectangle getEditProfileButtonBounds() {
        return new Rectangle(editProfileBtnBox);
    }

    /** Where the Edit profile modal's "Save changes" button was last painted — used by UI tests. */
    public Rectangle getProfileSaveButtonBounds() {
        return modals.getProfileSaveBounds();
    }

    /** Where "Share" was last painted (screen coordinates) — used by UI tests. */
    public Rectangle getShareProfileButtonBounds() {
        return new Rectangle(shareProfileBtnBox);
    }

    private void paintProfileContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = width < 760 ? 20 : 36;
        int contentW = width - padX * 2;
        int x = startX + padX;
        int curY = startY + 24;

        profileSkillBounds.clear();
        profileSkillIndex.clear();
        profileProjectBounds.clear();
        profileProjectIndex.clear();
        completeProfileBtnBox.setBounds(0, 0, 0, 0);

        g.setFont(PF_REG_12);
        g.setColor(TEXT_LIGHT);
        g.drawString("Workspace  /  ", x, curY + 14);
        int bw = g.getFontMetrics().stringWidth("Workspace  /  ");
        g.setColor(TEXT_MUTED);
        g.drawString("My Profile", x + bw, curY + 14);
        curY += 30;

        curY = paintProfileHero(g, x, curY, contentW) + 20;
        curY = paintProfileStats(g, x, curY, contentW) + 26;
        curY = paintProfileTabs(g, x, curY, contentW) + 20;

        int endY;
        switch (profileData.activeTabIndex) {
            case 1:
                endY = paintProfileProjectGrid(g, x, curY, contentW, Integer.MAX_VALUE, contentW >= 860 ? 2 : 1);
                break;
            case 2:
                endY = paintProfileSkillsFull(g, x, curY, contentW);
                break;
            case 3:
                endY = paintProfileReviewsFull(g, x, curY, contentW);
                break;
            default:
                if (contentW >= 900) {
                    int gap = 20;
                    int leftW = Math.round((contentW - gap) * 0.63f);
                    int rightW = contentW - leftW - gap;
                    int leftEnd = paintProfileOverviewMain(g, x, curY, leftW);
                    int rightEnd = paintProfileOverviewSidebar(g, x + leftW + gap, curY, rightW);
                    endY = Math.max(leftEnd, rightEnd);
                } else {
                    endY = paintProfileOverviewMain(g, x, curY, contentW);
                    endY = paintProfileOverviewSidebar(g, x, endY + 8, contentW);
                }
                break;
        }
        totalContentHeight = endY - startY + 60;
    }

    // ---------------------------------------------------------------- hero

    private int paintProfileHero(Graphics2D g, int x, int y, int w) {
        int bannerH = 108;
        int avD = 104;
        int innerX = x + 28;
        int innerW = w - 56;

        // --- measure everything first so the card height fits its content
        String handle = has(profileData.handle) ? profileData.handle.trim() : "";
        List<String> eduParts = new ArrayList<String>();
        if (has(profileData.university)) eduParts.add(profileData.university.trim());
        if (has(profileData.department)) eduParts.add(profileData.department.trim());
        if (has(profileData.gradYear)) eduParts.add(profileData.gradYear.trim());
        String edu = join(eduParts, "  ·  ");

        FontMetrics bioFm = g.getFontMetrics(PF_REG_13);
        boolean hasBio = has(profileData.bio);
        List<String> bioLines = hasBio ? wrapLines(profileData.bio.trim(), bioFm, innerW, 4)
                : wrapLines("Add a short bio so teammates know what you build, what you're learning and what kind of team you're looking for.", bioFm, innerW, 2);

        List<String[]> links = new ArrayList<String[]>(); // {iconType, text}
        if (has(profileData.location)) links.add(new String[] { String.valueOf(ICON_PIN), profileData.location.trim() });
        if (has(profileData.email)) links.add(new String[] { String.valueOf(ICON_MAIL), profileData.email.trim() });
        if (has(profileData.github)) links.add(new String[] { String.valueOf(ICON_CODE), profileData.github.trim() });
        if (has(profileData.portfolio)) links.add(new String[] { String.valueOf(ICON_LINK), profileData.portfolio.trim() });

        int identityY = y + bannerH + avD / 2 + 18; // baseline area below the avatar
        int cy = identityY + 4;
        cy += 28; // name
        if (has(profileData.headline)) cy += 24;
        if (!edu.isEmpty()) cy += 22;
        if (has(profileData.availabilityStatus)) cy += 34;
        cy += 22; // divider gap
        cy += 22; // "About" label
        cy += bioLines.size() * 20;
        int linksH = links.isEmpty() ? 0 : measureChipRows(g, links, innerW) + 16;
        cy += linksH + 22;
        int heroH = cy - y;

        // --- card + cover
        drawProfileCard(g, x, y, w, heroH, 16);
        Shape oldClip = g.getClip();
        g.clip(new RoundRectangle2D.Float(x, y, w, heroH, 16, 16));
        g.clip(new Rectangle(x, y, w, bannerH));
        g.setColor(Theme.PASTEL_VIOLET);
        g.fillRect(x, y, w, bannerH);
        // quiet decorative rings on the cover
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(new Color(0x7C, 0x5C, 0xFF, 28));
        for (int i = 0; i < 4; i++) {
            int r = 70 + i * 46;
            g.drawOval(x + w - 150 - r, y + bannerH / 2 - r, r * 2, r * 2);
        }
        g.setColor(new Color(0xEA, 0x4C, 0x89, 26));
        g.fillOval(x + w - 260, y - 40, 120, 120);
        g.setClip(oldClip);
        g.setColor(BORDER_COLOR);
        g.drawLine(x + 1, y + bannerH, x + w - 1, y + bannerH);

        // --- avatar overlapping the cover
        int avX = innerX;
        int avY = y + bannerH - avD / 2;
        g.setColor(Color.WHITE);
        g.fillOval(avX - 5, avY - 5, avD + 10, avD + 10);
        g.setColor(Theme.PASTEL_PINK);
        g.fillOval(avX, avY, avD, avD);
        g.setColor(BRAND_TINT_BORDER);
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(avX, avY, avD, avD);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        g.setColor(BRAND_ACCENT);
        FontMetrics afm = g.getFontMetrics();
        String ini = profileNameInitials();
        g.drawString(ini, avX + (avD - afm.stringWidth(ini)) / 2, avY + (avD + afm.getAscent() - afm.getDescent()) / 2);
        // online dot when available for teams
        if (has(profileData.availabilityStatus)) {
            int dotD = 20;
            int dx = avX + avD - dotD - 2;
            int dy = avY + avD - dotD - 4;
            g.setColor(Color.WHITE);
            g.fillOval(dx - 3, dy - 3, dotD + 6, dotD + 6);
            g.setColor(SUCCESS_GREEN);
            g.fillOval(dx, dy, dotD, dotD);
        }

        // --- action buttons (right, just below the cover)
        int btnH = 36;
        int btnY = y + bannerH + 16;
        g.setFont(PF_BOLD_12);
        FontMetrics bfm = g.getFontMetrics();
        int shareW = bfm.stringWidth("Share") + 50;
        int editW = bfm.stringWidth("Edit profile") + 50;
        int shareX = x + w - 24 - shareW;
        int editX = shareX - 10 - editW;
        shareProfileBtnBox.setBounds(shareX, btnY, shareW, btnH);
        editProfileBtnBox.setBounds(editX, btnY, editW, btnH);

        RoundRectangle2D share = new RoundRectangle2D.Float(shareX, btnY, shareW, btnH, 10, 10);
        g.setColor(shareProfileHovered ? BG_CANVAS : Color.WHITE);
        g.fill(share);
        g.setColor(shareProfileHovered ? TEXT_LIGHT : BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(share);
        drawProfileIcon(g, ICON_SHARE, shareX + 16, btnY + 11, TEXT_DARK);
        g.setColor(TEXT_DARK);
        g.drawString("Share", shareX + 36, btnY + 23);

        RoundRectangle2D edit = new RoundRectangle2D.Float(editX, btnY, editW, btnH, 10, 10);
        g.setColor(editProfileHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(edit);
        drawProfileIcon(g, ICON_EDIT, editX + 16, btnY + 11, Color.WHITE);
        g.setColor(Color.WHITE);
        g.drawString("Edit profile", editX + 36, btnY + 23);

        // --- identity
        int ty = identityY + 26;
        g.setFont(PF_NAME);
        g.setColor(TEXT_DARK);
        String name = has(profileData.name) ? profileData.name.trim() : "Your name";
        name = ellipsize(name, g.getFontMetrics(), innerW - 140);
        g.drawString(name, innerX, ty);
        int nameW = g.getFontMetrics().stringWidth(name);
        if (!handle.isEmpty()) {
            g.setFont(PF_REG_13);
            g.setColor(TEXT_LIGHT);
            String hd = ellipsize(handle, g.getFontMetrics(), Math.max(40, innerW - nameW - 14));
            g.drawString(hd, innerX + nameW + 12, ty);
        }

        if (has(profileData.headline)) {
            ty += 24;
            g.setFont(PF_REG_14);
            g.setColor(TEXT_DARK);
            g.drawString(ellipsize(profileData.headline.trim(), g.getFontMetrics(), innerW), innerX, ty);
        }
        if (!edu.isEmpty()) {
            ty += 22;
            drawProfileIcon(g, ICON_CAP, innerX, ty - 10, TEXT_MUTED);
            g.setFont(PF_REG_12);
            g.setColor(TEXT_MUTED);
            g.drawString(ellipsize(edu, g.getFontMetrics(), innerW - 20), innerX + 20, ty);
        }
        if (has(profileData.availabilityStatus)) {
            ty += 12;
            g.setFont(PF_BOLD_11);
            FontMetrics pfm = g.getFontMetrics();
            String avail = ellipsize(profileData.availabilityStatus.trim(), pfm, innerW - 40);
            int pw = pfm.stringWidth(avail) + 34;
            RoundRectangle2D pill = new RoundRectangle2D.Float(innerX, ty, pw, 24, 24, 24);
            g.setColor(SUCCESS_BG);
            g.fill(pill);
            g.setColor(SUCCESS_BORDER);
            g.setStroke(new BasicStroke(1f));
            g.draw(pill);
            g.setColor(SUCCESS_GREEN);
            g.fillOval(innerX + 12, ty + 8, 8, 8);
            g.setColor(new Color(0x04, 0x78, 0x57));
            g.drawString(avail, innerX + 26, ty + 16);
            ty += 22;
        }

        // --- divider + about
        ty += 18;
        g.setColor(BORDER_LIGHT);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(innerX, ty, innerX + innerW, ty);
        ty += 24;
        g.setFont(PF_BOLD_12);
        g.setColor(TEXT_LIGHT);
        g.drawString("ABOUT", innerX, ty);
        ty += 4;
        g.setFont(PF_REG_13);
        g.setColor(hasBio ? TEXT_DARK : TEXT_LIGHT);
        for (String line : bioLines) {
            ty += 20;
            g.drawString(line, innerX, ty);
        }

        // --- contact / link chips
        if (!links.isEmpty()) {
            ty += 18;
            paintLinkChips(g, links, innerX, ty, innerW);
        }
        return y + heroH;
    }

    /** Height of the wrapped chip rows for the hero links. */
    private int measureChipRows(Graphics2D g, List<String[]> links, int maxW) {
        FontMetrics fm = g.getFontMetrics(PF_REG_12);
        int cx = 0;
        int rows = 1;
        for (String[] l : links) {
            int cw = Math.min(maxW, fm.stringWidth(l[1]) + 40);
            if (cx > 0 && cx + cw > maxW) {
                rows++;
                cx = 0;
            }
            cx += cw + 8;
        }
        return rows * 30 - 8 + 4;
    }

    private void paintLinkChips(Graphics2D g, List<String[]> links, int x, int y, int maxW) {
        g.setFont(PF_REG_12);
        FontMetrics fm = g.getFontMetrics();
        int cx = 0;
        int cy = y;
        for (String[] l : links) {
            String text = ellipsize(l[1], fm, maxW - 40);
            int cw = fm.stringWidth(text) + 40;
            if (cx > 0 && cx + cw > maxW) {
                cx = 0;
                cy += 30;
            }
            RoundRectangle2D chip = new RoundRectangle2D.Float(x + cx, cy, cw, 26, 8, 8);
            g.setColor(BG_CANVAS);
            g.fill(chip);
            g.setColor(BORDER_COLOR);
            g.setStroke(new BasicStroke(1f));
            g.draw(chip);
            drawProfileIcon(g, Integer.parseInt(l[0]), x + cx + 12, cy + 7, TEXT_MUTED);
            g.setColor(TEXT_DARK);
            g.drawString(text, x + cx + 30, cy + 17);
            cx += cw + 8;
        }
    }

    // ---------------------------------------------------------------- KPI cards

    private int paintProfileStats(Graphics2D g, int x, int y, int w) {
        int cols = w >= 900 ? 4 : 2;
        int gap = 16;
        int cardW = (w - gap * (cols - 1)) / cols;
        int cardH = 112;

        boolean noReviews = profileData.totalReviewsCount <= 0;
        int ap = profileData.activeProjectsCount;
        int tm = profileData.totalTeammatesCount;
        String[] labels = { "Active projects", "Teammates", "On-time delivery", "Peer rating" };
        String[] values = { String.valueOf(ap), String.valueOf(tm), profileData.completionRatePercent + "%",
                noReviews ? "—" : String.format("%.1f", profileData.peerRating) };
        String[] subs = {
                ap == 1 ? "project in progress" : "projects in progress",
                tm == 1 ? "person you've worked with" : "people you've worked with",
                "of your tasks finished on time",
                noReviews ? "No peer reviews yet"
                        : "from " + profileData.totalReviewsCount + (profileData.totalReviewsCount == 1 ? " review" : " reviews") };
        int[] icons = { ICON_FOLDER, ICON_PEOPLE, ICON_CHECK, ICON_STAR };
        Color[] tints = { Theme.PASTEL_PINK, Theme.PASTEL_VIOLET, Theme.PASTEL_MINT, Theme.PASTEL_SUNNY };
        Color[] inks = { BRAND_ACCENT, Theme.ACCENT_VIOLET, SUCCESS_GREEN, Theme.HIGHLIGHT_WARM };

        int bottom = y;
        for (int i = 0; i < 4; i++) {
            int cx = x + (i % cols) * (cardW + gap);
            int cy = y + (i / cols) * (cardH + gap);
            drawProfileCard(g, cx, cy, cardW, cardH, 14);

            g.setColor(tints[i]);
            g.fill(new RoundRectangle2D.Float(cx + 18, cy + 18, 34, 34, 10, 10));
            drawProfileIcon(g, icons[i], cx + 29, cy + 29, inks[i]);

            g.setFont(PF_BOLD_12);
            g.setColor(TEXT_MUTED);
            g.drawString(labels[i], cx + 64, cy + 39);

            g.setFont(PF_METRIC);
            g.setColor(TEXT_DARK);
            g.drawString(values[i], cx + 18, cy + 86);
            int vw = g.getFontMetrics().stringWidth(values[i]);
            if (i == 3 && !noReviews) {
                drawStars(g, cx + 18 + vw + 10, cy + 72, 13, profileData.peerRating);
            }

            g.setFont(PF_REG_11);
            g.setColor(TEXT_LIGHT);
            g.drawString(ellipsize(subs[i], g.getFontMetrics(), cardW - 36), cx + 18, cy + 103);
            bottom = cy + cardH;
        }
        return bottom;
    }

    // ---------------------------------------------------------------- tabs

    private int paintProfileTabs(Graphics2D g, int x, int y, int w) {
        int[] counts = { -1, profileData.projects.size(), profileData.skills.size(), profileData.totalReviewsCount };
        int tx = x;
        for (int i = 0; i < profileData.tabs.size() && i < profileTabBounds.length; i++) {
            String label = profileData.tabs.get(i).replaceAll("\\s*\\(\\d+\\)\\s*$", "");
            boolean active = i == profileData.activeTabIndex;
            boolean hov = i == hoveredProfileTab;
            g.setFont(active ? PF_BOLD_13 : PF_REG_13);
            FontMetrics fm = g.getFontMetrics();
            int lw = fm.stringWidth(label);
            int tabW = lw;
            String cnt = counts[i] >= 0 ? String.valueOf(counts[i]) : null;
            int cntW = 0;
            if (cnt != null) {
                cntW = g.getFontMetrics(PF_BOLD_10).stringWidth(cnt) + 12;
                tabW += 8 + cntW;
            }
            profileTabBounds[i].setBounds(tx - 6, y - 4, tabW + 12, 36);

            g.setColor(active ? TEXT_DARK : (hov ? TEXT_DARK : TEXT_MUTED));
            g.drawString(label, tx, y + 17);
            if (cnt != null) {
                int px = tx + lw + 8;
                RoundRectangle2D pill = new RoundRectangle2D.Float(px, y + 4, cntW, 18, 18, 18);
                g.setColor(active ? BRAND_TINT_BG : new Color(0xF1, 0xEF, 0xEA));
                g.fill(pill);
                g.setFont(PF_BOLD_10);
                g.setColor(active ? BRAND_ACCENT : TEXT_MUTED);
                g.drawString(cnt, px + 6, y + 17);
            }
            if (active) {
                g.setColor(BRAND_ACCENT);
                g.fill(new RoundRectangle2D.Float(tx, y + 30, tabW, 3, 3, 3));
            }
            tx += tabW + 30;
        }
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, y + 33, x + w, y + 33);
        return y + 34;
    }

    // ---------------------------------------------------------------- overview tab

    private int paintProfileOverviewMain(Graphics2D g, int x, int y, int w) {
        int cy = paintSectionHeading(g, x, y, "Projects", profileData.projects.isEmpty() ? null
                : profileData.projects.size() + (profileData.projects.size() == 1 ? " project" : " projects") + " on your profile");
        if (profileData.projects.isEmpty()) {
            return paintEmptyState(g, x, cy, w, ICON_FOLDER, "No projects yet",
                    "Projects you create or join will show up here.");
        }
        int shown = Math.min(3, profileData.projects.size());
        cy = paintProfileProjectGrid(g, x, cy, w, shown, 1);
        if (profileData.projects.size() > shown) {
            g.setFont(PF_BOLD_12);
            g.setColor(BRAND_ACCENT);
            g.drawString("+ " + (profileData.projects.size() - shown) + " more in the Projects tab", x + 2, cy + 4);
            cy += 20;
        }
        return cy;
    }

    private int paintProfileOverviewSidebar(Graphics2D g, int x, int y, int w) {
        int cy = y;

        // 1. Profile strength
        List<String> missing = missingProfileItems();
        int strength = profileStrength();
        int shownMissing = Math.min(3, missing.size());
        int cardH = 104 + (strength < 100 ? shownMissing * 24 + 50 : 22);
        drawProfileCard(g, x, cy, w, cardH, 14);
        g.setFont(PF_SECTION);
        g.setColor(TEXT_DARK);
        g.drawString("Profile strength", x + 20, cy + 32);
        g.setFont(PF_BOLD_13);
        g.setColor(strength >= 80 ? SUCCESS_GREEN : BRAND_ACCENT);
        String pct = strength + "%";
        g.drawString(pct, x + w - 20 - g.getFontMetrics().stringWidth(pct), cy + 32);
        g.setFont(PF_REG_12);
        g.setColor(TEXT_MUTED);
        g.drawString(strength >= 100 ? "All set — teams can see everything." : strengthLabel(strength), x + 20, cy + 54);
        int barY = cy + 70;
        g.setColor(new Color(0xF1, 0xEF, 0xEA));
        g.fill(new RoundRectangle2D.Float(x + 20, barY, w - 40, 8, 8, 8));
        g.setColor(strength >= 80 ? SUCCESS_GREEN : BRAND_ACCENT);
        g.fill(new RoundRectangle2D.Float(x + 20, barY, Math.max(8, (w - 40) * strength / 100), 8, 8, 8));
        int ly = barY + 34;
        if (strength < 100) {
            g.setFont(PF_REG_12);
            for (int i = 0; i < shownMissing; i++) {
                g.setColor(BORDER_COLOR);
                g.setStroke(new BasicStroke(1.4f));
                g.drawOval(x + 20, ly - 10, 12, 12);
                g.setColor(TEXT_DARK);
                g.drawString(missing.get(i), x + 40, ly);
                ly += 24;
            }
            int bw = w - 40;
            completeProfileBtnBox.setBounds(x + 20, ly - 6, bw, 34);
            RoundRectangle2D btn = new RoundRectangle2D.Float(x + 20, ly - 6, bw, 34, 10, 10);
            g.setColor(completeProfileHovered ? BRAND_TINT_BG : Color.WHITE);
            g.fill(btn);
            g.setColor(BRAND_TINT_BORDER);
            g.draw(btn);
            g.setFont(PF_BOLD_12);
            g.setColor(BRAND_ACCENT);
            String bt = "Complete your profile";
            g.drawString(bt, x + 20 + (bw - g.getFontMetrics().stringWidth(bt)) / 2, ly + 15);
        } else {
            drawCheckCircle(g, x + 29, ly - 8);
            g.setFont(PF_BOLD_12);
            g.setColor(SUCCESS_GREEN);
            g.drawString("Profile complete", x + 44, ly - 4);
        }
        cy += cardH + 16;

        // 2. Top skills
        List<Integer> order = new ArrayList<Integer>();
        for (int i = 0; i < profileData.skills.size(); i++) order.add(i);
        java.util.Collections.sort(order, new java.util.Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                return profileData.skills.get(b).endorsements - profileData.skills.get(a).endorsements;
            }
        });
        int shownSkills = Math.min(5, order.size());
        int skillsH = 62 + (shownSkills == 0 ? 54 : shownSkills * 40) + 14;
        drawProfileCard(g, x, cy, w, skillsH, 14);
        g.setFont(PF_SECTION);
        g.setColor(TEXT_DARK);
        g.drawString("Top skills", x + 20, cy + 32);
        g.setFont(PF_REG_11);
        g.setColor(TEXT_LIGHT);
        g.drawString(shownSkills == 0 ? "" : "Click a skill to endorse it", x + 20, cy + 50);
        int ry = cy + 62;
        if (shownSkills == 0) {
            g.setFont(PF_REG_12);
            g.setColor(TEXT_MUTED);
            g.drawString("No skills yet — add them via Edit profile.", x + 20, ry + 26);
        }
        for (int k = 0; k < shownSkills; k++) {
            int idx = order.get(k);
            ProfileData.ProfileSkill s = profileData.skills.get(idx);
            Rectangle r = new Rectangle(x + 10, ry, w - 20, 36);
            boolean hov = profileSkillBounds.size() == hoveredProfileSkill;
            profileSkillBounds.add(r);
            profileSkillIndex.add(idx);
            if (hov) {
                g.setColor(BG_CANVAS);
                g.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 8, 8));
            }
            g.setFont(PF_BOLD_13);
            g.setColor(TEXT_DARK);
            String nm = ellipsize(s.name, g.getFontMetrics(), w - 150);
            g.drawString(nm, x + 20, ry + 17);
            if (has(s.category)) {
                g.setFont(PF_REG_11);
                g.setColor(TEXT_LIGHT);
                g.drawString(s.category, x + 20, ry + 31);
            }
            paintEndorseChip(g, x + w - 20, ry + 8, s);
            ry += 40;
        }
        cy += skillsH + 16;

        // 3. Latest feedback
        if (!profileData.reviews.isEmpty()) {
            ProfileData.PeerReview rev = profileData.reviews.get(0);
            FontMetrics qfm = g.getFontMetrics(PF_REG_13);
            List<String> q = wrapLines("“" + rev.feedback + "”", qfm, w - 40, 5);
            int fbH = 120 + q.size() * 20;
            drawProfileCard(g, x, cy, w, fbH, 14);
            g.setFont(PF_SECTION);
            g.setColor(TEXT_DARK);
            g.drawString("Latest feedback", x + 20, cy + 32);
            paintReviewerLine(g, rev, x + 20, cy + 50, w - 40);
            int qy = cy + 104;
            g.setFont(PF_REG_13);
            g.setColor(TEXT_DARK);
            for (String line : q) {
                g.drawString(line, x + 20, qy);
                qy += 20;
            }
            cy += fbH + 16;
        }
        return cy;
    }

    // ---------------------------------------------------------------- projects

    private int paintProfileProjectGrid(Graphics2D g, int x, int y, int w, int limit, int cols) {
        if (profileData.projects.isEmpty()) {
            return paintEmptyState(g, x, y, w, ICON_FOLDER, "No projects yet",
                    "Create a project or join one from Discover Projects — it will appear here.");
        }
        int gap = 16;
        int cardW = (w - gap * (cols - 1)) / cols;
        int n = Math.min(limit, profileData.projects.size());
        int cy = y;
        for (int i = 0; i < n; i += cols) {
            int rowH = 0;
            for (int c = 0; c < cols && i + c < n; c++) {
                rowH = Math.max(rowH, measureProjectCard(g, profileData.projects.get(i + c), cardW));
            }
            for (int c = 0; c < cols && i + c < n; c++) {
                paintProfileProjectCard(g, i + c, x + c * (cardW + gap), cy, cardW, rowH);
            }
            cy += rowH + gap;
        }
        return cy;
    }

    private int measureProjectCard(Graphics2D g, ProfileData.ProfileProject p, int w) {
        int descLines = has(p.description) ? wrapLines(p.description, g.getFontMetrics(PF_REG_12), w - 40, 2).size() : 0;
        return 84 + descLines * 18 + 50;
    }

    private void paintProfileProjectCard(Graphics2D g, int index, int x, int y, int w, int h) {
        ProfileData.ProfileProject p = profileData.projects.get(index);
        boolean hov = profileProjectBounds.size() == hoveredProfileProject;
        profileProjectBounds.add(new Rectangle(x, y, w, h));
        profileProjectIndex.add(index);

        drawProfileCard(g, x, y, w, h, 14);
        if (hov) {
            g.setColor(BRAND_TINT_BORDER);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new RoundRectangle2D.Float(x, y, w, h, 14, 14));
        }

        // monogram tile
        Color ink = p.accentColor != null ? p.accentColor : BRAND_ACCENT;
        g.setColor(new Color(ink.getRed(), ink.getGreen(), ink.getBlue(), 28));
        g.fill(new RoundRectangle2D.Float(x + 20, y + 20, 42, 42, 12, 12));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(ink);
        String mono = has(p.title) ? p.title.trim().substring(0, 1).toUpperCase() : "P";
        FontMetrics mfm = g.getFontMetrics();
        g.drawString(mono, x + 20 + (42 - mfm.stringWidth(mono)) / 2, y + 48);

        // status pill (right)
        String status = has(p.status) ? p.status.trim() : "Active";
        Color[] sc = statusColors(status);
        g.setFont(PF_BOLD_10);
        FontMetrics sfm = g.getFontMetrics();
        int sw = sfm.stringWidth(status) + 18;
        int sx = x + w - 20 - sw;
        g.setColor(sc[0]);
        g.fill(new RoundRectangle2D.Float(sx, y + 22, sw, 22, 22, 22));
        g.setColor(sc[1]);
        g.drawString(status, sx + 9, y + 37);

        // title + role
        g.setFont(PF_TITLE);
        g.setColor(TEXT_DARK);
        g.drawString(ellipsize(p.title, g.getFontMetrics(), sx - (x + 76) - 10), x + 76, y + 38);
        if (has(p.role)) {
            g.setFont(PF_BOLD_12);
            g.setColor(BRAND_ACCENT);
            g.drawString(ellipsize(p.role, g.getFontMetrics(), w - 96), x + 76, y + 57);
        }

        // description
        int dy = y + 86;
        if (has(p.description)) {
            g.setFont(PF_REG_12);
            g.setColor(TEXT_MUTED);
            for (String line : wrapLines(p.description, g.getFontMetrics(), w - 40, 2)) {
                g.drawString(line, x + 20, dy);
                dy += 18;
            }
        }

        // footer: tags (left) + team fill (right)
        int fy = y + h - 38;
        g.setColor(BORDER_LIGHT);
        g.drawLine(x + 20, fy - 8, x + w - 20, fy - 8);
        String team = p.teamSize + " / " + p.maxTeamSize;
        g.setFont(PF_BOLD_11);
        FontMetrics tfm = g.getFontMetrics();
        int teamTextW = tfm.stringWidth(team);
        int barW = 54;
        int teamBlockW = 18 + teamTextW + 8 + barW;
        int teamX = x + w - 20 - teamBlockW;
        drawProfileIcon(g, ICON_PEOPLE, teamX, fy + 6, TEXT_MUTED);
        g.setColor(TEXT_DARK);
        g.drawString(team, teamX + 18, fy + 16);
        int bx = teamX + 18 + teamTextW + 8;
        g.setColor(new Color(0xF1, 0xEF, 0xEA));
        g.fill(new RoundRectangle2D.Float(bx, fy + 9, barW, 6, 6, 6));
        float fill = p.maxTeamSize > 0 ? Math.min(1f, p.teamSize / (float) p.maxTeamSize) : 0f;
        g.setColor(ink);
        g.fill(new RoundRectangle2D.Float(bx, fy + 9, Math.max(6, Math.round(barW * fill)), 6, 6, 6));

        int tagX = x + 20;
        int tagLimit = teamX - 12;
        g.setFont(PF_BOLD_10);
        FontMetrics gfm = g.getFontMetrics();
        int hidden = 0;
        for (String tag : p.tags) {
            int tw = gfm.stringWidth(tag) + 16;
            if (tagX + tw > tagLimit) {
                hidden++;
                continue;
            }
            RoundRectangle2D chip = new RoundRectangle2D.Float(tagX, fy + 1, tw, 22, 8, 8);
            g.setColor(BG_CANVAS);
            g.fill(chip);
            g.setColor(BORDER_COLOR);
            g.draw(chip);
            g.setColor(TEXT_MUTED);
            g.drawString(tag, tagX + 8, fy + 16);
            tagX += tw + 6;
        }
        if (hidden > 0 && tagX + 26 <= tagLimit) {
            g.setColor(TEXT_LIGHT);
            g.drawString("+" + hidden, tagX + 2, fy + 16);
        }
    }

    // ---------------------------------------------------------------- skills tab

    private int paintProfileSkillsFull(Graphics2D g, int x, int y, int w) {
        if (profileData.skills.isEmpty()) {
            return paintEmptyState(g, x, y, w, ICON_STAR, "No skills added yet",
                    "Add your skills with Edit profile so teammates can find and endorse you.");
        }
        // group by category, keeping first-seen order
        List<String> cats = new ArrayList<String>();
        for (ProfileData.ProfileSkill s : profileData.skills) {
            String c = has(s.category) ? s.category.trim() : "Other";
            if (!cats.contains(c)) cats.add(c);
        }
        int maxEnd = 1;
        for (ProfileData.ProfileSkill s : profileData.skills) maxEnd = Math.max(maxEnd, s.endorsements);

        int rowH = 48;
        int cardH = 86 + cats.size() * 34 + profileData.skills.size() * rowH + 10;
        drawProfileCard(g, x, y, w, cardH, 14);
        g.setFont(PF_SECTION);
        g.setColor(TEXT_DARK);
        g.drawString("Skills & endorsements", x + 24, y + 34);
        g.setFont(PF_REG_12);
        g.setColor(TEXT_MUTED);
        g.drawString("Endorsements come from teammates you've worked with. Click a row to endorse.", x + 24, y + 54);

        int cy = y + 86;
        for (String cat : cats) {
            g.setFont(PF_BOLD_10);
            g.setColor(TEXT_LIGHT);
            g.drawString(cat.toUpperCase(), x + 24, cy + 12);
            g.setColor(BORDER_LIGHT);
            g.drawLine(x + 24 + g.getFontMetrics().stringWidth(cat.toUpperCase()) + 10, cy + 8, x + w - 24, cy + 8);
            cy += 34;
            for (int i = 0; i < profileData.skills.size(); i++) {
                ProfileData.ProfileSkill s = profileData.skills.get(i);
                String sc = has(s.category) ? s.category.trim() : "Other";
                if (!sc.equals(cat)) continue;
                Rectangle r = new Rectangle(x + 12, cy - 6, w - 24, rowH - 4);
                boolean hov = profileSkillBounds.size() == hoveredProfileSkill;
                profileSkillBounds.add(r);
                profileSkillIndex.add(i);
                if (hov) {
                    g.setColor(BG_CANVAS);
                    g.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 10, 10));
                }
                int nameColW = Math.min(220, (w - 48) / 3);
                g.setFont(PF_BOLD_13);
                g.setColor(TEXT_DARK);
                g.drawString(ellipsize(s.name, g.getFontMetrics(), nameColW - 10), x + 24, cy + 19);

                int chipRight = x + w - 24;
                int chipW = paintEndorseChip(g, chipRight, cy + 4, s, 96, false);
                int barX = x + 24 + nameColW;
                int barW = Math.max(40, chipRight - chipW - 24 - barX - 120);
                g.setColor(new Color(0xF1, 0xEF, 0xEA));
                g.fill(new RoundRectangle2D.Float(barX, cy + 11, barW, 8, 8, 8));
                g.setColor(s.userEndorsed ? BRAND_ACCENT : Theme.ACCENT_VIOLET);
                g.fill(new RoundRectangle2D.Float(barX, cy + 11, Math.max(8, Math.round(barW * s.endorsements / (float) maxEnd)), 8, 8, 8));
                g.setFont(PF_REG_12);
                g.setColor(TEXT_MUTED);
                g.drawString(s.endorsements + (s.endorsements == 1 ? " endorsement" : " endorsements"), barX + barW + 14, cy + 20);
                cy += rowH;
            }
        }
        return y + cardH + 20;
    }

    /** Right-aligned endorse chip ending at rightX (with endorsement count); returns its width. */
    private int paintEndorseChip(Graphics2D g, int rightX, int y, ProfileData.ProfileSkill s) {
        return paintEndorseChip(g, rightX, y, s, 0, true);
    }

    /** Right-aligned endorse chip; fixedW > 0 forces a uniform width so rows line up. */
    private int paintEndorseChip(Graphics2D g, int rightX, int y, ProfileData.ProfileSkill s, int fixedW, boolean withCount) {
        String text = (s.userEndorsed ? "✓ Endorsed" : "+ Endorse") + (withCount ? " · " + s.endorsements : "");
        g.setFont(PF_BOLD_11);
        FontMetrics fm = g.getFontMetrics();
        int cw = Math.max(fixedW, fm.stringWidth(text) + 20);
        RoundRectangle2D chip = new RoundRectangle2D.Float(rightX - cw, y, cw, 22, 22, 22);
        g.setColor(s.userEndorsed ? BRAND_TINT_BG : Color.WHITE);
        g.fill(chip);
        g.setColor(s.userEndorsed ? BRAND_TINT_BORDER : BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(chip);
        g.setColor(s.userEndorsed ? BRAND_ACCENT : TEXT_MUTED);
        g.drawString(text, rightX - cw + (cw - fm.stringWidth(text)) / 2, y + 15);
        return cw;
    }

    // ---------------------------------------------------------------- reviews tab

    private int paintProfileReviewsFull(Graphics2D g, int x, int y, int w) {
        if (profileData.reviews.isEmpty() && profileData.totalReviewsCount <= 0) {
            return paintEmptyState(g, x, y, w, ICON_STAR, "No peer reviews yet",
                    "When teammates review your work after a project, their feedback appears here.");
        }
        // summary card: average + distribution of the loaded reviews
        int sumH = 150;
        drawProfileCard(g, x, y, w, sumH, 14);
        float avg = profileData.peerRating;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
        g.setColor(TEXT_DARK);
        String avgStr = profileData.totalReviewsCount > 0 ? String.format("%.1f", avg) : "—";
        g.drawString(avgStr, x + 28, y + 70);
        drawStars(g, x + 30, y + 86, 15, avg);
        g.setFont(PF_REG_12);
        g.setColor(TEXT_MUTED);
        g.drawString(profileData.totalReviewsCount + (profileData.totalReviewsCount == 1 ? " peer review" : " peer reviews"), x + 28, y + 124);

        int[] dist = new int[6];
        for (ProfileData.PeerReview r : profileData.reviews) dist[Math.max(1, Math.min(5, r.ratingStars))]++;
        int n = Math.max(1, profileData.reviews.size());
        int dX = x + 210;
        int dW = Math.min(420, w - 210 - 80);
        g.setFont(PF_BOLD_10);
        g.setColor(TEXT_LIGHT);
        g.drawString("BREAKDOWN OF THE " + profileData.reviews.size() + " LATEST REVIEWS", dX, y + 30);
        for (int s = 5; s >= 1; s--) {
            int ry = y + 44 + (5 - s) * 19;
            g.setFont(PF_BOLD_11);
            g.setColor(TEXT_MUTED);
            g.drawString(s + "★", dX, ry + 10);
            g.setColor(new Color(0xF1, 0xEF, 0xEA));
            g.fill(new RoundRectangle2D.Float(dX + 30, ry + 2, dW, 8, 8, 8));
            if (dist[s] > 0) {
                g.setColor(Theme.ACCENT_SUNNY);
                g.fill(new RoundRectangle2D.Float(dX + 30, ry + 2, Math.max(8, dW * dist[s] / n), 8, 8, 8));
            }
            g.setFont(PF_REG_11);
            g.setColor(TEXT_LIGHT);
            g.drawString(String.valueOf(dist[s]), dX + 30 + dW + 10, ry + 10);
        }
        int cy = y + sumH + 16;

        FontMetrics qfm = g.getFontMetrics(PF_REG_13);
        for (ProfileData.PeerReview rev : profileData.reviews) {
            List<String> lines = wrapLines(rev.feedback, qfm, w - 48, 8);
            int cardH = 92 + lines.size() * 20;
            drawProfileCard(g, x, cy, w, cardH, 14);
            paintReviewerLine(g, rev, x + 24, cy + 22, w - 48);
            int ly = cy + 78;
            g.setFont(PF_REG_13);
            g.setColor(TEXT_DARK);
            for (String line : lines) {
                g.drawString(line, x + 24, ly);
                ly += 20;
            }
            cy += cardH + 14;
        }
        return cy;
    }

    /** Avatar, name, role · project · date, and the star rating on the right. */
    private void paintReviewerLine(Graphics2D g, ProfileData.PeerReview rev, int x, int y, int w) {
        Color bg = rev.avatarBg != null ? rev.avatarBg : Theme.PASTEL_VIOLET;
        Color fg = rev.avatarFg != null ? rev.avatarFg : Theme.ACCENT_VIOLET;
        drawAvatar(g, x + 18, y + 18, 18, has(rev.reviewerInitials) ? rev.reviewerInitials : "TU", bg, fg);
        int starsW = 5 * 15 - 3;
        g.setFont(PF_BOLD_13);
        g.setColor(TEXT_DARK);
        g.drawString(ellipsize(rev.reviewerName, g.getFontMetrics(), w - 46 - starsW - 12), x + 46, y + 15);
        List<String> meta = new ArrayList<String>();
        if (has(rev.reviewerRole)) meta.add(rev.reviewerRole);
        if (has(rev.projectName)) meta.add(rev.projectName);
        if (has(rev.date)) meta.add(rev.date);
        g.setFont(PF_REG_11);
        g.setColor(TEXT_MUTED);
        g.drawString(ellipsize(join(meta, "  ·  "), g.getFontMetrics(), w - 46 - starsW - 12), x + 46, y + 32);
        drawStars(g, x + w - starsW, y + 6, 12, rev.ratingStars);
    }

    // ---------------------------------------------------------------- shared pieces

    private int paintSectionHeading(Graphics2D g, int x, int y, String title, String sub) {
        g.setFont(PF_SECTION);
        g.setColor(TEXT_DARK);
        g.drawString(title, x + 2, y + 16);
        if (sub != null) {
            int tw = g.getFontMetrics().stringWidth(title);
            g.setFont(PF_REG_12);
            g.setColor(TEXT_LIGHT);
            g.drawString(sub, x + 2 + tw + 10, y + 16);
        }
        return y + 30;
    }

    private int paintEmptyState(Graphics2D g, int x, int y, int w, int icon, String title, String sub) {
        int h = 150;
        drawProfileCard(g, x, y, w, h, 14);
        g.setColor(BORDER_COLOR);
        float[] dash = { 5f, 4f };
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, dash, 0f));
        g.draw(new RoundRectangle2D.Float(x + 8, y + 8, w - 16, h - 16, 10, 10));
        g.setStroke(new BasicStroke(1f));
        int cx = x + w / 2;
        g.setColor(BG_CANVAS);
        g.fillOval(cx - 22, y + 24, 44, 44);
        drawProfileIcon(g, icon, cx - 6, y + 40, TEXT_LIGHT);
        g.setFont(PF_BOLD_13);
        g.setColor(TEXT_DARK);
        g.drawString(title, cx - g.getFontMetrics().stringWidth(title) / 2, y + 92);
        g.setFont(PF_REG_12);
        g.setColor(TEXT_MUTED);
        String s = ellipsize(sub, g.getFontMetrics(), w - 40);
        g.drawString(s, cx - g.getFontMetrics().stringWidth(s) / 2, y + 114);
        return y + h + 16;
    }

    private void drawProfileCard(Graphics2D g, int x, int y, int w, int h, int arc) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.1f));
        g.draw(card);
    }

    /** Five stars with partial fill for fractional ratings. */
    private void drawStars(Graphics2D g, int x, int y, int size, float rating) {
        Shape oldClip = g.getClip();
        for (int i = 0; i < 5; i++) {
            Shape star = starShape(x + i * (size + 3), y, size);
            g.setColor(new Color(0xE8, 0xE4, 0xDC));
            g.fill(star);
            float part = Math.max(0f, Math.min(1f, rating - i));
            if (part > 0f) {
                g.setClip(oldClip);
                g.clip(new Rectangle(x + i * (size + 3), y, Math.round(size * part), size + 1));
                g.setColor(Theme.ACCENT_SUNNY.darker());
                g.fill(star);
                g.setClip(oldClip);
            }
        }
    }

    private Shape starShape(int x, int y, int size) {
        GeneralPath p = new GeneralPath();
        double cx = x + size / 2.0, cy = y + size / 2.0 + size * 0.04;
        double ro = size / 2.0, ri = ro * 0.45;
        for (int k = 0; k < 10; k++) {
            double ang = -Math.PI / 2 + k * Math.PI / 5;
            double r = (k % 2 == 0) ? ro : ri;
            double px = cx + r * Math.cos(ang), py = cy + r * Math.sin(ang);
            if (k == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    /** 12×12 line icons, top-left at (x, y). */
    private void drawProfileIcon(Graphics2D g, int type, int x, int y, Color c) {
        Stroke old = g.getStroke();
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (type) {
            case ICON_PIN:
                g.drawOval(x + 2, y, 8, 8);
                g.drawLine(x + 3, y + 7, x + 6, y + 12);
                g.drawLine(x + 9, y + 7, x + 6, y + 12);
                g.fillOval(x + 5, y + 3, 2, 2);
                break;
            case ICON_MAIL:
                g.drawRect(x, y + 2, 12, 9);
                g.drawLine(x, y + 2, x + 6, y + 7);
                g.drawLine(x + 12, y + 2, x + 6, y + 7);
                break;
            case ICON_CODE:
                g.drawLine(x + 4, y + 2, x, y + 6);
                g.drawLine(x, y + 6, x + 4, y + 10);
                g.drawLine(x + 8, y + 2, x + 12, y + 6);
                g.drawLine(x + 12, y + 6, x + 8, y + 10);
                break;
            case ICON_LINK:
                g.drawOval(x, y, 12, 12);
                g.drawLine(x, y + 6, x + 12, y + 6);
                g.drawArc(x + 3, y, 6, 12, 90, 180);
                g.drawArc(x + 3, y, 6, 12, 270, 180);
                break;
            case ICON_CAP:
                g.drawPolygon(new int[] { x, x + 6, x + 12, x + 6 }, new int[] { y + 4, y + 1, y + 4, y + 7 }, 4);
                g.drawLine(x + 3, y + 6, x + 3, y + 9);
                g.drawArc(x + 3, y + 6, 6, 5, 180, 180);
                g.drawLine(x + 9, y + 6, x + 9, y + 9);
                break;
            case ICON_FOLDER:
                g.drawRoundRect(x, y + 2, 12, 9, 3, 3);
                g.drawLine(x + 1, y + 2, x + 4, y);
                g.drawLine(x + 4, y, x + 6, y + 2);
                break;
            case ICON_PEOPLE:
                g.drawOval(x + 1, y, 5, 5);
                g.drawArc(x - 1, y + 6, 9, 8, 0, 180);
                g.drawOval(x + 7, y + 1, 4, 4);
                g.drawArc(x + 7, y + 7, 6, 6, 0, 180);
                break;
            case ICON_CHECK:
                g.drawOval(x, y, 12, 12);
                g.drawLine(x + 3, y + 6, x + 5, y + 8);
                g.drawLine(x + 5, y + 8, x + 9, y + 4);
                break;
            case ICON_STAR:
                g.fill(starShape(x - 1, y - 1, 14));
                break;
            case ICON_EDIT:
                g.drawLine(x + 2, y + 10, x + 10, y + 2);
                g.drawLine(x + 8, y, x + 12, y + 4);
                g.drawLine(x, y + 12, x + 3, y + 11);
                break;
            case ICON_SHARE:
                g.drawLine(x + 6, y, x + 6, y + 8);
                g.drawLine(x + 3, y + 3, x + 6, y);
                g.drawLine(x + 9, y + 3, x + 6, y);
                g.drawLine(x, y + 6, x, y + 12);
                g.drawLine(x, y + 12, x + 12, y + 12);
                g.drawLine(x + 12, y + 12, x + 12, y + 6);
                break;
            default:
                break;
        }
        g.setStroke(old);
    }

    /** {background, text} colours for a project status label such as "Active · Sprint 3". */
    private Color[] statusColors(String status) {
        String s = status.toLowerCase();
        if (s.startsWith("completed") || s.startsWith("done"))
            return new Color[] { new Color(0xF1, 0xEF, 0xEA), TEXT_MUTED };
        if (s.startsWith("recruit"))
            return new Color[] { AMBER_BG, new Color(0xB4, 0x53, 0x09) };
        if (s.startsWith("under review") || s.startsWith("review"))
            return new Color[] { Theme.PASTEL_VIOLET, Theme.ACCENT_VIOLET };
        return new Color[] { SUCCESS_BG, new Color(0x04, 0x78, 0x57) };
    }

    // ---------------------------------------------------------------- profile text helpers

    private static boolean has(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String join(List<String> parts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (sb.length() > 0) sb.append(sep);
            sb.append(p);
        }
        return sb.toString();
    }

    private static String ellipsize(String s, FontMetrics fm, int maxW) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > maxW) t = t.substring(0, t.length() - 1);
        return t.trim() + "…";
    }

    /** Greedy word wrap; the last allowed line is ellipsized when text remains. */
    private static List<String> wrapLines(String text, FontMetrics fm, int maxW, int maxLines) {
        List<String> out = new ArrayList<String>();
        if (text == null) return out;
        String[] words = text.trim().split("\\s+");
        StringBuilder line = new StringBuilder();
        int i = 0;
        while (i < words.length) {
            String candidate = line.length() == 0 ? words[i] : line + " " + words[i];
            if (fm.stringWidth(candidate) <= maxW || line.length() == 0) {
                line.setLength(0);
                line.append(candidate);
                i++;
            } else {
                out.add(line.toString());
                line.setLength(0);
                if (out.size() == maxLines) break;
            }
        }
        if (out.size() < maxLines && line.length() > 0) {
            out.add(line.toString());
        } else if (!out.isEmpty() && (i < words.length || line.length() > 0)) {
            int last = out.size() - 1;
            StringBuilder rest = new StringBuilder(out.get(last));
            if (line.length() > 0) rest.append(' ').append(line);
            for (int k = i; k < words.length; k++) rest.append(' ').append(words[k]);
            out.set(last, ellipsize(rest.toString(), fm, maxW));
        }
        for (int k = 0; k < out.size(); k++) out.set(k, ellipsize(out.get(k), fm, maxW));
        return out;
    }

    private String profileNameInitials() {
        String n = has(profileData.name) ? profileData.name.trim() : "";
        if (n.isEmpty()) return has(profileInitials) ? profileInitials : "?";
        return initialsFor(n);
    }

    /** Human-readable checklist of profile fields that are still empty. */
    private List<String> missingProfileItems() {
        List<String> m = new ArrayList<String>();
        if (!has(profileData.headline)) m.add("Add a headline");
        if (!has(profileData.bio)) m.add("Write a short bio");
        if (profileData.skills.size() < 3) m.add("List at least 3 skills");
        if (!has(profileData.university)) m.add("Add your university");
        if (!has(profileData.availabilityStatus)) m.add("Set your availability");
        if (!has(profileData.github) && !has(profileData.portfolio)) m.add("Link GitHub or a portfolio");
        if (!has(profileData.location)) m.add("Add your location");
        if (!has(profileData.department) || !has(profileData.gradYear)) m.add("Add department & graduation year");
        return m;
    }

    private int profileStrength() {
        int total = 8;
        return Math.round((total - missingProfileItems().size()) * 100f / total);
    }

    private static String strengthLabel(int pct) {
        if (pct >= 80) return "Strong — just a few details left.";
        if (pct >= 50) return "Good start — fill in the rest to stand out.";
        return "Teams are more likely to invite complete profiles.";
    }

    // =========================================================================
    // 2. SETTINGS VIEW RENDERING (ALIGNED WITH LANDING PAGE PALETTE)
    // =========================================================================
    private void paintSettingsContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = 36;
        int padY = 24;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_LIGHT);
        g.drawString("Workspace / Settings", startX + padX, curY + 14);
        curY += 26;

        int titleY = curY + 24;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.setColor(TEXT_DARK);
        g.drawString("Settings", startX + padX, titleY);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString("Manage your account and collaboration preferences.", startX + padX, titleY + 22);

        curY += 68;

        int subNavW = 200;
        int gap = 24;
        int subNavX = startX + padX;
        int rightX = subNavX + subNavW + gap;
        int rightW = contentW - subNavW - gap;

        paintSettingsSubNav(g, subNavX, curY, subNavW);

        int rightBottomY = curY;
        switch (settingsData.activeTab) {
            case ACCOUNT:
                rightBottomY = paintAccountTab(g, rightX, curY, rightW);
                break;
            case PROFILE_PRIVACY:
                rightBottomY = paintProfilePrivacyTab(g, rightX, curY, rightW);
                break;
            case COLLAB_PREFERENCES:
                rightBottomY = paintCollabPreferencesTab(g, rightX, curY, rightW);
                break;
            case DANGER_ZONE:
                rightBottomY = paintDangerZoneTab(g, rightX, curY, rightW);
                break;
        }

        totalContentHeight = Math.max(curY + 220, rightBottomY) - startY + 50;
    }

    private void paintSettingsSubNav(Graphics2D g, int x, int y, int w) {
        int itemH = 40;
        SettingsData.SettingsTab[] tabs = SettingsData.SettingsTab.values();
        int cardH = tabs.length * (itemH + 4) + 16;

        RoundRectangle2D navCard = new RoundRectangle2D.Float(x, y, w, cardH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(navCard);
        g.setColor(BORDER_COLOR);
        g.draw(navCard);

        int curY = y + 8;
        for (int i = 0; i < tabs.length; i++) {
            SettingsData.SettingsTab tab = tabs[i];
            boolean isSel = (tab == settingsData.activeTab);
            boolean isHov = (i == hoveredSettingsTab);

            int itemX = x + 8;
            int itemW = w - 16;
            settingsTabBounds[i].setBounds(itemX, curY, itemW, itemH);

            RoundRectangle2D itemShape = new RoundRectangle2D.Float(itemX, curY, itemW, itemH, 8, 8);
            if (isSel) {
                g.setColor(BRAND_TINT_BG);
                g.fill(itemShape);
            } else if (isHov) {
                g.setColor(BG_CANVAS);
                g.fill(itemShape);
            }

            Color iconCol = isSel ? BRAND_ACCENT : TEXT_MUTED;
            Color textCol = isSel ? BRAND_ACCENT : TEXT_DARK;

            drawSettingsTabIcon(g, i, itemX + 12, curY + 12, iconCol);

            g.setFont(new Font(Font.SANS_SERIF, isSel ? Font.BOLD : Font.PLAIN, 12));
            g.setColor(textCol);
            g.drawString(tab.label, itemX + 36, curY + 24);

            curY += itemH + 4;
        }
    }

    private void drawSettingsTabIcon(Graphics2D g, int tabIndex, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (tabIndex == 0) {
            g.drawOval(x + 3, y, 6, 6);
            g.drawArc(x, y + 7, 12, 7, 0, 180);
        } else if (tabIndex == 1) {
            g.drawOval(x + 1, y + 2, 12, 8);
            g.drawOval(x + 5, y + 4, 4, 4);
        } else if (tabIndex == 2) {
            g.drawOval(x + 2, y + 1, 4, 4);
            g.drawArc(x, y + 6, 8, 5, 0, 180);
            g.drawOval(x + 8, y + 4, 4, 4);
            g.drawArc(x + 6, y + 9, 8, 5, 0, 180);
        } else {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(x + 6, y);
            p.lineTo(x + 12, y + 11);
            p.lineTo(x, y + 11);
            p.closePath();
            g.draw(p);
            g.drawLine(x + 6, y + 4, x + 6, y + 7);
            g.fillOval(x + 5, y + 9, 2, 2);
        }
    }

    private int paintAccountTab(Graphics2D g, int x, int y, int w) {
        int curY = y;
        int card1H = 340;
        RoundRectangle2D card1 = new RoundRectangle2D.Float(x, curY, w, card1H, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card1);
        g.setColor(BORDER_COLOR);
        g.draw(card1);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT_DARK);
        g.drawString("Account details", x + 24, curY + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Manage the details you use to sign in to TeamUp.", x + 24, curY + 48);
        g.drawString("Personal information", x + 24, curY + 76);

        if (settingsData.hasUnsavedChanges) {
            int ubW = 104;
            int ubH = 20;
            int ubX = x + w - ubW - 24;
            int ubY = curY + 62;
            RoundRectangle2D ubShape = new RoundRectangle2D.Float(ubX, ubY, ubW, ubH, 6, 6);
            g.setColor(AMBER_BG);
            g.fill(ubShape);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.setColor(AMBER_TEXT);
            g.drawString("Unsaved changes", ubX + 10, ubY + 14);
        }

        int fieldY = curY + 92;
        int inputH = 36;
        int gap = 16;
        int halfW = (w - 48 - gap) / 2;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Full name", x + 24, fieldY + 12);

        int fnX = x + 24;
        int fnY = fieldY + 20;
        fullNameBox.setBounds(fnX, fnY, halfW, inputH);
        paintInputField(g, fullNameBox, settingsData.fullName,
                settingsData.focusedField == SettingsData.ActiveField.FULL_NAME);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(TEXT_LIGHT);
        g.drawString("Updated from " + settingsData.originalFullName, fnX, fnY + inputH + 14);

        int unX = fnX + halfW + gap;
        int unY = fnY;
        usernameBox.setBounds(unX, unY, halfW, inputH);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Username", unX, fieldY + 12);
        paintInputField(g, usernameBox, settingsData.username,
                settingsData.focusedField == SettingsData.ActiveField.USERNAME);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(TEXT_LIGHT);
        g.drawString("teamup.com/u/" + settingsData.username, unX, unY + inputH + 14);

        int emailY = fnY + inputH + 28;
        int fullInputW = w - 48;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Email address", x + 24, emailY + 12);

        int emY = emailY + 20;
        emailBox.setBounds(x + 24, emY, fullInputW, inputH);
        paintInputField(g, emailBox, settingsData.email, settingsData.focusedField == SettingsData.ActiveField.EMAIL);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(TEXT_LIGHT);
        g.drawString("Your email is verified. We'll notify this address about important account changes.", x + 24,
                emY + inputH + 14);

        int alertY = emY + inputH + 26;
        int alertH = 46;
        RoundRectangle2D alertBox = new RoundRectangle2D.Float(x + 24, alertY, fullInputW, alertH, 8, 8);
        g.setColor(AMBER_BG);
        g.fill(alertBox);
        g.setColor(AMBER_BORDER);
        g.draw(alertBox);

        g.setColor(AMBER_TEXT);
        g.drawOval(x + 36, alertY + 14, 14, 14);
        g.drawLine(x + 43, alertY + 18, x + 43, alertY + 22);
        g.drawLine(x + 43, alertY + 22, x + 46, alertY + 22);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.drawString("You have unsaved changes", x + 58, alertY + 20);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("Your updated name will appear on your profile and in team conversations.", x + 58, alertY + 34);

        int btnY = alertY + alertH + 16;
        int saveW = 104;
        int cancelW = 72;
        int btnH = 34;

        // [Save changes] Button in Brand Hot Pink
        saveAccountBtnBox.setBounds(x + 24, btnY, saveW, btnH);
        RoundRectangle2D saveBtn = new RoundRectangle2D.Float(saveAccountBtnBox.x, saveAccountBtnBox.y,
                saveAccountBtnBox.width, saveAccountBtnBox.height, 8, 8);
        g.setColor(saveAccountHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(saveBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString("Save changes", saveAccountBtnBox.x + 16, saveAccountBtnBox.y + 21);

        cancelAccountBtnBox.setBounds(saveAccountBtnBox.x + saveW + 10, btnY, cancelW, btnH);
        RoundRectangle2D canBtn = new RoundRectangle2D.Float(cancelAccountBtnBox.x, cancelAccountBtnBox.y,
                cancelAccountBtnBox.width, cancelAccountBtnBox.height, 8, 8);
        g.setColor(cancelAccountHovered ? BG_CANVAS : Color.WHITE);
        g.fill(canBtn);
        g.setColor(BORDER_COLOR);
        g.draw(canBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Cancel", cancelAccountBtnBox.x + 16, cancelAccountBtnBox.y + 21);

        curY += card1H + 20;

        int card2H = 120;
        RoundRectangle2D card2 = new RoundRectangle2D.Float(x, curY, w, card2H, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card2);
        g.setColor(BORDER_COLOR);
        g.draw(card2);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT_DARK);
        g.drawString("Password", x + 24, curY + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Use a strong, unique password to protect your account.", x + 24, curY + 48);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Password last changed", x + 24, curY + 76);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(settingsData.passwordLastChanged, x + 24, curY + 92);

        int cpW = 128;
        int cpH = 34;
        changePassBtnBox.setBounds(x + w - cpW - 24, curY + 60, cpW, cpH);
        RoundRectangle2D cpBtn = new RoundRectangle2D.Float(changePassBtnBox.x, changePassBtnBox.y,
                changePassBtnBox.width, changePassBtnBox.height, 8, 8);
        g.setColor(changePassHovered ? BG_CANVAS : Color.WHITE);
        g.fill(cpBtn);
        g.setColor(BORDER_COLOR);
        g.draw(cpBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Change password", changePassBtnBox.x + 14, changePassBtnBox.y + 21);

        curY += card2H + 20;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_LIGHT);
        g.drawString("Member since " + settingsData.memberSince + " · Account ID " + settingsData.accountId, x,
                curY + 14);

        return curY + 40;
    }

    private int paintProfilePrivacyTab(Graphics2D g, int x, int y, int w) {
        int curY = y;
        int cardH = 510;

        RoundRectangle2D card = new RoundRectangle2D.Float(x, curY, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.draw(card);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT_DARK);
        g.drawString("Profile & Privacy", x + 24, curY + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Choose who can discover your profile and see your contributions.", x + 24, curY + 48);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Profile visibility", x + 24, curY + 76);

        int optY = curY + 88;
        int optW = w - 48;
        int optH = 56;

        radioPublicBox.setBounds(x + 24, optY, optW, optH);
        paintVisibilityOptionCard(g, radioPublicBox, SettingsData.ProfileVisibility.PUBLIC,
                settingsData.visibility == SettingsData.ProfileVisibility.PUBLIC);

        optY += optH + 12;
        radioTeamBox.setBounds(x + 24, optY, optW, optH);
        paintVisibilityOptionCard(g, radioTeamBox, SettingsData.ProfileVisibility.TEAM_MEMBERS,
                settingsData.visibility == SettingsData.ProfileVisibility.TEAM_MEMBERS);

        optY += optH + 12;
        radioPrivateBox.setBounds(x + 24, optY, optW, optH);
        paintVisibilityOptionCard(g, radioPrivateBox, SettingsData.ProfileVisibility.PRIVATE,
                settingsData.visibility == SettingsData.ProfileVisibility.PRIVATE);

        int togY = optY + optH + 24;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Show skills", x + 24, togY + 12);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("Display your skills and proficiency levels on your profile.", x + 24, togY + 28);

        toggleSkillsBox.setBounds(x + w - 24 - 44, togY + 6, 44, 22);
        paintToggleSwitch(g, toggleSkillsBox, settingsData.showSkills);

        togY += 54;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Show activity", x + 24, togY + 12);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("Show recent contributions and completed projects to profile visitors.", x + 24, togY + 28);

        toggleActivityBox.setBounds(x + w - 24 - 44, togY + 6, 44, 22);
        paintToggleSwitch(g, toggleActivityBox, settingsData.showActivity);

        togY += 54;

        // Privacy note banner in Theme Pastel Mint
        RoundRectangle2D privBanner = new RoundRectangle2D.Float(x + 24, togY, optW, 46, 8, 8);
        g.setColor(SUCCESS_BG);
        g.fill(privBanner);
        g.setColor(SUCCESS_BORDER);
        g.draw(privBanner);

        drawCheckCircle(g, x + 42, togY + 22);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(SUCCESS_GREEN);
        g.drawString("Your contact details stay private", x + 58, togY + 18);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("Your email address is never shown on your public profile.", x + 58, togY + 32);

        int btnY = togY + 60;
        savePrivacyBtnBox.setBounds(x + 24, btnY, 104, 34);
        RoundRectangle2D saveBtn = new RoundRectangle2D.Float(savePrivacyBtnBox.x, savePrivacyBtnBox.y,
                savePrivacyBtnBox.width, savePrivacyBtnBox.height, 8, 8);
        g.setColor(savePrivacyHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(saveBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString("Save changes", savePrivacyBtnBox.x + 16, savePrivacyBtnBox.y + 21);

        cancelPrivacyBtnBox.setBounds(savePrivacyBtnBox.x + 104 + 10, btnY, 72, 34);
        RoundRectangle2D canBtn = new RoundRectangle2D.Float(cancelPrivacyBtnBox.x, cancelPrivacyBtnBox.y,
                cancelPrivacyBtnBox.width, cancelPrivacyBtnBox.height, 8, 8);
        g.setColor(cancelPrivacyHovered ? BG_CANVAS : Color.WHITE);
        g.fill(canBtn);
        g.setColor(BORDER_COLOR);
        g.draw(canBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Cancel", cancelPrivacyBtnBox.x + 16, cancelPrivacyBtnBox.y + 21);

        return curY + cardH + 30;
    }

    private void paintVisibilityOptionCard(Graphics2D g, Rectangle b, SettingsData.ProfileVisibility opt,
            boolean isSelected) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(b.x, b.y, b.width, b.height, 8, 8);
        if (isSelected) {
            g.setColor(BRAND_TINT_BG);
            g.fill(shape);
            g.setColor(BRAND_ACCENT);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);
        } else {
            g.setColor(Color.WHITE);
            g.fill(shape);
            g.setColor(BORDER_COLOR);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(shape);
        }

        int rx = b.x + 18;
        int ry = b.y + 20;
        int rd = 16;
        g.setColor(isSelected ? BRAND_ACCENT : BORDER_COLOR);
        g.drawOval(rx, ry, rd, rd);
        if (isSelected) {
            g.setColor(BRAND_ACCENT);
            g.fillOval(rx + 4, ry + 4, rd - 8, rd - 8);
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString(opt.title, b.x + 46, b.y + 24);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(opt.desc, b.x + 46, b.y + 40);
    }

    private void paintToggleSwitch(Graphics2D g, Rectangle b, boolean isOn) {
        RoundRectangle2D pill = new RoundRectangle2D.Float(b.x, b.y, b.width, b.height, b.height, b.height);
        g.setColor(isOn ? BRAND_ACCENT : CONTROL_OFF_BG);
        g.fill(pill);

        int knobSize = b.height - 4;
        int knobX = isOn ? (b.x + b.width - knobSize - 2) : (b.x + 2);
        int knobY = b.y + 2;
        g.setColor(Color.WHITE);
        g.fillOval(knobX, knobY, knobSize, knobSize);
    }

    private int paintCollabPreferencesTab(Graphics2D g, int x, int y, int w) {
        int curY = y;
        int cardH = 520;
        catRemoveBounds.clear();

        RoundRectangle2D card = new RoundRectangle2D.Float(x, curY, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.draw(card);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT_DARK);
        g.drawString("Collaboration Preferences", x + 24, curY + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Help us connect you with projects that fit your interests and schedule.", x + 24, curY + 48);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Project categories", x + 24, curY + 76);

        int chipX = x + 24;
        int chipY = curY + 86;
        for (int i = 0; i < settingsData.categories.size(); i++) {
            String cat = settingsData.categories.get(i);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            int tw = g.getFontMetrics().stringWidth(cat);
            int chipW = tw + 28;
            int chipH = 24;

            RoundRectangle2D chip = new RoundRectangle2D.Float(chipX, chipY, chipW, chipH, 6, 6);
            g.setColor(BRAND_TINT_BG);
            g.fill(chip);
            g.setColor(BRAND_TINT_BORDER);
            g.draw(chip);

            g.setColor(BRAND_ACCENT);
            g.drawString(cat, chipX + 8, chipY + 16);
            g.drawString("×", chipX + tw + 14, chipY + 16);

            catRemoveBounds.add(new Rectangle(chipX, chipY, chipW, chipH));
            chipX += chipW + 10;
        }

        int addY = chipY + 36;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Add a category", x + 24, addY + 12);

        int fullInputW = w - 48;
        addCatBox.setBounds(x + 24, addY + 20, fullInputW, 36);
        RoundRectangle2D addCatShape = new RoundRectangle2D.Float(addCatBox.x, addCatBox.y, addCatBox.width,
                addCatBox.height, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(addCatShape);
        g.setColor(BORDER_COLOR);
        g.draw(addCatShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Search project categories...", addCatBox.x + 14, addCatBox.y + 22);
        Draw.drawChevronDown(g, addCatBox.x + addCatBox.width - 24, addCatBox.y + 14, 8, TEXT_MUTED, 0f);

        int rolesY = addCatBox.y + 50;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Preferred roles", x + 24, rolesY + 12);

        int row1Y = rolesY + 24;
        int halfW = (w - 48) / 2;

        checkFrontendBox.setBounds(x + 24, row1Y, 18, 18);
        paintCheckbox(g, checkFrontendBox, settingsData.roleFrontendDev, "Frontend Developer");

        checkUiBox.setBounds(x + 24 + halfW, row1Y, 18, 18);
        paintCheckbox(g, checkUiBox, settingsData.roleUiDev, "UI Developer");

        int row2Y = row1Y + 28;
        checkBackendBox.setBounds(x + 24, row2Y, 18, 18);
        paintCheckbox(g, checkBackendBox, settingsData.roleBackendDev, "Backend Developer");

        checkLeadBox.setBounds(x + 24 + halfW, row2Y, 18, 18);
        paintCheckbox(g, checkLeadBox, settingsData.roleProjectLead, "Project Lead");

        int ddY = row2Y + 36;
        int ddW = (w - 48 - 16) / 2;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Weekly availability", x + 24, ddY + 12);
        availDropdown.setBounds(x + 24, ddY + 20, ddW, 36);
        paintDropdownInput(g, availDropdown, settingsData.weeklyAvailability);

        g.drawString("Preferred commitment", x + 24 + ddW + 16, ddY + 12);
        commitDropdown.setBounds(x + 24 + ddW + 16, ddY + 20, ddW, 36);
        paintDropdownInput(g, commitDropdown, settingsData.preferredCommitment);

        int collabY = ddY + 70;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("How would you like to collaborate?", x + 24, collabY + 12);

        int segW = w - 48;
        int segH = 38;
        int segX = x + 24;
        int segY = collabY + 20;

        RoundRectangle2D segBg = new RoundRectangle2D.Float(segX, segY, segW, segH, 8, 8);
        g.setColor(BG_CANVAS);
        g.fill(segBg);
        g.setColor(BORDER_COLOR);
        g.draw(segBg);

        int itemSegW = (segW - 8) / 3;
        modeRemoteBox.setBounds(segX + 4, segY + 4, itemSegW, segH - 8);
        paintSegmentItem(g, modeRemoteBox, "Remote", settingsData.collabMode == SettingsData.CollabMode.REMOTE);

        modeInPersonBox.setBounds(segX + 4 + itemSegW, segY + 4, itemSegW, segH - 8);
        paintSegmentItem(g, modeInPersonBox, "In person", settingsData.collabMode == SettingsData.CollabMode.IN_PERSON);

        modeEitherBox.setBounds(segX + 4 + itemSegW * 2, segY + 4, itemSegW, segH - 8);
        paintSegmentItem(g, modeEitherBox, "Either works", settingsData.collabMode == SettingsData.CollabMode.EITHER);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(TEXT_LIGHT);
        g.drawString(settingsData.timeZoneInfo, x + 24, segY + segH + 16);

        int btnY = segY + segH + 32;
        saveCollabBtnBox.setBounds(x + 24, btnY, 104, 34);
        RoundRectangle2D saveBtn = new RoundRectangle2D.Float(saveCollabBtnBox.x, saveCollabBtnBox.y,
                saveCollabBtnBox.width, saveCollabBtnBox.height, 8, 8);
        g.setColor(saveCollabHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(saveBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString("Save changes", saveCollabBtnBox.x + 16, saveCollabBtnBox.y + 21);

        cancelCollabBtnBox.setBounds(saveCollabBtnBox.x + 104 + 10, btnY, 72, 34);
        RoundRectangle2D canBtn = new RoundRectangle2D.Float(cancelCollabBtnBox.x, cancelCollabBtnBox.y,
                cancelCollabBtnBox.width, cancelCollabBtnBox.height, 8, 8);
        g.setColor(cancelCollabHovered ? BG_CANVAS : Color.WHITE);
        g.fill(canBtn);
        g.setColor(BORDER_COLOR);
        g.draw(canBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Cancel", cancelCollabBtnBox.x + 16, cancelCollabBtnBox.y + 21);

        return curY + cardH + 30;
    }

    private void paintCheckbox(Graphics2D g, Rectangle b, boolean checked, String label) {
        RoundRectangle2D box = new RoundRectangle2D.Float(b.x, b.y, b.width, b.height, 4, 4);
        if (checked) {
            g.setColor(BRAND_ACCENT);
            g.fill(box);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(b.x + 4, b.y + 9, b.x + 7, b.y + 13);
            g.drawLine(b.x + 7, b.y + 13, b.x + 13, b.y + 5);
        } else {
            g.setColor(Color.WHITE);
            g.fill(box);
            g.setColor(BORDER_COLOR);
            g.draw(box);
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_DARK);
        g.drawString(label, b.x + b.width + 10, b.y + 14);
    }

    private void paintDropdownInput(Graphics2D g, Rectangle b, String text) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(b.x, b.y, b.width, b.height, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(BORDER_COLOR);
        g.draw(shape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_DARK);
        g.drawString(text, b.x + 14, b.y + 22);

        Draw.drawChevronDown(g, b.x + b.width - 24, b.y + 14, 8, TEXT_MUTED, 0f);
    }

    private void paintSegmentItem(Graphics2D g, Rectangle b, String label, boolean isSelected) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(b.x, b.y, b.width, b.height, 6, 6);
        if (isSelected) {
            g.setColor(Color.WHITE);
            g.fill(shape);
            g.setColor(BORDER_COLOR);
            g.draw(shape);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(BRAND_ACCENT);
        } else {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(TEXT_MUTED);
        }
        FontMetrics fm = g.getFontMetrics();
        int lw = fm.stringWidth(label);
        g.drawString(label, b.x + (b.width - lw) / 2, b.y + 20);
    }

    private int paintDangerZoneTab(Graphics2D g, int x, int y, int w) {
        int curY = y;
        int bannerH = 46;
        RoundRectangle2D banner = new RoundRectangle2D.Float(x, curY, w, bannerH, 10, 10);
        g.setColor(AMBER_BG);
        g.fill(banner);
        g.setColor(AMBER_BORDER);
        g.draw(banner);

        g.setColor(AMBER_TEXT);
        g.drawOval(x + 16, curY + 14, 16, 16);
        g.drawLine(x + 21, curY + 22, x + 23, curY + 25);
        g.drawLine(x + 23, curY + 25, x + 28, curY + 19);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(AMBER_TEXT);
        g.drawString("Please review these actions carefully", x + 40, curY + 20);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("Changes here affect your account and the teams that rely on you.", x + 40, curY + 34);

        curY += bannerH + 20;

        int card1H = 210;
        RoundRectangle2D card1 = new RoundRectangle2D.Float(x, curY, w, card1H, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card1);
        g.setColor(BORDER_COLOR);
        g.draw(card1);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Deactivate account", x + 24, curY + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Take a break without permanently deleting your information.", x + 24, curY + 48);
        g.drawString(
                "Your profile will be hidden from discovery and you won't receive new invitations. Your projects, messages and contributions will",
                x + 24, curY + 76);
        g.drawString("remain available to your teammates.", x + 24, curY + 92);

        g.drawString("•  You can reactivate by signing back in.", x + 24, curY + 118);
        g.drawString("•  Existing team members will see your account as inactive.", x + 24, curY + 136);
        g.drawString("•  Transfer any project ownership before taking a break.", x + 24, curY + 154);

        deactivateBtnBox.setBounds(x + 24, curY + 168, 136, 32);
        RoundRectangle2D deactBtn = new RoundRectangle2D.Float(deactivateBtnBox.x, deactivateBtnBox.y,
                deactivateBtnBox.width, deactivateBtnBox.height, 8, 8);
        g.setColor(deactivateHovered ? BG_CANVAS : Color.WHITE);
        g.fill(deactBtn);
        g.setColor(BORDER_COLOR);
        g.draw(deactBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("Deactivate account", deactivateBtnBox.x + 16, deactivateBtnBox.y + 20);

        curY += card1H + 20;

        int card2H = 260;
        RoundRectangle2D card2 = new RoundRectangle2D.Float(x, curY, w, card2H, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card2);
        g.setColor(RED_BORDER);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card2);

        drawWarningTriangle(g, x + 24, curY + 20, 14, ERROR_RED);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(ERROR_RED);
        g.drawString("Delete account", x + 44, curY + 32);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Permanently delete your TeamUp account and personal profile. This action cannot be undone.",
                x + 24, curY + 54);
        g.drawString("•  Your profile, skills and saved preferences will be removed.", x + 24, curY + 80);
        g.drawString("•  Shared project contributions may remain without your name.", x + 24, curY + 98);
        g.drawString("•  You'll leave 2 teams and lose access to team conversations.", x + 24, curY + 116);

        int redBoxH = 46;
        int redBoxY = curY + 132;
        RoundRectangle2D redAlert = new RoundRectangle2D.Float(x + 24, redBoxY, w - 48, redBoxH, 8, 8);
        g.setColor(RED_BG);
        g.fill(redAlert);

        g.setColor(ERROR_RED);
        g.drawOval(x + 36, redBoxY + 15, 16, 16);
        g.drawLine(x + 44, redBoxY + 19, x + 44, redBoxY + 24);
        g.fillOval(x + 43, redBoxY + 26, 2, 2);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.drawString("You own " + settingsData.ownedProject, x + 60, redBoxY + 20);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.drawString("Transfer project ownership to a teammate before deleting your account.", x + 60, redBoxY + 34);

        // Solid Delete Button in Theme.ERROR_RED
        deleteAccountBtnBox.setBounds(x + 24, curY + 194, 126, 36);
        RoundRectangle2D delBtn = new RoundRectangle2D.Float(deleteAccountBtnBox.x, deleteAccountBtnBox.y,
                deleteAccountBtnBox.width, deleteAccountBtnBox.height, 8, 8);
        g.setColor(deleteAccountHovered ? ERROR_RED_HOVER : ERROR_RED);
        g.fill(delBtn);
        drawTrashIcon(g, deleteAccountBtnBox.x + 14, deleteAccountBtnBox.y + 11, 13, Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString("Delete account", deleteAccountBtnBox.x + 34, deleteAccountBtnBox.y + 22);

        curY += card2H + 20;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_LIGHT);
        g.drawString("Need help with your account? Contact " + settingsData.supportEmail + ".", x, curY + 14);

        return curY + 40;
    }

    private void paintInputField(Graphics2D g, Rectangle b, String val, boolean focused) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(b.x, b.y, b.width, b.height, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(focused ? BRAND_ACCENT : BORDER_COLOR);
        g.draw(shape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_DARK);
        g.drawString(val, b.x + 12, b.y + 22);

        if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int tw = g.getFontMetrics().stringWidth(val);
            g.setColor(BRAND_ACCENT);
            g.drawLine(b.x + 12 + tw + 2, b.y + 10, b.x + 12 + tw + 2, b.y + 26);
        }
    }

    private void drawWarningTriangle(Graphics2D g, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x + size / 2f, y);
        p.lineTo(x + size, y + size);
        p.lineTo(x, y + size);
        p.closePath();
        g.draw(p);
        g.drawLine(x + size / 2, y + 4, x + size / 2, y + size - 5);
        g.fillOval(x + size / 2 - 1, y + size - 3, 2, 2);
    }

    private void drawTrashIcon(Graphics2D g, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x, y + 3, x + size, y + 3);
        g.drawLine(x + 3, y + 1, x + size - 3, y + 1);
        g.drawRoundRect(x + 2, y + 3, size - 4, size - 3, 2, 2);
        g.drawLine(x + 5, y + 6, x + 5, y + size - 2);
        g.drawLine(x + size - 5, y + 6, x + size - 5, y + size - 2);
    }

    // =========================================================================
    // 4. MY TEAMS VIEW RENDERING (CAMPUSCONNECT WORKSPACE)
    // =========================================================================
    private void paintMyTeamsContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = 36;
        int padY = 24;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        memberCardBounds.clear();
        requestItemBounds.clear();

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_LIGHT);
        g.drawString(teamData.breadcrumbs, startX + padX, curY + 14);
        curY += 26;

        int titleY = curY + 24;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.setColor(TEXT_DARK);
        g.drawString(teamData.workspaceName, startX + padX, titleY);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString(teamData.subtitle, startX + padX, titleY + 22);

        int btnH = 36;
        teamSettingsBtnW = 136;
        teamSettingsBtnH = btnH;
        teamSettingsBtnX = startX + padX + contentW - teamSettingsBtnW;
        teamSettingsBtnY = curY + 4;

        RoundRectangle2D setBtnShape = new RoundRectangle2D.Float(teamSettingsBtnX, teamSettingsBtnY, teamSettingsBtnW,
                teamSettingsBtnH, 10, 10);
        g.setColor(teamSettingsBtnHovered ? BG_CANVAS : Color.WHITE);
        g.fill(setBtnShape);
        g.setColor(BORDER_COLOR);
        g.draw(setBtnShape);

        drawGearIcon(g, teamSettingsBtnX + 14, teamSettingsBtnY + 11, 14, BRAND_ACCENT);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Team settings", teamSettingsBtnX + 34, teamSettingsBtnY + 22);

        curY += 66;

        int tabX = startX + padX;
        int tabY = curY;
        for (int i = 0; i < teamData.tabs.size(); i++) {
            String tabText = teamData.tabs.get(i);
            boolean isActive = (i == teamData.activeTabIndex);
            boolean isHov = (i == hoveredTeamTabIndex);

            g.setFont(new Font(Font.SANS_SERIF, isActive ? Font.BOLD : Font.PLAIN, 13));
            FontMetrics tfm = g.getFontMetrics();
            int tabTextW = tfm.stringWidth(tabText);
            int totalTabW = tabTextW + 16;

            if (i < teamTabBounds.length) {
                teamTabBounds[i].setBounds(tabX, tabY - 4, totalTabW, 32);
            }

            if (isActive) {
                g.setColor(BRAND_ACCENT);
                g.drawString(tabText, tabX + 8, tabY + 18);
                g.fill(new RoundRectangle2D.Float(tabX + 8, tabY + 26, tabTextW, 3, 2, 2));
            } else {
                g.setColor(isHov ? TEXT_DARK : TEXT_MUTED);
                g.drawString(tabText, tabX + 8, tabY + 18);
            }
            tabX += totalTabW + 16;
        }

        g.setColor(BORDER_COLOR);
        g.drawLine(startX + padX, tabY + 28, startX + padX + contentW, tabY + 28);
        curY += 46;

        int kpiGap = 16;
        int kpiW = (contentW - kpiGap * 3) / 4;
        int kpiH = 108;

        paintKpiCard(g, startX + padX, curY, kpiW, kpiH, "Project progress", teamData.progressPercent + "%",
                teamData.tasksCompleted + " of " + teamData.tasksTotal + " tasks completed", 0);
        paintKpiCard(g, startX + padX + (kpiW + kpiGap), curY, kpiW, kpiH, "Team members",
                teamData.currentMembers + " / " + teamData.maxMembers,
                teamData.openPositions + " open positions", 1);
        paintKpiCard(g, startX + padX + (kpiW + kpiGap) * 2, curY, kpiW, kpiH, "Next milestone",
                teamData.nextMilestoneDate, teamData.nextMilestoneSub, 2);
        paintKpiCard(g, startX + padX + (kpiW + kpiGap) * 3, curY, kpiW, kpiH, "Tasks due soon",
                String.valueOf(teamData.tasksDueSoon), teamData.tasksDueSoonSub, 3);

        curY += kpiH + 22;

        int colGap = 20;
        int leftColW = Math.round((contentW - colGap) * 0.64f);
        int rightColW = contentW - leftColW - colGap;
        int leftX = startX + padX;
        int rightX = leftX + leftColW + colGap;

        int curLeftY = curY;
        int card1H = 154;
        paintPeopleMakingItHappen(g, leftX, curLeftY, leftColW, card1H);
        curLeftY += card1H + 20;

        int card2H = 196;
        paintMilestonesCard(g, leftX, curLeftY, leftColW, card2H);
        curLeftY += card2H + 20;

        int card3H = 126;
        paintTaskSummaryCard(g, leftX, curLeftY, leftColW, card3H);
        curLeftY += card3H + 20;

        int curRightY = curY;
        int cardR1H = 226;
        paintRecruitmentRequestsCard(g, rightX, curRightY, rightColW, cardR1H);
        curRightY += cardR1H + 20;

        int cardR2H = 252;
        paintTeamActivityCard(g, rightX, curRightY, rightColW, cardR2H);
        curRightY += cardR2H + 20;

        totalContentHeight = Math.max(curLeftY, curRightY) - startY + 50;
    }

    private void paintKpiCard(Graphics2D g, int x, int y, int w, int h, String title, String val, String sub,
            int type) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.1f));
        g.draw(shape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString(title, x + 18, y + 26);

        if (type == 1) {
            drawUsersIcon(g, x + w - 34, y + 14, SUCCESS_GREEN);
        } else if (type == 3) {
            drawCalendarIcon(g, x + w - 34, y + 14, Theme.HIGHLIGHT_WARM);
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.setColor(TEXT_DARK);
        g.drawString(val, x + 18, y + 64);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(sub, x + 18, y + 90);
    }

    private void paintPeopleMakingItHappen(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.draw(card);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("The people making it happen", x + 20, y + 28);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String invText = "Invite member →";
        int invW = fm.stringWidth(invText);
        inviteMemberBtnX = x + w - invW - 20;
        inviteMemberBtnY = y + 16;
        inviteMemberBtnW = invW;
        inviteMemberBtnH = 20;

        g.setColor(inviteMemberBtnHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.drawString(invText, inviteMemberBtnX, y + 28);

        int memCount = Math.max(1, teamData.members.size());
        int padSide = 16;
        int slotW = (w - padSide * 2) / memCount;
        int slotY = y + 44;

        for (int i = 0; i < teamData.members.size(); i++) {
            TeamWorkspaceData.TeamMember m = teamData.members.get(i);
            int slotX = x + padSide + i * slotW;
            int cx = slotX + slotW / 2;

            memberCardBounds.add(new Rectangle(slotX + 4, slotY, slotW - 8, 96));
            drawAvatar(g, cx, slotY + 22, 18, m.initials, m.avatarBg, m.avatarFg);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(TEXT_DARK);
            int nw = g.getFontMetrics().stringWidth(m.name);
            g.drawString(m.name, cx - nw / 2, slotY + 54);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            int rw = g.getFontMetrics().stringWidth(m.role);
            g.drawString(m.role, cx - rw / 2, slotY + 70);

            boolean isYou = "You".equalsIgnoreCase(m.badge);
            int bw = isYou ? 38 : 52;
            int bh = 18;
            int bx = cx - bw / 2;
            int by = slotY + 78;

            RoundRectangle2D bShape = new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh);
            if (isYou) {
                g.setColor(BRAND_TINT_BG);
                g.fill(bShape);
                g.setColor(BRAND_TINT_BORDER);
                g.draw(bShape);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                g.setColor(BRAND_ACCENT);
                int ytw = g.getFontMetrics().stringWidth("You");
                g.drawString("You", bx + (bw - ytw) / 2, by + 13);
            } else {
                g.setColor(BG_CANVAS);
                g.fill(bShape);
                g.setColor(BORDER_COLOR);
                g.draw(bShape);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                g.setColor(TEXT_MUTED);
                int mtw = g.getFontMetrics().stringWidth("Member");
                g.drawString("Member", bx + (bw - mtw) / 2, by + 13);
            }
        }
    }

    private void paintMilestonesCard(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.draw(card);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Milestones", x + 20, y + 28);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String roadText = "View roadmap →";
        int roadW = fm.stringWidth(roadText);
        viewRoadmapBtnX = x + w - roadW - 20;
        viewRoadmapBtnY = y + 16;
        viewRoadmapBtnW = roadW;
        viewRoadmapBtnH = 20;

        g.setColor(viewRoadmapBtnHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.drawString(roadText, viewRoadmapBtnX, y + 28);

        int itemY = y + 46;
        int rowH = 46;

        for (int i = 0; i < teamData.milestones.size(); i++) {
            TeamWorkspaceData.MilestoneItem m = teamData.milestones.get(i);
            int rowCenterY = itemY + i * rowH + 20;

            if ("Completed".equalsIgnoreCase(m.status)) {
                drawCheckCircle(g, x + 28, rowCenterY);
            } else if ("In progress".equalsIgnoreCase(m.status)) {
                g.setColor(BRAND_ACCENT);
                g.setStroke(new BasicStroke(1.8f));
                g.drawOval(x + 20, rowCenterY - 8, 16, 16);
                g.fillOval(x + 25, rowCenterY - 3, 6, 6);
            } else {
                g.setColor(BORDER_COLOR);
                g.drawOval(x + 20, rowCenterY - 8, 16, 16);
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g.setColor(TEXT_DARK);
            g.drawString(m.title, x + 48, rowCenterY - 1);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString(m.dueInfo, x + 48, rowCenterY + 14);

            int barW = 126;
            int barH = 6;
            int pctW = 44;
            int barX = x + w - barW - pctW - 20;
            int barY = rowCenterY - barH / 2;

            RoundRectangle2D track = new RoundRectangle2D.Float(barX, barY, barW, barH, barH, barH);
            g.setColor(BORDER_LIGHT);
            g.fill(track);

            if (m.progressPercent > 0) {
                int fillW = Math.max(barH, Math.round(barW * (m.progressPercent / 100f)));
                RoundRectangle2D fill = new RoundRectangle2D.Float(barX, barY, fillW, barH, barH, barH);
                g.setColor(BRAND_ACCENT);
                g.fill(fill);
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(TEXT_DARK);
            g.drawString(m.progressPercent + "%", barX + barW + 12, rowCenterY + 4);
        }
    }

    private void paintTaskSummaryCard(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.draw(card);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Task summary", x + 20, y + 28);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String boardText = "Open board →";
        int boardW = fm.stringWidth(boardText);
        openBoardBtnX = x + w - boardW - 20;
        openBoardBtnY = y + 16;
        openBoardBtnW = boardW;
        openBoardBtnH = 20;

        g.setColor(openBoardBtnHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.drawString(boardText, openBoardBtnX, y + 28);

        int colW = (w - 40) / 4;
        int statY = y + 46;

        paintSummaryColumn(g, x + 20, statY, colW, String.valueOf(teamData.taskSummary.todo), "To Do", TEXT_DARK);
        paintSummaryColumn(g, x + 20 + colW, statY, colW, String.valueOf(teamData.taskSummary.inProgress),
                "In Progress", TEXT_DARK);
        paintSummaryColumn(g, x + 20 + colW * 2, statY, colW, String.valueOf(teamData.taskSummary.inReview),
                "In Review", TEXT_DARK);
        paintSummaryColumn(g, x + 20 + colW * 3, statY, colW, String.valueOf(teamData.taskSummary.done), "Done",
                SUCCESS_GREEN);
    }

    private void paintSummaryColumn(Graphics2D g, int x, int y, int w, String val, String label, Color numColor) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(numColor);
        g.drawString(val, x + 4, y + 30);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString(label, x + 4, y + 54);
    }

    private void paintRecruitmentRequestsCard(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.draw(card);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Recruitment requests", x + 20, y + 28);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String revText = "Review all →";
        int revW = fm.stringWidth(revText);
        reviewAllBtnX = x + w - revW - 20;
        reviewAllBtnY = y + 16;
        reviewAllBtnW = revW;
        reviewAllBtnH = 20;

        g.setColor(reviewAllBtnHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.drawString(revText, reviewAllBtnX, y + 28);

        int badgeW = 118;
        int badgeH = 22;
        int badgeX = x + 20;
        int badgeY = y + 40;
        RoundRectangle2D bShape = new RoundRectangle2D.Float(badgeX, badgeY, badgeW, badgeH, badgeH, badgeH);
        g.setColor(AMBER_BG);
        g.fill(bShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(AMBER_TEXT);
        g.drawString(teamData.pendingRequestsCount + " pending review", badgeX + 12, badgeY + 15);

        int curY = y + 74;
        int rowH = 48;

        for (int i = 0; i < teamData.recruitmentRequests.size(); i++) {
            TeamWorkspaceData.RecruitmentRequest req = teamData.recruitmentRequests.get(i);
            int rowCenterY = curY + i * rowH + 20;

            requestItemBounds.add(new Rectangle(x + 16, curY + i * rowH, w - 32, rowH));
            drawAvatar(g, x + 36, rowCenterY, 17, req.initials, req.avatarBg, req.avatarFg);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g.setColor(TEXT_DARK);
            g.drawString(req.name, x + 62, rowCenterY - 2);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString(req.roleSkill, x + 62, rowCenterY + 14);
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        String appText = "Review applications →";
        int appW = g.getFontMetrics().stringWidth(appText);
        reviewAppsBtnX = x + 20;
        reviewAppsBtnY = y + h - 28;
        reviewAppsBtnW = appW;
        reviewAppsBtnH = 20;

        g.setColor(reviewAppsBtnHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.drawString(appText, reviewAppsBtnX, y + h - 14);
    }

    private void paintTeamActivityCard(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.draw(card);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Team activity", x + 20, y + 28);

        int curY = y + 42;
        int rowH = 50;

        for (int i = 0; i < teamData.activities.size(); i++) {
            TeamWorkspaceData.TeamActivity act = teamData.activities.get(i);
            int rowCenterY = curY + i * rowH + 20;

            drawAvatar(g, x + 34, rowCenterY, 15, act.initials, act.avatarBg, act.avatarFg);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(TEXT_DARK);
            g.drawString(act.name, x + 58, rowCenterY - 2);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString(act.actionText, x + 58, rowCenterY + 13);
        }

        g.setColor(BORDER_LIGHT);
        g.drawLine(x + 20, y + h - 34, x + w - 20, y + h - 34);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_LIGHT);
        g.drawString(teamData.syncNotice, x + 20, y + h - 14);
    }

    private void drawAvatar(Graphics2D g, int cx, int cy, int radius, String initials, Color bg, Color fg) {
        g.setColor(bg);
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        g.setColor(fg);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, radius > 16 ? 12 : 11));
        FontMetrics fm = g.getFontMetrics();
        int sw = fm.stringWidth(initials);
        int sh = fm.getAscent() - fm.getDescent();
        g.drawString(initials, cx - sw / 2, cy + sh / 2);
    }

    private void drawCheckCircle(Graphics2D g, int cx, int cy) {
        int r = 9;
        g.setColor(Theme.PASTEL_MINT);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);

        g.setColor(SUCCESS_GREEN);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 4, cy, cx - 1, cy + 3);
        g.drawLine(cx - 1, cy + 3, cx + 4, cy - 3);
    }

    private void drawUsersIcon(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(x + 2, y, 6, 6);
        g.drawArc(x - 1, y + 7, 12, 7, 0, 180);
        g.drawOval(x + 10, y + 2, 5, 5);
        g.drawArc(x + 7, y + 8, 10, 6, 0, 180);
    }

    private void drawCalendarIcon(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(x + 2, y + 2, 14, 13, 3, 3);
        g.drawLine(x + 5, y, x + 5, y + 2);
        g.drawLine(x + 13, y, x + 13, y + 2);
        g.drawLine(x + 2, y + 6, x + 16, y + 6);
    }

    private void drawGearIcon(Graphics2D g, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = x + size / 2;
        int cy = y + size / 2;
        int r = size / 2 - 2;
        g.drawOval(cx - r, cy - r, r * 2, r * 2);
        g.drawOval(cx - 2, cy - 2, 4, 4);
        g.drawLine(cx, cy - r - 2, cx, cy - r);
        g.drawLine(cx, cy + r, cx, cy + r + 2);
        g.drawLine(cx - r - 2, cy, cx - r, cy);
        g.drawLine(cx + r, cy, cx + r + 2, cy);
    }

    private void drawMessageBubbleIcon(Graphics2D g, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(x + 1, y + 1, size - 2, size - 4, 4, 4);
        g.drawLine(x + 4, y + size - 3, x + 2, y + size);
    }

    // =========================================================================
    // EVENT HANDLING & USER ACTIONS
    // =========================================================================
    public boolean onClick(int mx, int my) {
        // 1. Modals take priority
        if (modals != null && modals.onClick(mx, my, this)) {
            return true;
        }

        // 2. Active View's High Z-index overlays take priority (Dropdowns & Profile
        // Modals)
        if (activeNav == NavItem.MY_PROJECTS) {
            if (myProjectsView.onClick(mx, my, this))
                return true;
        } else if (activeNav == NavItem.FIND_TEAMMATES) {
            if (teammatesView.onClick(mx, my, this))
                return true;
        } else if (activeNav == NavItem.DISCOVER_PROJECTS) {
            if (projectsView.onClick(mx, my, this))
                return true;
        }

        // 3. Header controls
        if (header != null && header.onClick(mx, my, this)) {
            return true;
        }

        // 4. Sidebar controls
        if (sidebar != null && sidebar.onClick(mx, my, this)) {
            return true;
        }

        // 5. Main Content Area controls
        switch (activeNav) {
            case MY_PROJECTS:
                if (myProjectsView.onClick(mx, my, this))
                    return true;
                break;
            case FIND_TEAMMATES:
                if (teammatesView.onClick(mx, my, this))
                    return true;
                break;
            case DISCOVER_PROJECTS:
                if (projectsView.onClick(mx, my, this))
                    return true;
                break;
            case OVERVIEW:
                if (overview.onClick(mx, my, this))
                    return true;
                break;
            case MY_PROFILE:
                if (handleProfileClick(mx, my))
                    return true;
                break;
            case SETTINGS:
                if (handleSettingsClick(mx, my))
                    return true;
                break;
        }

        return false;
    }

    private boolean handleProfileClick(int mx, int my) {
        for (int i = 0; i < profileTabBounds.length; i++) {
            if (profileTabBounds[i] != null && profileTabBounds[i].contains(mx, my)) {
                profileData.activeTabIndex = i;
                targetScrollY = 0f;
                showToast("Switched to " + profileData.tabs.get(i));
                return true;
            }
        }

        if (editProfileBtnBox.contains(mx, my)) {
            openModal(ActiveModal.COMPLETE_PROFILE);
            return true;
        }

        if (shareProfileBtnBox.contains(mx, my)) {
            String url = "https://teamup.com/u/" + (profileData.handle != null ? profileData.handle.replace("@", "") : "alex");
            try {
                java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(url);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
                showToast("Profile link copied to clipboard: " + url + " 📋");
            } catch (Exception ex) {
                showToast("Profile link: " + url);
            }
            return true;
        }

        if (completeProfileBtnBox.contains(mx, my)) {
            openModal(ActiveModal.COMPLETE_PROFILE);
            return true;
        }

        for (int i = 0; i < profileSkillBounds.size() && i < profileSkillIndex.size(); i++) {
            if (profileSkillBounds.get(i).contains(mx, my)) {
                int skillIdx = profileSkillIndex.get(i);
                profileData.toggleEndorsement(skillIdx);
                ProfileData.ProfileSkill s = profileData.skills.get(skillIdx);
                DashboardSync.setEndorsement(this, s.name, s.userEndorsed);
                showToast(s.userEndorsed ? ("Endorsed " + s.name + " (" + s.endorsements + ")")
                        : ("Removed endorsement for " + s.name));
                return true;
            }
        }

        for (int i = 0; i < profileProjectBounds.size() && i < profileProjectIndex.size(); i++) {
            if (profileProjectBounds.get(i).contains(mx, my)) {
                ProfileData.ProfileProject p = profileData.projects.get(profileProjectIndex.get(i));
                showToast("Opening workspace for " + p.title + "...");
                return true;
            }
        }
        return false;
    }

    private boolean handleSettingsClick(int mx, int my) {
        for (int i = 0; i < settingsTabBounds.length; i++) {
            if (settingsTabBounds[i] != null && settingsTabBounds[i].contains(mx, my)) {
                settingsData.activeTab = SettingsData.SettingsTab.values()[i];
                targetScrollY = 0f;
                showToast("Switched to Settings: " + settingsData.activeTab.label);
                return true;
            }
        }

        if (settingsData.activeTab == SettingsData.SettingsTab.ACCOUNT) {
            if (fullNameBox.contains(mx, my)) {
                settingsData.focusedField = SettingsData.ActiveField.FULL_NAME;
                return true;
            }
            if (usernameBox.contains(mx, my)) {
                settingsData.focusedField = SettingsData.ActiveField.USERNAME;
                return true;
            }
            if (emailBox.contains(mx, my)) {
                settingsData.focusedField = SettingsData.ActiveField.EMAIL;
                return true;
            }
            settingsData.focusedField = SettingsData.ActiveField.NONE;

            if (saveAccountBtnBox.contains(mx, my)) {
                settingsData.saveAccountChanges();
                setProfileName(settingsData.fullName);
                if (settingsData.username != null && !settingsData.username.isEmpty()) {
                    profileData.handle = "@" + settingsData.username.replace("@", "");
                }
                if (settingsData.email != null && !settingsData.email.isEmpty()) {
                    profileData.email = settingsData.email;
                }
                DashboardSync.saveAccount(this, settingsData);
                showToast("Account details updated successfully! 🚀");
                return true;
            }
            if (cancelAccountBtnBox.contains(mx, my)) {
                settingsData.cancelAccountChanges();
                showToast("Account changes discarded.");
                return true;
            }
            if (changePassBtnBox.contains(mx, my)) {
                javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 8, 8));
                javax.swing.JPasswordField curPass = new javax.swing.JPasswordField();
                javax.swing.JPasswordField newPass = new javax.swing.JPasswordField();
                panel.add(new javax.swing.JLabel("Current Password:"));
                panel.add(curPass);
                panel.add(new javax.swing.JLabel("New Password:"));
                panel.add(newPass);
                int res = javax.swing.JOptionPane.showConfirmDialog(
                        null, panel, "Update Account Password",
                        javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
                if (res == javax.swing.JOptionPane.OK_OPTION) {
                    String curP = new String(curPass.getPassword()).trim();
                    String newP = new String(newPass.getPassword()).trim();
                    if (newP.length() < 6) {
                        showToast("New password must be at least 6 characters.");
                    } else {
                        DashboardSync.changePassword(this, curP, newP);
                    }
                }
                return true;
            }
        }

        if (settingsData.activeTab == SettingsData.SettingsTab.PROFILE_PRIVACY) {
            if (radioPublicBox.contains(mx, my)) {
                settingsData.visibility = SettingsData.ProfileVisibility.PUBLIC;
                showToast("Profile visibility: Public");
                return true;
            }
            if (radioTeamBox.contains(mx, my)) {
                settingsData.visibility = SettingsData.ProfileVisibility.TEAM_MEMBERS;
                showToast("Profile visibility: Team members");
                return true;
            }
            if (radioPrivateBox.contains(mx, my)) {
                settingsData.visibility = SettingsData.ProfileVisibility.PRIVATE;
                showToast("Profile visibility: Private");
                return true;
            }
            if (toggleSkillsBox.contains(mx, my)) {
                settingsData.showSkills = !settingsData.showSkills;
                showToast("Show skills on profile: " + (settingsData.showSkills ? "Enabled" : "Disabled"));
                return true;
            }
            if (toggleActivityBox.contains(mx, my)) {
                settingsData.showActivity = !settingsData.showActivity;
                showToast("Show activity on profile: " + (settingsData.showActivity ? "Enabled" : "Disabled"));
                return true;
            }
            if (savePrivacyBtnBox.contains(mx, my)) {
                DashboardSync.savePreferences(this, settingsData);
                showToast("Privacy preferences saved!");
                return true;
            }
            if (cancelPrivacyBtnBox.contains(mx, my)) {
                showToast("Privacy changes cancelled.");
                return true;
            }
        }

        if (settingsData.activeTab == SettingsData.SettingsTab.COLLAB_PREFERENCES) {
            for (int i = 0; i < catRemoveBounds.size(); i++) {
                if (catRemoveBounds.get(i).contains(mx, my)) {
                    String rem = settingsData.categories.get(i);
                    settingsData.removeCategory(i);
                    showToast("Removed: " + rem);
                    return true;
                }
            }
            if (addCatBox.contains(mx, my)) {
                settingsData.addCategory("Mobile Development");
                showToast("Added 'Mobile Development' category!");
                return true;
            }
            if (checkFrontendBox.contains(mx, my)) {
                settingsData.roleFrontendDev = !settingsData.roleFrontendDev;
                showToast("Frontend Dev: " + (settingsData.roleFrontendDev ? "Checked" : "Unchecked"));
                return true;
            }
            if (checkUiBox.contains(mx, my)) {
                settingsData.roleUiDev = !settingsData.roleUiDev;
                showToast("UI Dev: " + (settingsData.roleUiDev ? "Checked" : "Unchecked"));
                return true;
            }
            if (checkBackendBox.contains(mx, my)) {
                settingsData.roleBackendDev = !settingsData.roleBackendDev;
                showToast("Backend Dev: " + (settingsData.roleBackendDev ? "Checked" : "Unchecked"));
                return true;
            }
            if (checkLeadBox.contains(mx, my)) {
                settingsData.roleProjectLead = !settingsData.roleProjectLead;
                showToast("Project Lead: " + (settingsData.roleProjectLead ? "Checked" : "Unchecked"));
                return true;
            }
            if (availDropdown.contains(mx, my)) {
                settingsData.toggleWeeklyAvailability();
                showToast("Availability: " + settingsData.weeklyAvailability);
                return true;
            }
            if (commitDropdown.contains(mx, my)) {
                settingsData.toggleCommitment();
                showToast("Commitment: " + settingsData.preferredCommitment);
                return true;
            }
            if (modeRemoteBox.contains(mx, my)) {
                settingsData.collabMode = SettingsData.CollabMode.REMOTE;
                showToast("Collab: Remote");
                return true;
            }
            if (modeInPersonBox.contains(mx, my)) {
                settingsData.collabMode = SettingsData.CollabMode.IN_PERSON;
                showToast("Collab: In person");
                return true;
            }
            if (modeEitherBox.contains(mx, my)) {
                settingsData.collabMode = SettingsData.CollabMode.EITHER;
                showToast("Collab: Either works");
                return true;
            }
            if (saveCollabBtnBox.contains(mx, my)) {
                DashboardSync.savePreferences(this, settingsData);
                showToast("Collaboration preferences saved!");
                return true;
            }
        }

        if (settingsData.activeTab == SettingsData.SettingsTab.DANGER_ZONE) {
            if (deactivateBtnBox.contains(mx, my)) {
                int opt = javax.swing.JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to deactivate your account?\nYou can reactivate anytime by logging back in.",
                        "Deactivate TeamUp Account",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                if (opt == javax.swing.JOptionPane.YES_OPTION) {
                    showToast("Account deactivated. Logging out...");
                    if (listener != null) listener.returnToLanding();
                }
                return true;
            }
            if (deleteAccountBtnBox.contains(mx, my)) {
                int opt = javax.swing.JOptionPane.showConfirmDialog(
                        null,
                        "Warning: Are you sure you want to permanently delete your account?\nAll your data and project ownership will be removed.",
                        "Delete Account Permanently",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                if (opt == javax.swing.JOptionPane.YES_OPTION) {
                    showToast("Account deleted. Returning to home...");
                    if (listener != null) listener.returnToLanding();
                }
                return true;
            }
        }
        return false;
    }

    private boolean handleMyTeamsClick(int mx, int my) {
        for (int i = 0; i < teamData.tabs.size() && i < teamTabBounds.length; i++) {
            if (teamTabBounds[i] != null && teamTabBounds[i].contains(mx, my)) {
                teamData.activeTabIndex = i;
                targetScrollY = 0f;
                showToast("Switched to tab: " + teamData.tabs.get(i));
                return true;
            }
        }

        if (mx >= teamSettingsBtnX && mx <= teamSettingsBtnX + teamSettingsBtnW &&
                my >= teamSettingsBtnY && my <= teamSettingsBtnY + teamSettingsBtnH) {
            activeNav = NavItem.SETTINGS;
            showToast("Opening workspace settings...");
            return true;
        }

        if (mx >= inviteMemberBtnX && mx <= inviteMemberBtnX + inviteMemberBtnW &&
                my >= inviteMemberBtnY && my <= inviteMemberBtnY + inviteMemberBtnH) {
            if (listener != null) {
                listener.openModal("Invite Member to " + teamData.breadcrumbs.replace("My Teams / ", "").replace(" / Workspace", ""),
                        "Enter student email or UW NetID to invite them.",
                        true);
            }
            return true;
        }

        if (mx >= viewRoadmapBtnX && mx <= viewRoadmapBtnX + viewRoadmapBtnW &&
                my >= viewRoadmapBtnY && my <= viewRoadmapBtnY + viewRoadmapBtnH) {
            showToast("Next milestone: " + teamData.nextMilestoneDate + " · " + teamData.nextMilestoneSub);
            return true;
        }

        if (mx >= openBoardBtnX && mx <= openBoardBtnX + openBoardBtnW &&
                my >= openBoardBtnY && my <= openBoardBtnY + openBoardBtnH) {
            teamData.activeTabIndex = 2;
            showToast("Opening Sprint Tasks Kanban Board (" + teamData.tasksTotal + " tasks)...");
            return true;
        }

        if ((mx >= reviewAllBtnX && mx <= reviewAllBtnX + reviewAllBtnW &&
                my >= reviewAllBtnY && my <= reviewAllBtnY + reviewAllBtnH) ||
                (mx >= reviewAppsBtnX && mx <= reviewAppsBtnX + reviewAppsBtnW &&
                        my >= reviewAppsBtnY && my <= reviewAppsBtnY + reviewAppsBtnH)) {
            teamData.activeTabIndex = 4;
            showToast("Viewing " + teamData.pendingRequestsCount + " pending candidate applications...");
            return true;
        }
        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        if (modals != null && modals.onMouseMove(mx, my)) {
            return true;
        }

        boolean hHov = header != null && header.onMouseMove(mx, my);
        boolean sHov = sidebar != null && sidebar.onMouseMove(mx, my, this);

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
            case OVERVIEW:
                vHov = overview.onMouseMove(mx, my);
                break;
            case MY_PROFILE:
                vHov = handleProfileMouseMove(mx, my);
                break;
            case SETTINGS:
                vHov = handleSettingsMouseMove(mx, my);
                break;
        }

        return hHov || sHov || vHov;
    }

    private boolean handleProfileMouseMove(int mx, int my) {
        boolean changed = false;
        boolean pEdit = editProfileHovered;
        editProfileHovered = editProfileBtnBox.contains(mx, my);
        if (editProfileHovered != pEdit)
            changed = true;

        boolean pShare = shareProfileHovered;
        shareProfileHovered = shareProfileBtnBox.contains(mx, my);
        if (shareProfileHovered != pShare)
            changed = true;

        int oldTabHov = hoveredProfileTab;
        hoveredProfileTab = -1;
        for (int i = 0; i < profileTabBounds.length; i++) {
            if (profileTabBounds[i] != null && profileTabBounds[i].contains(mx, my)) {
                hoveredProfileTab = i;
                break;
            }
        }
        if (hoveredProfileTab != oldTabHov)
            changed = true;

        boolean pComplete = completeProfileHovered;
        completeProfileHovered = completeProfileBtnBox.contains(mx, my);
        if (completeProfileHovered != pComplete)
            changed = true;

        int oldSkill = hoveredProfileSkill;
        hoveredProfileSkill = -1;
        for (int i = 0; i < profileSkillBounds.size(); i++) {
            if (profileSkillBounds.get(i).contains(mx, my)) {
                hoveredProfileSkill = i;
                break;
            }
        }
        if (hoveredProfileSkill != oldSkill)
            changed = true;

        int oldProject = hoveredProfileProject;
        hoveredProfileProject = -1;
        for (int i = 0; i < profileProjectBounds.size(); i++) {
            if (profileProjectBounds.get(i).contains(mx, my)) {
                hoveredProfileProject = i;
                break;
            }
        }
        if (hoveredProfileProject != oldProject)
            changed = true;
        // true = pointer over something clickable (drives the hand cursor)
        return changed || editProfileHovered || shareProfileHovered || hoveredProfileTab >= 0
                || completeProfileHovered || hoveredProfileSkill >= 0 || hoveredProfileProject >= 0;
    }

    private boolean handleSettingsMouseMove(int mx, int my) {
        boolean changed = false;
        int oldHov = hoveredSettingsTab;
        hoveredSettingsTab = -1;
        for (int i = 0; i < settingsTabBounds.length; i++) {
            if (settingsTabBounds[i] != null && settingsTabBounds[i].contains(mx, my)) {
                hoveredSettingsTab = i;
                break;
            }
        }
        if (hoveredSettingsTab != oldHov)
            changed = true;
        return changed;
    }

    private boolean handleMyTeamsMouseMove(int mx, int my) {
        boolean changed = false;
        boolean p1 = teamSettingsBtnHovered;
        teamSettingsBtnHovered = (mx >= teamSettingsBtnX && mx <= teamSettingsBtnX + teamSettingsBtnW &&
                my >= teamSettingsBtnY && my <= teamSettingsBtnY + teamSettingsBtnH);
        if (teamSettingsBtnHovered != p1)
            changed = true;

        boolean p3 = inviteMemberBtnHovered;
        inviteMemberBtnHovered = (mx >= inviteMemberBtnX && mx <= inviteMemberBtnX + inviteMemberBtnW &&
                my >= inviteMemberBtnY && my <= inviteMemberBtnY + inviteMemberBtnH);
        if (inviteMemberBtnHovered != p3)
            changed = true;

        boolean p4 = viewRoadmapBtnHovered;
        viewRoadmapBtnHovered = (mx >= viewRoadmapBtnX && mx <= viewRoadmapBtnX + viewRoadmapBtnW &&
                my >= viewRoadmapBtnY && my <= viewRoadmapBtnY + viewRoadmapBtnH);
        if (viewRoadmapBtnHovered != p4)
            changed = true;

        boolean p5 = openBoardBtnHovered;
        openBoardBtnHovered = (mx >= openBoardBtnX && mx <= openBoardBtnX + openBoardBtnW &&
                my >= openBoardBtnY && my <= openBoardBtnY + openBoardBtnH);
        if (openBoardBtnHovered != p5)
            changed = true;

        boolean p6 = reviewAllBtnHovered;
        reviewAllBtnHovered = (mx >= reviewAllBtnX && mx <= reviewAllBtnX + reviewAllBtnW &&
                my >= reviewAllBtnY && my <= reviewAllBtnY + reviewAllBtnH);
        if (reviewAllBtnHovered != p6)
            changed = true;

        boolean p7 = reviewAppsBtnHovered;
        reviewAppsBtnHovered = (mx >= reviewAppsBtnX && mx <= reviewAppsBtnX + reviewAppsBtnW &&
                my >= reviewAppsBtnY && my <= reviewAppsBtnY + reviewAppsBtnH);
        if (reviewAppsBtnHovered != p7)
            changed = true;

        int oldTabHov = hoveredTeamTabIndex;
        hoveredTeamTabIndex = -1;
        for (int i = 0; i < teamData.tabs.size() && i < teamTabBounds.length; i++) {
            if (teamTabBounds[i] != null && teamTabBounds[i].contains(mx, my)) {
                hoveredTeamTabIndex = i;
                break;
            }
        }
        if (hoveredTeamTabIndex != oldTabHov)
            changed = true;

        return changed || teamSettingsBtnHovered || inviteMemberBtnHovered;
    }

    public void onMouseWheel(int wheelRotation, int screenH) {
        // An open task board scrolls its own list instead of the page behind it
        if (activeNav == NavItem.MY_PROJECTS && myProjectsView.onMouseWheel(wheelRotation)) {
            return;
        }
        int maxScroll = Math.max(0, getCurrentContentHeight() - (screenH - 60));
        targetScrollY = Math.max(0, Math.min(targetScrollY + wheelRotation * 45, maxScroll));
    }

    public boolean onKey(KeyEvent e) {
        // Modal keys take priority
        if (modals != null && modals.onKey(e, this)) {
            return true;
        }

        // Header search keys
        if (header != null && header.onKey(e, this)) {
            return true;
        }

        // Active View keys (Search boxes etc.)
        switch (activeNav) {
            case MY_PROJECTS:
                if (myProjectsView.onKey(e))
                    return true;
                break;
            case FIND_TEAMMATES:
                if (teammatesView.onKey(e))
                    return true;
                break;
            case DISCOVER_PROJECTS:
                if (projectsView.onKey(e))
                    return true;
                break;
            case SETTINGS:
                return handleSettingsKey(e);
            default:
                break;
        }

        return false;
    }

    private boolean handleSettingsKey(KeyEvent e) {
        if (settingsData.focusedField != SettingsData.ActiveField.NONE) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (settingsData.focusedField == SettingsData.ActiveField.FULL_NAME
                        && settingsData.fullName.length() > 0) {
                    settingsData.fullName = settingsData.fullName.substring(0, settingsData.fullName.length() - 1);
                    settingsData.hasUnsavedChanges = true;
                    return true;
                }
                if (settingsData.focusedField == SettingsData.ActiveField.USERNAME
                        && settingsData.username.length() > 0) {
                    settingsData.username = settingsData.username.substring(0, settingsData.username.length() - 1);
                    settingsData.hasUnsavedChanges = true;
                    return true;
                }
                if (settingsData.focusedField == SettingsData.ActiveField.EMAIL && settingsData.email.length() > 0) {
                    settingsData.email = settingsData.email.substring(0, settingsData.email.length() - 1);
                    settingsData.hasUnsavedChanges = true;
                    return true;
                }
            }

            char c = e.getKeyChar();
            if (c >= 32 && c <= 126) {
                if (settingsData.focusedField == SettingsData.ActiveField.FULL_NAME
                        && settingsData.fullName.length() < 40) {
                    settingsData.fullName += c;
                    settingsData.hasUnsavedChanges = true;
                    return true;
                }
                if (settingsData.focusedField == SettingsData.ActiveField.USERNAME
                        && settingsData.username.length() < 30) {
                    settingsData.username += Character.toLowerCase(c);
                    settingsData.hasUnsavedChanges = true;
                    return true;
                }
                if (settingsData.focusedField == SettingsData.ActiveField.EMAIL && settingsData.email.length() < 50) {
                    settingsData.email += Character.toLowerCase(c);
                    settingsData.hasUnsavedChanges = true;
                    return true;
                }
            }
        }
        return false;
    }

    // Getters and Setters
    public NavItem getActiveNav() {
        return activeNav;
    }

    public void setActiveNav(NavItem nav) {
        this.activeNav = nav;
        this.targetScrollY = 0f;
        this.scrollY = 0f;
    }

    public NavItem getHoveredNav() {
        return hoveredNav;
    }

    public void setHoveredNav(NavItem nav) {
        this.hoveredNav = nav;
    }

    public int getActiveProjectsCount() {
        return activeProjectsCount;
    }

    public void setActiveProjectsCount(int c) {
        this.activeProjectsCount = c;
    }

    public int getMyTeamsCount() {
        return myTeamsCount;
    }

    public void setMyTeamsCount(int c) {
        this.myTeamsCount = c;
    }

    public int getPendingRequestsCount() {
        return pendingRequestsCount;
    }

    public void setPendingRequestsCount(int c) {
        this.pendingRequestsCount = c;
    }

    public int getTasksDueCount() {
        return tasksDueCount;
    }

    public void setTasksDueCount(int c) {
        this.tasksDueCount = c;
    }

    public int getProfileCompletePercent() {
        return profileCompletePercent;
    }

    public void setProfileCompletePercent(int p) {
        this.profileCompletePercent = p;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String n) {
        if (n == null) n = "";
        this.profileName = n;
        if (profileData != null) {
            profileData.name = n;
        }
        if (settingsData != null) {
            settingsData.fullName = n;
            settingsData.originalFullName = n;
        }
        // Avatar initials always follow the name (header, sidebar, profile)
        if (!n.trim().isEmpty()) {
            setProfileInitials(initialsFor(n));
        }
    }

    /**
     * Two-letter avatar initials for a display name. Mirrors server/src/utils/format.js initials():
     *  "Md Shakib Akando" → "MA" (first + last word), "Test Admin User 123" → "TU" (skips number-only words),
     *  "testPro" → "TP" (camelCase), "john_doe" / "john.doe" → "JD", "Prena" → "PR".
     */
    public static String initialsFor(String name) {
        if (name == null || name.trim().isEmpty()) return "TU";
        List<String> words = new ArrayList<String>();
        for (String w : name.trim().split("[\\s._-]+")) {
            if (!w.isEmpty()) words.add(w);
        }
        if (words.isEmpty()) return "TU";
        if (words.size() >= 2) {
            String last = words.get(words.size() - 1);
            for (int i = words.size() - 1; i > 0; i--) {
                if (Character.isLetter(words.get(i).charAt(0))) { last = words.get(i); break; }
            }
            return ("" + words.get(0).charAt(0) + last.charAt(0)).toUpperCase();
        }
        String w = words.get(0);
        for (int i = 1; i < w.length(); i++) {
            if (Character.isUpperCase(w.charAt(i)) && Character.isLowerCase(w.charAt(i - 1))) {
                return ("" + w.charAt(0) + w.charAt(i)).toUpperCase(); // camelCase: testPro → TP
            }
        }
        return w.substring(0, Math.min(2, w.length())).toUpperCase();
    }

    public String getProfileBio() {
        return profileBio;
    }

    public void setProfileBio(String b) {
        this.profileBio = b;
        if (profileData != null) {
            profileData.bio = b;
        }
    }

    public String getProfilePortfolio() {
        return profilePortfolio;
    }

    public void setProfilePortfolio(String u) {
        this.profilePortfolio = u;
        if (profileData != null) {
            profileData.portfolio = u;
        }
    }

    public String getProfileInitials() {
        return profileInitials;
    }

    public void setProfileInitials(String i) {
        this.profileInitials = i;
    }

    public String getProfileRole() {
        return profileRole;
    }

    public void setProfileRole(String r) {
        this.profileRole = r;
    }

    /** First name for the greeting ("Welcome back, Alex!"). */
    public String getFirstName() {
        String n = profileName == null ? "" : profileName.trim();
        int sp = n.indexOf(' ');
        return n.isEmpty() ? "there" : (sp > 0 ? n.substring(0, sp) : n);
    }

    /** Stat card subtitle from the backend, or the demo fallback when offline. */
    public String getStatSubtitle(int index, String fallback) {
        String[] subs = statSubtitles;
        return (subs != null && index >= 0 && index < subs.length && subs[index] != null) ? subs[index] : fallback;
    }

    public void setStatSubtitles(String[] subs) {
        this.statSubtitles = subs;
    }

    // =========================================================================
    // BACKEND SYNC
    // =========================================================================

    /** Queue work to run on the render thread before the next frame. Safe to call from any thread. */
    public void runOnUi(Runnable r) {
        if (r != null) uiQueue.add(r);
    }

    /** Fetch the logged-in user's dashboard from the API (no-op when offline / demo mode). */
    public void loadFromServer() {
        DashboardSync.load(this);
    }

    public boolean isServerDataLoaded() {
        return serverDataLoaded;
    }

    void markServerDataLoaded() {
        this.serverDataLoaded = true;
    }

    public ProjectItem getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(ProjectItem p) {
        this.selectedProject = p;
    }

    public TeammateItem getSelectedTeammate() {
        return selectedTeammate;
    }

    public void setSelectedTeammate(TeammateItem t) {
        this.selectedTeammate = t;
    }

    public List<ProjectItem> getProjects() {
        return projects;
    }

    public List<TeammateItem> getTeammates() {
        return teammates;
    }

    public List<TaskItem> getTasks() {
        return tasks;
    }

    public List<ActivityItem> getActivities() {
        return activities;
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public DashboardProjects getProjectsView() {
        return projectsView;
    }

    public DashboardTeammates getTeammatesView() {
        return teammatesView;
    }

    public DashboardMyProjects getMyProjectsView() {
        return myProjectsView;
    }

    public TeamWorkspaceData getTeamData() {
        return teamData;
    }

    public SettingsData getSettingsData() {
        return settingsData;
    }

    public ProfileData getProfileData() {
        return profileData;
    }
}

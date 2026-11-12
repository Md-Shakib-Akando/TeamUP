import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private final MessagesData messagesData = new MessagesData();

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

    public static class ProjectItem {
        public String title, author, desc;
        public String[] skills;
        public String footer;
        public boolean isGreen, bookmarked;
        public int type;
        public int hitX, hitY, hitW, hitH;
        public int bmX, bmY;

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
    private final List<String> chatHistory = new ArrayList<String>(Arrays.asList(
            "Priya Shah: Hey Alex, have you checked the new UI mockups for GreenRoute?",
            "Alex Morgan: Yes! Looks super clean, especially the route mapping cards.",
            "Priya Shah: Awesome! Let's sync today at 4 PM to finalize the schema."));

    // =========================================================================
    // HITBOXES - MY PROFILE
    // =========================================================================
    private final Rectangle editProfileBtnBox = new Rectangle();
    private boolean editProfileHovered = false;
    private final Rectangle shareProfileBtnBox = new Rectangle();
    private boolean shareProfileHovered = false;
    private final Rectangle profileStatusBadgeBox = new Rectangle();
    private boolean profileStatusBadgeHovered = false;
    private final Rectangle profileContactBox = new Rectangle();
    private boolean profileContactHovered = false;
    private final Rectangle[] profileStatBounds = new Rectangle[4];
    private int hoveredProfileStatIndex = -1;
    private final Rectangle[] profileTabBounds = new Rectangle[4];
    private int hoveredProfileTab = -1;
    private final List<Rectangle> profileSkillBounds = new ArrayList<Rectangle>();
    private final List<Rectangle> profileProjectBounds = new ArrayList<Rectangle>();
    private final Rectangle profileAddProjectBtnBox = new Rectangle();
    private boolean profileAddProjectHovered = false;
    private final Rectangle profileAddSkillBtnBox = new Rectangle();
    private boolean profileAddSkillHovered = false;
    private final Rectangle profileRequestReviewBtnBox = new Rectangle();
    private boolean profileRequestReviewHovered = false;

    // =========================================================================
    // HITBOXES - MESSAGES
    // =========================================================================
    private final List<Rectangle> convBounds = new ArrayList<Rectangle>();
    private int hoveredConvIndex = -1;
    private final Rectangle msgInputBox = new Rectangle();
    private final Rectangle msgSendBtnBox = new Rectangle();
    private boolean msgSendHovered = false;
    private final Rectangle videoCallBtnBox = new Rectangle();
    private boolean videoCallHovered = false;

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
    private int teamMsgBtnX, teamMsgBtnY, teamMsgBtnW, teamMsgBtnH;
    private boolean teamMsgBtnHovered = false;
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
    private final Rectangle[] kpiCardBounds = new Rectangle[4];
    private final Rectangle[] taskSummaryBounds = new Rectangle[4];
    private final List<Rectangle> milestoneItemBounds = new ArrayList<Rectangle>();

    // Tab 1: Members hitboxes
    private final Rectangle tabInviteMemberBtnBox = new Rectangle();
    private boolean tabInviteMemberHovered = false;
    private final Rectangle inviteTeammateSlotBox = new Rectangle();
    private boolean inviteTeammateSlotHovered = false;
    private final List<Rectangle> memberTabMsgBtns = new ArrayList<Rectangle>();
    private final List<Rectangle> memberTabProfileBtns = new ArrayList<Rectangle>();
    private final List<Rectangle> memberTabRemoveBtns = new ArrayList<Rectangle>();

    // Tab 2: Tasks hitboxes
    private final Rectangle tabAddTaskBtnBox = new Rectangle();
    private boolean tabAddTaskHovered = false;
    private final Rectangle tabOpenModalBtnBox = new Rectangle();
    private boolean tabOpenModalHovered = false;
    private final List<Rectangle> taskCardBounds = new ArrayList<Rectangle>();
    private final List<Rectangle> taskAdvanceBtns = new ArrayList<Rectangle>();

    // Tab 3: Files hitboxes
    private final Rectangle tabUploadFileBtnBox = new Rectangle();
    private boolean tabUploadFileHovered = false;
    private final List<Rectangle> fileRowBounds = new ArrayList<Rectangle>();
    private final List<Rectangle> fileDownloadBtns = new ArrayList<Rectangle>();
    private final List<Rectangle> filePreviewBtns = new ArrayList<Rectangle>();
    private final List<Rectangle> fileDeleteBtns = new ArrayList<Rectangle>();

    // Tab 4: Requests hitboxes
    private final Rectangle tabPostRoleBtnBox = new Rectangle();
    private boolean tabPostRoleHovered = false;
    private final List<Rectangle> reqAcceptBtns = new ArrayList<Rectangle>();
    private final List<Rectangle> reqDeclineBtns = new ArrayList<Rectangle>();
    private final List<Rectangle> reqMessageBtns = new ArrayList<Rectangle>();

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
        for (int i = 0; i < profileStatBounds.length; i++) profileStatBounds[i] = new Rectangle(0, 0, 0, 0);

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

    public String getToastMessage() {
        return toastMessage;
    }

    public void openModal(ActiveModal modal) {
        if (header != null)
            header.closePopups();
        if (modals != null)
            modals.open(modal);
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
            case MY_TEAMS:
                paintMyTeamsContent(cg, sideW, contentY, width - sideW, height);
                break;
            case MESSAGES:
                paintMessagesContent(cg, sideW, contentY, width - sideW, height);
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

        // 5. Scrollbar for Content (Hidden for Messages split view)
        if (activeNav != NavItem.MESSAGES) {
            paintContentScrollbar(g2, width - 8, headerH, 6, height - headerH);
        }

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
            case MY_TEAMS:
            case MESSAGES:
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
    // 1. MY PROFILE VIEW RENDERING (ALIGNED WITH LANDING PAGE PALETTE)
    // =========================================================================
    private void paintProfileContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = 36;
        int padY = 24;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        profileSkillBounds.clear();
        profileProjectBounds.clear();
        profileAddProjectBtnBox.setBounds(0, 0, 0, 0);
        profileAddSkillBtnBox.setBounds(0, 0, 0, 0);
        profileRequestReviewBtnBox.setBounds(0, 0, 0, 0);

        // Breadcrumb
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_LIGHT);
        g.drawString("Workspace / My Profile", startX + padX, curY + 14);
        curY += 26;

        // HERO PROFILE CARD
        int heroH = 220;
        RoundRectangle2D heroCard = new RoundRectangle2D.Float(startX + padX, curY, contentW, heroH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(heroCard);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.1f));
        g.draw(heroCard);

        // Cover banner strip: single accent tint, no gradient
        g.setColor(Theme.PASTEL_VIOLET);
        g.fill(new RoundRectangle2D.Float(startX + padX, curY, contentW, 64, 14, 14));
        g.fillRect(startX + padX, curY + 20, contentW, 44);

        // Profile Avatar Circle "AM" (Pastel Pink background with Brand Pink initials)
        int avD = 68;
        int avX = startX + padX + 24;
        int avY = curY + 30;
        g.setColor(Color.WHITE);
        g.fillOval(avX - 4, avY - 4, avD + 8, avD + 8);
        drawAvatar(g, avX + avD / 2, avY + avD / 2, avD / 2, "AM", Theme.PASTEL_PINK, BRAND_ACCENT);

        // Right Top Buttons: [✎ Edit profile] & [🔗 Share profile]
        int btnH = 32;
        int shareW = 104;
        int editW = 98;
        int btnY = curY + 76;

        shareProfileBtnBox.setBounds(startX + padX + contentW - shareW - 20, btnY, shareW, btnH);
        RoundRectangle2D shareBtn = new RoundRectangle2D.Float(shareProfileBtnBox.x, shareProfileBtnBox.y,
                shareProfileBtnBox.width, shareProfileBtnBox.height, 8, 8);
        g.setColor(shareProfileHovered ? BG_CANVAS : Color.WHITE);
        g.fill(shareBtn);
        g.setColor(BORDER_COLOR);
        g.draw(shareBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("🔗 Share profile", shareProfileBtnBox.x + 12, shareProfileBtnBox.y + 20);

        editProfileBtnBox.setBounds(shareProfileBtnBox.x - editW - 10, btnY, editW, btnH);
        RoundRectangle2D editBtn = new RoundRectangle2D.Float(editProfileBtnBox.x, editProfileBtnBox.y,
                editProfileBtnBox.width, editProfileBtnBox.height, 8, 8);
        g.setColor(editProfileHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(editBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString("✎ Edit profile", editProfileBtnBox.x + 14, editProfileBtnBox.y + 20);

        // Name, Handle & Status Badge
        int textX = avX + avD + 20;
        int infoY = curY + 74;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(TEXT_DARK);
        g.drawString(profileData.name, textX, infoY + 20);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        int nameW = g.getFontMetrics(new Font(Font.SANS_SERIF, Font.BOLD, 20)).stringWidth(profileData.name);
        g.drawString(profileData.handle, textX + nameW + 10, infoY + 20);

        // Status Badge Pill (Mint tint with green text - clickable to cycle availability)
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        int stTextW = g.getFontMetrics().stringWidth("● " + profileData.availabilityStatus);
        int stW = Math.max(220, stTextW + 24);
        int stH = 22;
        int stX = textX + nameW + 18;
        int stY = infoY + 5;
        profileStatusBadgeBox.setBounds(stX, stY, stW, stH);
        RoundRectangle2D stShape = new RoundRectangle2D.Float(stX, stY, stW, stH, stH, stH);
        g.setColor(profileStatusBadgeHovered ? new Color(0xCC, 0xF5, 0xE1) : SUCCESS_BG);
        g.fill(stShape);
        g.setColor(SUCCESS_BORDER);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(stShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g.setColor(SUCCESS_GREEN);
        g.drawString("● " + profileData.availabilityStatus, stX + 10, stY + 15);

        // Headline & Academic Info
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString(profileData.headline, textX, infoY + 42);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(profileData.university + " · " + profileData.department + " · " + profileData.gradYear, textX,
                infoY + 60);

        // Bio
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_DARK);
        g.drawString(profileData.bio, startX + padX + 24, curY + 146);

        // Contact info line (clickable)
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        String contactStr = profileData.location + "   ·   " + profileData.email + "   ·   " + profileData.github + "   ·   "
                + profileData.portfolio;
        int contactW = g.getFontMetrics().stringWidth(contactStr);
        int contactX = startX + padX + 24;
        int contactY = curY + 158;
        profileContactBox.setBounds(contactX, contactY, contactW, 20);
        g.setColor(profileContactHovered ? BRAND_ACCENT : TEXT_MUTED);
        g.drawString(contactStr, contactX, curY + 172);

        curY += heroH + 18;

        // 4 STAT CARDS ROW
        int statGap = 16;
        int statW = (contentW - statGap * 3) / 4;
        int statH = 92;

        profileStatBounds[0].setBounds(startX + padX, curY, statW, statH);
        paintProfileStatCard(g, startX + padX, curY, statW, statH, "Active Projects",
                String.valueOf(profileData.activeProjectsCount), "Across 3 collaborative teams", 0);

        profileStatBounds[1].setBounds(startX + padX + (statW + statGap), curY, statW, statH);
        paintProfileStatCard(g, startX + padX + (statW + statGap), curY, statW, statH, "Teammates",
                String.valueOf(profileData.totalTeammatesCount), "Collaborated with at UW", 1);

        profileStatBounds[2].setBounds(startX + padX + (statW + statGap) * 2, curY, statW, statH);
        paintProfileStatCard(g, startX + padX + (statW + statGap) * 2, curY, statW, statH, "On-Time Completion",
                profileData.completionRatePercent + "%", "Tasks delivered before deadline", 2);

        profileStatBounds[3].setBounds(startX + padX + (statW + statGap) * 3, curY, statW, statH);
        paintProfileStatCard(g, startX + padX + (statW + statGap) * 3, curY, statW, statH, "Peer Rating",
                profileData.peerRating + " ★", "From " + profileData.totalReviewsCount + " verified peer reviews", 3);

        curY += statH + 20;

        // PROFILE NAVIGATION TABS
        int tabX = startX + padX;
        int tabY = curY;
        for (int i = 0; i < profileData.tabs.size(); i++) {
            String tabText = profileData.tabs.get(i);
            boolean isActive = (i == profileData.activeTabIndex);
            boolean isHov = (i == hoveredProfileTab);

            g.setFont(new Font(Font.SANS_SERIF, isActive ? Font.BOLD : Font.PLAIN, 13));
            FontMetrics tfm = g.getFontMetrics();
            int tw = tfm.stringWidth(tabText);
            int totalTabW = tw + 16;

            if (i < profileTabBounds.length) {
                profileTabBounds[i].setBounds(tabX, tabY - 4, totalTabW, 30);
            }

            if (isActive) {
                g.setColor(BRAND_ACCENT);
                g.drawString(tabText, tabX + 8, tabY + 18);
                g.fill(new RoundRectangle2D.Float(tabX + 8, tabY + 24, tw, 3, 2, 2));
            } else {
                g.setColor(isHov ? TEXT_DARK : TEXT_MUTED);
                g.drawString(tabText, tabX + 8, tabY + 18);
            }
            tabX += totalTabW + 18;
        }

        g.setColor(BORDER_COLOR);
        g.drawLine(startX + padX, tabY + 26, startX + padX + contentW, tabY + 26);
        curY += 44;

        // TAB CONTENT
        if (profileData.activeTabIndex == 0) {
            int leftW = Math.round((contentW - 20) * 0.62f);
            int rightW = contentW - leftW - 20;
            int leftX = startX + padX;
            int rX = leftX + leftW + 20;

            int leftEndY = paintProfileProjectsList(g, leftX, curY, leftW);
            int rightEndY = paintProfileOverviewSidebar(g, rX, curY, rightW);
            totalContentHeight = Math.max(leftEndY, rightEndY) - startY + 60;
        } else if (profileData.activeTabIndex == 1) {
            int endY = paintProfileProjectsList(g, startX + padX, curY, contentW);
            totalContentHeight = endY - startY + 60;
        } else if (profileData.activeTabIndex == 2) {
            int endY = paintProfileSkillsFull(g, startX + padX, curY, contentW);
            totalContentHeight = endY - startY + 60;
        } else {
            int endY = paintProfileReviewsFull(g, startX + padX, curY, contentW);
            totalContentHeight = endY - startY + 60;
        }
    }

    private void paintProfileStatCard(Graphics2D g, int x, int y, int w, int h, String title, String val, String sub,
            int type) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        boolean isHov = (hoveredProfileStatIndex == type);
        g.setColor(isHov ? BG_CANVAS : Color.WHITE);
        g.fill(shape);
        g.setColor(isHov ? BRAND_ACCENT : BORDER_COLOR);
        g.setStroke(new BasicStroke(isHov ? 1.4f : 1.1f));
        g.draw(shape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(title, x + 16, y + 22);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(type == 3 ? Theme.HIGHLIGHT_WARM : TEXT_DARK);
        g.drawString(val, x + 16, y + 54);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(TEXT_MUTED);
        g.drawString(sub, x + 16, y + 76);
    }

    private int paintProfileProjectsList(Graphics2D g, int x, int y, int w) {
        int curY = y;
        if (profileData.activeTabIndex == 1) {
            int topH = 34;
            int btnW = 124;
            int btnH = 30;
            int btnX = x + w - btnW;
            int btnY = curY;
            profileAddProjectBtnBox.setBounds(btnX, btnY, btnW, btnH);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            g.setColor(TEXT_DARK);
            g.drawString("All Projects (" + profileData.projects.size() + ")", x, curY + 20);

            RoundRectangle2D addBtn = new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 8, 8);
            g.setColor(profileAddProjectHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
            g.fill(addBtn);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(Color.WHITE);
            g.drawString("+ Create Project", btnX + 14, btnY + 19);

            curY += topH + 12;
        } else {
            profileAddProjectBtnBox.setBounds(0, 0, 0, 0);
        }

        for (int i = 0; i < profileData.projects.size(); i++) {
            ProfileData.ProfileProject p = profileData.projects.get(i);
            int cardH = 138;
            RoundRectangle2D card = new RoundRectangle2D.Float(x, curY, w, cardH, 12, 12);
            g.setColor(Color.WHITE);
            g.fill(card);
            g.setColor(BORDER_COLOR);
            g.draw(card);

            profileProjectBounds.add(new Rectangle(x, curY, w, cardH));

            g.setColor(p.accentColor);
            g.fill(new RoundRectangle2D.Float(x, curY + 16, 4, cardH - 32, 2, 2));

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g.setColor(TEXT_DARK);
            g.drawString(p.title, x + 18, curY + 28);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            int tw = g.getFontMetrics().stringWidth(p.status);
            int badgeW = tw + 16;
            int badgeX = x + w - badgeW - 16;
            RoundRectangle2D bShape = new RoundRectangle2D.Float(badgeX, curY + 16, badgeW, 20, 6, 6);
            g.setColor(BRAND_TINT_BG);
            g.fill(bShape);
            g.setColor(BRAND_ACCENT);
            g.drawString(p.status, badgeX + 8, curY + 30);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(p.accentColor);
            g.drawString(p.role, x + 18, curY + 46);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString(p.description, x + 18, curY + 68);

            int tagX = x + 18;
            int tagY = curY + 86;
            for (String tag : p.tags) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                int stw = g.getFontMetrics().stringWidth(tag);
                RoundRectangle2D tagShape = new RoundRectangle2D.Float(tagX, tagY, stw + 14, 20, 6, 6);
                g.setColor(BG_CANVAS);
                g.fill(tagShape);
                g.setColor(BORDER_COLOR);
                g.draw(tagShape);
                g.setColor(TEXT_MUTED);
                g.drawString(tag, tagX + 7, tagY + 14);
                tagX += stw + 20;
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g.setColor(TEXT_LIGHT);
            g.drawString("Team: " + p.teamSize + " / " + p.maxTeamSize + " members", x + w - 120, curY + 100);

            curY += cardH + 14;
        }
        return curY;
    }

    private int paintProfileOverviewSidebar(Graphics2D g, int x, int y, int w) {
        int curY = y;

        int card1H = 220;
        RoundRectangle2D card1 = new RoundRectangle2D.Float(x, curY, w, card1H, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card1);
        g.setColor(BORDER_COLOR);
        g.draw(card1);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Endorsed Skills", x + 16, curY + 26);

        int chipX = x + 16;
        int chipY = curY + 42;
        for (int i = 0; i < Math.min(6, profileData.skills.size()); i++) {
            ProfileData.ProfileSkill s = profileData.skills.get(i);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            String text = s.name + " (" + s.endorsements + (s.userEndorsed ? " ✓" : "") + ")";
            int tw = g.getFontMetrics().stringWidth(text);
            int chipW = tw + 18;

            if (chipX + chipW > x + w - 16) {
                chipX = x + 16;
                chipY += 30;
            }

            Rectangle b = new Rectangle(chipX, chipY, chipW, 22);
            profileSkillBounds.add(b);

            RoundRectangle2D chip = new RoundRectangle2D.Float(chipX, chipY, chipW, 22, 6, 6);
            g.setColor(s.userEndorsed ? BRAND_TINT_BG : BG_CANVAS);
            g.fill(chip);
            g.setColor(s.userEndorsed ? BRAND_ACCENT : BORDER_COLOR);
            g.draw(chip);
            g.setColor(s.userEndorsed ? BRAND_ACCENT : TEXT_DARK);
            g.drawString(text, chipX + 9, chipY + 15);

            chipX += chipW + 8;
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(TEXT_LIGHT);
        g.drawString("Click any skill to endorse Alex", x + 16, curY + card1H - 14);

        curY += card1H + 16;

        int card2H = 180;
        RoundRectangle2D card2 = new RoundRectangle2D.Float(x, curY, w, card2H, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card2);
        g.setColor(BORDER_COLOR);
        g.draw(card2);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Top Peer Feedback", x + 16, curY + 26);

        if (!profileData.reviews.isEmpty()) {
            ProfileData.PeerReview rev = profileData.reviews.get(0);
            drawAvatar(g, x + 30, curY + 54, 14, rev.reviewerInitials, rev.avatarBg, rev.avatarFg);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(TEXT_DARK);
            g.drawString(rev.reviewerName + " (" + rev.reviewerRole + ")", x + 52, curY + 52);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g.setColor(TEXT_MUTED);
            g.drawString(rev.projectName + " · " + rev.date, x + 52, curY + 66);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_DARK);
            g.drawString("\"" + rev.feedback + "\"", x + 16, curY + 92);

            g.setColor(Theme.HIGHLIGHT_WARM);
            g.drawString("★★★★★  5.0 Peer Rating", x + 16, curY + 154);
        }

        return curY + card2H + 20;
    }

    private int paintProfileSkillsFull(Graphics2D g, int x, int y, int w) {
        int neededH = 110 + ((profileData.skills.size() + 1) / 2) * 44 + 30;
        int cardH = Math.max(340, neededH);
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, cardH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(BORDER_COLOR);
        g.draw(card);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT_DARK);
        g.drawString("Skills Matrix & Peer Endorsements", x + 24, y + 32);

        int btnW = 104;
        int btnH = 28;
        int btnX = x + w - btnW - 24;
        int btnY = y + 18;
        profileAddSkillBtnBox.setBounds(btnX, btnY, btnW, btnH);

        RoundRectangle2D addSkBtn = new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 8, 8);
        g.setColor(profileAddSkillHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(addSkBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString("+ Add Skill", btnX + 18, btnY + 18);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("All skills are verified through team project commits and teammate endorsements.", x + 24, y + 54);

        int curY = y + 80;
        for (int i = 0; i < profileData.skills.size(); i++) {
            ProfileData.ProfileSkill s = profileData.skills.get(i);
            int rowX = x + 24 + (i % 2) * (w / 2);
            int rowY = curY + (i / 2) * 44;

            RoundRectangle2D skCard = new RoundRectangle2D.Float(rowX, rowY, w / 2 - 36, 36, 8, 8);
            g.setColor(s.userEndorsed ? BRAND_TINT_BG : BG_CANVAS);
            g.fill(skCard);
            g.setColor(s.userEndorsed ? BRAND_ACCENT : BORDER_COLOR);
            g.draw(skCard);

            profileSkillBounds.add(new Rectangle(rowX, rowY, w / 2 - 36, 36));

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(TEXT_DARK);
            g.drawString(s.name, rowX + 12, rowY + 22);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString(s.category, rowX + 110, rowY + 22);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(s.userEndorsed ? BRAND_ACCENT : Theme.ACCENT_VIOLET);
            g.drawString(s.endorsements + " endorsements" + (s.userEndorsed ? " ✓" : " +"), rowX + w / 2 - 160,
                    rowY + 22);
        }
        return y + cardH + 20;
    }

    private int paintProfileReviewsFull(Graphics2D g, int x, int y, int w) {
        int curY = y;
        int topH = 34;
        int btnW = 130;
        int btnH = 30;
        int btnX = x + w - btnW;
        int btnY = curY;
        profileRequestReviewBtnBox.setBounds(btnX, btnY, btnW, btnH);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Teammate Endorsements & Feedback (" + profileData.reviews.size() + ")", x, curY + 20);

        RoundRectangle2D reqBtn = new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 8, 8);
        g.setColor(profileRequestReviewHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(reqBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString("+ Request Review", btnX + 14, btnY + 19);

        curY += topH + 14;

        for (ProfileData.PeerReview rev : profileData.reviews) {
            int cardH = 110;
            RoundRectangle2D card = new RoundRectangle2D.Float(x, curY, w, cardH, 12, 12);
            g.setColor(Color.WHITE);
            g.fill(card);
            g.setColor(BORDER_COLOR);
            g.draw(card);

            drawAvatar(g, x + 34, curY + 36, 16, rev.reviewerInitials, rev.avatarBg, rev.avatarFg);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g.setColor(TEXT_DARK);
            g.drawString(rev.reviewerName + "  ·  " + rev.reviewerRole, x + 60, curY + 30);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString("Project: " + rev.projectName + "  ·  " + rev.date, x + 60, curY + 46);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(TEXT_DARK);
            g.drawString("\"" + rev.feedback + "\"", x + 60, curY + 76);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(Theme.HIGHLIGHT_WARM);
            g.drawString("★★★★★ 5.0", x + w - 110, curY + 30);

            curY += cardH + 14;
        }
        return curY;
    }

    // =========================================================================
    // 2. MESSAGES VIEW RENDERING (ALIGNED WITH LANDING PAGE PALETTE)
    // =========================================================================
    private void paintMessagesContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = 28;
        int curY = startY + 20;
        int contentW = width - padX * 2;
        int contentH = height - 80;

        convBounds.clear();

        RoundRectangle2D mainCard = new RoundRectangle2D.Float(startX + padX, curY, contentW, contentH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(mainCard);
        g.setColor(BORDER_COLOR);
        g.draw(mainCard);

        int leftPaneW = 280;
        int rightPaneW = contentW - leftPaneW;
        int paneX = startX + padX;

        g.setColor(BORDER_COLOR);
        g.drawLine(paneX + leftPaneW, curY, paneX + leftPaneW, curY + contentH);

        // LEFT PANE: CONVERSATIONS LIST
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT_DARK);
        g.drawString("Messages", paneX + 16, curY + 28);

        int unreadTotal = messagesData.getTotalUnreadCount();
        if (unreadTotal > 0) {
            RoundRectangle2D unreadPill = new RoundRectangle2D.Float(paneX + 104, curY + 14, 22, 18, 18, 18);
            g.setColor(BRAND_TINT_BG);
            g.fill(unreadPill);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.setColor(BRAND_ACCENT);
            g.drawString(String.valueOf(unreadTotal), paneX + 111, curY + 27);
        }

        int convY = curY + 48;
        int convItemH = 58;

        for (int i = 0; i < messagesData.conversations.size(); i++) {
            MessagesData.Conversation c = messagesData.conversations.get(i);
            boolean isSel = (i == messagesData.activeConvIndex);
            boolean isHov = (i == hoveredConvIndex);

            Rectangle itemBox = new Rectangle(paneX + 8, convY, leftPaneW - 16, convItemH);
            convBounds.add(itemBox);

            RoundRectangle2D itemShape = new RoundRectangle2D.Float(itemBox.x, itemBox.y, itemBox.width, itemBox.height,
                    8, 8);
            if (isSel) {
                g.setColor(BRAND_TINT_BG);
                g.fill(itemShape);
            } else if (isHov) {
                g.setColor(BG_CANVAS);
                g.fill(itemShape);
            }

            drawAvatar(g, itemBox.x + 22, itemBox.y + 29, 16, c.initials, c.avatarBg, c.avatarFg);
            if (c.isOnline && !c.isChannel) {
                g.setColor(SUCCESS_GREEN);
                g.fillOval(itemBox.x + 32, itemBox.y + 36, 7, 7);
                g.setColor(Color.WHITE);
                g.drawOval(itemBox.x + 32, itemBox.y + 36, 7, 7);
            }

            g.setFont(new Font(Font.SANS_SERIF, isSel ? Font.BOLD : Font.PLAIN, 12));
            g.setColor(isSel ? BRAND_ACCENT : TEXT_DARK);
            g.drawString(c.title, itemBox.x + 46, itemBox.y + 24);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g.setColor(TEXT_LIGHT);
            g.drawString(c.lastTime, itemBox.x + itemBox.width - 48, itemBox.y + 24);

            g.setColor(TEXT_MUTED);
            String prev = c.lastPreview;
            if (prev.length() > 28)
                prev = prev.substring(0, 26) + "...";
            g.drawString(prev, itemBox.x + 46, itemBox.y + 42);

            if (c.unreadCount > 0) {
                RoundRectangle2D uPill = new RoundRectangle2D.Float(itemBox.x + itemBox.width - 24, itemBox.y + 32, 16,
                        16, 16, 16);
                g.setColor(BRAND_ACCENT);
                g.fill(uPill);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
                g.setColor(Color.WHITE);
                g.drawString(String.valueOf(c.unreadCount), itemBox.x + itemBox.width - 19, itemBox.y + 44);
            }

            convY += convItemH + 4;
        }

        // RIGHT PANE: ACTIVE CHAT VIEW
        MessagesData.Conversation activeConv = messagesData.getActiveConversation();
        int chatX = paneX + leftPaneW;

        int chatHeaderH = 56;
        g.setColor(Color.WHITE);
        g.fillRect(chatX + 1, curY + 1, rightPaneW - 2, chatHeaderH);
        g.setColor(BORDER_COLOR);
        g.drawLine(chatX, curY + chatHeaderH, chatX + rightPaneW, curY + chatHeaderH);

        drawAvatar(g, chatX + 30, curY + 28, 16, activeConv.initials, activeConv.avatarBg, activeConv.avatarFg);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(TEXT_DARK);
        g.drawString(activeConv.title, chatX + 54, curY + 24);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(activeConv.subtitle, chatX + 54, curY + 40);

        videoCallBtnBox.setBounds(chatX + rightPaneW - 110, curY + 14, 94, 28);
        RoundRectangle2D callBtn = new RoundRectangle2D.Float(videoCallBtnBox.x, videoCallBtnBox.y,
                videoCallBtnBox.width, videoCallBtnBox.height, 6, 6);
        g.setColor(videoCallHovered ? BG_CANVAS : Color.WHITE);
        g.fill(callBtn);
        g.setColor(BORDER_COLOR);
        g.draw(callBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("📹 Start Call", videoCallBtnBox.x + 10, videoCallBtnBox.y + 18);

        int historyY = curY + chatHeaderH + 16;
        int composerH = 54;
        int maxHistoryH = contentH - chatHeaderH - composerH - 24;

        List<MessagesData.ChatMessage> msgList = messagesData.getActiveMessages();
        int curMsgY = historyY;

        for (MessagesData.ChatMessage msg : msgList) {
            if (curMsgY > historyY + maxHistoryH)
                break;

            if (msg.isOutgoing) {
                // Outgoing Bubble (Right side in signature Brand Hot Pink #EA4C89)
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                FontMetrics mfm = g.getFontMetrics();
                int textW = mfm.stringWidth(msg.text);
                int bubbleW = Math.min(rightPaneW - 100, Math.max(80, textW + 28));
                int bubbleH = 34;
                int bubbleX = chatX + rightPaneW - bubbleW - 24;

                RoundRectangle2D outBubble = new RoundRectangle2D.Float(bubbleX, curMsgY, bubbleW, bubbleH, 12, 12);
                g.setColor(BRAND_ACCENT);
                g.fill(outBubble);

                g.setColor(Color.WHITE);
                g.drawString(msg.text, bubbleX + 14, curMsgY + 21);

                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
                g.setColor(TEXT_LIGHT);
                g.drawString(msg.time, bubbleX + bubbleW - 48, curMsgY + bubbleH + 11);

                curMsgY += bubbleH + 18;
            } else {
                // Incoming Bubble (Left side)
                drawAvatar(g, chatX + 28, curMsgY + 16, 14, msg.senderInitials, msg.avatarBg, msg.avatarFg);

                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g.setColor(TEXT_DARK);
                g.drawString(msg.senderName, chatX + 50, curMsgY + 12);

                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
                g.setColor(TEXT_LIGHT);
                g.drawString(msg.time, chatX + 56 + g.getFontMetrics().stringWidth(msg.senderName), curMsgY + 12);

                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                FontMetrics mfm = g.getFontMetrics();
                int textW = mfm.stringWidth(msg.text);
                int bubbleW = Math.min(rightPaneW - 120, Math.max(80, textW + 28));
                int bubbleH = 34;
                int bubbleX = chatX + 50;

                RoundRectangle2D inBubble = new RoundRectangle2D.Float(bubbleX, curMsgY + 18, bubbleW, bubbleH, 12, 12);
                g.setColor(BG_CANVAS);
                g.fill(inBubble);
                g.setColor(BORDER_COLOR);
                g.draw(inBubble);

                g.setColor(TEXT_DARK);
                g.drawString(msg.text, bubbleX + 14, curMsgY + 39);

                curMsgY += bubbleH + 28;
            }
        }

        // Chat Composer Input Bar at Bottom
        int compY = curY + contentH - composerH;
        g.setColor(Color.WHITE);
        g.fillRect(chatX + 1, compY, rightPaneW - 2, composerH - 1);
        g.setColor(BORDER_COLOR);
        g.drawLine(chatX, compY, chatX + rightPaneW, compY);

        msgInputBox.setBounds(chatX + 16, compY + 9, rightPaneW - 84, 36);
        RoundRectangle2D inShape = new RoundRectangle2D.Float(msgInputBox.x, msgInputBox.y, msgInputBox.width,
                msgInputBox.height, 8, 8);
        g.setColor(messagesData.inputFocused ? Color.WHITE : BG_CANVAS);
        g.fill(inShape);
        g.setColor(messagesData.inputFocused ? BRAND_ACCENT : BORDER_COLOR);
        g.draw(inShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_LIGHT);
        g.drawString("📎", msgInputBox.x + 10, msgInputBox.y + 23);

        if (messagesData.inputDraft.isEmpty()) {
            g.setColor(TEXT_LIGHT);
            g.drawString("Type a message to " + activeConv.title + " (Press Enter to send)...", msgInputBox.x + 32,
                    msgInputBox.y + 22);
        } else {
            g.setColor(TEXT_DARK);
            g.drawString(messagesData.inputDraft, msgInputBox.x + 32, msgInputBox.y + 22);

            if (messagesData.inputFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
                int dw = g.getFontMetrics().stringWidth(messagesData.inputDraft);
                g.setColor(BRAND_ACCENT);
                g.drawLine(msgInputBox.x + 34 + dw, msgInputBox.y + 10, msgInputBox.x + 34 + dw, msgInputBox.y + 26);
            }
        }

        // Send Button (➤) in signature Brand Hot Pink #EA4C89
        msgSendBtnBox.setBounds(chatX + rightPaneW - 56, compY + 9, 40, 36);
        RoundRectangle2D sendBtn = new RoundRectangle2D.Float(msgSendBtnBox.x, msgSendBtnBox.y, msgSendBtnBox.width,
                msgSendBtnBox.height, 8, 8);
        g.setColor(msgSendHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(sendBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("➤", msgSendBtnBox.x + 13, msgSendBtnBox.y + 23);

        totalContentHeight = height;
    }

    // =========================================================================
    // 3. SETTINGS VIEW RENDERING (ALIGNED WITH LANDING PAGE PALETTE)
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
        g.drawString("Updated from Alex Morgan", fnX, fnY + inputH + 14);

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
        int btnW1 = 128;
        int btnW2 = 128;

        teamMsgBtnW = btnW1;
        teamMsgBtnH = btnH;
        teamMsgBtnX = startX + padX + contentW - teamMsgBtnW;
        teamMsgBtnY = curY + 4;

        teamSettingsBtnW = btnW2;
        teamSettingsBtnH = btnH;
        teamSettingsBtnX = teamMsgBtnX - teamSettingsBtnW - 12;
        teamSettingsBtnY = teamMsgBtnY;

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

        // [Message team] Solid Button in signature Brand Hot Pink #EA4C89
        RoundRectangle2D msgBtnShape = new RoundRectangle2D.Float(teamMsgBtnX, teamMsgBtnY, teamMsgBtnW, teamMsgBtnH,
                10, 10);
        g.setColor(teamMsgBtnHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(msgBtnShape);

        drawMessageBubbleIcon(g, teamMsgBtnX + 14, teamMsgBtnY + 12, 14, Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.drawString("Message team", teamMsgBtnX + 34, teamMsgBtnY + 22);

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

        if (teamData.activeTabIndex == 0) {
            int kpiGap = 16;
            int kpiW = (contentW - kpiGap * 3) / 4;
            int kpiH = 108;

            kpiCardBounds[0] = new Rectangle(startX + padX, curY, kpiW, kpiH);
            kpiCardBounds[1] = new Rectangle(startX + padX + (kpiW + kpiGap), curY, kpiW, kpiH);
            kpiCardBounds[2] = new Rectangle(startX + padX + (kpiW + kpiGap) * 2, curY, kpiW, kpiH);
            kpiCardBounds[3] = new Rectangle(startX + padX + (kpiW + kpiGap) * 3, curY, kpiW, kpiH);

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
        } else if (teamData.activeTabIndex == 1) {
            int endY = paintMyTeamsMembersTab(g, startX + padX, curY, contentW);
            totalContentHeight = endY - startY + 50;
        } else if (teamData.activeTabIndex == 2) {
            int endY = paintMyTeamsTasksTab(g, startX + padX, curY, contentW);
            totalContentHeight = endY - startY + 50;
        } else if (teamData.activeTabIndex == 3) {
            int endY = paintMyTeamsFilesTab(g, startX + padX, curY, contentW);
            totalContentHeight = endY - startY + 50;
        } else {
            int endY = paintMyTeamsRequestsTab(g, startX + padX, curY, contentW);
            totalContentHeight = endY - startY + 50;
        }
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

        milestoneItemBounds.clear();
        for (int i = 0; i < teamData.milestones.size(); i++) {
            milestoneItemBounds.add(new Rectangle(x + 16, itemY + i * rowH, w - 32, rowH));
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

        taskSummaryBounds[0] = new Rectangle(x + 20, statY, colW, 60);
        taskSummaryBounds[1] = new Rectangle(x + 20 + colW, statY, colW, 60);
        taskSummaryBounds[2] = new Rectangle(x + 20 + colW * 2, statY, colW, 60);
        taskSummaryBounds[3] = new Rectangle(x + 20 + colW * 3, statY, colW, 60);

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

    private void drawFileIcon(Graphics2D g, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(x + 2, y + 1, size - 4, size - 2, 3, 3);
        g.drawLine(x + 5, y + 6, x + size - 5, y + 6);
        g.drawLine(x + 5, y + 10, x + size - 5, y + 10);
    }

    private void openChatWithPerson(String name) {
        if (messagesData == null || messagesData.conversations.isEmpty()) {
            activeNav = NavItem.MESSAGES;
            showToast("Opening messages view...");
            return;
        }
        int found = -1;
        String s = name.trim().toLowerCase();
        for (int i = 0; i < messagesData.conversations.size(); i++) {
            MessagesData.Conversation c = messagesData.conversations.get(i);
            if (c.title != null && c.title.toLowerCase().contains(s)) {
                found = i;
                break;
            }
        }
        if (found >= 0) {
            messagesData.selectConversation(found);
            activeNav = NavItem.MESSAGES;
            showToast("Opened chat with " + name);
        } else {
            messagesData.selectConversation(0);
            activeNav = NavItem.MESSAGES;
            showToast("Opened CampusConnect chat for " + name);
        }
    }

    private int paintMyTeamsMembersTab(Graphics2D g, int x, int y, int w) {
        memberTabMsgBtns.clear();
        memberTabProfileBtns.clear();
        memberTabRemoveBtns.clear();

        int curY = y;

        // Top Action Bar
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(TEXT_DARK);
        g.drawString("Team Members (" + teamData.members.size() + " of " + teamData.maxMembers + ")", x, curY + 22);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Collaborate with your project team, assign permissions, and invite new students.", x, curY + 42);

        // "+ Invite Member" Button
        int invW = 136;
        int invH = 34;
        tabInviteMemberBtnBox.setBounds(x + w - invW, curY + 12, invW, invH);
        RoundRectangle2D invShape = new RoundRectangle2D.Float(tabInviteMemberBtnBox.x, tabInviteMemberBtnBox.y, invW, invH, 8, 8);
        g.setColor(tabInviteMemberHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(invShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.drawString("+ Invite Member", tabInviteMemberBtnBox.x + 18, tabInviteMemberBtnBox.y + 22);

        curY += 60;

        // Grid of member cards (2 cards per row)
        int gap = 16;
        int cardW = (w - gap) / 2;
        int cardH = 132;

        for (int i = 0; i < teamData.members.size(); i++) {
            TeamWorkspaceData.TeamMember m = teamData.members.get(i);
            int col = i % 2;
            int row = i / 2;
            int cx = x + col * (cardW + gap);
            int cy = curY + row * (cardH + gap);

            RoundRectangle2D cardShape = new RoundRectangle2D.Float(cx, cy, cardW, cardH, 12, 12);
            g.setColor(Color.WHITE);
            g.fill(cardShape);
            g.setColor(BORDER_COLOR);
            g.draw(cardShape);

            // Avatar
            drawAvatar(g, cx + 38, cy + 38, 20, m.initials, m.avatarBg, m.avatarFg);

            // Name
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g.setColor(TEXT_DARK);
            g.drawString(m.name, cx + 70, cy + 32);

            // Role
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(TEXT_MUTED);
            g.drawString(m.role, cx + 70, cy + 50);

            // Badge ("You" or "Member" or "Lead")
            boolean isYou = "You".equalsIgnoreCase(m.badge);
            int badgeW = isYou ? 42 : 58;
            int badgeH = 20;
            int bx = cx + cardW - badgeW - 16;
            int by = cy + 18;
            RoundRectangle2D bShape = new RoundRectangle2D.Float(bx, by, badgeW, badgeH, 6, 6);
            g.setColor(isYou ? BRAND_TINT_BG : BG_CANVAS);
            g.fill(bShape);
            g.setColor(isYou ? BRAND_TINT_BORDER : BORDER_COLOR);
            g.draw(bShape);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.setColor(isYou ? BRAND_ACCENT : TEXT_MUTED);
            int tw = g.getFontMetrics().stringWidth(m.badge);
            g.drawString(m.badge, bx + (badgeW - tw) / 2, by + 14);

            // Action Buttons at bottom of card
            int btnY = cy + 82;
            int btnH2 = 28;

            // [Message] Button
            int msgW = 86;
            Rectangle msgBox = new Rectangle(cx + 18, btnY, msgW, btnH2);
            memberTabMsgBtns.add(msgBox);
            g.setColor(BRAND_TINT_BG);
            g.fill(new RoundRectangle2D.Float(msgBox.x, msgBox.y, msgBox.width, msgBox.height, 6, 6));
            g.setColor(BRAND_ACCENT);
            g.draw(new RoundRectangle2D.Float(msgBox.x, msgBox.y, msgBox.width, msgBox.height, 6, 6));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.drawString("Message", msgBox.x + 18, msgBox.y + 18);

            // [View Profile] Button
            int profW = 92;
            Rectangle profBox = new Rectangle(cx + 18 + msgW + 10, btnY, profW, btnH2);
            memberTabProfileBtns.add(profBox);
            g.setColor(BG_CANVAS);
            g.fill(new RoundRectangle2D.Float(profBox.x, profBox.y, profBox.width, profBox.height, 6, 6));
            g.setColor(BORDER_COLOR);
            g.draw(new RoundRectangle2D.Float(profBox.x, profBox.y, profBox.width, profBox.height, 6, 6));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(TEXT_DARK);
            g.drawString("View Profile", profBox.x + 14, profBox.y + 18);

            // [Remove] Button (only for non-You members)
            if (!isYou) {
                int remW = 68;
                Rectangle remBox = new Rectangle(cx + cardW - remW - 16, btnY, remW, btnH2);
                memberTabRemoveBtns.add(remBox);
                g.setColor(new Color(0xFF, 0xF1, 0xF0));
                g.fill(new RoundRectangle2D.Float(remBox.x, remBox.y, remBox.width, remBox.height, 6, 6));
                g.setColor(new Color(0xFF, 0xCC, 0xC7));
                g.draw(new RoundRectangle2D.Float(remBox.x, remBox.y, remBox.width, remBox.height, 6, 6));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g.setColor(new Color(0xCF, 0x13, 0x22));
                g.drawString("Remove", remBox.x + 12, remBox.y + 18);
            } else {
                memberTabRemoveBtns.add(new Rectangle());
            }
        }

        // Dashed "+ Invite Teammate" Slot
        int totalMembers = teamData.members.size();
        int slotCol = totalMembers % 2;
        int slotRow = totalMembers / 2;
        int slotX = x + slotCol * (cardW + gap);
        int slotY = curY + slotRow * (cardH + gap);
        inviteTeammateSlotBox.setBounds(slotX, slotY, cardW, cardH);

        RoundRectangle2D slotShape = new RoundRectangle2D.Float(slotX, slotY, cardW, cardH, 12, 12);
        g.setColor(inviteTeammateSlotHovered ? BG_CANVAS : Color.WHITE);
        g.fill(slotShape);
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 4.0f}, 0.0f));
        g.setColor(BRAND_TINT_BORDER);
        g.draw(slotShape);
        g.setStroke(oldStroke);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(BRAND_ACCENT);
        g.drawString("+", slotX + cardW / 2 - 6, slotY + 54);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        String sText = "Invite another teammate";
        int stw = g.getFontMetrics().stringWidth(sText);
        g.drawString(sText, slotX + (cardW - stw) / 2, slotY + 80);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        String subText = (teamData.maxMembers - totalMembers) + " open spots remaining";
        int subw = g.getFontMetrics().stringWidth(subText);
        g.drawString(subText, slotX + (cardW - subw) / 2, slotY + 98);

        int rows = (totalMembers + 1 + 1) / 2;
        return curY + rows * (cardH + gap) + 30;
    }

    private int paintMyTeamsTasksTab(Graphics2D g, int x, int y, int w) {
        taskCardBounds.clear();
        taskAdvanceBtns.clear();

        int curY = y;

        // Top Action Bar
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(TEXT_DARK);
        g.drawString("Sprint Tasks & Kanban Board (" + teamData.tasksTotal + " tasks)", x, curY + 22);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Click any task card or status arrow to advance it to the next workflow state.", x, curY + 42);

        // [Open Modal Board] Button
        int openW = 142;
        int btnH = 34;
        tabOpenModalBtnBox.setBounds(x + w - openW - 128, curY + 12, openW, btnH);
        RoundRectangle2D omShape = new RoundRectangle2D.Float(tabOpenModalBtnBox.x, tabOpenModalBtnBox.y, openW, btnH, 8, 8);
        g.setColor(tabOpenModalHovered ? BG_CANVAS : Color.WHITE);
        g.fill(omShape);
        g.setColor(BORDER_COLOR);
        g.draw(omShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Interactive Modal ↗", tabOpenModalBtnBox.x + 14, tabOpenModalBtnBox.y + 22);

        // [+ Add Task] Button
        int addW = 116;
        tabAddTaskBtnBox.setBounds(x + w - addW, curY + 12, addW, btnH);
        RoundRectangle2D addShape = new RoundRectangle2D.Float(tabAddTaskBtnBox.x, tabAddTaskBtnBox.y, addW, btnH, 8, 8);
        g.setColor(tabAddTaskHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(addShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.drawString("+ Add Task", tabAddTaskBtnBox.x + 22, tabAddTaskBtnBox.y + 22);

        curY += 60;

        // 4 Kanban Columns: To Do, In Progress, In Review, Done
        String[] colTitles = {"To Do (" + teamData.taskSummary.todo + ")",
                              "In Progress (" + teamData.taskSummary.inProgress + ")",
                              "In Review (" + teamData.taskSummary.inReview + ")",
                              "Done (" + teamData.taskSummary.done + ")"};
        String[] colKeys = {"todo", "in_progress", "in_review", "done"};
        Color[] colHeaderAccents = {new Color(0x64, 0x74, 0x8B), BRAND_ACCENT, Theme.HIGHLIGHT_WARM, SUCCESS_GREEN};

        int colGap = 14;
        int colW = (w - colGap * 3) / 4;

        int maxBottomY = curY;

        for (int c = 0; c < 4; c++) {
            int colX = x + c * (colW + colGap);
            int colTopY = curY;

            // Column Header
            RoundRectangle2D colHeader = new RoundRectangle2D.Float(colX, colTopY, colW, 36, 8, 8);
            g.setColor(Color.WHITE);
            g.fill(colHeader);
            g.setColor(BORDER_COLOR);
            g.draw(colHeader);

            g.setColor(colHeaderAccents[c]);
            g.fillOval(colX + 12, colTopY + 14, 8, 8);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(TEXT_DARK);
            g.drawString(colTitles[c], colX + 26, colTopY + 23);

            int itemY = colTopY + 46;

            for (int i = 0; i < teamData.tasks.size(); i++) {
                TeamWorkspaceData.WorkspaceTask t = teamData.tasks.get(i);
                if (!colKeys[c].equalsIgnoreCase(t.status)) continue;

                int tCardH = 88;
                Rectangle tBox = new Rectangle(colX, itemY, colW, tCardH);
                taskCardBounds.add(tBox);

                RoundRectangle2D tShape = new RoundRectangle2D.Float(tBox.x, tBox.y, tBox.width, tBox.height, 10, 10);
                g.setColor(Color.WHITE);
                g.fill(tShape);
                g.setColor(BORDER_COLOR);
                g.draw(tShape);

                // Title
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                g.setColor(TEXT_DARK);
                String drawTitle = t.title;
                if (g.getFontMetrics().stringWidth(drawTitle) > colW - 24) {
                    drawTitle = drawTitle.substring(0, Math.min(20, drawTitle.length())) + "...";
                }
                g.drawString(drawTitle, colX + 12, itemY + 22);

                // Assignee & Due
                drawAvatar(g, colX + 22, itemY + 54, 11, t.assigneeInitials, t.avatarBg, t.avatarFg);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                g.setColor(TEXT_MUTED);
                g.drawString(t.assignee, colX + 38, itemY + 58);

                // Due badge
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                g.setColor("done".equalsIgnoreCase(t.status) ? SUCCESS_GREEN : TEXT_MUTED);
                g.drawString(t.due, colX + 12, itemY + 76);

                // Status cycle / Advance Action Button
                int advW = 28;
                int advH = 22;
                Rectangle advBox = new Rectangle(colX + colW - advW - 10, itemY + tCardH - advH - 8, advW, advH);
                taskAdvanceBtns.add(advBox);

                RoundRectangle2D advShape = new RoundRectangle2D.Float(advBox.x, advBox.y, advBox.width, advBox.height, 6, 6);
                g.setColor(BRAND_TINT_BG);
                g.fill(advShape);
                g.setColor(BRAND_TINT_BORDER);
                g.draw(advShape);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                g.setColor(BRAND_ACCENT);
                String arrow = "done".equalsIgnoreCase(t.status) ? "✓" : "→";
                g.drawString(arrow, advBox.x + 9, advBox.y + 16);

                itemY += tCardH + 12;
            }

            maxBottomY = Math.max(maxBottomY, itemY);
        }

        return maxBottomY + 30;
    }

    private int paintMyTeamsFilesTab(Graphics2D g, int x, int y, int w) {
        fileRowBounds.clear();
        fileDownloadBtns.clear();
        filePreviewBtns.clear();
        fileDeleteBtns.clear();

        int curY = y;

        // Top Action Bar
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(TEXT_DARK);
        g.drawString("Workspace Files & Deliverables (" + teamData.files.size() + " files)", x, curY + 22);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Shared Figma wireframes, OpenAPI specifications, architecture docs, and project assets.", x, curY + 42);

        // [+ Upload File] Button
        int upW = 120;
        int btnH = 34;
        tabUploadFileBtnBox.setBounds(x + w - upW, curY + 12, upW, btnH);
        RoundRectangle2D upShape = new RoundRectangle2D.Float(tabUploadFileBtnBox.x, tabUploadFileBtnBox.y, upW, btnH, 8, 8);
        g.setColor(tabUploadFileHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(upShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.drawString("+ Upload File", tabUploadFileBtnBox.x + 18, tabUploadFileBtnBox.y + 22);

        curY += 60;

        // Table Card Container
        int rowH = 58;
        int totalH = 42 + Math.max(1, teamData.files.size()) * rowH;
        RoundRectangle2D tableCard = new RoundRectangle2D.Float(x, curY, w, totalH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(tableCard);
        g.setColor(BORDER_COLOR);
        g.draw(tableCard);

        // Table Header
        g.setColor(BG_CANVAS);
        g.fillRect(x + 1, curY + 1, w - 2, 40);
        g.setColor(BORDER_COLOR);
        g.drawLine(x, curY + 41, x + w, curY + 41);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("FILE NAME", x + 24, curY + 26);
        g.drawString("TYPE", x + 340, curY + 26);
        g.drawString("SIZE", x + 440, curY + 26);
        g.drawString("UPLOADED", x + 530, curY + 26);
        g.drawString("AUTHOR", x + 650, curY + 26);
        g.drawString("ACTIONS", x + w - 220, curY + 26);

        int itemY = curY + 42;

        for (int i = 0; i < teamData.files.size(); i++) {
            TeamWorkspaceData.WorkspaceFile f = teamData.files.get(i);
            int rowCenterY = itemY + rowH / 2;

            fileRowBounds.add(new Rectangle(x, itemY, w, rowH));

            if (i > 0) {
                g.setColor(BORDER_LIGHT);
                g.drawLine(x + 16, itemY, x + w - 16, itemY);
            }

            // File icon + name
            drawFileIcon(g, x + 24, rowCenterY - 10, 18, BRAND_ACCENT);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g.setColor(TEXT_DARK);
            g.drawString(f.name, x + 52, rowCenterY + 4);

            // Type
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(TEXT_MUTED);
            g.drawString(f.type, x + 340, rowCenterY + 4);

            // Size
            g.drawString(f.size, x + 440, rowCenterY + 4);

            // Uploaded
            g.drawString(f.updatedAgo, x + 530, rowCenterY + 4);

            // Author
            g.drawString(f.author, x + 650, rowCenterY + 4);

            // Action Buttons: [Download] [Preview] [Delete]
            int aBtnY = rowCenterY - 14;
            int aBtnH = 28;

            // Download
            int dlW = 76;
            Rectangle dlBox = new Rectangle(x + w - 230, aBtnY, dlW, aBtnH);
            fileDownloadBtns.add(dlBox);
            g.setColor(BRAND_TINT_BG);
            g.fill(new RoundRectangle2D.Float(dlBox.x, dlBox.y, dlBox.width, dlBox.height, 6, 6));
            g.setColor(BRAND_ACCENT);
            g.draw(new RoundRectangle2D.Float(dlBox.x, dlBox.y, dlBox.width, dlBox.height, 6, 6));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.drawString("Download", dlBox.x + 12, dlBox.y + 18);

            // Preview
            int pvW = 68;
            Rectangle pvBox = new Rectangle(x + w - 146, aBtnY, pvW, aBtnH);
            filePreviewBtns.add(pvBox);
            g.setColor(BG_CANVAS);
            g.fill(new RoundRectangle2D.Float(pvBox.x, pvBox.y, pvBox.width, pvBox.height, 6, 6));
            g.setColor(BORDER_COLOR);
            g.draw(new RoundRectangle2D.Float(pvBox.x, pvBox.y, pvBox.width, pvBox.height, 6, 6));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(TEXT_DARK);
            g.drawString("Preview", pvBox.x + 14, pvBox.y + 18);

            // Delete
            int delW = 60;
            Rectangle delBox = new Rectangle(x + w - 70, aBtnY, delW, aBtnH);
            fileDeleteBtns.add(delBox);
            g.setColor(new Color(0xFF, 0xF1, 0xF0));
            g.fill(new RoundRectangle2D.Float(delBox.x, delBox.y, delBox.width, delBox.height, 6, 6));
            g.setColor(new Color(0xFF, 0xCC, 0xC7));
            g.draw(new RoundRectangle2D.Float(delBox.x, delBox.y, delBox.width, delBox.height, 6, 6));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(new Color(0xCF, 0x13, 0x22));
            g.drawString("Delete", delBox.x + 12, delBox.y + 18);

            itemY += rowH;
        }

        return curY + totalH + 30;
    }

    private int paintMyTeamsRequestsTab(Graphics2D g, int x, int y, int w) {
        reqAcceptBtns.clear();
        reqDeclineBtns.clear();
        reqMessageBtns.clear();

        int curY = y;

        // Top Action Bar
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(TEXT_DARK);
        g.drawString("Candidate Applications & Recruitment (" + teamData.recruitmentRequests.size() + " pending)", x, curY + 22);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Review students requesting to join CampusConnect. Accept to onboard them immediately.", x, curY + 42);

        // [+ Post Open Position] Button
        int postW = 162;
        int btnH = 34;
        tabPostRoleBtnBox.setBounds(x + w - postW, curY + 12, postW, btnH);
        RoundRectangle2D postShape = new RoundRectangle2D.Float(tabPostRoleBtnBox.x, tabPostRoleBtnBox.y, postW, btnH, 8, 8);
        g.setColor(tabPostRoleHovered ? BRAND_ACCENT_HOVER : BRAND_ACCENT);
        g.fill(postShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.drawString("+ Post Open Position", tabPostRoleBtnBox.x + 16, tabPostRoleBtnBox.y + 22);

        curY += 60;

        if (teamData.recruitmentRequests.isEmpty()) {
            RoundRectangle2D emptyCard = new RoundRectangle2D.Float(x, curY, w, 140, 12, 12);
            g.setColor(Color.WHITE);
            g.fill(emptyCard);
            g.setColor(BORDER_COLOR);
            g.draw(emptyCard);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g.setColor(TEXT_DARK);
            g.drawString("All candidate applications have been reviewed! 🎉", x + 30, curY + 54);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            g.setColor(TEXT_MUTED);
            g.drawString("Post a new role to recruit more talent from the UW student network.", x + 30, curY + 84);

            return curY + 170;
        }

        int cardH = 118;
        int gap = 16;

        for (int i = 0; i < teamData.recruitmentRequests.size(); i++) {
            TeamWorkspaceData.RecruitmentRequest req = teamData.recruitmentRequests.get(i);
            int cardY = curY + i * (cardH + gap);

            RoundRectangle2D cardShape = new RoundRectangle2D.Float(x, cardY, w, cardH, 12, 12);
            g.setColor(Color.WHITE);
            g.fill(cardShape);
            g.setColor(BORDER_COLOR);
            g.draw(cardShape);

            // Avatar
            drawAvatar(g, x + 40, cardY + 40, 20, req.initials, req.avatarBg, req.avatarFg);

            // Name
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            g.setColor(TEXT_DARK);
            g.drawString(req.name, x + 76, cardY + 34);

            // Role & Skill
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(TEXT_MUTED);
            g.drawString(req.roleSkill, x + 76, cardY + 54);

            // Note / pitch
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_LIGHT);
            String pitch = "Applicant note: 'Excited to contribute to CampusConnect and collaborate with full-stack developers.'";
            g.drawString(pitch, x + 76, cardY + 76);

            // Action Buttons: [Accept] (Solid Green) [Decline] (Outline) [Message] (Pink)
            int aBtnY = cardY + cardH / 2 - 16;
            int aBtnH = 32;

            // [Accept]
            int accW = 86;
            Rectangle accBox = new Rectangle(x + w - 280, aBtnY, accW, aBtnH);
            reqAcceptBtns.add(accBox);
            RoundRectangle2D accShape = new RoundRectangle2D.Float(accBox.x, accBox.y, accBox.width, accBox.height, 8, 8);
            g.setColor(SUCCESS_GREEN);
            g.fill(accShape);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(Color.WHITE);
            g.drawString("Accept ✓", accBox.x + 14, accBox.y + 21);

            // [Decline]
            int decW = 80;
            Rectangle decBox = new Rectangle(x + w - 186, aBtnY, decW, aBtnH);
            reqDeclineBtns.add(decBox);
            RoundRectangle2D decShape = new RoundRectangle2D.Float(decBox.x, decBox.y, decBox.width, decBox.height, 8, 8);
            g.setColor(BG_CANVAS);
            g.fill(decShape);
            g.setColor(BORDER_COLOR);
            g.draw(decShape);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(TEXT_MUTED);
            g.drawString("Decline", decBox.x + 18, decBox.y + 21);

            // [Message]
            int msgW = 92;
            Rectangle msgBox = new Rectangle(x + w - 98, aBtnY, msgW, aBtnH);
            reqMessageBtns.add(msgBox);
            RoundRectangle2D mShape = new RoundRectangle2D.Float(msgBox.x, msgBox.y, msgBox.width, msgBox.height, 8, 8);
            g.setColor(BRAND_TINT_BG);
            g.fill(mShape);
            g.setColor(BRAND_TINT_BORDER);
            g.draw(mShape);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(BRAND_ACCENT);
            g.drawString("Message 💬", msgBox.x + 12, msgBox.y + 21);
        }

        return curY + teamData.recruitmentRequests.size() * (cardH + gap) + 30;
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
        if (my >= 60 && header != null) {
            header.setSearchFocused(false);
        }
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
            case MESSAGES:
                if (handleMessagesClick(mx, my))
                    return true;
                break;
            case SETTINGS:
                if (handleSettingsClick(mx, my))
                    return true;
                break;
            case MY_TEAMS:
                if (handleMyTeamsClick(mx, my))
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
            if (listener != null) {
                listener.openModal("Edit Profile - Alex Morgan",
                        "Update your academic details, availability, headline, and bio.", false);
            }
            openModal(ActiveModal.COMPLETE_PROFILE);
            showToast("Opening Edit Profile...");
            return true;
        }

        if (shareProfileBtnBox.contains(mx, my)) {
            showToast("Profile link copied: teamup.com/u/alexmorgan");
            return true;
        }

        if (profileStatusBadgeBox.contains(mx, my)) {
            profileData.cycleAvailability();
            showToast("Status updated: " + profileData.availabilityStatus);
            return true;
        }

        if (profileContactBox.contains(mx, my)) {
            showToast("Contact: " + profileData.email + " | " + profileData.github);
            return true;
        }

        for (int i = 0; i < profileStatBounds.length; i++) {
            if (profileStatBounds[i] != null && profileStatBounds[i].contains(mx, my)) {
                if (i == 0) {
                    profileData.activeTabIndex = 1;
                    targetScrollY = 0f;
                    showToast("Showing Active Projects (" + profileData.activeProjectsCount + ")");
                } else if (i == 1) {
                    setActiveNav(NavItem.FIND_TEAMMATES);
                    showToast("Explore Teammates at University of Washington");
                } else if (i == 2) {
                    setActiveNav(NavItem.MY_TEAMS);
                    showToast("Opening My Teams tasks & performance");
                } else if (i == 3) {
                    profileData.activeTabIndex = 3;
                    targetScrollY = 0f;
                    showToast("Showing Peer Ratings & Reviews (" + profileData.totalReviewsCount + ")");
                }
                return true;
            }
        }

        if (profileData.activeTabIndex == 1 && profileAddProjectBtnBox.contains(mx, my)) {
            openModal(ActiveModal.CREATE_PROJECT);
            showToast("Create a new project...");
            return true;
        }

        if (profileData.activeTabIndex == 2 && profileAddSkillBtnBox.contains(mx, my)) {
            String added = profileData.addNextSkill();
            showToast("Added skill: " + added);
            return true;
        }

        if (profileData.activeTabIndex == 3 && profileRequestReviewBtnBox.contains(mx, my)) {
            profileData.addReview("Sarah Connor", "SC", "Teammate", "DevOps Pipeline",
                    "Exceptional work on automation pipelines and team collaboration!", 5);
            showToast("Peer review request sent and recorded!");
            return true;
        }

        for (int i = 0; i < profileSkillBounds.size(); i++) {
            if (profileSkillBounds.get(i).contains(mx, my)) {
                profileData.toggleEndorsement(i % profileData.skills.size());
                ProfileData.ProfileSkill s = profileData.skills.get(i % profileData.skills.size());
                showToast(s.userEndorsed ? ("Endorsed " + s.name + " (" + s.endorsements + ")")
                        : ("Removed endorsement for " + s.name));
                return true;
            }
        }

        for (int i = 0; i < profileProjectBounds.size(); i++) {
            if (profileProjectBounds.get(i).contains(mx, my)) {
                ProfileData.ProfileProject p = profileData.projects.get(i % profileData.projects.size());
                showToast("Opening workspace for " + p.title + "...");
                return true;
            }
        }
        return false;
    }

    private boolean handleMessagesClick(int mx, int my) {
        for (int i = 0; i < convBounds.size(); i++) {
            if (convBounds.get(i).contains(mx, my)) {
                messagesData.selectConversation(i);
                showToast("Opened: " + messagesData.getActiveConversation().title);
                return true;
            }
        }

        if (msgInputBox.contains(mx, my)) {
            messagesData.inputFocused = true;
            return true;
        } else {
            messagesData.inputFocused = false;
        }

        if (msgSendBtnBox.contains(mx, my)) {
            messagesData.sendCurrentDraft();
            showToast("Message sent to " + messagesData.getActiveConversation().title);
            return true;
        }

        if (videoCallBtnBox.contains(mx, my)) {
            if (listener != null) {
                listener.openModal("Start Video Meeting",
                        "Connect via UW Zoom or WebRTC with " + messagesData.getActiveConversation().title + ".",
                        false);
            }
            return true;
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
                showToast("Account details updated successfully!");
                return true;
            }
            if (cancelAccountBtnBox.contains(mx, my)) {
                settingsData.cancelAccountChanges();
                showToast("Account changes discarded.");
                return true;
            }
            if (changePassBtnBox.contains(mx, my)) {
                if (listener != null) {
                    listener.openModal("Update Account Password",
                            "Enter your current password and choose a secure new password.", false);
                }
                showToast("Password reset link sent to " + settingsData.email);
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
                showToast("Privacy preferences saved!");
                return true;
            }
            if (cancelPrivacyBtnBox.contains(mx, my)) {
                settingsData.resetPrivacy();
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
                settingsData.addNextCategory();
                String added = settingsData.categories.get(settingsData.categories.size() - 1);
                showToast("Added '" + added + "' category!");
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
                showToast("Collaboration preferences saved!");
                return true;
            }
            if (cancelCollabBtnBox.contains(mx, my)) {
                settingsData.resetCollabPreferences();
                showToast("Collaboration preferences reset.");
                return true;
            }
        }

        if (settingsData.activeTab == SettingsData.SettingsTab.DANGER_ZONE) {
            if (deactivateBtnBox.contains(mx, my)) {
                if (listener != null) {
                    listener.openModal("Deactivate TeamUp Account",
                            "Take a break without deleting projects. You can reactivate anytime.", false);
                }
                showToast("Deactivation confirmation sent to " + settingsData.email);
                return true;
            }
            if (deleteAccountBtnBox.contains(mx, my)) {
                if (listener != null) {
                    listener.openModal("Delete Account Permanently",
                            "Warning: You own '" + settingsData.ownedProject + "'. Transfer ownership before deleting.",
                            true);
                }
                showToast("Warning: Please transfer '" + settingsData.ownedProject + "' before deleting account.");
                return true;
            }
        }
        return false;
    }

    private boolean handleMyTeamsClick(int mx, int my) {
        // 1. Navigation Tabs across the workspace
        for (int i = 0; i < teamData.tabs.size() && i < teamTabBounds.length; i++) {
            if (teamTabBounds[i] != null && teamTabBounds[i].contains(mx, my)) {
                teamData.activeTabIndex = i;
                targetScrollY = 0f;
                showToast("Switched to tab: " + teamData.tabs.get(i));
                return true;
            }
        }

        // 2. Header action buttons
        if (mx >= teamSettingsBtnX && mx <= teamSettingsBtnX + teamSettingsBtnW &&
                my >= teamSettingsBtnY && my <= teamSettingsBtnY + teamSettingsBtnH) {
            activeNav = NavItem.SETTINGS;
            showToast("Opening workspace settings...");
            return true;
        }

        if (mx >= teamMsgBtnX && mx <= teamMsgBtnX + teamMsgBtnW &&
                my >= teamMsgBtnY && my <= teamMsgBtnY + teamMsgBtnH) {
            messagesData.selectConversation(0);
            activeNav = NavItem.MESSAGES;
            showToast("Opening CampusConnect team chat: #campusconnect-general");
            return true;
        }

        // 3. Tab-specific interactive buttons
        if (teamData.activeTabIndex == 0) {
            // TAB 0: OVERVIEW
            if (mx >= inviteMemberBtnX && mx <= inviteMemberBtnX + inviteMemberBtnW &&
                    my >= inviteMemberBtnY && my <= inviteMemberBtnY + inviteMemberBtnH) {
                if (listener != null) {
                    listener.openModal("Invite Member to CampusConnect", "Enter student email or UW NetID to invite them.",
                            true);
                } else {
                    showToast("Invite member to CampusConnect");
                }
                return true;
            }

            if (mx >= viewRoadmapBtnX && mx <= viewRoadmapBtnX + viewRoadmapBtnW &&
                    my >= viewRoadmapBtnY && my <= viewRoadmapBtnY + viewRoadmapBtnH) {
                if (listener != null) {
                    listener.openModal("CampusConnect Q4 Roadmap",
                            "Milestones:\n• Research & design (100% · Completed Oct 12)\n• MVP development (68% · Due Nov 06)\n• Beta testing & launch (Upcoming · Due Nov 20)",
                            false);
                }
                showToast("CampusConnect Q4 Roadmap: MVP Nov 06, Beta Launch Nov 20.");
                return true;
            }

            if (mx >= openBoardBtnX && mx <= openBoardBtnX + openBoardBtnW &&
                    my >= openBoardBtnY && my <= openBoardBtnY + openBoardBtnH) {
                teamData.activeTabIndex = 2; // Tasks
                targetScrollY = 0f;
                showToast("Opening Sprint Tasks Kanban Board (28 tasks)...");
                return true;
            }

            if ((mx >= reviewAllBtnX && mx <= reviewAllBtnX + reviewAllBtnW &&
                    my >= reviewAllBtnY && my <= reviewAllBtnY + reviewAllBtnH) ||
                    (mx >= reviewAppsBtnX && mx <= reviewAppsBtnX + reviewAppsBtnW &&
                            my >= reviewAppsBtnY && my <= reviewAppsBtnY + reviewAppsBtnH)) {
                teamData.activeTabIndex = 4; // Requests
                targetScrollY = 0f;
                showToast("Viewing pending candidate applications...");
                return true;
            }

            // Member cards click
            for (int i = 0; i < memberCardBounds.size() && i < teamData.members.size(); i++) {
                if (memberCardBounds.get(i).contains(mx, my)) {
                    TeamWorkspaceData.TeamMember m = teamData.members.get(i);
                    if ("You".equalsIgnoreCase(m.badge)) {
                        activeNav = NavItem.MY_PROFILE;
                        showToast("Opened your profile - Alex Morgan");
                    } else {
                        openChatWithPerson(m.name);
                    }
                    return true;
                }
            }

            // Recruitment candidate rows click
            for (int i = 0; i < requestItemBounds.size() && i < teamData.recruitmentRequests.size(); i++) {
                if (requestItemBounds.get(i).contains(mx, my)) {
                    teamData.activeTabIndex = 4; // Requests
                    targetScrollY = 0f;
                    showToast("Viewing application from " + teamData.recruitmentRequests.get(i).name);
                    return true;
                }
            }

            // KPI Cards click
            for (int i = 0; i < kpiCardBounds.length; i++) {
                if (kpiCardBounds[i] != null && kpiCardBounds[i].contains(mx, my)) {
                    if (i == 0 || i == 3) {
                        teamData.activeTabIndex = 2; // Tasks
                        targetScrollY = 0f;
                        showToast("Viewing Tasks: " + (i == 0 ? "Project progress" : "Tasks due soon"));
                    } else if (i == 1) {
                        teamData.activeTabIndex = 1; // Members
                        targetScrollY = 0f;
                        showToast("Viewing Team Members (" + teamData.members.size() + ")");
                    } else if (i == 2) {
                        if (listener != null) {
                            listener.openModal("CampusConnect Q4 Roadmap",
                                    "Next Milestone: MVP development · Nov 06 (21 days left)", false);
                        }
                        showToast("Next milestone: MVP development · Nov 06");
                    }
                    return true;
                }
            }

            // Task summary columns click
            for (int i = 0; i < taskSummaryBounds.length; i++) {
                if (taskSummaryBounds[i] != null && taskSummaryBounds[i].contains(mx, my)) {
                    teamData.activeTabIndex = 2; // Tasks
                    targetScrollY = 0f;
                    showToast("Opening Sprint Tasks Kanban Board...");
                    return true;
                }
            }

            // Milestone items click
            for (int i = 0; i < milestoneItemBounds.size() && i < teamData.milestones.size(); i++) {
                if (milestoneItemBounds.get(i).contains(mx, my)) {
                    TeamWorkspaceData.MilestoneItem m = teamData.milestones.get(i);
                    showToast("Milestone: " + m.title + " (" + m.progressPercent + "% - " + m.dueInfo + ")");
                    return true;
                }
            }
        } else if (teamData.activeTabIndex == 1) {
            // TAB 1: MEMBERS
            if (tabInviteMemberBtnBox.contains(mx, my) || inviteTeammateSlotBox.contains(mx, my)) {
                if (listener != null) {
                    listener.openModal("Invite Member to CampusConnect", "Enter student email or UW NetID to invite them.", true);
                }
                showToast("Opening invite member dialog...");
                return true;
            }

            for (int i = 0; i < memberTabMsgBtns.size() && i < teamData.members.size(); i++) {
                if (memberTabMsgBtns.get(i).contains(mx, my)) {
                    TeamWorkspaceData.TeamMember m = teamData.members.get(i);
                    openChatWithPerson(m.name);
                    return true;
                }
            }

            for (int i = 0; i < memberTabProfileBtns.size() && i < teamData.members.size(); i++) {
                if (memberTabProfileBtns.get(i).contains(mx, my)) {
                    TeamWorkspaceData.TeamMember m = teamData.members.get(i);
                    if ("You".equalsIgnoreCase(m.badge)) {
                        activeNav = NavItem.MY_PROFILE;
                        showToast("Viewing your profile - Alex Morgan");
                    } else {
                        if (listener != null) {
                            listener.openModal("Profile - " + m.name, "Role: " + m.role + "\nCampusConnect Team Member", false);
                        }
                        showToast("Viewing profile of " + m.name);
                    }
                    return true;
                }
            }

            for (int i = 0; i < memberTabRemoveBtns.size() && i < teamData.members.size(); i++) {
                if (memberTabRemoveBtns.get(i).contains(mx, my)) {
                    TeamWorkspaceData.TeamMember m = teamData.members.get(i);
                    String name = m.name;
                    teamData.removeMember(i);
                    showToast("Removed " + name + " from CampusConnect team.");
                    return true;
                }
            }
        } else if (teamData.activeTabIndex == 2) {
            // TAB 2: TASKS
            if (tabAddTaskBtnBox.contains(mx, my)) {
                teamData.addTask("Implement User Feedback Drawer", "Alex Morgan", "AM", Theme.PASTEL_PINK, Theme.BRAND_PINK, "todo", "Today");
                showToast("Added new task: Implement User Feedback Drawer");
                return true;
            }

            if (tabOpenModalBtnBox.contains(mx, my)) {
                openModal(ActiveModal.ALL_TASKS);
                showToast("Opened interactive Sprint Tasks modal!");
                return true;
            }

            for (int i = 0; i < taskAdvanceBtns.size() && i < teamData.tasks.size(); i++) {
                if (taskAdvanceBtns.get(i).contains(mx, my) || (i < taskCardBounds.size() && taskCardBounds.get(i).contains(mx, my))) {
                    TeamWorkspaceData.WorkspaceTask t = teamData.tasks.get(i);
                    teamData.advanceTaskStatus(t);
                    showToast("Updated '" + t.title + "' status to: " + t.status.replace('_', ' ').toUpperCase());
                    return true;
                }
            }
        } else if (teamData.activeTabIndex == 3) {
            // TAB 3: FILES
            if (tabUploadFileBtnBox.contains(mx, my)) {
                teamData.addFile("architecture-diagram-v2.png", "PNG", "3.4 MB", "Alex Morgan");
                showToast("File uploaded: architecture-diagram-v2.png");
                return true;
            }

            for (int i = 0; i < fileDownloadBtns.size() && i < teamData.files.size(); i++) {
                if (fileDownloadBtns.get(i).contains(mx, my)) {
                    showToast("Downloading " + teamData.files.get(i).name + "...");
                    return true;
                }
            }

            for (int i = 0; i < filePreviewBtns.size() && i < teamData.files.size(); i++) {
                if (filePreviewBtns.get(i).contains(mx, my)) {
                    TeamWorkspaceData.WorkspaceFile f = teamData.files.get(i);
                    if (listener != null) {
                        listener.openModal("Preview - " + f.name, "File Type: " + f.type + "\nSize: " + f.size + "\nAuthor: " + f.author, false);
                    }
                    showToast("Opening preview: " + f.name);
                    return true;
                }
            }

            for (int i = 0; i < fileDeleteBtns.size() && i < teamData.files.size(); i++) {
                if (fileDeleteBtns.get(i).contains(mx, my)) {
                    String fName = teamData.files.get(i).name;
                    teamData.removeFile(i);
                    showToast("Deleted " + fName);
                    return true;
                }
            }
        } else if (teamData.activeTabIndex == 4) {
            // TAB 4: REQUESTS
            if (tabPostRoleBtnBox.contains(mx, my)) {
                teamData.openPositions++;
                showToast("Posted new position: Mobile Developer (Flutter) · Open spots: " + teamData.openPositions);
                return true;
            }

            for (int i = 0; i < reqAcceptBtns.size() && i < teamData.recruitmentRequests.size(); i++) {
                if (reqAcceptBtns.get(i).contains(mx, my)) {
                    String name = teamData.recruitmentRequests.get(i).name;
                    teamData.acceptRequest(i);
                    showToast("Accepted " + name + " into CampusConnect team! 🎉");
                    return true;
                }
            }

            for (int i = 0; i < reqDeclineBtns.size() && i < teamData.recruitmentRequests.size(); i++) {
                if (reqDeclineBtns.get(i).contains(mx, my)) {
                    String name = teamData.recruitmentRequests.get(i).name;
                    teamData.declineRequest(i);
                    showToast("Declined application from " + name + ".");
                    return true;
                }
            }

            for (int i = 0; i < reqMessageBtns.size() && i < teamData.recruitmentRequests.size(); i++) {
                if (reqMessageBtns.get(i).contains(mx, my)) {
                    String name = teamData.recruitmentRequests.get(i).name;
                    openChatWithPerson(name);
                    return true;
                }
            }
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
            case MESSAGES:
                vHov = handleMessagesMouseMove(mx, my);
                break;
            case SETTINGS:
                vHov = handleSettingsMouseMove(mx, my);
                break;
            case MY_TEAMS:
                vHov = handleMyTeamsMouseMove(mx, my);
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

        boolean pBadge = profileStatusBadgeHovered;
        profileStatusBadgeHovered = profileStatusBadgeBox.contains(mx, my);
        if (profileStatusBadgeHovered != pBadge)
            changed = true;

        boolean pContact = profileContactHovered;
        profileContactHovered = profileContactBox.contains(mx, my);
        if (profileContactHovered != pContact)
            changed = true;

        boolean pAddProj = profileAddProjectHovered;
        profileAddProjectHovered = profileAddProjectBtnBox.contains(mx, my);
        if (profileAddProjectHovered != pAddProj)
            changed = true;

        boolean pAddSk = profileAddSkillHovered;
        profileAddSkillHovered = profileAddSkillBtnBox.contains(mx, my);
        if (profileAddSkillHovered != pAddSk)
            changed = true;

        boolean pReqRev = profileRequestReviewHovered;
        profileRequestReviewHovered = profileRequestReviewBtnBox.contains(mx, my);
        if (profileRequestReviewHovered != pReqRev)
            changed = true;

        int oldStatHov = hoveredProfileStatIndex;
        hoveredProfileStatIndex = -1;
        for (int i = 0; i < profileStatBounds.length; i++) {
            if (profileStatBounds[i] != null && profileStatBounds[i].contains(mx, my)) {
                hoveredProfileStatIndex = i;
                break;
            }
        }
        if (hoveredProfileStatIndex != oldStatHov)
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

        boolean anyInteractiveHovered = editProfileHovered || shareProfileHovered || profileStatusBadgeHovered
                || profileContactHovered || profileAddProjectHovered || profileAddSkillHovered
                || profileRequestReviewHovered || (hoveredProfileStatIndex >= 0) || (hoveredProfileTab >= 0);

        if (!anyInteractiveHovered) {
            for (Rectangle r : profileSkillBounds) {
                if (r != null && r.contains(mx, my)) {
                    anyInteractiveHovered = true;
                    break;
                }
            }
        }
        if (!anyInteractiveHovered) {
            for (Rectangle r : profileProjectBounds) {
                if (r != null && r.contains(mx, my)) {
                    anyInteractiveHovered = true;
                    break;
                }
            }
        }

        return changed || anyInteractiveHovered;
    }

    private boolean handleMessagesMouseMove(int mx, int my) {
        boolean changed = false;
        boolean pSend = msgSendHovered;
        msgSendHovered = msgSendBtnBox.contains(mx, my);
        if (msgSendHovered != pSend)
            changed = true;

        boolean pCall = videoCallHovered;
        videoCallHovered = videoCallBtnBox.contains(mx, my);
        if (videoCallHovered != pCall)
            changed = true;

        int oldConvHov = hoveredConvIndex;
        hoveredConvIndex = -1;
        for (int i = 0; i < convBounds.size(); i++) {
            if (convBounds.get(i).contains(mx, my)) {
                hoveredConvIndex = i;
                break;
            }
        }
        if (hoveredConvIndex != oldConvHov)
            changed = true;
        return changed || msgSendHovered;
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

        boolean anyInteractiveHovered = (hoveredSettingsTab >= 0);

        if (settingsData.activeTab == SettingsData.SettingsTab.ACCOUNT) {
            boolean pSave = saveAccountHovered;
            saveAccountHovered = saveAccountBtnBox.contains(mx, my);
            if (saveAccountHovered != pSave) changed = true;

            boolean pCan = cancelAccountHovered;
            cancelAccountHovered = cancelAccountBtnBox.contains(mx, my);
            if (cancelAccountHovered != pCan) changed = true;

            boolean pPass = changePassHovered;
            changePassHovered = changePassBtnBox.contains(mx, my);
            if (changePassHovered != pPass) changed = true;

            anyInteractiveHovered = anyInteractiveHovered || saveAccountHovered || cancelAccountHovered
                    || changePassHovered || fullNameBox.contains(mx, my) || usernameBox.contains(mx, my)
                    || emailBox.contains(mx, my);
        } else if (settingsData.activeTab == SettingsData.SettingsTab.PROFILE_PRIVACY) {
            boolean pSave = savePrivacyHovered;
            savePrivacyHovered = savePrivacyBtnBox.contains(mx, my);
            if (savePrivacyHovered != pSave) changed = true;

            boolean pCan = cancelPrivacyHovered;
            cancelPrivacyHovered = cancelPrivacyBtnBox.contains(mx, my);
            if (cancelPrivacyHovered != pCan) changed = true;

            anyInteractiveHovered = anyInteractiveHovered || savePrivacyHovered || cancelPrivacyHovered
                    || radioPublicBox.contains(mx, my) || radioTeamBox.contains(mx, my)
                    || radioPrivateBox.contains(mx, my) || toggleSkillsBox.contains(mx, my)
                    || toggleActivityBox.contains(mx, my);
        } else if (settingsData.activeTab == SettingsData.SettingsTab.COLLAB_PREFERENCES) {
            boolean pSave = saveCollabHovered;
            saveCollabHovered = saveCollabBtnBox.contains(mx, my);
            if (saveCollabHovered != pSave) changed = true;

            boolean pCan = cancelCollabHovered;
            cancelCollabHovered = cancelCollabBtnBox.contains(mx, my);
            if (cancelCollabHovered != pCan) changed = true;

            boolean chipHov = false;
            for (Rectangle r : catRemoveBounds) {
                if (r != null && r.contains(mx, my)) {
                    chipHov = true;
                    break;
                }
            }

            anyInteractiveHovered = anyInteractiveHovered || saveCollabHovered || cancelCollabHovered
                    || chipHov || addCatBox.contains(mx, my) || checkFrontendBox.contains(mx, my)
                    || checkUiBox.contains(mx, my) || checkBackendBox.contains(mx, my)
                    || checkLeadBox.contains(mx, my) || availDropdown.contains(mx, my)
                    || commitDropdown.contains(mx, my) || modeRemoteBox.contains(mx, my)
                    || modeInPersonBox.contains(mx, my) || modeEitherBox.contains(mx, my);
        } else if (settingsData.activeTab == SettingsData.SettingsTab.DANGER_ZONE) {
            boolean pDeact = deactivateHovered;
            deactivateHovered = deactivateBtnBox.contains(mx, my);
            if (deactivateHovered != pDeact) changed = true;

            boolean pDel = deleteAccountHovered;
            deleteAccountHovered = deleteAccountBtnBox.contains(mx, my);
            if (deleteAccountHovered != pDel) changed = true;

            anyInteractiveHovered = anyInteractiveHovered || deactivateHovered || deleteAccountHovered;
        }

        return changed || anyInteractiveHovered;
    }

    private boolean handleMyTeamsMouseMove(int mx, int my) {
        boolean changed = false;
        boolean p1 = teamSettingsBtnHovered;
        teamSettingsBtnHovered = (mx >= teamSettingsBtnX && mx <= teamSettingsBtnX + teamSettingsBtnW &&
                my >= teamSettingsBtnY && my <= teamSettingsBtnY + teamSettingsBtnH);
        if (teamSettingsBtnHovered != p1)
            changed = true;

        boolean p2 = teamMsgBtnHovered;
        teamMsgBtnHovered = (mx >= teamMsgBtnX && mx <= teamMsgBtnX + teamMsgBtnW &&
                my >= teamMsgBtnY && my <= teamMsgBtnY + teamMsgBtnH);
        if (teamMsgBtnHovered != p2)
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

        boolean anyActionHov = false;
        if (teamData.activeTabIndex == 1) {
            boolean pInv = tabInviteMemberHovered;
            tabInviteMemberHovered = tabInviteMemberBtnBox.contains(mx, my);
            boolean pSlot = inviteTeammateSlotHovered;
            inviteTeammateSlotHovered = inviteTeammateSlotBox.contains(mx, my);
            if (pInv != tabInviteMemberHovered || pSlot != inviteTeammateSlotHovered)
                changed = true;
            for (Rectangle r : memberTabMsgBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : memberTabProfileBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : memberTabRemoveBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            anyActionHov = anyActionHov || tabInviteMemberHovered || inviteTeammateSlotHovered;
        } else if (teamData.activeTabIndex == 2) {
            boolean pAdd = tabAddTaskHovered;
            tabAddTaskHovered = tabAddTaskBtnBox.contains(mx, my);
            boolean pOm = tabOpenModalHovered;
            tabOpenModalHovered = tabOpenModalBtnBox.contains(mx, my);
            if (pAdd != tabAddTaskHovered || pOm != tabOpenModalHovered)
                changed = true;
            for (Rectangle r : taskAdvanceBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            anyActionHov = anyActionHov || tabAddTaskHovered || tabOpenModalHovered;
        } else if (teamData.activeTabIndex == 3) {
            boolean pUp = tabUploadFileHovered;
            tabUploadFileHovered = tabUploadFileBtnBox.contains(mx, my);
            if (pUp != tabUploadFileHovered)
                changed = true;
            for (Rectangle r : fileDownloadBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : filePreviewBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : fileDeleteBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            anyActionHov = anyActionHov || tabUploadFileHovered;
        } else if (teamData.activeTabIndex == 4) {
            boolean pPost = tabPostRoleHovered;
            tabPostRoleHovered = tabPostRoleBtnBox.contains(mx, my);
            if (pPost != tabPostRoleHovered)
                changed = true;
            for (Rectangle r : reqAcceptBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : reqDeclineBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : reqMessageBtns) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            anyActionHov = anyActionHov || tabPostRoleHovered;
        } else {
            for (Rectangle r : memberCardBounds) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : requestItemBounds) {
                if (r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : kpiCardBounds) {
                if (r != null && r.contains(mx, my)) anyActionHov = true;
            }
            for (Rectangle r : taskSummaryBounds) {
                if (r != null && r.contains(mx, my)) anyActionHov = true;
            }
            anyActionHov = anyActionHov || inviteMemberBtnHovered || viewRoadmapBtnHovered || openBoardBtnHovered
                    || reviewAllBtnHovered || reviewAppsBtnHovered;
        }

        return changed || teamSettingsBtnHovered || teamMsgBtnHovered || (hoveredTeamTabIndex >= 0) || anyActionHov;
    }

    public void onMouseWheel(int wheelRotation, int screenH) {
        if (activeNav == NavItem.MESSAGES)
            return;
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
            case MESSAGES:
                return handleMessagesKey(e);
            case SETTINGS:
                return handleSettingsKey(e);
            default:
                break;
        }

        return false;
    }

    private boolean handleMessagesKey(KeyEvent e) {
        if (messagesData.inputFocused) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                messagesData.sendCurrentDraft();
                showToast("Message sent to " + messagesData.getActiveConversation().title);
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (messagesData.inputDraft.length() > 0) {
                    messagesData.inputDraft = messagesData.inputDraft.substring(0,
                            messagesData.inputDraft.length() - 1);
                }
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126 && messagesData.inputDraft.length() < 120) {
                messagesData.inputDraft += c;
                return true;
            }
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
        if (header != null) {
            header.setSearchFocused(false);
            header.closePopups();
        }
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
        this.profileName = n;
    }

    public String getProfileBio() {
        return profileBio;
    }

    public void setProfileBio(String b) {
        this.profileBio = b;
    }

    public String getProfilePortfolio() {
        return profilePortfolio;
    }

    public void setProfilePortfolio(String u) {
        this.profilePortfolio = u;
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

    public List<String> getChatHistory() {
        return chatHistory;
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

    public MessagesData getMessagesData() {
        return messagesData;
    }

    public DashboardHeader getHeader() {
        return header;
    }

    // Profile Hitbox Getters
    public Rectangle getEditProfileBtnBox() { return editProfileBtnBox; }
    public Rectangle getShareProfileBtnBox() { return shareProfileBtnBox; }
    public Rectangle getProfileStatusBadgeBox() { return profileStatusBadgeBox; }
    public Rectangle getProfileContactBox() { return profileContactBox; }
    public Rectangle[] getProfileStatBounds() { return profileStatBounds; }
    public Rectangle[] getProfileTabBounds() { return profileTabBounds; }
    public Rectangle getProfileAddProjectBtnBox() { return profileAddProjectBtnBox; }
    public Rectangle getProfileAddSkillBtnBox() { return profileAddSkillBtnBox; }
    public Rectangle getProfileRequestReviewBtnBox() { return profileRequestReviewBtnBox; }

    // Settings Hitbox Getters
    public Rectangle[] getSettingsTabBounds() { return settingsTabBounds; }
    public Rectangle getSaveAccountBtnBox() { return saveAccountBtnBox; }
    public Rectangle getCancelAccountBtnBox() { return cancelAccountBtnBox; }
    public Rectangle getChangePassBtnBox() { return changePassBtnBox; }
    public Rectangle getSavePrivacyBtnBox() { return savePrivacyBtnBox; }
    public Rectangle getCancelPrivacyBtnBox() { return cancelPrivacyBtnBox; }
    public Rectangle getSaveCollabBtnBox() { return saveCollabBtnBox; }
    public Rectangle getCancelCollabBtnBox() { return cancelCollabBtnBox; }
    public Rectangle getDeactivateBtnBox() { return deactivateBtnBox; }
    public Rectangle getDeleteAccountBtnBox() { return deleteAccountBtnBox; }
    public Rectangle getAddCatBox() { return addCatBox; }
    public Rectangle getAvailDropdown() { return availDropdown; }
    public Rectangle getCommitDropdown() { return commitDropdown; }
    public Rectangle getModeRemoteBox() { return modeRemoteBox; }
    public Rectangle getModeInPersonBox() { return modeInPersonBox; }
    public Rectangle getModeEitherBox() { return modeEitherBox; }
    public Rectangle getRadioPublicBox() { return radioPublicBox; }
    public Rectangle getRadioTeamBox() { return radioTeamBox; }
    public Rectangle getRadioPrivateBox() { return radioPrivateBox; }
    public Rectangle getToggleSkillsBox() { return toggleSkillsBox; }
    public Rectangle getToggleActivityBox() { return toggleActivityBox; }
}

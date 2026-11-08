import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Controller and main container for TeamUp Admin Dashboard.
 * Coordinates AdminSidebar, AdminHeader, AdminModals, and all 7 admin sub-views:
 * - AdminOverview (Overview)
 * - AdminUsers (Users)
 * - AdminProjects (Projects)
 * - AdminReports (Reports)
 * - AdminCategories (Categories & Skills)
 * - AdminActivity (Activity Logs)
 * - AdminSettings (Platform Settings)
 *
 * Pure core Java - zero third-party dependencies.
 */
public class AdminDashboard {

    public interface AdminListener {
        void returnToLanding();
        void switchToStudentDashboard();
    }

    private final AdminListener listener;

    // Fixed navigation chrome
    private final AdminSidebar sidebar;
    private final AdminHeader header;
    private final AdminModals modals;

    // Sub-views
    private final AdminOverview overview;
    private final AdminUsers usersView;
    private final AdminProjects projectsView;
    private final AdminReports reportsView;
    private final AdminCategories categoriesView;
    private final AdminSettings settingsView;

    // Viewport & scrolling
    private float scrollY = 0f;
    private float targetScrollY = 0f;

    // Toast message system
    private String toastMessage = "";
    private float toastTimer = 0f;

    // Active sub-view tracker for change detection
    private AdminSidebar.AdminNavItem lastNav = AdminSidebar.AdminNavItem.OVERVIEW;

    public AdminDashboard(AdminListener listener) {
        this.listener = listener;
        this.sidebar = new AdminSidebar();
        this.header = new AdminHeader();
        this.modals = new AdminModals();

        this.overview = new AdminOverview();
        this.usersView = new AdminUsers();
        this.projectsView = new AdminProjects();
        this.reportsView = new AdminReports();
        this.categoriesView = new AdminCategories();
        this.settingsView = new AdminSettings();

        // Initial breadcrumb
        this.header.setBreadcrumb(sidebar.getActiveNav().label);
    }

    public void update(float dt) {
        // Track nav change to update breadcrumbs & reset scroll
        if (sidebar.getActiveNav() != lastNav) {
            lastNav = sidebar.getActiveNav();
            header.setBreadcrumb(lastNav.label);
            targetScrollY = 0f;
            scrollY = 0f;
        }

        // Smooth scroll interpolation
        scrollY = Anim.approach(scrollY, targetScrollY, dt, 14f);

        // Header animation (caret)
        header.update(dt);

        // Toast fade-out
        if (toastTimer > 0f) {
            toastTimer -= dt;
            if (toastTimer <= 0f) {
                toastMessage = "";
            }
        }
    }

    public void paint(Graphics2D g, int width, int height) {
        // Base canvas background
        g.setColor(AdminTheme.BG_CANVAS);
        g.fillRect(0, 0, width, height);

        int sideW = 230;
        int headH = 68;

        // 1. Scrollable Main Content Area
        Graphics2D gContent = (Graphics2D) g.create();
        gContent.clipRect(sideW, headH, width - sideW, height - headH);
        gContent.translate(0, -Math.round(scrollY));

        int contentW = width - sideW;
        int totalContentH = getActiveContentHeight(contentW);

        // Prevent scrolling beyond bounds
        int maxScroll = Math.max(0, totalContentH - (height - headH) + 40);
        if (targetScrollY > maxScroll) targetScrollY = maxScroll;
        if (targetScrollY < 0) targetScrollY = 0;

        paintActiveView(gContent, sideW, headH, contentW, height - headH);
        gContent.dispose();

        // 2. Fixed Left Sidebar
        sidebar.paint(g, 0, 0, sideW, height, this);

        // 3. Fixed Top Header
        header.paint(g, sideW, 0, width - sideW, headH);

        // 4. Toast overlay
        paintToast(g, width, height);

        // 5. Interactive Modals overlay
        modals.paint(g, width, height);
    }

    private int getActiveContentHeight(int contentW) {
        switch (sidebar.getActiveNav()) {
            case OVERVIEW:
                return overview.getComputedHeight(contentW);
            case USERS:
                return usersView.getComputedHeight(contentW);
            case PROJECTS:
                return projectsView.getComputedHeight(contentW);
            case REPORTS:
                return reportsView.getComputedHeight(contentW);
            case SKILLS:
                return categoriesView.getComputedHeight(contentW);
            case SETTINGS:
                return settingsView.getComputedHeight(contentW);
            default:
                return 800;
        }
    }

    private void paintActiveView(Graphics2D g, int x, int y, int w, int h) {
        switch (sidebar.getActiveNav()) {
            case OVERVIEW:
                overview.paint(g, x, y, w, h, this);
                break;
            case USERS:
                usersView.paint(g, x, y, w, h, this);
                break;
            case PROJECTS:
                projectsView.paint(g, x, y, w, h, this);
                break;
            case REPORTS:
                reportsView.paint(g, x, y, w, h, this);
                break;
            case SKILLS:
                categoriesView.paint(g, x, y, w, h, this);
                break;
            case SETTINGS:
                settingsView.paint(g, x, y, w, h, this);
                break;
            default:
                overview.paint(g, x, y, w, h, this);
                break;
        }
    }

    private void paintToast(Graphics2D g, int w, int h) {
        if (toastMessage == null || toastMessage.isEmpty() || toastTimer <= 0f) return;

        float alpha = Math.min(1.0f, toastTimer * 2f);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(AdminTheme.FONT_BOLD_12);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(toastMessage) + 36;
        int th = 38;
        int tx = (w - tw) / 2;
        int ty = h - th - 32;

        RoundRectangle2D tShape = new RoundRectangle2D.Float(tx, ty, tw, th, 12, 12);
        g2.setColor(new Color(0x0D, 0x0C, 0x22, Math.round(230 * alpha)));
        g2.fill(tShape);

        g2.setColor(new Color(255, 255, 255, Math.round(255 * alpha)));
        g2.drawString(toastMessage, tx + 18, ty + (th - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }

    public void showToast(String message) {
        this.toastMessage = message;
        this.toastTimer = 2.4f;
    }

    public void returnToLanding() {
        if (listener != null) {
            listener.returnToLanding();
        }
    }

    public void switchToStudentDashboard() {
        if (listener != null) {
            listener.switchToStudentDashboard();
        }
    }

    public void navigateTo(AdminSidebar.AdminNavItem item) {
        sidebar.setActiveNav(item);
        header.setBreadcrumb(item.label);
        targetScrollY = 0f;
        scrollY = 0f;
    }

    public void openReviewModal(AdminOverview.ModerationItem item) {
        modals.openReview(item);
    }

    public void openReportsQueue() {
        navigateTo(AdminSidebar.AdminNavItem.REPORTS);
    }

    public void openEditRoleModal(String name, String role) {
        modals.openEditRole(name, role);
    }

    public void openAddCategoryModal() {
        modals.openAddCategory();
    }

    public void openAddSkillModal() {
        modals.openAddSkill();
    }

    public boolean onClick(int mx, int my) {
        // Modal consumes all clicks when open (0ms dismiss)
        if (modals.getActiveModal() != AdminModals.ActiveModal.NONE) {
            return modals.onClick(mx, my, this);
        }

        int sideW = 230;
        int headH = 68;

        // Sidebar click
        if (mx < sideW) {
            return sidebar.onClick(mx, my, this);
        }

        // Header click
        if (my < headH) {
            return header.onClick(mx, my, this);
        }

        // Content click with scroll translation
        int cy = my + Math.round(scrollY);
        switch (sidebar.getActiveNav()) {
            case OVERVIEW:
                return overview.onClick(mx, cy, this);
            case USERS:
                return usersView.onClick(mx, cy, this);
            case PROJECTS:
                return projectsView.onClick(mx, cy, this);
            case REPORTS:
                return reportsView.onClick(mx, cy, this);
            case SKILLS:
                return categoriesView.onClick(mx, cy, this);
            case SETTINGS:
                return settingsView.onClick(mx, cy, this);
            default:
                return false;
        }
    }

    public boolean onMouseMove(int mx, int my) {
        if (modals.getActiveModal() != AdminModals.ActiveModal.NONE) {
            return modals.onMouseMove(mx, my);
        }

        int sideW = 230;
        int headH = 68;

        boolean c1 = sidebar.onMouseMove(mx, my);
        int cy = my + Math.round(scrollY);

        boolean c2 = false;
        switch (sidebar.getActiveNav()) {
            case OVERVIEW:
                c2 = overview.onMouseMove(mx, cy);
                break;
            case USERS:
                c2 = usersView.onMouseMove(mx, cy);
                break;
            case PROJECTS:
                c2 = projectsView.onMouseMove(mx, cy);
                break;
            case REPORTS:
                c2 = reportsView.onMouseMove(mx, cy);
                break;
            case SKILLS:
                c2 = categoriesView.onMouseMove(mx, cy);
                break;
            case SETTINGS:
                c2 = settingsView.onMouseMove(mx, cy);
                break;
            default:
                break;
        }
        return c1 || c2;
    }

    public boolean onMouseWheel(int rotation) {
        if (modals.getActiveModal() != AdminModals.ActiveModal.NONE) {
            return false;
        }
        targetScrollY += rotation * 48f;
        if (targetScrollY < 0f) targetScrollY = 0f;
        return true;
    }

    public boolean onKey(char ch, int keyCode) {
        if (modals.getActiveModal() != AdminModals.ActiveModal.NONE) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                modals.closeModal();
                return true;
            }
            if (modals.onKey(ch, keyCode)) {
                return true;
            }
            return true;
        }

        // Subview key handling (search input, etc.)
        boolean handled = false;
        switch (sidebar.getActiveNav()) {
            case USERS:
                handled = usersView.onKey(ch, keyCode, this);
                break;
            case PROJECTS:
                handled = projectsView.onKey(ch, keyCode, this);
                break;
            case REPORTS:
                handled = reportsView.onKey(ch, keyCode, this);
                break;
            case SKILLS:
                handled = categoriesView.onKey(ch, keyCode, this);
                break;
            case SETTINGS:
                handled = settingsView.onKey(ch, keyCode, this);
                break;
            default:
                break;
        }
        if (handled) return true;

        return header.onKey(ch, keyCode, this);
    }
}

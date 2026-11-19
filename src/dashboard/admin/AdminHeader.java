import java.awt.*;

/**
 * Top header bar for TeamUp Admin Dashboard.
 * Displays clean breadcrumb: "Administration / <Section>".
 * Search bar and profile dropdown have been completely removed as requested.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminHeader {

    private String breadcrumb = "Overview";

    // Layout bounds kept as off-screen constants for API compatibility
    private int searchX = -9999;
    private int searchY = -9999;
    private int searchW = 0;
    private int searchH = 0;
    private int userX = -9999;
    private int userY = -9999;
    private int userW = 0;
    private int userH = 0;

    public boolean isAnyPopoverOpen() {
        return false;
    }

    public void setBreadcrumb(String breadcrumb) {
        this.breadcrumb = (breadcrumb == null) ? "Overview" : breadcrumb;
    }

    public String getBreadcrumb() {
        return breadcrumb;
    }

    public void update(float dt) {
        // No animations needed
    }

    public void paint(Graphics2D g, int x, int y, int w, int h) {
        // Header background
        g.setColor(AdminTheme.SIDEBAR_BG);
        g.fillRect(x, y, w, h);

        // Header bottom border
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, y + h - 1, x + w, y + h - 1);

        // Clean breadcrumb: "Administration / <Section>"
        int curX = x + 32;
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Administration", curX, y + 36);

        FontMetrics bfm = g.getFontMetrics();
        int slashX = curX + bfm.stringWidth("Administration") + 8;
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("/", slashX, y + 36);

        int ovX = slashX + 12;
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(breadcrumb, ovX, y + 36);

        // Ensure off-screen coordinates
        searchX = -9999;
        searchY = -9999;
        searchW = 0;
        searchH = 0;
        userX = -9999;
        userY = -9999;
        userW = 0;
        userH = 0;
    }

    public boolean onMouseMove(int mx, int my) {
        return false;
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        return false;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        return false;
    }

    // API getters and setters for compatibility
    public boolean isSearchFocused() { return false; }
    public void setSearchFocused(boolean f) { }
    public String getSearchText() { return ""; }
    public void setSearchText(String text) { }
    public int getSearchMatchCount() { return 0; }
    public boolean isProfileOpen() { return false; }
    public void setProfileOpen(boolean open) { }
    public boolean isClearHovered() { return false; }
    public int getHoveredResultIdx() { return -1; }
    public int getSearchX() { return searchX; }
    public int getSearchY() { return searchY; }
    public int getSearchW() { return searchW; }
    public int getSearchH() { return searchH; }
    public int getUserX() { return userX; }
}

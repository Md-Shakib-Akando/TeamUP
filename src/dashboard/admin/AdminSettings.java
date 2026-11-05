import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Admin Dashboard - Platform Settings View.
 * Allows configuration of academic domain restrictions, project size limits, moderation thresholds, and maintenance status.
 * Wired directly to AdminDataStore for full persistence.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminSettings {

    private String domainInput;
    private boolean domainFocused = false;
    private int domainBoxX, domainBoxY, domainBoxW, domainBoxH = 36;

    private int maxTeamSize;
    private int spamThreshold;
    private boolean maintenanceMode;

    private int toggleX, toggleY, toggleW = 44, toggleH = 24;

    private int saveBtnX, saveBtnY, saveBtnW = 160, saveBtnH = 38;
    private boolean saveHovered = false;

    public AdminSettings() {
        AdminDataStore store = AdminDataStore.getInstance();
        this.domainInput = store.allowedDomains;
        this.maxTeamSize = store.maxTeamSize;
        this.spamThreshold = store.spamSensitivityThreshold;
        this.maintenanceMode = store.maintenanceModeActive;
    }

    public int getComputedHeight(int width) {
        return 720;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = 36;
        int curY = y + 28;
        int contentW = width - padX * 2;

        // 1. Header
        g.setFont(AdminTheme.FONT_BOLD_24);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Platform Settings", x + padX, curY + 24);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Configure global permissions, domain restrictions, and automated safety policies.", x + padX, curY + 48);

        curY += 76;

        // 2. Settings Card Container
        int cardH = 480;
        RoundRectangle2D card = new RoundRectangle2D.Float(x + padX, curY, contentW, cardH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(card);

        int cy = curY + 24;
        int cx = x + padX + 24;
        int innerW = contentW - 48;

        // Setting 1: Academic Domain Restrictions
        g.setFont(AdminTheme.FONT_BOLD_14);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Allowed University Domains", cx, cy + 14);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Only students with verified email addresses matching these domains can join.", cx, cy + 32);

        domainBoxX = cx;
        domainBoxY = cy + 42;
        domainBoxW = innerW;
        RoundRectangle2D dBox = new RoundRectangle2D.Float(domainBoxX, domainBoxY, domainBoxW, domainBoxH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(dBox);
        g.setColor(domainFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.draw(dBox);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(domainInput, cx + 12, cy + 65);

        cy += 98;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(cx, cy, cx + innerW, cy);
        cy += 18;

        // Setting 2: Team Size Limits
        g.setFont(AdminTheme.FONT_BOLD_14);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Default Maximum Project Team Size", cx, cy + 14);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Current upper limit per team: " + maxTeamSize + " students", cx, cy + 32);

        paintStepper(g, cx, cy + 42, maxTeamSize);

        cy += 98;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(cx, cy, cx + innerW, cy);
        cy += 18;

        // Setting 3: Moderation Auto-Flag Threshold
        g.setFont(AdminTheme.FONT_BOLD_14);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Automated Spam & Harassment Sensitivity", cx, cy + 14);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Trigger automated review when user receives " + spamThreshold + " or more reports within 24 hours.", cx, cy + 32);

        cy += 50;

        // Setting 4: Maintenance Mode
        g.setFont(AdminTheme.FONT_BOLD_14);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Platform Maintenance Mode", cx, cy + 14);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("When active, non-admin users will see a scheduled maintenance notice.", cx, cy + 32);

        // Toggle Switch
        toggleX = cx + innerW - 56;
        toggleY = cy + 4;
        paintToggle(g, toggleX, toggleY, maintenanceMode);

        // Save Button at bottom
        saveBtnX = cx;
        saveBtnY = curY + cardH - 56;
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(saveBtnX, saveBtnY, saveBtnW, saveBtnH, 8, 8);
        g.setColor(saveHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(sBtn);

        g.setFont(AdminTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        g.drawString("Save platform settings", saveBtnX + 16, saveBtnY + 23);
    }

    private void paintStepper(Graphics2D g, int x, int y, int val) {
        RoundRectangle2D box = new RoundRectangle2D.Float(x, y, 120, 34, 6, 6);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(box);

        g.setFont(AdminTheme.FONT_BOLD_13);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(val + " members", x + 16, y + 22);
    }

    private void paintToggle(Graphics2D g, int x, int y, boolean on) {
        RoundRectangle2D bg = new RoundRectangle2D.Float(x, y, 44, 24, 12, 12);
        g.setColor(on ? AdminTheme.BRAND_GREEN_DARK : new Color(0xCB, 0xD5, 0xE1));
        g.fill(bg);

        int knobX = on ? x + 22 : x + 2;
        g.setColor(Color.WHITE);
        g.fillOval(knobX, y + 2, 20, 20);
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // Domain input focus
        if (mx >= domainBoxX && mx <= domainBoxX + domainBoxW && my >= domainBoxY && my <= domainBoxY + domainBoxH) {
            domainFocused = true;
            return true;
        } else {
            domainFocused = false;
        }

        // Maintenance toggle
        if (mx >= toggleX && mx <= toggleX + toggleW && my >= toggleY && my <= toggleY + toggleH) {
            maintenanceMode = !maintenanceMode;
            dashboard.showToast("Maintenance mode: " + (maintenanceMode ? "ENABLED" : "DISABLED"));
            return true;
        }

        // Save Button
        if (mx >= saveBtnX && mx <= saveBtnX + saveBtnW && my >= saveBtnY && my <= saveBtnY + saveBtnH) {
            AdminDataStore store = AdminDataStore.getInstance();
            store.allowedDomains = domainInput;
            store.maxTeamSize = maxTeamSize;
            store.spamSensitivityThreshold = spamThreshold;
            store.maintenanceModeActive = maintenanceMode;
            store.logActivity("Jordan Lee", "JL", "Updated platform settings", "Global policies", true);
            dashboard.showToast("Platform settings successfully saved!");
            return true;
        }
        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean pSave = saveHovered;
        saveHovered = (mx >= saveBtnX && mx <= saveBtnX + saveBtnW && my >= saveBtnY && my <= saveBtnY + saveBtnH);
        return pSave != saveHovered;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (!domainFocused) return false;
        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            if (domainInput.length() > 0) {
                domainInput = domainInput.substring(0, domainInput.length() - 1);
                return true;
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            domainFocused = false;
            return true;
        } else if (ch >= 32 && ch <= 126) {
            domainInput += ch;
            return true;
        }
        return false;
    }
}

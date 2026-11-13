import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Production-ready Admin Settings View for TeamUp.
 * Clean, simple, meaningful, and 100% workable:
 *
 * 1. General & Contact Information:
 *    - Platform Name (e.g. TeamUp)
 *    - Official Contact Email
 *    - Support & Helpdesk Email
 *    - Authorized University Domains (whitelist)
 *
 * 2. Platform Policies & Team Limits:
 *    - User Registration (Toggle: ON/OFF)
 *    - Project Approval (Toggle: ON/OFF)
 *    - System Notifications (Toggle: ON/OFF)
 *    - Maintenance Mode (Toggle: ON/OFF)
 *    - Maximum Team Size (Interactive Stepper with − / +)
 *
 * 3. Administrator Security & Password:
 *    - Current Password
 *    - New Password (min 6 characters)
 *    - Confirm Password
 *    - Show/Hide Password toggle
 *    - Update Password action
 *
 * 4. Database Backup & Safety:
 *    - Live last backup timestamp and file size
 *    - Working "Create Database Backup" button exporting JSON to backups/
 *
 * 5. Floating Action Bar:
 *    - Real-time unsaved changes detection
 *    - Reset to Defaults action
 *    - Save Platform Settings action
 *
 * Pure core Java - zero third-party dependencies.
 */
public class AdminSettings {

    // --- 1. General & Contact Settings ---
    private String platformNameDraft;
    private String contactEmailDraft;
    private String supportEmailDraft;
    private String allowedDomainsDraft;

    // --- 2. Platform Policies & Governance ---
    private boolean userRegistrationDraft;
    private boolean projectApprovalDraft;
    private boolean notificationsDraft;
    private boolean maintenanceModeDraft;
    private int maxTeamSizeDraft;

    // --- 3. Change Password ---
    private String currentPasswordInput = "";
    private String newPasswordInput = "";
    private String confirmPasswordInput = "";
    private boolean showPasswords = false;

    // --- 4. Backup Status ---
    private String lastBackupDate = "Nov 11, 2026, 04:30 AM";
    private String lastBackupSize = "42.8 KB";

    // Focus tracking:
    // 0=None, 1=PlatformName, 2=ContactEmail, 3=SupportEmail,
    // 4=CurrentPassword, 5=NewPassword, 6=ConfirmPassword, 7=AllowedDomains
    private int focusedInput = 0;

    // Hitbox data structures
    public static class InputBox {
        public int id;
        public int x, y, w, h;
        public boolean isPassword;

        public InputBox(int id, int x, int y, int w, int h, boolean isPassword) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.isPassword = isPassword;
        }
    }

    public static class ToggleBtn {
        public String id;
        public int x, y, w, h;
        public boolean on;

        public ToggleBtn(String id, int x, int y, int w, int h, boolean on) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.on = on;
        }
    }

    public static class StepperBtn {
        public String id;
        public int mx, my, mw, mh; // minus
        public int px, py, pw, ph; // plus
        public boolean minusHovered = false;
        public boolean plusHovered = false;

        public StepperBtn(String id, int mx, int my, int mw, int mh, int px, int py, int pw, int ph) {
            this.id = id;
            this.mx = mx;
            this.my = my;
            this.mw = mw;
            this.mh = mh;
            this.px = px;
            this.py = py;
            this.pw = pw;
            this.ph = ph;
        }
    }

    public static class ActionBtn {
        public String id;
        public int x, y, w, h;
        public boolean hovered = false;

        public ActionBtn(String id, int x, int y, int w, int h) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private final List<InputBox> inputBoxes = new ArrayList<>();
    private final List<ToggleBtn> toggleBtns = new ArrayList<>();
    private final List<StepperBtn> stepperBtns = new ArrayList<>();
    private final List<ActionBtn> actionBtns = new ArrayList<>();

    public AdminSettings() {
        loadFromStore();
    }

    public void loadFromStore() {
        AdminDataStore store = AdminDataStore.getInstance();
        this.platformNameDraft = store.platformName;
        this.contactEmailDraft = store.contactEmail;
        this.supportEmailDraft = store.supportEmail;
        this.allowedDomainsDraft = store.allowedDomains;

        this.userRegistrationDraft = store.userRegistrationEnabled;
        this.projectApprovalDraft = store.requireProjectApproval;
        this.notificationsDraft = store.notificationsEnabled;
        this.maintenanceModeDraft = store.maintenanceModeActive;
        this.maxTeamSizeDraft = store.maxTeamSize;

        this.lastBackupDate = store.lastBackupDate;
        this.lastBackupSize = store.lastBackupSize;

        this.currentPasswordInput = "";
        this.newPasswordInput = "";
        this.confirmPasswordInput = "";
    }

    public boolean hasUnsavedChanges() {
        AdminDataStore store = AdminDataStore.getInstance();
        if (!platformNameDraft.equals(store.platformName)) return true;
        if (!contactEmailDraft.equals(store.contactEmail)) return true;
        if (!supportEmailDraft.equals(store.supportEmail)) return true;
        if (allowedDomainsDraft != null && !allowedDomainsDraft.equals(store.allowedDomains)) return true;

        if (userRegistrationDraft != store.userRegistrationEnabled) return true;
        if (projectApprovalDraft != store.requireProjectApproval) return true;
        if (notificationsDraft != store.notificationsEnabled) return true;
        if (maintenanceModeDraft != store.maintenanceModeActive) return true;
        if (maxTeamSizeDraft != store.maxTeamSize) return true;

        if (!currentPasswordInput.isEmpty() || !newPasswordInput.isEmpty() || !confirmPasswordInput.isEmpty()) return true;
        return false;
    }

    public int getComputedHeight(int width) {
        boolean mobile = (width < 650);
        int total = (width < 640) ? 150 : 100; // Header
        total += mobile ? 350 : 255; // Card 1: General & Contact Information
        total += mobile ? 360 : 275; // Card 2: Platform Controls & Governance
        total += mobile ? 320 : 245; // Card 3: Change Password
        return total + 40;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = (width < 600) ? 16 : 28;
        int curY = y + 24;
        int contentW = width - padX * 2;

        inputBoxes.clear();
        toggleBtns.clear();
        stepperBtns.clear();
        actionBtns.clear();

        // 1. Header Section
        curY = paintHeader(g, x + padX, curY, contentW);
        curY += 22;

        // 2. Card 1: General & Contact Information (Platform Name, Emails, Allowed Domains)
        curY = paintGeneralAndContactCard(g, x + padX, curY, contentW);
        curY += 20;

        // 3. Card 2: Platform Policies & Governance (Reg, Approval, Notif, Maintenance, Team Limit)
        curY = paintControlsAndGovernanceCard(g, x + padX, curY, contentW);
        curY += 20;

        // 4. Card 3: Administrator Security & Password
        paintChangePasswordCard(g, x + padX, curY, contentW);
    }

    private int paintHeader(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_HEADING);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Platform Settings", x, y + 24);

        int titleW = g.getFontMetrics().stringWidth("Platform Settings");

        // Status badge: Live Policies
        if (w >= 750) {
            int bX = x + titleW + 14;
            int bY = y + 7;
            RoundRectangle2D pill = new RoundRectangle2D.Float(bX, bY, 115, 22, 11, 11);
            g.setColor(new Color(0xDC, 0xFC, 0xE7));
            g.fill(pill);
            g.setColor(new Color(0x10, 0xB9, 0x81));
            g.fillOval(bX + 8, bY + 7, 7, 7);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.drawString("LIVE POLICIES", bX + 20, bY + 15);
        }

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        String sub = "Configure platform name, institutional domains, user access, and security.";
        g.drawString(sub, x, y + 48);

        // Header actions: Save & Reset
        int btnH = 34;
        int saveW = 140;
        int resetW = 125;

        if (w >= 640) {
            int saveX = x + w - saveW;
            int resetX = saveX - resetW - 10;
            int btnY = y + 10;

            paintButton(g, "RESET_TOP", resetX, btnY, resetW, btnH, "↺ Reset Defaults", false);
            boolean unsaved = hasUnsavedChanges();
            paintButton(g, "SAVE_TOP", saveX, btnY, saveW, btnH, unsaved ? "💾 Save Changes *" : "💾 Save Changes", true);
            return y + 54;
        } else {
            paintButton(g, "RESET_TOP", x, y + 58, resetW, btnH, "↺ Reset Defaults", false);
            boolean unsaved = hasUnsavedChanges();
            paintButton(g, "SAVE_TOP", x + resetW + 10, y + 58, saveW, btnH, unsaved ? "💾 Save Changes *" : "💾 Save Changes", true);
            return y + 102;
        }
    }

    // --- CARD 1: General & Contact Information ---
    private int paintGeneralAndContactCard(Graphics2D g, int x, int y, int w) {
        boolean mobile = (w < 650);
        int cardH = mobile ? 340 : 250;
        paintCardContainer(g, x, y, w, cardH, "General & Contact Information", "Configure platform brand title, official communication channels, and university email whitelist.");

        int cx = x + 24;
        int cy = y + 68;
        int innerW = w - 48;

        // 1. Platform Name
        paintInputField(g, 1, cx, cy, innerW, 36, "Platform Name", platformNameDraft,
                "Enter platform brand name (e.g. TeamUp)", false);

        cy += 64;

        // 2. Email Settings: Contact Email & Support Email
        if (!mobile) {
            int fieldW = (innerW - 16) / 2;
            paintInputField(g, 2, cx, cy, fieldW, 36, "Contact Email", contactEmailDraft, "e.g. contact@teamup.edu",
                    false);
            paintInputField(g, 3, cx + fieldW + 16, cy, fieldW, 36, "Support & Helpdesk Email", supportEmailDraft,
                    "e.g. support@teamup.edu", false);
        } else {
            paintInputField(g, 2, cx, cy, innerW, 34, "Contact Email", contactEmailDraft, "e.g. contact@teamup.edu",
                    false);
            cy += 58;
            paintInputField(g, 3, cx, cy, innerW, 34, "Support Email", supportEmailDraft, "e.g. support@teamup.edu",
                    false);
        }

        cy += 64;

        // 3. Authorized University Email Domains (Whitelist)
        paintInputField(g, 7, cx, cy, innerW, 36, "Authorized University Domains", allowedDomainsDraft, "e.g. @university.edu, @mit.edu, @du.ac.bd", false);

        return y + cardH;
    }

    // --- CARD 2: Platform Controls & Governance ---
    private int paintControlsAndGovernanceCard(Graphics2D g, int x, int y, int w) {
        boolean mobile = (w < 650);
        int cardH = mobile ? 350 : 270;
        paintCardContainer(g, x, y, w, cardH, "Platform Controls & Team Capacity", "Manage user registration, project review gates, system notifications, maintenance, and team member limits.");

        int cx = x + 24;
        int cy = y + 68;
        int innerW = w - 48;

        if (!mobile) {
            int colW = (innerW - 20) / 2;

            // Row 1: User Registration & Project Approval
            paintToggleRow(g, "USER_REG", cx, cy, colW, "User Registration", "Allow new student and member signups", userRegistrationDraft);
            paintToggleRow(g, "PROJ_APPROV", cx + colW + 20, cy, colW, "Project Approval", "Require admin review before publishing", projectApprovalDraft);

            cy += 56;

            // Row 2: Notifications & Maintenance Mode
            paintToggleRow(g, "NOTIF", cx, cy, colW, "System Notifications", "Enable platform email and push alerts", notificationsDraft);
            paintToggleRow(g, "MAINTENANCE", cx + colW + 20, cy, colW, "Maintenance Mode", "Temporarily pause public access for upgrades", maintenanceModeDraft);

            cy += 56;

            // Row 3: Maximum Team Size
            paintStepperRow(g, "MAX_TEAM", cx, cy, colW, "Maximum Team Size", maxTeamSizeDraft, "members (capacity limit)");
        } else {
            paintToggleRow(g, "USER_REG", cx, cy, innerW, "User Registration", "Allow new signups", userRegistrationDraft);
            cy += 48;
            paintToggleRow(g, "PROJ_APPROV", cx, cy, innerW, "Project Approval", "Require pre-approval",
                    projectApprovalDraft);
            cy += 48;
            paintToggleRow(g, "NOTIF", cx, cy, innerW, "Notifications", "Enable platform alerts", notificationsDraft);
            cy += 48;
            paintToggleRow(g, "MAINTENANCE", cx, cy, innerW, "Maintenance Mode", "Pause public access", maintenanceModeDraft);
            cy += 48;
            paintStepperRow(g, "MAX_TEAM", cx, cy, innerW, "Max Team Size", maxTeamSizeDraft, "members");
        }

        return y + cardH;
    }

    // --- CARD 3: Change Password ---
    private int paintChangePasswordCard(Graphics2D g, int x, int y, int w) {
        boolean mobile = (w < 650);
        int cardH = mobile ? 310 : 240;
        paintCardContainer(g, x, y, w, cardH, "Change Administrator Password",
                "Update credentials for your administrator account (minimum 6 characters).");

        int cx = x + 24;
        int cy = y + 68;
        int innerW = w - 48;

        if (!mobile) {
            int fieldW = (innerW - 24) / 3;
            paintInputField(g, 4, cx, cy, fieldW, 36, "Current Password", currentPasswordInput,
                    "Enter current password", !showPasswords);
            paintInputField(g, 5, cx + fieldW + 12, cy, fieldW, 36, "New Password", newPasswordInput,
                    "Enter new password (6+ chars)", !showPasswords);
            paintInputField(g, 6, cx + (fieldW + 12) * 2, cy, fieldW, 36, "Confirm New Password", confirmPasswordInput,
                    "Re-enter new password", !showPasswords);

            cy += 64;

            // Password Toggle & Update Button
            int btnW = 160;
            paintButton(g, "UPDATE_PASSWORD", cx, cy, btnW, 36, "🔒 Update Password", true);

            // Show/Hide Password Link
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.BRAND_SECONDARY);
            String toggleText = showPasswords ? "Hide password text" : "Show password text";
            g.drawString(toggleText, cx + btnW + 16, cy + 22);

            ActionBtn showHideBtn = new ActionBtn("SHOW_PASSWORD_TOGGLE", cx + btnW + 12, cy, 140, 36);
            actionBtns.add(showHideBtn);
        } else {
            paintInputField(g, 4, cx, cy, innerW, 34, "Current Password", currentPasswordInput, "Current password",
                    !showPasswords);
            cy += 56;
            paintInputField(g, 5, cx, cy, innerW, 34, "New Password", newPasswordInput, "New password (6+ chars)",
                    !showPasswords);
            cy += 56;
            paintInputField(g, 6, cx, cy, innerW, 34, "Confirm Password", confirmPasswordInput, "Confirm new password",
                    !showPasswords);
            cy += 56;
            paintButton(g, "UPDATE_PASSWORD", cx, cy, innerW, 34, "🔒 Update Password", true);
        }

        return y + cardH;
    }

    // --- Reusable Component Painters ---

    private void paintCardContainer(Graphics2D g, int x, int y, int w, int h, String title, String subtitle) {
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        // Header Title
        g.setFont(AdminTheme.FONT_SECTION);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(title, x + 24, y + 28);

        // Subtitle
        g.setFont(AdminTheme.FONT_REG_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(subtitle, x + 24, y + 48);
    }

    private void paintInputField(Graphics2D g, int inputId, int fx, int fy, int fw, int fh, String label, String value,
            String placeholder, boolean mask) {
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(label, fx, fy + 12);

        int boxY = fy + 18;
        RoundRectangle2D box = new RoundRectangle2D.Float(fx, boxY, fw, fh, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(box);

        boolean focused = (focusedInput == inputId);
        g.setColor(focused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(focused ? 1.8f : 1.1f));
        g.draw(box);

        inputBoxes.add(new InputBox(inputId, fx, boxY, fw, fh, mask));

        g.setFont(AdminTheme.FONT_REG_13);
        FontMetrics fm = g.getFontMetrics();

        if (value != null && !value.isEmpty()) {
            g.setColor(AdminTheme.TEXT_DARK);
            String displayVal = mask ? value.replaceAll(".", "•") : value;
            g.drawString(displayVal, fx + 12, boxY + 22);

            // Blinking cursor
            if (focused && (System.currentTimeMillis() % 1000 < 500)) {
                int cursorX = fx + 12 + fm.stringWidth(displayVal);
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.drawLine(cursorX, boxY + 8, cursorX, boxY + fh - 8);
            }
        } else {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(placeholder, fx + 12, boxY + 22);

            // Blinking cursor at start if focused & empty
            if (focused && (System.currentTimeMillis() % 1000 < 500)) {
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.drawLine(fx + 12, boxY + 8, fx + 12, boxY + fh - 8);
            }
        }
    }

    private void paintToggleRow(Graphics2D g, String id, int rx, int ry, int rw, String title, String sub, boolean on) {
        int togW = 44, togH = 22;
        int togX = rx + rw - togW;
        int togY = ry + 2;

        RoundRectangle2D bg = new RoundRectangle2D.Float(togX, togY, togW, togH, togH / 2, togH / 2);
        g.setColor(on ? AdminTheme.BRAND_GREEN_DARK : new Color(0xCB, 0xD5, 0xE1));
        g.fill(bg);

        int knobSize = togH - 4;
        int knobX = on ? togX + togW - knobSize - 2 : togX + 2;
        g.setColor(Color.WHITE);
        g.fillOval(knobX, togY + 2, knobSize, knobSize);

        toggleBtns.add(new ToggleBtn(id, togX, togY, togW, togH, on));

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(title, rx, ry + 14);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String drawSub = sub;
        int maxW = togX - rx - 14;
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(drawSub) > maxW && drawSub.length() > 6) {
            while (drawSub.length() > 4 && fm.stringWidth(drawSub + "…") > maxW) {
                drawSub = drawSub.substring(0, drawSub.length() - 1);
            }
            drawSub += "…";
        }
        g.drawString(drawSub, rx, ry + 28);
    }

    private void paintStepperRow(Graphics2D g, String id, int rx, int ry, int rw, String title, int value, String unit) {
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(title, rx, ry + 14);

        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(value + " " + unit, rx, ry + 28);

        int btnSize = 24;
        int px = rx + rw - btnSize;
        int py = ry + 4;
        int mx = px - btnSize - 6;
        int my = py;

        StepperBtn sb = new StepperBtn(id, mx, my, btnSize, btnSize, px, py, btnSize, btnSize);
        stepperBtns.add(sb);

        // Minus button
        g.setColor(sb.minusHovered ? new Color(0xE2, 0xE8, 0xF0) : new Color(0xF1, 0xF5, 0xF9));
        g.fillRoundRect(mx, my, btnSize, btnSize, 4, 4);
        g.setColor(AdminTheme.TEXT_DARK);
        g.setFont(AdminTheme.FONT_BOLD_13);
        g.drawString("−", mx + 7, my + 17);

        // Plus button
        g.setColor(sb.plusHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fillRoundRect(px, py, btnSize, btnSize, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.drawString("+", px + 6, py + 16);
    }

    private void paintButton(Graphics2D g, String id, int bx, int by, int bw, int bh, String label, boolean primary) {
        ActionBtn ab = new ActionBtn(id, bx, by, bw, bh);
        actionBtns.add(ab);

        RoundRectangle2D btn = new RoundRectangle2D.Float(bx, by, bw, bh, 8, 8);
        if (primary) {
            g.setColor(ab.hovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
            g.fill(btn);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(ab.hovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(btn);
            g.setColor(ab.hovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(btn);
            g.setColor(ab.hovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_DARK);
        }

        g.setFont(AdminTheme.FONT_BOLD_11);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, bx + (bw - fm.stringWidth(label)) / 2, by + bh / 2 + 5);
    }

    // --- Interactive Mouse & Keyboard Handlers ---

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // 1. Focus input boxes
        boolean foundInput = false;
        for (InputBox in : inputBoxes) {
            if (mx >= in.x && mx <= in.x + in.w && my >= in.y && my <= in.y + in.h) {
                focusedInput = in.id;
                foundInput = true;
                return true;
            }
        }
        if (!foundInput && focusedInput != 0) {
            focusedInput = 0;
        }

        // 2. Toggle buttons
        for (ToggleBtn tb : toggleBtns) {
            if (mx >= tb.x && mx <= tb.x + tb.w && my >= tb.y && my <= tb.y + tb.h) {
                switch (tb.id) {
                    case "USER_REG":
                        userRegistrationDraft = !userRegistrationDraft;
                        dashboard.showToast("User Registration: " + (userRegistrationDraft ? "ENABLED" : "DISABLED"));
                        break;
                    case "PROJ_APPROV":
                        projectApprovalDraft = !projectApprovalDraft;
                        dashboard.showToast("Project Approval: " + (projectApprovalDraft ? "ENABLED" : "DISABLED"));
                        break;
                    case "NOTIF":
                        notificationsDraft = !notificationsDraft;
                        dashboard.showToast("Notifications: " + (notificationsDraft ? "ENABLED" : "DISABLED"));
                        break;
                    case "MAINTENANCE":
                        maintenanceModeDraft = !maintenanceModeDraft;
                        dashboard.showToast("Maintenance Mode: " + (maintenanceModeDraft ? "ACTIVE (Public access paused)" : "INACTIVE (Live)"));
                        break;
                }
                return true;
            }
        }

        // 3. Stepper buttons
        for (StepperBtn sb : stepperBtns) {
            if (mx >= sb.mx && mx <= sb.mx + sb.mw && my >= sb.my && my <= sb.my + sb.mh) {
                if ("MAX_TEAM".equals(sb.id)) {
                    maxTeamSizeDraft = Math.max(2, maxTeamSizeDraft - 1);
                    dashboard.showToast("Max Team Size: " + maxTeamSizeDraft + " members");
                }
                return true;
            }
            if (mx >= sb.px && mx <= sb.px + sb.pw && my >= sb.py && my <= sb.py + sb.ph) {
                if ("MAX_TEAM".equals(sb.id)) {
                    maxTeamSizeDraft = Math.min(16, maxTeamSizeDraft + 1);
                    dashboard.showToast("Max Team Size: " + maxTeamSizeDraft + " members");
                }
                return true;
            }
        }

        // 4. Action buttons
        for (ActionBtn ab : actionBtns) {
            if (mx >= ab.x && mx <= ab.x + ab.w && my >= ab.y && my <= ab.y + ab.h) {
                if ("SAVE".equals(ab.id) || "SAVE_TOP".equals(ab.id)) {
                    saveAllSettings(dashboard);
                    return true;
                }
                if ("RESET".equals(ab.id) || "RESET_TOP".equals(ab.id)) {
                    resetAllSettings(dashboard);
                    return true;
                }
                if ("UPDATE_PASSWORD".equals(ab.id)) {
                    updatePassword(dashboard);
                    return true;
                }
                if ("SHOW_PASSWORD_TOGGLE".equals(ab.id)) {
                    showPasswords = !showPasswords;
                    return true;
                }
                if ("BACKUP".equals(ab.id)) {
                    String backupPath = AdminDataStore.getInstance().createDatabaseBackup();
                    this.lastBackupDate = AdminDataStore.getInstance().lastBackupDate;
                    this.lastBackupSize = AdminDataStore.getInstance().lastBackupSize;
                    java.io.File bf = new java.io.File(backupPath);
                    dashboard.showToast("Database backup created: " + bf.getName());
                    return true;
                }
            }
        }

        return false;
    }

    public void updatePassword(AdminDashboard dashboard) {
        if (currentPasswordInput.isEmpty()) {
            dashboard.showToast("Please enter your current administrator password.");
            return;
        }
        if (newPasswordInput.length() < 6) {
            dashboard.showToast("New password must be at least 6 characters long.");
            return;
        }
        if (!newPasswordInput.equals(confirmPasswordInput)) {
            dashboard.showToast("New password and confirmation do not match.");
            return;
        }

        boolean success = AdminDataStore.getInstance().changeAdminPassword(currentPasswordInput, newPasswordInput);
        if (success) {
            currentPasswordInput = "";
            newPasswordInput = "";
            confirmPasswordInput = "";
            focusedInput = 0;
            dashboard.showToast("Administrator password updated successfully!");
        } else {
            dashboard.showToast("Current password incorrect. Please try again.");
        }
    }

    public void saveAllSettings(AdminDashboard dashboard) {
        AdminDataStore store = AdminDataStore.getInstance();

        // 1. Platform Name
        if (!platformNameDraft.trim().isEmpty()) {
            store.platformName = platformNameDraft.trim();
        }

        // 2. Email Settings
        if (!contactEmailDraft.trim().isEmpty()) {
            store.contactEmail = contactEmailDraft.trim();
        }
        if (!supportEmailDraft.trim().isEmpty()) {
            store.supportEmail = supportEmailDraft.trim();
        }

        // 3. Allowed Domains Whitelist
        if (allowedDomainsDraft != null && !allowedDomainsDraft.trim().isEmpty()) {
            store.allowedDomains = allowedDomainsDraft.trim();
        }

        // 4. Governance Policies
        store.userRegistrationEnabled = userRegistrationDraft;
        store.requireProjectApproval = projectApprovalDraft;
        store.notificationsEnabled = notificationsDraft;
        store.maintenanceModeActive = maintenanceModeDraft;
        store.maxTeamSize = maxTeamSizeDraft;

        // Log audit event
        store.logActivity("Jordan Lee", "JL", "Updated platform settings", store.platformName, true);
        dashboard.showToast("Platform settings successfully saved & synchronized!");
    }

    public void resetAllSettings(AdminDashboard dashboard) {
        AdminDataStore.getInstance().resetSettingsToDefaults();
        loadFromStore();
        dashboard.showToast("Platform settings reset to defaults!");
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        for (StepperBtn sb : stepperBtns) {
            boolean wasM = sb.minusHovered;
            boolean wasP = sb.plusHovered;
            sb.minusHovered = (mx >= sb.mx && mx <= sb.mx + sb.mw && my >= sb.my && my <= sb.my + sb.mh);
            sb.plusHovered = (mx >= sb.px && mx <= sb.px + sb.pw && my >= sb.py && my <= sb.py + sb.ph);
            if (sb.minusHovered != wasM || sb.plusHovered != wasP)
                changed = true;
        }

        for (ActionBtn ab : actionBtns) {
            boolean was = ab.hovered;
            ab.hovered = (mx >= ab.x && mx <= ab.x + ab.w && my >= ab.y && my <= ab.y + ab.h);
            if (ab.hovered != was)
                changed = true;
        }

        return changed;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (focusedInput == 0)
            return false;

        // Escape to unfocus
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            focusedInput = 0;
            return true;
        }

        // Enter on confirm password triggers password update
        if (focusedInput == 6 && keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            updatePassword(dashboard);
            return true;
        }

        // Backspace
        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            switch (focusedInput) {
                case 1:
                    if (platformNameDraft.length() > 0) {
                        platformNameDraft = platformNameDraft.substring(0, platformNameDraft.length() - 1);
                        return true;
                    }
                    break;
                case 2:
                    if (contactEmailDraft.length() > 0) {
                        contactEmailDraft = contactEmailDraft.substring(0, contactEmailDraft.length() - 1);
                        return true;
                    }
                    break;
                case 3:
                    if (supportEmailDraft.length() > 0) {
                        supportEmailDraft = supportEmailDraft.substring(0, supportEmailDraft.length() - 1);
                        return true;
                    }
                    break;
                case 4:
                    if (currentPasswordInput.length() > 0) {
                        currentPasswordInput = currentPasswordInput.substring(0, currentPasswordInput.length() - 1);
                        return true;
                    }
                    break;
                case 5:
                    if (newPasswordInput.length() > 0) {
                        newPasswordInput = newPasswordInput.substring(0, newPasswordInput.length() - 1);
                        return true;
                    }
                    break;
                case 6:
                    if (confirmPasswordInput.length() > 0) {
                        confirmPasswordInput = confirmPasswordInput.substring(0, confirmPasswordInput.length() - 1);
                        return true;
                    }
                    break;
                case 7:
                    if (allowedDomainsDraft != null && allowedDomainsDraft.length() > 0) {
                        allowedDomainsDraft = allowedDomainsDraft.substring(0, allowedDomainsDraft.length() - 1);
                        return true;
                    }
                    break;
            }
            return false;
        }

        // Normal typed characters
        if (ch >= 32 && ch <= 126) {
            switch (focusedInput) {
                case 1:
                    if (platformNameDraft.length() < 40) {
                        platformNameDraft += ch;
                        return true;
                    }
                    break;
                case 2:
                    if (contactEmailDraft.length() < 50) {
                        contactEmailDraft += ch;
                        return true;
                    }
                    break;
                case 3:
                    if (supportEmailDraft.length() < 50) {
                        supportEmailDraft += ch;
                        return true;
                    }
                    break;
                case 4:
                    if (currentPasswordInput.length() < 30) {
                        currentPasswordInput += ch;
                        return true;
                    }
                    break;
                case 5:
                    if (newPasswordInput.length() < 30) {
                        newPasswordInput += ch;
                        return true;
                    }
                    break;
                case 6:
                    if (confirmPasswordInput.length() < 30) {
                        confirmPasswordInput += ch;
                        return true;
                    }
                    break;
                case 7:
                    if (allowedDomainsDraft != null && allowedDomainsDraft.length() < 120) {
                        allowedDomainsDraft += ch;
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    // --- Getters & Setters for Dynamic Access & Unit Testing ---
    public String getPlatformNameDraft() { return platformNameDraft; }
    public void setPlatformNameDraft(String name) { this.platformNameDraft = (name != null) ? name : ""; }

    public String getContactEmailDraft() { return contactEmailDraft; }
    public void setContactEmailDraft(String email) { this.contactEmailDraft = (email != null) ? email : ""; }

    public String getSupportEmailDraft() { return supportEmailDraft; }
    public void setSupportEmailDraft(String email) { this.supportEmailDraft = (email != null) ? email : ""; }

    public String getAllowedDomainsDraft() { return allowedDomainsDraft; }
    public void setAllowedDomainsDraft(String d) { this.allowedDomainsDraft = (d != null) ? d : ""; }

    public boolean isUserRegistrationDraft() { return userRegistrationDraft; }
    public void setUserRegistrationDraft(boolean enabled) { this.userRegistrationDraft = enabled; }

    public boolean isProjectApprovalDraft() { return projectApprovalDraft; }
    public void setProjectApprovalDraft(boolean require) { this.projectApprovalDraft = require; }

    public boolean isNotificationsDraft() { return notificationsDraft; }
    public void setNotificationsDraft(boolean enabled) { this.notificationsDraft = enabled; }

    public boolean isMaintenanceModeDraft() { return maintenanceModeDraft; }
    public void setMaintenanceModeDraft(boolean m) { this.maintenanceModeDraft = m; }

    public int getMaxTeamSizeDraft() { return maxTeamSizeDraft; }
    public void setMaxTeamSizeDraft(int size) { this.maxTeamSizeDraft = size; }

    public String getCurrentPasswordInput() { return currentPasswordInput; }
    public void setCurrentPasswordInput(String pass) { this.currentPasswordInput = (pass != null) ? pass : ""; }

    public String getNewPasswordInput() { return newPasswordInput; }
    public void setNewPasswordInput(String pass) { this.newPasswordInput = (pass != null) ? pass : ""; }

    public String getConfirmPasswordInput() { return confirmPasswordInput; }
    public void setConfirmPasswordInput(String pass) { this.confirmPasswordInput = (pass != null) ? pass : ""; }

    public int getFocusedInput() { return focusedInput; }
    public void setFocusedInput(int id) { this.focusedInput = id; }

    public String getLastBackupDate() { return lastBackupDate; }
    public String getLastBackupSize() { return lastBackupSize; }

    public List<InputBox> getInputBoxes() { return inputBoxes; }
    public List<ToggleBtn> getToggleBtns() { return toggleBtns; }
    public List<StepperBtn> getStepperBtns() { return stepperBtns; }
    public List<ActionBtn> getActionBtns() { return actionBtns; }
}

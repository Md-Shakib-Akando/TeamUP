import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Production-ready Admin Settings View for TeamUp.
 * Contains ONLY the 7 required platform governance features:
 * 1. Platform Name – TeamUp নাম পরিবর্তন।
 * 2. Email Settings – Contact ও Support Email।
 * 3. User Registration – Registration On/Off।
 * 4. Project Approval – Project approval On/Off।
 * 5. Maximum Team Size – Member limit নির্ধারণ।
 * 6. Notifications – Notification On/Off।
 * 7. Change Password – Admin password পরিবর্তন।
 * Plus Save Changes & Reset Defaults.
 *
 * Fully dynamic, interactive, and responsive across all viewports (Mobile 360px up to 4K 2560px).
 * Pure core Java - zero third-party dependencies.
 */
public class AdminSettings {

    // --- 1. Platform Name ---
    private String platformNameDraft;

    // --- 2. Email Settings ---
    private String contactEmailDraft;
    private String supportEmailDraft;

    // --- 3. User Registration ---
    private boolean userRegistrationDraft;

    // --- 4. Project Approval ---
    private boolean projectApprovalDraft;

    // --- 5. Maximum Team Size ---
    private int maxTeamSizeDraft;

    // --- 6. Notifications ---
    private boolean notificationsDraft;

    // --- 7. Change Password ---
    private String currentPasswordInput = "";
    private String newPasswordInput = "";
    private String confirmPasswordInput = "";
    private boolean showPasswords = false;

    // Focus tracking:
    // 0=None, 1=PlatformName, 2=ContactEmail, 3=SupportEmail,
    // 4=CurrentPassword, 5=NewPassword, 6=ConfirmPassword
    private int focusedInput = 0;

    // Hitbox data structures for click & hover detection
    public static class InputBox {
        public int id;
        public int x, y, w, h;
        public boolean isPassword;
        public InputBox(int id, int x, int y, int w, int h, boolean isPassword) {
            this.id = id; this.x = x; this.y = y; this.w = w; this.h = h; this.isPassword = isPassword;
        }
    }

    public static class ToggleBtn {
        public String id;
        public int x, y, w, h;
        public boolean on;
        public ToggleBtn(String id, int x, int y, int w, int h, boolean on) {
            this.id = id; this.x = x; this.y = y; this.w = w; this.h = h; this.on = on;
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
            this.mx = mx; this.my = my; this.mw = mw; this.mh = mh;
            this.px = px; this.py = py; this.pw = pw; this.ph = ph;
        }
    }

    public static class ActionBtn {
        public String id;
        public int x, y, w, h;
        public boolean hovered = false;
        public ActionBtn(String id, int x, int y, int w, int h) {
            this.id = id; this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    private final java.util.List<InputBox> inputBoxes = new java.util.ArrayList<>();
    private final java.util.List<ToggleBtn> toggleBtns = new java.util.ArrayList<>();
    private final java.util.List<StepperBtn> stepperBtns = new java.util.ArrayList<>();
    private final java.util.List<ActionBtn> actionBtns = new java.util.ArrayList<>();

    public AdminSettings() {
        loadFromStore();
    }

    public void loadFromStore() {
        AdminDataStore store = AdminDataStore.getInstance();
        this.platformNameDraft = store.platformName;
        this.contactEmailDraft = store.contactEmail;
        this.supportEmailDraft = store.supportEmail;
        this.userRegistrationDraft = store.userRegistrationEnabled;
        this.projectApprovalDraft = store.requireProjectApproval;
        this.maxTeamSizeDraft = store.maxTeamSize;
        this.notificationsDraft = store.notificationsEnabled;

        this.currentPasswordInput = "";
        this.newPasswordInput = "";
        this.confirmPasswordInput = "";
    }

    public boolean hasUnsavedChanges() {
        AdminDataStore store = AdminDataStore.getInstance();
        if (!platformNameDraft.equals(store.platformName)) return true;
        if (!contactEmailDraft.equals(store.contactEmail)) return true;
        if (!supportEmailDraft.equals(store.supportEmail)) return true;
        if (userRegistrationDraft != store.userRegistrationEnabled) return true;
        if (projectApprovalDraft != store.requireProjectApproval) return true;
        if (maxTeamSizeDraft != store.maxTeamSize) return true;
        if (notificationsDraft != store.notificationsEnabled) return true;
        if (!currentPasswordInput.isEmpty() || !newPasswordInput.isEmpty() || !confirmPasswordInput.isEmpty()) return true;
        return false;
    }

    public int getComputedHeight(int width) {
        boolean mobile = (width < 650);
        int total = 100; // Header
        total += mobile ? 280 : 210; // Card 1: Platform & Emails
        total += mobile ? 300 : 250; // Card 2: Controls & Governance
        total += mobile ? 310 : 250; // Card 3: Change Password
        total += mobile ? 120 : 80;  // Card 4: Action bar
        return total + 60;
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

        // 2. Card 1: General & Email Settings (Platform Name, Contact Email, Support Email)
        curY = paintGeneralAndEmailCard(g, x + padX, curY, contentW);
        curY += 20;

        // 3. Card 2: Platform Controls & Governance (User Reg, Project Approval, Notifications, Max Team Size)
        curY = paintControlsAndTeamCard(g, x + padX, curY, contentW);
        curY += 20;

        // 4. Card 3: Security & Change Password (Current Pass, New Pass, Confirm Pass)
        curY = paintChangePasswordCard(g, x + padX, curY, contentW);
        curY += 20;

        // 5. Card 4: Save Changes & Reset Action Bar
        paintBottomActionBar(g, x + padX, curY, contentW);
    }

    private int paintHeader(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_HEADING);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Platform Settings", x, y + 24);

        int titleW = g.getFontMetrics().stringWidth("Platform Settings");

        // Status badge: Active
        if (w >= 650) {
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
        String sub = "Configure platform name, communication emails, registration, project approvals, team limits, and administrator password.";
        if (w < 700) sub = "Manage platform name, emails, user access, team limits, and password.";
        g.drawString(sub, x, y + 48);

        // Top right quick actions: Save & Reset (if w >= 800)
        if (w >= 800) {
            int btnH = 34;
            int saveW = 140;
            int resetW = 125;
            int saveX = x + w - saveW;
            int resetX = saveX - resetW - 10;
            int btnY = y + 10;

            paintButton(g, "RESET_TOP", resetX, btnY, resetW, btnH, "↺ Reset Defaults", false);
            boolean unsaved = hasUnsavedChanges();
            paintButton(g, "SAVE_TOP", saveX, btnY, saveW, btnH, unsaved ? "💾 Save Changes *" : "💾 Save Changes", true);
        }

        return y + 54;
    }

    // --- CARD 1: General & Email Settings ---
    private int paintGeneralAndEmailCard(Graphics2D g, int x, int y, int w) {
        boolean mobile = (w < 650);
        int cardH = mobile ? 270 : 205;
        paintCardContainer(g, x, y, w, cardH, "General & Email Settings", "Set platform brand title and official contact channels.");

        int cx = x + 24;
        int cy = y + 68;
        int innerW = w - 48;

        // 1. Platform Name
        paintInputField(g, 1, cx, cy, innerW, 36, "Platform Name", platformNameDraft, "Enter platform brand name (e.g. TeamUp)", false);

        cy += 64;

        // 2. Email Settings: Contact Email & Support Email
        if (!mobile) {
            int fieldW = (innerW - 16) / 2;
            paintInputField(g, 2, cx, cy, fieldW, 36, "Contact Email", contactEmailDraft, "e.g. contact@teamup.edu", false);
            paintInputField(g, 3, cx + fieldW + 16, cy, fieldW, 36, "Support & Helpdesk Email", supportEmailDraft, "e.g. support@teamup.edu", false);
        } else {
            paintInputField(g, 2, cx, cy, innerW, 34, "Contact Email", contactEmailDraft, "e.g. contact@teamup.edu", false);
            cy += 58;
            paintInputField(g, 3, cx, cy, innerW, 34, "Support Email", supportEmailDraft, "e.g. support@teamup.edu", false);
        }

        return y + cardH;
    }

    // --- CARD 2: Platform Controls & Governance ---
    private int paintControlsAndTeamCard(Graphics2D g, int x, int y, int w) {
        boolean mobile = (w < 650);
        int cardH = mobile ? 290 : 240;
        paintCardContainer(g, x, y, w, cardH, "Platform Controls & Team Capacity", "Manage user registration, project review gates, notifications, and maximum member limits.");

        int cx = x + 24;
        int cy = y + 68;
        int innerW = w - 48;

        if (!mobile) {
            int colW = (innerW - 20) / 2;

            // Row 1: User Registration & Project Approval
            paintToggleRow(g, "USER_REG", cx, cy, colW, "User Registration", "Allow new student and member signups", userRegistrationDraft);
            paintToggleRow(g, "PROJ_APPROV", cx + colW + 20, cy, colW, "Project Approval", "Require admin review before projects are published", projectApprovalDraft);

            cy += 56;

            // Row 2: Notifications & Maximum Team Size
            paintToggleRow(g, "NOTIF", cx, cy, colW, "System Notifications", "Enable platform alerts, invites, and notifications", notificationsDraft);
            paintStepperRow(g, "MAX_TEAM", cx + colW + 20, cy, colW, "Maximum Team Size", maxTeamSizeDraft, "members (limit)");
        } else {
            paintToggleRow(g, "USER_REG", cx, cy, innerW, "User Registration", "Allow new registrations", userRegistrationDraft);
            cy += 48;
            paintToggleRow(g, "PROJ_APPROV", cx, cy, innerW, "Project Approval", "Require pre-approval", projectApprovalDraft);
            cy += 48;
            paintToggleRow(g, "NOTIF", cx, cy, innerW, "Notifications", "Enable platform alerts", notificationsDraft);
            cy += 48;
            paintStepperRow(g, "MAX_TEAM", cx, cy, innerW, "Max Team Size", maxTeamSizeDraft, "members");
        }

        return y + cardH;
    }

    // --- CARD 3: Change Password ---
    private int paintChangePasswordCard(Graphics2D g, int x, int y, int w) {
        boolean mobile = (w < 650);
        int cardH = mobile ? 300 : 235;
        paintCardContainer(g, x, y, w, cardH, "Change Administrator Password", "Update credentials for your administrator account (minimum 6 characters).");

        int cx = x + 24;
        int cy = y + 68;
        int innerW = w - 48;

        if (!mobile) {
            int fieldW = (innerW - 24) / 3;
            paintInputField(g, 4, cx, cy, fieldW, 36, "Current Password", currentPasswordInput, "Enter current password", !showPasswords);
            paintInputField(g, 5, cx + fieldW + 12, cy, fieldW, 36, "New Password", newPasswordInput, "Enter new password (6+ chars)", !showPasswords);
            paintInputField(g, 6, cx + (fieldW + 12) * 2, cy, fieldW, 36, "Confirm New Password", confirmPasswordInput, "Re-enter new password", !showPasswords);

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
            paintInputField(g, 4, cx, cy, innerW, 34, "Current Password", currentPasswordInput, "Current password", !showPasswords);
            cy += 56;
            paintInputField(g, 5, cx, cy, innerW, 34, "New Password", newPasswordInput, "New password (6+ chars)", !showPasswords);
            cy += 56;
            paintInputField(g, 6, cx, cy, innerW, 34, "Confirm Password", confirmPasswordInput, "Confirm new password", !showPasswords);
            cy += 56;
            paintButton(g, "UPDATE_PASSWORD", cx, cy, innerW, 34, "🔒 Update Password", true);
        }

        return y + cardH;
    }

    // --- CARD 4: Save & Reset Bottom Action Bar ---
    private void paintBottomActionBar(Graphics2D g, int x, int y, int w) {
        boolean mobile = (w < 650);
        int barH = mobile ? 116 : 70;

        RoundRectangle2D bar = new RoundRectangle2D.Float(x, y, w, barH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(bar);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(bar);

        int cx = x + 20;
        int cy = y + (mobile ? 14 : 18);
        boolean unsaved = hasUnsavedChanges();

        // Status Indicator
        g.setFont(AdminTheme.FONT_BOLD_12);
        if (unsaved) {
            g.setColor(new Color(0xD9, 0x77, 0x06)); // Amber
            g.drawString("● Unsaved changes pending", cx, cy + 18);
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("Click 'Save Platform Settings' to persist updates.", cx, cy + 32);
        } else {
            g.setColor(new Color(0x05, 0x96, 0x69)); // Emerald Mint
            g.drawString("✓ All settings up to date", cx, cy + 18);
            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("Platform configurations synchronized with AdminDataStore.", cx, cy + 32);
        }

        // Action Buttons: Reset & Save
        int saveW = 175;
        int resetW = 135;

        if (!mobile) {
            int saveX = x + w - saveW - 20;
            int resetX = saveX - resetW - 10;
            int btnY = y + 17;

            paintButton(g, "RESET", resetX, btnY, resetW, 36, "↺ Reset Defaults", false);
            paintButton(g, "SAVE", saveX, btnY, saveW, 36, "💾 Save Settings", true);
        } else {
            int btnY = y + 60;
            int halfW = (w - 50) / 2;
            paintButton(g, "RESET", cx, btnY, halfW, 34, "↺ Reset Defaults", false);
            paintButton(g, "SAVE", cx + halfW + 10, btnY, halfW, 34, "💾 Save Settings", true);
        }
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

    private void paintInputField(Graphics2D g, int inputId, int fx, int fy, int fw, int fh, String label, String value, String placeholder, boolean mask) {
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
        if (value != null && !value.isEmpty()) {
            g.setColor(AdminTheme.TEXT_DARK);
            String displayVal = mask ? value.replaceAll(".", "•") : value;
            g.drawString(displayVal + (focused ? "|" : ""), fx + 12, boxY + 22);
        } else {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(focused ? "|" : placeholder, fx + 12, boxY + 22);
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
        g.drawString(drawSub, rx, ry + 30);
    }

    private void paintStepperRow(Graphics2D g, String id, int rx, int ry, int rw, String title, int val, String unit) {
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString(title, rx, ry + 14);

        int btnSize = 24;
        int stepBoxW = 135;
        int stepBoxX = rx + rw - stepBoxW;
        int stepBoxY = ry;

        RoundRectangle2D sBox = new RoundRectangle2D.Float(stepBoxX, stepBoxY, stepBoxW, btnSize, 6, 6);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(sBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(sBox);

        // Minus
        int mx = stepBoxX + 2;
        int my = stepBoxY + 2;
        int mw = 20, mh = 20;

        // Plus
        int px = stepBoxX + stepBoxW - 22;
        int py = stepBoxY + 2;
        int pw = 20, ph = 20;

        StepperBtn sb = new StepperBtn(id, mx, my, mw, mh, px, py, pw, ph);
        stepperBtns.add(sb);

        // Minus icon
        g.setColor(sb.minusHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_MUTED);
        g.fillRoundRect(mx, my, mw, mh, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.drawString("-", mx + 7, my + 14);

        // Value text
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_DARK);
        String valStr = val + " " + unit;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(valStr, stepBoxX + (stepBoxW - fm.stringWidth(valStr)) / 2, stepBoxY + 16);

        // Plus icon
        g.setColor(sb.plusHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_MUTED);
        g.fillRoundRect(px, py, pw, ph, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.drawString("+", px + 5, py + 15);
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
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(btn);
            g.setColor(AdminTheme.TEXT_DARK);
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

        // 3. User Registration
        store.userRegistrationEnabled = userRegistrationDraft;

        // 4. Project Approval
        store.requireProjectApproval = projectApprovalDraft;

        // 5. Maximum Team Size
        store.maxTeamSize = maxTeamSizeDraft;

        // 6. Notifications
        store.notificationsEnabled = notificationsDraft;

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
            if (sb.minusHovered != wasM || sb.plusHovered != wasP) changed = true;
        }

        for (ActionBtn ab : actionBtns) {
            boolean was = ab.hovered;
            ab.hovered = (mx >= ab.x && mx <= ab.x + ab.w && my >= ab.y && my <= ab.y + ab.h);
            if (ab.hovered != was) changed = true;
        }

        return changed;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (focusedInput == 0) return false;

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
            }
        }

        return false;
    }

    // Getters and setters for automated tests
    public int getFocusedInput() { return focusedInput; }
    public void setFocusedInput(int id) { this.focusedInput = id; }
    public String getPlatformNameDraft() { return platformNameDraft; }
    public void setPlatformNameDraft(String name) { this.platformNameDraft = name; }
    public String getContactEmailDraft() { return contactEmailDraft; }
    public void setContactEmailDraft(String email) { this.contactEmailDraft = email; }
    public String getSupportEmailDraft() { return supportEmailDraft; }
    public void setSupportEmailDraft(String email) { this.supportEmailDraft = email; }
    public boolean isUserRegistrationDraft() { return userRegistrationDraft; }
    public void setUserRegistrationDraft(boolean val) { this.userRegistrationDraft = val; }
    public boolean isProjectApprovalDraft() { return projectApprovalDraft; }
    public void setProjectApprovalDraft(boolean val) { this.projectApprovalDraft = val; }
    public int getMaxTeamSizeDraft() { return maxTeamSizeDraft; }
    public void setMaxTeamSizeDraft(int size) { this.maxTeamSizeDraft = size; }
    public boolean isNotificationsDraft() { return notificationsDraft; }
    public void setNotificationsDraft(boolean val) { this.notificationsDraft = val; }
    public String getCurrentPasswordInput() { return currentPasswordInput; }
    public void setCurrentPasswordInput(String pass) { this.currentPasswordInput = pass; }
    public String getNewPasswordInput() { return newPasswordInput; }
    public void setNewPasswordInput(String pass) { this.newPasswordInput = pass; }
    public String getConfirmPasswordInput() { return confirmPasswordInput; }
    public void setConfirmPasswordInput(String pass) { this.confirmPasswordInput = pass; }
}

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Settings for TeamUp — a settings-list layout (label + help on the left,
 * control on the right; stacked on narrow screens) in two cards:
 *
 * 1. Access & policies: registration, project approval, notifications, maintenance mode
 *    (amber warning while on) and the maximum team size stepper.
 * 2. Admin password: current / new (strength meter) / confirm (match check), show/hide, update.
 *
 * The General card (platform name, emails, domains) was removed from the page by request;
 * its drafts are still loaded and sent with saveAllSettings() so stored values are kept.
 *
 * Toggles and the stepper save immediately; text fields save on Enter or when focus leaves.
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

    // =========================================================================
    // LAYOUT & PAINTING
    // Settings-list pattern: every card is a list of rows with the label + help
    // text on the left and the control on the right (stacked on narrow widths).
    // The same layout pass measures the page height (measuring = true).
    // =========================================================================
    private boolean measuring = false;
    private static final int MAX_CONTENT_W = 1100;
    private static final Color FIELD_BG = new Color(0xF8, 0xFA, 0xFC);
    private static final Color LABEL_INK = new Color(0x33, 0x41, 0x55);

    public int getComputedHeight(int width) {
        java.awt.image.BufferedImage scratch = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scratch.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        measuring = true;
        try {
            return layout(g, 0, 0, width) + 40;
        } finally {
            measuring = false;
            g.dispose();
        }
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        lastActions = new ArrayList<>(actionBtns);
        inputBoxes.clear();
        toggleBtns.clear();
        stepperBtns.clear();
        actionBtns.clear();
        layout(g, x, y, width);
    }

    /** Lays out (and, unless measuring, paints) the whole page. Returns the bottom y. */
    private int layout(Graphics2D g, int x, int y, int width) {
        int padX = (width < 600) ? 16 : 28;
        int contentW = Math.min(MAX_CONTENT_W, width - padX * 2);
        int cx = x + padX;
        int curY = y + 24;

        curY = paintHeader(g, cx, curY, contentW) + 24;
        curY = paintAccessCard(g, cx, curY, contentW) + 20;
        curY = paintSecurityCard(g, cx, curY, contentW);
        return curY;
    }

    private int paintHeader(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_HEADING);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Settings", x, y + 24);

        g.setFont(AdminTheme.FONT_REG_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(fit(g, "Who can join, what needs approval, team limits, and your admin security.", w), x, y + 48);

        // Status pill: changes are applied as you go (toggles instantly, fields on Enter / blur)
        boolean editing = focusedInput != 0;
        String status = editing ? "Editing — press Enter to save" : "Changes save automatically";
        g.setFont(AdminTheme.FONT_BOLD_11);
        int pw = g.getFontMetrics().stringWidth(status) + 34;
        boolean wide = w >= 760;
        int px = wide ? x + w - pw : x;
        int py = wide ? y + 8 : y + 62;
        if (!measuring) {
            g.setColor(editing ? AdminTheme.STATUS_PENDING_BG : AdminTheme.STATUS_GREEN_BG);
            g.fill(new RoundRectangle2D.Float(px, py, pw, 26, 26, 26));
            g.setColor(editing ? AdminTheme.STATUS_PENDING_TXT : AdminTheme.STATUS_GREEN_TEXT);
            g.fillOval(px + 12, py + 10, 7, 7);
            g.drawString(status, px + 24, py + 17);
        }

        // Kept as hidden entries (the Save / Reset bar was removed by request)
        addAction(new ActionBtn("RESET_TOP", -9999, -9999, 0, 0));
        addAction(new ActionBtn("SAVE_TOP", -9999, -9999, 0, 0));
        return wide ? y + 54 : y + 90;
    }

    // ---------------------------------------------------------------- cards

    private interface CardBody {
        int paint(Graphics2D g, int x, int y, int w); // returns the body's bottom y
    }

    /** Measures the card body, paints the white card, then the body on top, then the border. */
    private int card(Graphics2D g, int x, int y, int w, CardBody body) {
        boolean outer = measuring;
        measuring = true;
        int bottom = body.paint(g, x, y, w);
        measuring = outer;
        if (!measuring) {
            RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, bottom - y, 16, 16);
            g.setColor(Color.WHITE);
            g.fill(shape);
            body.paint(g, x, y, w);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);
        }
        return bottom;
    }

    private int paintAccessCard(Graphics2D g, int x, int y, int w) {
        return card(g, x, y, w, this::accessBody);
    }

    private int paintSecurityCard(Graphics2D g, int x, int y, int w) {
        return card(g, x, y, w, this::securityBody);
    }

    private int accessBody(Graphics2D g, int x, int y, int w) {
        y = cardHeader(g, x, y, w, "Access & policies", "Control who can join and what needs an admin's approval.", 1,
                AdminTheme.BRAND_TINT, AdminTheme.BRAND_PRIMARY);
        y = toggleRow(g, x, y, w, "USER_REG", "User registration", "Allow new students to create accounts.",
                userRegistrationDraft, false, true);
        y = toggleRow(g, x, y, w, "PROJ_APPROV", "Project approval", "New projects wait for an admin review before going live.",
                projectApprovalDraft, false, true);
        y = toggleRow(g, x, y, w, "NOTIF", "System notifications", "Email and in-app alerts sent by the platform.",
                notificationsDraft, false, true);
        y = toggleRow(g, x, y, w, "MAINTENANCE", "Maintenance mode", "Temporarily blocks student access while you upgrade.",
                maintenanceModeDraft, true, true);
        y = stepperRow(g, x, y, w);
        return y + 4;
    }

    private int securityBody(Graphics2D g, int x, int y, int w) {
        y = cardHeader(g, x, y, w, "Admin password", "Change the password for your administrator account.", 2,
                AdminTheme.STATUS_GREEN_BG, AdminTheme.STATUS_GREEN_TEXT);
        y = inputRow(g, x, y, w, "Current password", "Required to confirm it's you.",
                4, currentPasswordInput, "Current password", !showPasswords, true);
        y = newPasswordRow(g, x, y, w);
        y = confirmRow(g, x, y, w);

        // footer: show/hide + update
        int pad = 24;
        int by = y + 18;
        g.setFont(AdminTheme.FONT_BOLD_12);
        FontMetrics fm = g.getFontMetrics();
        String upd = "Update password";
        int bw = fm.stringWidth(upd) + 40;
        int bx = x + w - pad - bw;
        paintButton(g, "UPDATE_PASSWORD", bx, by, bw, 38, upd, true);

        String toggle = showPasswords ? "Hide passwords" : "Show passwords";
        int tw = fm.stringWidth(toggle);
        int tx = x + pad;
        if (!measuring) {
            g.setColor(AdminTheme.BORDER_COLOR);
            g.drawRoundRect(tx, by + 11, 16, 16, 5, 5);
            if (showPasswords) {
                g.setColor(AdminTheme.BRAND_PRIMARY);
                g.fillRoundRect(tx, by + 11, 16, 16, 5, 5);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(tx + 4, by + 19, tx + 7, by + 22);
                g.drawLine(tx + 7, by + 22, tx + 12, by + 15);
                g.setStroke(new BasicStroke(1f));
            }
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(LABEL_INK);
            g.drawString(toggle, tx + 24, by + 24);
        }
        addAction(new ActionBtn("SHOW_PASSWORD_TOGGLE", tx - 4, by + 4, tw + 36, 30));
        return by + 38 + 20;
    }

    // ---------------------------------------------------------------- card chrome

    /** Paints the card header (icon, title, subtitle, divider) and returns the y where rows start. */
    private int cardHeader(Graphics2D g, int x, int y, int w, String title, String sub, int icon, Color tint, Color ink) {
        int pad = 24;
        if (!measuring) {
            g.setColor(tint);
            g.fill(new RoundRectangle2D.Float(x + pad, y + 22, 38, 38, 12, 12));
            paintCardIcon(g, icon, x + pad + 11, y + 33, ink);
            g.setFont(AdminTheme.FONT_SECTION);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(title, x + pad + 52, y + 38);
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(fit(g, sub, w - pad * 2 - 52), x + pad + 52, y + 56);
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.drawLine(x + 1, y + 80, x + w - 1, y + 80);
        }
        return y + 80;
    }

    private void paintCardIcon(Graphics2D g, int type, int x, int y, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (type == 0) { // globe
            g.drawOval(x, y, 16, 16);
            g.drawLine(x, y + 8, x + 16, y + 8);
            g.drawArc(x + 4, y, 8, 16, 90, 180);
            g.drawArc(x + 4, y, 8, 16, 270, 180);
        } else if (type == 1) { // shield
            java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
            p.moveTo(x + 8, y);
            p.lineTo(x + 16, y + 3);
            p.lineTo(x + 15, y + 10);
            p.quadTo(x + 13, y + 15, x + 8, y + 17);
            p.quadTo(x + 3, y + 15, x + 1, y + 10);
            p.lineTo(x, y + 3);
            p.closePath();
            g.draw(p);
            g.drawLine(x + 5, y + 8, x + 7, y + 11);
            g.drawLine(x + 7, y + 11, x + 11, y + 6);
        } else { // lock
            g.drawRoundRect(x + 1, y + 7, 14, 10, 3, 3);
            g.drawArc(x + 4, y, 8, 12, 0, 180);
            g.drawLine(x + 4, y + 6, x + 4, y + 8);
            g.drawLine(x + 12, y + 6, x + 12, y + 8);
        }
        g.setStroke(new BasicStroke(1f));
    }

    // ---------------------------------------------------------------- rows

    /** Geometry for a settings row: {labelX, labelW, controlX, controlW, wide ? 1 : 0}. */
    private int[] rowGeometry(int x, int w) {
        int pad = 24;
        int inner = w - pad * 2;
        boolean wide = inner >= 640;
        int labelW = wide ? Math.min(300, inner * 2 / 5) : inner;
        int controlX = wide ? x + pad + labelW + 32 : x + pad;
        int controlW = wide ? Math.min(560, x + w - pad - controlX) : inner;
        return new int[] { x + pad, labelW, controlX, controlW, wide ? 1 : 0 };
    }

    /** Paints the label + help text; returns its height. */
    private int paintRowLabel(Graphics2D g, int lx, int y, int lw, String label, String help) {
        if (!measuring) {
            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(label, lx, y + 14);
        }
        g.setFont(AdminTheme.FONT_REG_12);
        List<String> lines = wrap(g, help, lw, 2);
        int ly = y + 32;
        for (String l : lines) {
            if (!measuring) {
                g.setColor(AdminTheme.TEXT_MUTED);
                g.drawString(l, lx, ly);
            }
            ly += 17;
        }
        return 18 + lines.size() * 17;
    }

    private void rowDivider(Graphics2D g, int x, int y, int w) {
        if (!measuring) {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.drawLine(x + 24, y, x + w - 24, y);
        }
    }

    private int inputRow(Graphics2D g, int x, int y, int w, String label, String help, int id, String value,
            String placeholder, boolean mask, boolean divider) {
        int[] r = rowGeometry(x, w);
        int top = y + 20;
        int labelH = paintRowLabel(g, r[0], top, r[1], label, help);
        int fieldY = r[4] == 1 ? top : top + labelH + 10;
        paintInputField(g, id, r[2], fieldY, r[3], 40, value, placeholder, mask);
        int bottom = Math.max(top + labelH, fieldY + 40) + 20;
        if (divider) rowDivider(g, x, bottom, w);
        return bottom;
    }

    private int toggleRow(Graphics2D g, int x, int y, int w, String id, String label, String help, boolean on,
            boolean warnWhenOn, boolean divider) {
        int[] r = rowGeometry(x, w);
        int top = y + 20;
        int labelH = paintRowLabel(g, r[0], top, r[1], label, help);
        int ty = r[4] == 1 ? top + 4 : top + labelH + 8;
        int togW = 46, togH = 26;
        Color onColor = warnWhenOn ? new Color(0xD9, 0x77, 0x06) : AdminTheme.BRAND_PRIMARY;
        if (!measuring) {
            RoundRectangle2D track = new RoundRectangle2D.Float(r[2], ty, togW, togH, togH, togH);
            g.setColor(on ? onColor : new Color(0xCB, 0xD5, 0xE1));
            g.fill(track);
            int knob = togH - 6;
            int kx = on ? r[2] + togW - knob - 3 : r[2] + 3;
            g.setColor(new Color(15, 23, 42, 30));
            g.fillOval(kx, ty + 4, knob, knob);
            g.setColor(Color.WHITE);
            g.fillOval(kx, ty + 3, knob, knob);

            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(on ? (warnWhenOn ? onColor : AdminTheme.TEXT_DARK) : AdminTheme.TEXT_MUTED);
            String state = on ? (warnWhenOn ? "On — students can't sign in" : "Enabled") : (warnWhenOn ? "Off — platform is live" : "Disabled");
            g.drawString(fit(g, state, r[3] - togW - 14), r[2] + togW + 14, ty + 18);
        }
        if (!measuring) toggleBtns.add(new ToggleBtn(id, r[2], ty, togW, togH, on));
        int bottom = Math.max(top + labelH, ty + togH) + 20;
        if (divider) rowDivider(g, x, bottom, w);
        return bottom;
    }

    private int stepperRow(Graphics2D g, int x, int y, int w) {
        int[] r = rowGeometry(x, w);
        int top = y + 20;
        int labelH = paintRowLabel(g, r[0], top, r[1], "Maximum team size", "Largest team a project can recruit (2–16 members).");
        int sy = r[4] == 1 ? top : top + labelH + 8;
        int b = 36, valW = 64;
        int mx = r[2], vx = mx + b, px = vx + valW;

        if (!measuring) {
            stepperBtns.add(new StepperBtn("MAX_TEAM", mx, sy, b, b, px, sy, b, b));
            boolean minDisabled = maxTeamSizeDraft <= 2, maxDisabled = maxTeamSizeDraft >= 16;
            RoundRectangle2D whole = new RoundRectangle2D.Float(mx, sy, b * 2 + valW, b, 10, 10);
            g.setColor(Color.WHITE);
            g.fill(whole);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.1f));
            g.draw(whole);
            g.drawLine(vx, sy, vx, sy + b);
            g.drawLine(px, sy, px, sy + b);
            g.setColor(minDisabled ? AdminTheme.TEXT_LIGHT : (lastMinusHover ? AdminTheme.BRAND_PRIMARY : LABEL_INK));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(mx + 12, sy + b / 2, mx + b - 12, sy + b / 2);
            g.setColor(maxDisabled ? AdminTheme.TEXT_LIGHT : (lastPlusHover ? AdminTheme.BRAND_PRIMARY : LABEL_INK));
            g.drawLine(px + 12, sy + b / 2, px + b - 12, sy + b / 2);
            g.drawLine(px + b / 2, sy + 12, px + b / 2, sy + b - 12);
            g.setStroke(new BasicStroke(1f));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g.setColor(AdminTheme.TEXT_DARK);
            String v = String.valueOf(maxTeamSizeDraft);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(v, vx + (valW - fm.stringWidth(v)) / 2, sy + 24);
            g.setFont(AdminTheme.FONT_REG_12);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("members per team", px + b + 14, sy + 23);
        }
        return Math.max(top + labelH, sy + b) + 20;
    }

    private boolean lastMinusHover = false, lastPlusHover = false;

    private int newPasswordRow(Graphics2D g, int x, int y, int w) {
        int[] r = rowGeometry(x, w);
        int top = y + 20;
        int labelH = paintRowLabel(g, r[0], top, r[1], "New password", "At least 6 characters. Mix letters, numbers and symbols.");
        int fieldY = r[4] == 1 ? top : top + labelH + 10;
        paintInputField(g, 5, r[2], fieldY, r[3], 40, newPasswordInput, "New password", !showPasswords);

        // strength meter
        int my = fieldY + 40 + 10;
        int score = passwordScore(newPasswordInput);
        String[] names = { "Too short", "Weak", "Fair", "Good", "Strong" };
        Color[] cols = { AdminTheme.STATUS_RED_TEXT, AdminTheme.STATUS_RED_TEXT, AdminTheme.STATUS_PENDING_TXT,
                AdminTheme.BRAND_SECONDARY, AdminTheme.STATUS_GREEN_TEXT };
        if (!measuring && !newPasswordInput.isEmpty()) {
            int segW = Math.min(64, (r[3] - 100) / 4);
            for (int i = 0; i < 4; i++) {
                g.setColor(i < score ? cols[score] : new Color(0xE2, 0xE8, 0xF0));
                g.fill(new RoundRectangle2D.Float(r[2] + i * (segW + 4), my + 5, segW, 5, 5, 5));
            }
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(cols[score]);
            g.drawString(names[score], r[2] + 4 * (segW + 4) + 8, my + 12);
        }
        int bottom = Math.max(top + labelH, newPasswordInput.isEmpty() ? fieldY + 40 : my + 16) + 20;
        rowDivider(g, x, bottom, w);
        return bottom;
    }

    private int confirmRow(Graphics2D g, int x, int y, int w) {
        int[] r = rowGeometry(x, w);
        int top = y + 20;
        int labelH = paintRowLabel(g, r[0], top, r[1], "Confirm new password", "Type the new password again.");
        int fieldY = r[4] == 1 ? top : top + labelH + 10;
        paintInputField(g, 6, r[2], fieldY, r[3], 40, confirmPasswordInput, "Repeat new password", !showPasswords);
        int bottom = fieldY + 40;
        if (!confirmPasswordInput.isEmpty()) {
            boolean match = confirmPasswordInput.equals(newPasswordInput);
            if (!measuring) {
                g.setFont(AdminTheme.FONT_BOLD_11);
                g.setColor(match ? AdminTheme.STATUS_GREEN_TEXT : AdminTheme.STATUS_RED_TEXT);
                g.drawString(match ? "✓ Passwords match" : "✕ Passwords don't match", r[2], fieldY + 40 + 22);
            }
            bottom += 28;
        }
        bottom = Math.max(top + labelH, bottom) + 4;
        rowDivider(g, x, bottom + 14, w);
        return bottom;
    }

    /** 0 = too short (< 6), then 1–4 by length and character variety. */
    private static int passwordScore(String p) {
        if (p == null || p.length() < 6) return 0;
        int variety = 0;
        if (p.matches(".*[a-z].*")) variety++;
        if (p.matches(".*[A-Z].*")) variety++;
        if (p.matches(".*[0-9].*")) variety++;
        if (p.matches(".*[^A-Za-z0-9].*")) variety++;
        int score = 1;
        if (p.length() >= 8 && variety >= 2) score = 2;
        if (p.length() >= 10 && variety >= 3) score = 3;
        if (p.length() >= 12 && variety >= 4) score = 4;
        return score;
    }

    // ---------------------------------------------------------------- controls

    private void paintInputField(Graphics2D g, int inputId, int fx, int fy, int fw, int fh, String value,
            String placeholder, boolean mask) {
        if (measuring) return;
        boolean focused = (focusedInput == inputId);
        if (focused) {
            g.setColor(new Color(0xEA, 0x4C, 0x89, 28));
            g.setStroke(new BasicStroke(4f));
            g.draw(new RoundRectangle2D.Float(fx - 1, fy - 1, fw + 2, fh + 2, 12, 12));
        }
        RoundRectangle2D box = new RoundRectangle2D.Float(fx, fy, fw, fh, 10, 10);
        g.setColor(focused ? Color.WHITE : FIELD_BG);
        g.fill(box);
        g.setColor(focused ? AdminTheme.BRAND_PRIMARY : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(focused ? 1.6f : 1.1f));
        g.draw(box);
        g.setStroke(new BasicStroke(1f));
        inputBoxes.add(new InputBox(inputId, fx, fy, fw, fh, mask));

        g.setFont(AdminTheme.FONT_REG_13);
        FontMetrics fm = g.getFontMetrics();
        int tx = fx + 14;
        int maxW = fw - 28;
        int base = fy + (fh + fm.getAscent() - fm.getDescent()) / 2;
        boolean caret = focused && (System.currentTimeMillis() % 1000 < 550);
        if (value != null && !value.isEmpty()) {
            String shown = mask ? value.replaceAll(".", "•") : value;
            while (shown.length() > 1 && fm.stringWidth(shown) > maxW) shown = shown.substring(1); // keep the caret end visible
            g.setColor(AdminTheme.TEXT_DARK);
            g.drawString(shown, tx, base);
            if (caret) {
                g.setColor(AdminTheme.BRAND_PRIMARY);
                int cx = tx + fm.stringWidth(shown) + 1;
                g.drawLine(cx, fy + 11, cx, fy + fh - 11);
            }
        } else {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(fit(g, placeholder, maxW), tx, base);
            if (caret) {
                g.setColor(AdminTheme.BRAND_PRIMARY);
                g.drawLine(tx, fy + 11, tx, fy + fh - 11);
            }
        }
    }

    private void paintButton(Graphics2D g, String id, int bx, int by, int bw, int bh, String label, boolean primary) {
        ActionBtn ab = new ActionBtn(id, bx, by, bw, bh);
        for (ActionBtn old : lastActions) if (old.id.equals(id)) ab.hovered = old.hovered;
        addAction(ab);
        if (measuring) return;
        RoundRectangle2D btn = new RoundRectangle2D.Float(bx, by, bw, bh, 10, 10);
        if (primary) {
            g.setColor(ab.hovered ? AdminTheme.BRAND_HOVER : AdminTheme.BRAND_PRIMARY);
            g.fill(btn);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(ab.hovered ? FIELD_BG : Color.WHITE);
            g.fill(btn);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(btn);
            g.setColor(AdminTheme.TEXT_DARK);
        }
        g.setFont(AdminTheme.FONT_BOLD_12);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, bx + (bw - fm.stringWidth(label)) / 2, by + (bh + fm.getAscent() - fm.getDescent()) / 2);
    }

    /** Previous frame's buttons, so hover state survives the per-frame rebuild. */
    private List<ActionBtn> lastActions = new ArrayList<>();

    private void addAction(ActionBtn ab) {
        if (!measuring) actionBtns.add(ab);
    }

    // ---------------------------------------------------------------- text helpers

    private static String fit(Graphics2D g, String s, int maxW) {
        if (s == null) return "";
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > maxW) t = t.substring(0, t.length() - 1);
        return t.trim() + "…";
    }

    private static List<String> wrap(Graphics2D g, String text, int maxW, int maxLines) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) return out;
        FontMetrics fm = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            String cand = line.length() == 0 ? words[i] : line + " " + words[i];
            if (fm.stringWidth(cand) <= maxW || line.length() == 0) {
                line.setLength(0);
                line.append(cand);
            } else {
                out.add(line.toString());
                line.setLength(0);
                line.append(words[i]);
                if (out.size() == maxLines - 1) {
                    StringBuilder rest = new StringBuilder(line);
                    for (int k = i + 1; k < words.length; k++) rest.append(' ').append(words[k]);
                    out.add(fit(g, rest.toString(), maxW));
                    return out;
                }
            }
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }

    // --- Interactive Mouse & Keyboard Handlers ---

    private void commitInput(int id, AdminDashboard dashboard) {
        AdminDataStore store = AdminDataStore.getInstance();
        if (id == 1 && !platformNameDraft.trim().isEmpty() && !platformNameDraft.equals(store.platformName)) {
            store.platformName = platformNameDraft.trim();
            AdminSync.syncUpdateSetting("platformName", store.platformName);
        } else if (id == 2 && !contactEmailDraft.trim().isEmpty() && !contactEmailDraft.equals(store.contactEmail)) {
            store.contactEmail = contactEmailDraft.trim();
            AdminSync.syncUpdateSetting("contactEmail", store.contactEmail);
        } else if (id == 3 && !supportEmailDraft.trim().isEmpty() && !supportEmailDraft.equals(store.supportEmail)) {
            store.supportEmail = supportEmailDraft.trim();
            AdminSync.syncUpdateSetting("supportEmail", store.supportEmail);
        } else if (id == 7 && allowedDomainsDraft != null && !allowedDomainsDraft.equals(store.allowedDomains)) {
            store.allowedDomains = allowedDomainsDraft.trim();
            AdminSync.syncUpdateSetting("allowedDomains", store.allowedDomains);
        }
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // 1. Focus input boxes
        boolean foundInput = false;
        int prevFocused = focusedInput;
        for (InputBox in : inputBoxes) {
            if (mx >= in.x && mx <= in.x + in.w && my >= in.y && my <= in.y + in.h) {
                focusedInput = in.id;
                foundInput = true;
                if (prevFocused != 0 && prevFocused != focusedInput) {
                    commitInput(prevFocused, dashboard);
                }
                return true;
            }
        }
        if (!foundInput && focusedInput != 0) {
            commitInput(focusedInput, dashboard);
            focusedInput = 0;
        }

        // 2. Toggle buttons (100% dynamic live sync)
        for (ToggleBtn tb : toggleBtns) {
            if (mx >= tb.x && mx <= tb.x + tb.w && my >= tb.y && my <= tb.y + tb.h) {
                AdminDataStore store = AdminDataStore.getInstance();
                switch (tb.id) {
                    case "USER_REG":
                        userRegistrationDraft = !userRegistrationDraft;
                        store.userRegistrationEnabled = userRegistrationDraft;
                        AdminSync.syncUpdateSetting("userRegistrationEnabled", userRegistrationDraft);
                        dashboard.showToast("User Registration: " + (userRegistrationDraft ? "ENABLED" : "DISABLED"));
                        break;
                    case "PROJ_APPROV":
                        projectApprovalDraft = !projectApprovalDraft;
                        store.requireProjectApproval = projectApprovalDraft;
                        AdminSync.syncUpdateSetting("requireProjectApproval", projectApprovalDraft);
                        dashboard.showToast("Project Approval: " + (projectApprovalDraft ? "ENABLED" : "DISABLED"));
                        break;
                    case "NOTIF":
                        notificationsDraft = !notificationsDraft;
                        store.notificationsEnabled = notificationsDraft;
                        AdminSync.syncUpdateSetting("notificationsEnabled", notificationsDraft);
                        dashboard.showToast("Notifications: " + (notificationsDraft ? "ENABLED" : "DISABLED"));
                        break;
                    case "MAINTENANCE":
                        maintenanceModeDraft = !maintenanceModeDraft;
                        store.maintenanceModeActive = maintenanceModeDraft;
                        AdminSync.syncUpdateSetting("maintenanceModeActive", maintenanceModeDraft);
                        dashboard.showToast("Maintenance Mode: " + (maintenanceModeDraft ? "ACTIVE (Public access paused)" : "INACTIVE (Live)"));
                        break;
                }
                return true;
            }
        }

        // 3. Stepper buttons (100% dynamic live sync)
        for (StepperBtn sb : stepperBtns) {
            if (mx >= sb.mx && mx <= sb.mx + sb.mw && my >= sb.my && my <= sb.my + sb.mh) {
                if ("MAX_TEAM".equals(sb.id)) {
                    maxTeamSizeDraft = Math.max(2, maxTeamSizeDraft - 1);
                    AdminDataStore.getInstance().maxTeamSize = maxTeamSizeDraft;
                    AdminSync.syncUpdateSetting("maxTeamSize", maxTeamSizeDraft);
                    dashboard.showToast("Max Team Size: " + maxTeamSizeDraft + " members");
                }
                return true;
            }
            if (mx >= sb.px && mx <= sb.px + sb.pw && my >= sb.py && my <= sb.py + sb.ph) {
                if ("MAX_TEAM".equals(sb.id)) {
                    maxTeamSizeDraft = Math.min(16, maxTeamSizeDraft + 1);
                    AdminDataStore.getInstance().maxTeamSize = maxTeamSizeDraft;
                    AdminSync.syncUpdateSetting("maxTeamSize", maxTeamSizeDraft);
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

        // Sync all to backend
        java.util.Map<String, Object> patch = new java.util.HashMap<>();
        patch.put("platformName", store.platformName);
        patch.put("contactEmail", store.contactEmail);
        patch.put("supportEmail", store.supportEmail);
        patch.put("allowedDomains", store.allowedDomains);
        patch.put("userRegistrationEnabled", store.userRegistrationEnabled);
        patch.put("requireProjectApproval", store.requireProjectApproval);
        patch.put("notificationsEnabled", store.notificationsEnabled);
        patch.put("maintenanceModeActive", store.maintenanceModeActive);
        patch.put("maxTeamSize", store.maxTeamSize);
        AdminSync.syncUpdateSettings(patch);

        // Log audit event
        store.logActivity(store.getActorName(), store.getActorInitials(), "Updated platform settings", store.platformName, true);
        dashboard.showToast("Platform settings successfully saved & synchronized!");
    }

    public void resetAllSettings(AdminDashboard dashboard) {
        AdminDataStore.getInstance().resetSettingsToDefaults();
        loadFromStore();
        AdminSync.syncResetSettings();
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
            lastMinusHover = sb.minusHovered;
            lastPlusHover = sb.plusHovered;
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
            commitInput(focusedInput, dashboard);
            focusedInput = 0;
            return true;
        }

        // Enter key
        if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            if (focusedInput == 6) {
                updatePassword(dashboard);
                return true;
            }
            commitInput(focusedInput, dashboard);
            dashboard.showToast("Setting updated!");
            focusedInput = 0;
            return true;
        }

        // Backspace
        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            switch (focusedInput) {
                case 1:
                    if (platformNameDraft.length() > 0) {
                        platformNameDraft = platformNameDraft.substring(0, platformNameDraft.length() - 1);
                        AdminDataStore.getInstance().platformName = platformNameDraft;
                        return true;
                    }
                    break;
                case 2:
                    if (contactEmailDraft.length() > 0) {
                        contactEmailDraft = contactEmailDraft.substring(0, contactEmailDraft.length() - 1);
                        AdminDataStore.getInstance().contactEmail = contactEmailDraft;
                        return true;
                    }
                    break;
                case 3:
                    if (supportEmailDraft.length() > 0) {
                        supportEmailDraft = supportEmailDraft.substring(0, supportEmailDraft.length() - 1);
                        AdminDataStore.getInstance().supportEmail = supportEmailDraft;
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
                        AdminDataStore.getInstance().allowedDomains = allowedDomainsDraft;
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
                        AdminDataStore.getInstance().platformName = platformNameDraft;
                        return true;
                    }
                    break;
                case 2:
                    if (contactEmailDraft.length() < 50) {
                        contactEmailDraft += ch;
                        AdminDataStore.getInstance().contactEmail = contactEmailDraft;
                        return true;
                    }
                    break;
                case 3:
                    if (supportEmailDraft.length() < 50) {
                        supportEmailDraft += ch;
                        AdminDataStore.getInstance().supportEmail = supportEmailDraft;
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
                        AdminDataStore.getInstance().allowedDomains = allowedDomainsDraft;
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
